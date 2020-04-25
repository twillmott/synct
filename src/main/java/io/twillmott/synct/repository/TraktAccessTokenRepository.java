package io.twillmott.synct.repository;

import io.twillmott.synct.domain.TraktAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TraktAccessTokenRepository extends JpaRepository<TraktAccessToken, UUID> {

    TraktAccessToken findFirstByOrderByCreatedAt();

}
