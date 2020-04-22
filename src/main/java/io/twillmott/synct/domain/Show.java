package io.twillmott.synct.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class Show {

    @Id
    private UUID id;
//    private ExternalIds externalIds;
    private String title;
    private String overview;
    private String updatedAt;
    private Integer year;

}
