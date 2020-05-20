package io.twillmott.synct.repository;

import io.twillmott.synct.domain.ShowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShowRepository extends JpaRepository<ShowEntity, UUID> {
}
