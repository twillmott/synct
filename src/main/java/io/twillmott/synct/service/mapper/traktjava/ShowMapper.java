package io.twillmott.synct.service.mapper.traktjava;

import com.uwetrottmann.trakt5.entities.BaseShow;
import io.twillmott.synct.domain.ShowEntity;
import io.twillmott.synct.dto.Show;

import java.util.stream.Collectors;

import static io.twillmott.synct.util.SynctUtils.toOffsetDateTime;

public class ShowMapper {

    public static Show toDto(ShowEntity showEntity) {
        return new Show(
                showEntity.getId(),
                showEntity.getTitle(),
                showEntity.getOverview(),
                showEntity.getYear()
        );
    }

    public static ShowEntity toEntity(BaseShow baseShow) {
        return new ShowEntity(
                ExternalIdsMapper.toEntity(baseShow.show.ids),
                baseShow.show.title,
                baseShow.show.overview,
                toOffsetDateTime(baseShow.show.updated_at),
                baseShow.show.year,
                baseShow.seasons.stream().map(SeasonMapper::toEntity).collect(Collectors.toList())
        );
    }
}
