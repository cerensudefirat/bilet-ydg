package com.ydg.bilet.dto.etkinlik;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EtkinlikCreateRequest {
    private String baslik;
    private String tur;
    private String sehir;
    private LocalDateTime tarih;
    private Integer kapasite;
    private BigDecimal temelFiyat;
    private Long mekanId;
}

