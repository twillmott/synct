package io.twillmott.synct.domain;

import javax.persistence.Id;

public class ExternalIds {

    @Id
    public Integer trakt;
    public String imdb;
    public Integer tmdb;
    public Integer tvdb;
}
