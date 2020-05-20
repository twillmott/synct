package io.twillmott.synct.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@Table(name = "external_ids")
@AllArgsConstructor
@NoArgsConstructor
public class ExternalIdsEntity {

    @Id
    @GeneratedValue
    @Column( columnDefinition = "uuid", updatable = false )
    public UUID id;
    public Integer trakt;
    public String imdb;
    public Integer tmdb;
    public Integer tvdb;

    public ExternalIdsEntity(Integer trakt, String imdb, Integer tmdb, Integer tvdb) {
        this.trakt = trakt;
        this.imdb = imdb;
        this.tmdb = tmdb;
        this.tvdb = tvdb;
    }
}
