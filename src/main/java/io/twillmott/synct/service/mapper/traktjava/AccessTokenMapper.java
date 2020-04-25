package io.twillmott.synct.service.mapper.traktjava;

import com.uwetrottmann.trakt5.entities.AccessToken;
import io.twillmott.synct.domain.TraktAccessToken;

import java.time.Instant;
import java.time.OffsetDateTime;

import static io.twillmott.synct.util.SynctUtils.utcZoneId;
import static java.util.Objects.isNull;

/**
 * Mapper to map all types of access token model
 */
public class AccessTokenMapper {

    public static TraktAccessToken toEntity(AccessToken accessToken) {

        if (isNull(accessToken) || isNull(accessToken.created_at) || isNull(accessToken.expires_in)) {
            throw new IllegalArgumentException("Unable to map invalid access token from Trakt");
        }

        OffsetDateTime tokenCreated = OffsetDateTime.ofInstant(Instant.ofEpochSecond(accessToken.created_at), utcZoneId());

        return new TraktAccessToken(
                accessToken.access_token,
                accessToken.token_type,
                tokenCreated.plusSeconds(accessToken.expires_in),
                accessToken.refresh_token,
                accessToken.scope,
                tokenCreated
        );
    }
}
