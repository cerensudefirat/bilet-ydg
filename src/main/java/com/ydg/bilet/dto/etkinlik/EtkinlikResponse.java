package com.ydg.bilet.dto.etkinlik;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EtkinlikResponse {
    private Long id;
    private String baslik;
    private String tur;
    private String sehir;
    private LocalDateTime tarih;
    private BigDecimal temelFiyat;
    private Integer satilan;
    private String durum;
    private Integer kapasite;

    private Long mekanId;
    private String mekanAd;
    private Integer mekanKapasite;
}
