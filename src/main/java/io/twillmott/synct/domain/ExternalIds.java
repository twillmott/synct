package io.twillmott.synct.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Data
public class ExternalIds {

    @Id
    @GeneratedValue
    @Column( columnDefinition = "uuid", updatable = false )
    public UUID id;
    public Integer trakt;
    public String imdb;
    public Integer tmdb;
    public Integer tvdb;
}
