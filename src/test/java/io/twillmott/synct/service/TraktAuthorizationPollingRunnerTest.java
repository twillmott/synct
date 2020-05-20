package io.twillmott.synct.service;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import retrofit2.Response;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TraktAuthorizationPollingRunnerTest {

    @Mock
    TraktV2 traktV2;
    @Mock
    CompletableFuture authorizationFuture;

    private TraktAuthorizationPollingRunner subject;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        subject = new TraktAuthorizationPollingRunner(
                "verification",
                "deviceCode",
                "userCode",
                traktV2,
                authorizationFuture);
    }

    @Test
    void run_returnsToken_whenSuccessfullyGainedFromAccessToken() throws IOException {
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
        verify(authorizationFuture).complete(accessToken);

    }

    @Test
    void run_doesNotReturnToken_whenTraktReturns400() throws IOException {
        // Given
        Response response = mock(Response.class);
        when(traktV2.exchangeDeviceCodeForAccessToken("deviceCode")).thenReturn(response);
        when(response.isSuccessful()).thenReturn(false);
        when(response.code()).thenReturn(400);

        // When
        subject.run();

        // Then
        verify(authorizationFuture, times(0)).complete(any());

    }

    @Test
    void run_doesNotReturnToken_whenTraktReturnsError() throws IOException {
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
        verify(authorizationFuture, times(0)).complete(any());

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
        for (int i = 1; i < 100; i++) {
            subject.run();
            verify(authorizationFuture, times(0)).complete(any());
        }
        // 100th run throws exception
        assertThrows(RuntimeException.class, () -> subject.run());
        verify(authorizationFuture, times(0)).complete(any());

    }
}
