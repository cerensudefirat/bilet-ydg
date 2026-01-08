package com.ydg.bilet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "etkinlik")
public class Etkinlik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String baslik;

    @Column(nullable = false, length = 40)
    private String tur;

    @Column(nullable = false, length = 80)
    private String sehir;

    @Column(nullable = false)
    private LocalDateTime tarih;

    @Column(name = "temel_fiyat", nullable = false, precision = 12, scale = 2)
    private BigDecimal temelFiyat;

    @Column(nullable = false)
    private Integer satilan = 0;

    @Column(nullable = false)
    private Integer kapasite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtkinlikDurum durum = EtkinlikDurum.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mekan_id", nullable = false)
    private Mekan mekan;
}
