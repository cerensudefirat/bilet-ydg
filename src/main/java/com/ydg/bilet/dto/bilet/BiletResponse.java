package com.ydg.bilet.dto.bilet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BiletResponse {

    private Long biletId;

    private Long etkinlikId;
    private String etkinlikBaslik;
    private String etkinlikTur;
    private String etkinlikSehir;
    private LocalDateTime etkinlikTarih;

    private BigDecimal temelFiyat;

    private LocalDateTime satinAlmaTarihi;
}
