package com.gtcafe.asimov.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "產品名稱不能為空")
    @Size(min = 2, max = 100, message = "產品名稱長度必須在2-100字之間")
    private String name;

    @NotNull(message = "價格不能為空")
    @Positive(message = "價格必須大於0")
    @Column(precision = 10, scale = 2)
    private Double price;

    @NotBlank(message = "類別不能為空")
    private String category;
}