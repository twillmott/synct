package io.twillmott.synct.service.mapper.traktjava;

import com.uwetrottmann.trakt5.entities.BaseEpisode;
import io.twillmott.synct.domain.EpisodeEntity;

import static io.twillmott.synct.util.SynctUtils.toOffsetDateTime;

public class EpisodeMapper {

    public static EpisodeEntity toEntity(BaseEpisode baseEpisode) {
        return new EpisodeEntity(
                baseEpisode.number,
                baseEpisode.last_watched_at == null ? null : toOffsetDateTime(baseEpisode.last_watched_at),
                baseEpisode.completed == null || baseEpisode.completed == false ? false: true
        );
    }
}
