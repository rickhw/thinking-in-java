package com.gtcafe.asimov;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_config")
@Data
public class ConfigEntity {
    @Id
    @Column(length = 255)
    private String configKey;
    
    @Column(length = 1000)
    private String configValue;
}