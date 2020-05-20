package io.twillmott.synct.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shows")
@Data
@NoArgsConstructor
public class ShowEntity {

    @Id
    @GeneratedValue
    @Column( columnDefinition = "uuid", updatable = false )
    private UUID id;
    private String title;
    @OneToOne(cascade=CascadeType.ALL, orphanRemoval =  true)
    private ExternalIdsEntity externalIds;
    @Column(columnDefinition = "TEXT")
    private String overview;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;
    private Integer year;
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "show_id")
    private List<SeasonEntity> seasons;

    public ShowEntity(ExternalIdsEntity externalIds, String title, String overview, OffsetDateTime updatedAt, Integer year,
                      List<SeasonEntity> seasons) {
        this.externalIds = externalIds;
        this.title = title;
        this.overview = overview;
        this.updatedAt = updatedAt;
        this.year = year;
        this.seasons = seasons;
    }
}
