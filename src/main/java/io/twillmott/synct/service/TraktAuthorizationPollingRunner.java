package io.twillmott.synct.service;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

import java.util.concurrent.CompletableFuture;

/**
 * A polling {@link Runnable} class used to continuously poll to see if a user has visited their browser in order
 * to authorize the application. It gives up after 100 polls.
 */
@Slf4j
@EqualsAndHashCode
class TraktAuthorizationPollingRunner implements Runnable {

    private final String verificationUrl;
    private final String deviceCode;
    private final String userCode;
    private final TraktV2 trakt;
    private final CompletableFuture<AccessToken> authorizationFuture;

    private int count = 0;

    public TraktAuthorizationPollingRunner(
            String verificationUrl,
            String deviceCode,
            String userCode,
            TraktV2 trakt,
            CompletableFuture<AccessToken> authorizationFuture) {
        this.verificationUrl = verificationUrl;
        this.deviceCode = deviceCode;
        this.userCode = userCode;
        this.trakt = trakt;
        this.authorizationFuture = authorizationFuture;
    }

    @SneakyThrows
    @Override
    public void run() {
        count++;
        Response<AccessToken> response = trakt.exchangeDeviceCodeForAccessToken(deviceCode);
        if (response.isSuccessful()) {
            AccessToken accessToken = response.body();
            log.info("Trakt authorization success");
            authorizationFuture.complete(accessToken);
        } else if (response.code() == 400) {
            // The trakt API returns a 400 until the user has entered their code, so we'll keep waiting
            log.info("Waiting for trakt authorization... Please go to " + verificationUrl +
                    " and enter code: " + userCode);
        } else {
            log.warn("Unable to authorize device with error:" + response.code() +
                    " - " + response.errorBody().string());
        }
        if (count >= 100) {
            log.warn("Authorization unsuccessful. Please restart the application to retry.");
            throw new RuntimeException();
        }
    }
}
