package com.gtcafe.asimov.dto;

import lombok.Data;

@Data
public class VolumeDTO {
    private Long id;
    private String name;
    private String description;
    private Long size;
}