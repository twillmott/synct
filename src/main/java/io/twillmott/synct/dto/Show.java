package io.twillmott.synct.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Show {
    private UUID id;
    private String title;
    private String overview;
    private Integer year;
}
