package io.twillmott.synct.service;


import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.DeviceCode;
import io.twillmott.synct.domain.TraktAccessToken;
import io.twillmott.synct.events.publisher.TraktAuthorizedEventPublisher;
import io.twillmott.synct.repository.TraktAccessTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import retrofit2.Response;

import java.io.IOException;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraktAuthorizationServiceTest {

    @Mock
    TraktAccessTokenRepository traktAccessTokenRepository;
    @Mock
    TraktV2 traktV2;
    @Mock
    ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Mock
    TraktAuthorizedEventPublisher traktAuthorizedEventPublisher;

    @InjectMocks
    TraktAuthorizationService subject;

    @Test
    void authorize_doesNotReauthorize_whenAuthorized() throws IOException {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        TraktAccessToken traktAccessToken = new TraktAccessToken(
                "access",
                "bearer",
                now.plusDays(11),
                "refresh",
                "scope",
                now.minusDays(20)
        );
        when(traktAccessTokenRepository.findFirstByOrderByCreatedAt()).thenReturn(traktAccessToken);

        // When
        subject.authorize();

        // Then
        verify(traktV2, times(0)).refreshAccessToken(any());
        verify(traktV2).accessToken("access");
        verify(traktV2, times(0)).exchangeDeviceCodeForAccessToken(any());
        verify(traktAuthorizedEventPublisher).publish(subject, true);
    }

    @Test
    void authorize_refreshesToken_whenAuthorizedButTokenExpiresInLessThan10Days() throws IOException {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        TraktAccessToken traktAccessToken = new TraktAccessToken(
                "access",
                "bearer",
                now.plusDays(1),
                "refresh",
                "scope",
                now.minusDays(20)
        );
        when(traktAccessTokenRepository.findFirstByOrderByCreatedAt()).thenReturn(traktAccessToken);

        // When
        subject.authorize();

        // Then
        verify(traktV2).refreshAccessToken("refresh");
        verify(traktV2).accessToken("access");
        verify(traktV2, times(0)).exchangeDeviceCodeForAccessToken(any());
    }

    @Test
    void authorize_requestsAuthorization_whenAccessTokenDoesNotExist() throws IOException {
        // Given
        DeviceCode deviceCode = new DeviceCode();
        deviceCode.user_code = "userCode";
        deviceCode.device_code = "deviceCode";
        deviceCode.verification_url = "verification";
        deviceCode.interval = 10;
        Response response = mock(Response.class);
        when(response.body()).thenReturn(deviceCode);
        when(traktV2.generateDeviceCode()).thenReturn(response);
        when(traktAccessTokenRepository.findFirstByOrderByCreatedAt()).thenReturn(null);

        // When
        subject.authorize();

        // Then
        verify(traktV2, times(0)).refreshAccessToken("refresh");
        verify(traktV2, times(0)).accessToken("access");
        verify(threadPoolTaskScheduler).scheduleAtFixedRate(eq(new TraktAuthorizationPollingRunner(
                    "verification", "deviceCode", "userCode", traktV2,
                    traktAccessTokenRepository, threadPoolTaskScheduler, traktAuthorizedEventPublisher)),
                eq(10000L)
        );
    }

    @Test
    void authorize_throwsException_whenAuthorising() throws IOException {
        // Given
        when(traktV2.generateDeviceCode()).thenThrow(new IOException());

        // When
        assertThrows(RuntimeException.class, () -> subject.authorize());
        verify(traktAuthorizedEventPublisher).publish(subject, false);
    }



    @Test
    void authorize_throwsException_whenRefreshingToken() throws IOException {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        TraktAccessToken traktAccessToken = new TraktAccessToken(
                "access",
                "bearer",
                now.plusDays(1),
                "refresh",
                "scope",
                now.minusDays(20)
        );
        when(traktAccessTokenRepository.findFirstByOrderByCreatedAt()).thenReturn(traktAccessToken);
        when(traktV2.refreshAccessToken(any())).thenThrow(new IOException());

        // When
        assertThrows(RuntimeException.class, () -> subject.authorize());
    }

}