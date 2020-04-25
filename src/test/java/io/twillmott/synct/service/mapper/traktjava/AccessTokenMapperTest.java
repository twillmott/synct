package io.twillmott.synct.service.mapper.traktjava;

import com.uwetrottmann.trakt5.entities.AccessToken;
import io.twillmott.synct.domain.TraktAccessToken;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessTokenMapperTest {

    @Test
    void toEntity_fromTraktJavaEntity_whenNullAccessToken() {
        assertThrows(IllegalArgumentException.class, () -> AccessTokenMapper.toEntity(null));
    }

    @Test
    void toEntity_fromTraktJavaEntity() {
        // Given
        AccessToken accessToken = new AccessToken();
        accessToken.expires_in = 600;
        accessToken.access_token = "access";
        accessToken.created_at = 957508200;
        accessToken.refresh_token = "refresh";
        accessToken.scope = "scope";
        accessToken.token_type = "tokentype";

        // When
        TraktAccessToken traktAccessToken = AccessTokenMapper.toEntity(accessToken);
        // Then
        assertThat(traktAccessToken.getCreatedAt()).isEqualTo("2000-05-05T06:30:00Z");
        assertThat(traktAccessToken.getExpiry()).isEqualTo("2000-05-05T06:40:00Z");
        assertThat(traktAccessToken.getAccessToken()).isEqualTo("access");
        assertThat(traktAccessToken.getRefreshToken()).isEqualTo("refresh");
        assertThat(traktAccessToken.getScope()).isEqualTo("scope");
        assertThat(traktAccessToken.getTokenType()).isEqualTo("tokentype");

    }
}