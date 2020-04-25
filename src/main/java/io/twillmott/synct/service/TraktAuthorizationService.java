package io.twillmott.synct.service;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.DeviceCode;
import io.twillmott.synct.domain.TraktAccessToken;
import io.twillmott.synct.repository.TraktAccessTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.OffsetDateTime;

import static java.util.Objects.isNull;

/**
 * Service used to authorize a users trakt account via device authentication (https://trakt.docs.apiary.io/#reference/authentication-devices)
 *
 * The saved access token is written to the database along with the refresh key in order to refresh the token at a later date.
 */
@Service
@Slf4j
public class TraktAuthorizationService {

    private final TraktAccessTokenRepository traktAccessTokenRepository;
    private final TraktV2 trakt;
    private final ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    public TraktAuthorizationService(TraktV2 trakt,
                                     ThreadPoolTaskScheduler taskScheduler,
                                     TraktAccessTokenRepository traktAccessTokenRepository) {
        this.trakt = trakt;
        this.taskScheduler = taskScheduler;
        this.traktAccessTokenRepository = traktAccessTokenRepository;
    }

    @PostConstruct
    public void authorize() {

        if (refreshTokenIfExists()) {
            return;
        }

        try {
            DeviceCode deviceCode = trakt.generateDeviceCode().body();
            log.info("Please go to " + deviceCode.verification_url + " and enter code: " + deviceCode.user_code);

            // Poll for the interval to see if the user has authorized us in their browser.
            taskScheduler.scheduleAtFixedRate(new TraktAuthorizationPollingRunner(deviceCode.verification_url,
                            deviceCode.device_code, deviceCode.user_code, trakt, traktAccessTokenRepository, taskScheduler),
                    deviceCode.interval * 1000L);

        } catch (IOException e) {
            log.error("Unable to authorize application.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Refresh the trakt authorization token and provide it to the {@link TraktV2} service if one exists.
     * The token is only refreshed if it has run out, or if it's going to run out in the next ten days.
     *
     * @return whether or not we have a valid access token - if not, we need to generate a new one.
     */
    private boolean refreshTokenIfExists() {
        TraktAccessToken traktAccessToken = traktAccessTokenRepository.findFirstByOrderByCreatedAt();

        if (isNull(traktAccessToken)) {
            return false;
        }

        // Refresh token if it's expired of due to expire in the next 10 days
        if (traktAccessToken.getExpiry().isBefore(OffsetDateTime.now().plusDays(10))) {
            try {
                log.info("Trakt access token has expired or is close to expiring. Refreshing...");
                trakt.refreshAccessToken(traktAccessToken.getRefreshToken());
            } catch (IOException e) {
                log.error("Error refreshing access token: " + e);
                throw new RuntimeException(e);
            }
        }

        log.info("Trakt access token found. No need to re-authorize.");
        trakt.accessToken(traktAccessToken.getAccessToken());

        return true;
    }
}

