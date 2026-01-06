package com.ydg.bilet.dto.etkinlik;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EtkinlikUpdateRequest {
    private String baslik;
    private String tur;
    private String sehir;
    private LocalDateTime tarih;
    private BigDecimal temelFiyat;
    private Long mekanId;
}
