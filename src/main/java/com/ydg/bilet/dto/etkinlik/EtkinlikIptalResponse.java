package com.ydg.bilet.dto.etkinlik;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EtkinlikIptalResponse {
    private Long etkinlikId;
    private String durum;
    private int iptalEdilenBiletSayisi;
}
