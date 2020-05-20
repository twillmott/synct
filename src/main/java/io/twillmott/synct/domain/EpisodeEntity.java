package io.twillmott.synct.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "episodes")
@Data
@NoArgsConstructor
public class EpisodeEntity {

    @Id
    @GeneratedValue
    @Column( columnDefinition = "uuid", updatable = false )
    private UUID id;
    private int number;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastWatched;
    boolean complete;

    public EpisodeEntity(int number, OffsetDateTime lastWatched, boolean complete) {
        this.number = number;
        this.lastWatched = lastWatched;
        this.complete = complete;
    }
}
