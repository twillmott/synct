package io.twillmott.synct.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "seasons")
@Data
@NoArgsConstructor
public class SeasonEntity {

    @Id
    @GeneratedValue
    @Column( columnDefinition = "uuid", updatable = false )
    private UUID id;
    private int number;
    boolean complete;
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "season_id")
    private List<EpisodeEntity> episodes;

    public SeasonEntity(int number, boolean complete, List<EpisodeEntity> episodes) {
        this.number = number;
        this.complete = complete;
        this.episodes = episodes;
    }
}
