package io.twillmott.synct.service;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import io.twillmott.synct.events.publisher.TraktAuthorizedEventPublisher;
import io.twillmott.synct.repository.TraktAccessTokenRepository;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import retrofit2.Response;

import static io.twillmott.synct.service.mapper.traktjava.AccessTokenMapper.toEntity;

/**
 * A polling {@link Runnable} class used to continuously poll to see if a user has visited their browser in order
 * to authorize the application. It gives up after 100 polls.
 *
 */
@Slf4j
@EqualsAndHashCode
class TraktAuthorizationPollingRunner implements Runnable {

    private final String verificationUrl;
    private final String deviceCode;
    private final String userCode;
    private final TraktV2 trakt;
    private final TraktAccessTokenRepository traktAccessTokenRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final TraktAuthorizedEventPublisher traktAuthorizedEventPublisher;

    private int count = 0;

    public TraktAuthorizationPollingRunner(
            String verificationUrl,
            String deviceCode,
            String userCode,
            TraktV2 trakt,
            TraktAccessTokenRepository traktAccessTokenRepository,
            ThreadPoolTaskScheduler taskScheduler,
            TraktAuthorizedEventPublisher traktAuthorizedEventPublisher) {
        this.verificationUrl = verificationUrl;
        this.deviceCode = deviceCode;
        this.userCode = userCode;
        this.trakt = trakt;
        this.traktAccessTokenRepository = traktAccessTokenRepository;
        this.taskScheduler = taskScheduler;
        this.traktAuthorizedEventPublisher = traktAuthorizedEventPublisher;
    }

    @SneakyThrows
    @Override
    public void run() {
        count++;
        Response<AccessToken> response = trakt.exchangeDeviceCodeForAccessToken(deviceCode);
        if (response.isSuccessful()) {
            AccessToken accessToken = response.body();
            log.info("Trakt authorization success");
            trakt.accessToken(accessToken.access_token);
            trakt.refreshToken(accessToken.refresh_token);
            traktAccessTokenRepository.save(toEntity(accessToken));
            traktAuthorizedEventPublisher.publish(this, true);
            taskScheduler.shutdown();
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
            traktAuthorizedEventPublisher.publish(this, false);
            taskScheduler.shutdown();
            throw new RuntimeException();
        }
    }
}
