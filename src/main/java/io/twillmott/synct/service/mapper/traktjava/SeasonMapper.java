package io.twillmott.synct.service.mapper.traktjava;

import com.uwetrottmann.trakt5.entities.BaseSeason;
import io.twillmott.synct.domain.SeasonEntity;

import java.util.stream.Collectors;

public class SeasonMapper {

    public static SeasonEntity toEntity(BaseSeason baseSeason) {
        return new SeasonEntity(
                baseSeason.number,
                baseSeason.completed != null  && baseSeason.completed > 0 ? true : false,
                baseSeason.episodes.stream().map(EpisodeMapper::toEntity).collect(Collectors.toList())
        );
    }

}
