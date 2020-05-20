package io.twillmott.synct.service;


import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import com.uwetrottmann.trakt5.entities.DeviceCode;
import io.twillmott.synct.domain.TraktAccessToken;
import io.twillmott.synct.events.publisher.TraktAuthorizedEventPublisher;
import io.twillmott.synct.repository.TraktAccessTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.Response;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;

import static io.twillmott.synct.service.mapper.traktjava.AccessTokenMapper.toEntity;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraktAuthorizationServiceTest {

    @Mock
    TraktAccessTokenRepository traktAccessTokenRepository;
    @Mock
    TraktV2 traktV2;
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
    void authorize_requestsAuthorization_whenAccessTokenDoesNotExist() throws Exception {
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

        Response tokenResponse = mock(Response.class);
        AccessToken accessToken = new AccessToken();
        accessToken.access_token = "access";
        accessToken.refresh_token = "refresh";
        accessToken.created_at = Math.toIntExact(Instant.now().getEpochSecond());
        accessToken.expires_in = 1000;
        when(traktV2.exchangeDeviceCodeForAccessToken("deviceCode")).thenReturn(tokenResponse);
        when(tokenResponse.isSuccessful()).thenReturn(true);
        when(tokenResponse.body()).thenReturn(accessToken);

        // When
        subject.authorize();

        // Then
        Thread.sleep(100);
        verify(traktV2).refreshToken("refresh");
        verify(traktV2).accessToken("access");
        verify(traktAccessTokenRepository).save(toEntity(accessToken));
        verify(traktAuthorizedEventPublisher).publish(any(), eq(true));
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