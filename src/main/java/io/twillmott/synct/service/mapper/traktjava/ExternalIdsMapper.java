package io.twillmott.synct.service.mapper.traktjava;

import com.uwetrottmann.trakt5.entities.ShowIds;
import io.twillmott.synct.domain.ExternalIdsEntity;

public class ExternalIdsMapper {
    public static ExternalIdsEntity toEntity(ShowIds showIds) {
        return new ExternalIdsEntity(
                showIds.trakt,
                showIds.imdb,
                showIds.tmdb,
                showIds.tvdb
        );
    }
}
