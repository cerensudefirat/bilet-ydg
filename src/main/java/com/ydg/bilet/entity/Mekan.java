package com.ydg.bilet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mekan")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Mekan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String ad;

    @Column(nullable = false, length = 80)
    private String sehir;

    @Column(nullable = false)
    private Integer kapasite;
}
