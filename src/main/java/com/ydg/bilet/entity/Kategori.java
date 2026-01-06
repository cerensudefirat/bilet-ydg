package com.ydg.bilet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "kategori")
public class Kategori {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String ad; // VIP, GENEL, OGRENCI

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal carpan; // 1.50 vs
}
