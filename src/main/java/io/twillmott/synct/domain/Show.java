package io.twillmott.synct.domain;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Show {

    @Id
    @GeneratedValue
    @Column( columnDefinition = "uuid", updatable = false )
    private UUID id;
    @OneToOne
    private ExternalIds externalIds;
    private String title;
    private String overview;
    private String updatedAt;
    private Integer year;

}
