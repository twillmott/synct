package io.twillmott.synct.service;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import io.twillmott.synct.events.publisher.TraktAuthorizedEventPublisher;
import io.twillmott.synct.repository.TraktAccessTokenRepository;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import retrofit2.Response;

import java.io.IOException;
import java.time.Instant;

import static io.twillmott.synct.service.mapper.traktjava.AccessTokenMapper.toEntity;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TraktAuthorizationPollingRunnerTest {

    @Mock
    TraktV2 traktV2;
    @Mock
    TraktAccessTokenRepository traktAccessTokenRepository;
    @Mock
    ThreadPoolTaskScheduler taskScheduler;
    @Mock
    TraktAuthorizedEventPublisher traktAuthorizedEventPublisher;

    private TraktAuthorizationPollingRunner subject;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        subject = new TraktAuthorizationPollingRunner(
                "verification",
                "deviceCode",
                "userCode",
                traktV2,
                traktAccessTokenRepository,
                taskScheduler,
                traktAuthorizedEventPublisher);
    }

    @Test
    void run_savesToken_whenSuccessfullyGainedFromAccessToken() throws IOException {
        // Given
        Response response = mock(Response.class);
        AccessToken accessToken = new AccessToken();
        accessToken.access_token = "access";
        accessToken.refresh_token = "refresh";
        accessToken.created_at = Math.toIntExact(Instant.now().getEpochSecond());
        accessToken.expires_in = 1000;

        when(traktV2.exchangeDeviceCodeForAccessToken("deviceCode")).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(accessToken);

        // When
        subject.run();

        // Then
        verify(traktV2).accessToken("access");
        verify(traktV2).refreshToken("refresh");
        verify(traktAccessTokenRepository).save(toEntity(accessToken));
        verify(traktAuthorizedEventPublisher).publish(subject, true);
        verify(taskScheduler).shutdown();

    }

    @Test
    void run_doesNotSaveToken_whenTraktReturns400() throws IOException {
        // Given
        Response response = mock(Response.class);
        when(traktV2.exchangeDeviceCodeForAccessToken("deviceCode")).thenReturn(response);
        when(response.isSuccessful()).thenReturn(false);
        when(response.code()).thenReturn(400);

        // When
        subject.run();

        // Then
        verify(traktV2, times(0)).accessToken("access");
        verify(traktV2, times(0)).refreshToken("refresh");
        verify(traktAccessTokenRepository, times(0)).save(any());
        verify(traktAuthorizedEventPublisher, times(0)).publish(subject, true);
        verify(taskScheduler, times(0)).shutdown();

    }

    @Test
    void run_doesNotSaveToken_whenTraktReturnsError() throws IOException {
        // Given
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        when(traktV2.exchangeDeviceCodeForAccessToken("deviceCode")).thenReturn(response);
        when(response.isSuccessful()).thenReturn(false);
        when(response.code()).thenReturn(500);
        when(response.errorBody()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("error");
        // When
        subject.run();

        // Then
        verify(traktV2, times(0)).accessToken("access");
        verify(traktV2, times(0)).refreshToken("refresh");
        verify(traktAccessTokenRepository, times(0)).save(any());
        verify(traktAuthorizedEventPublisher, times(0)).publish(subject, true);
        verify(taskScheduler, times(0)).shutdown();

    }



    @Test
    void run_doesNotSaveToken_whenRuns100Times() throws IOException {
        // Given
        Response response = mock(Response.class);
        when(traktV2.exchangeDeviceCodeForAccessToken("deviceCode")).thenReturn(response);
        when(response.isSuccessful()).thenReturn(false);
        when(response.code()).thenReturn(400);

        // When/Then
        // Run 99 times, should not throw exception
        for (int i = 1; i<100; i++) {
            subject.run();
            verify(traktV2, times(0)).accessToken("access");
            verify(traktV2, times(0)).refreshToken("refresh");
            verify(traktAccessTokenRepository, times(0)).save(any());
            verify(taskScheduler, times(0)).shutdown();
        }
        // 100th run throws exception
        assertThrows(RuntimeException.class, () -> subject.run());
        verify(traktV2, times(0)).accessToken("access");
        verify(traktV2, times(0)).refreshToken("refresh");
        verify(traktAccessTokenRepository, times(0)).save(any());
        verify(traktAuthorizedEventPublisher).publish(subject, false);
        verify(taskScheduler).shutdown();

    }
}
