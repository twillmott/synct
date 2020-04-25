package io.twillmott.synct.domain;

import io.twillmott.synct.security.DatabaseColumnEncryptor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class TraktAccessToken {

    @Id
    @GeneratedValue
    @Column( columnDefinition = "uuid", updatable = false )
    private UUID id;
    @Convert(converter = DatabaseColumnEncryptor.class)
    private String accessToken;
    private String tokenType;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime expiry;
    @Convert(converter = DatabaseColumnEncryptor.class)
    private String refreshToken;
    private String scope;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    public TraktAccessToken(String accessToken, String tokenType, OffsetDateTime expiry, String refreshToken, String scope, OffsetDateTime createdAt) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiry = expiry;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.createdAt = createdAt;
    }
}
