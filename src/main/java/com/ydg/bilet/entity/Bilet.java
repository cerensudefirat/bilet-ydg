package com.ydg.bilet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "bilet",
        indexes = {
                @Index(name = "ix_bilet_kullanici", columnList = "kullanici_id"),
                @Index(name = "ix_bilet_etkinlik", columnList = "etkinlik_id"),
                @Index(name = "ix_bilet_created_at", columnList = "created_at")
        }
)
public class Bilet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bir etkinliğe ait bilet
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etkinlik_id", nullable = false)
    private Etkinlik etkinlik;

    // Bileti alan kullanıcı
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private Kullanici kullanici;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BiletDurum durum = BiletDurum.ACTIVE;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;



}
