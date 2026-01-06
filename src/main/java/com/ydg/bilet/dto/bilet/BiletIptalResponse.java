package com.ydg.bilet.dto.bilet;

import java.time.LocalDateTime;

public class BiletIptalResponse {
    private Long biletId;
    private Long etkinlikId;
    private Integer kalanKoltuk;
    private LocalDateTime cancelledAt;

    public BiletIptalResponse(Long biletId, Long etkinlikId, Integer kalanKoltuk, LocalDateTime cancelledAt) {
        this.biletId = biletId;
        this.etkinlikId = etkinlikId;
        this.kalanKoltuk = kalanKoltuk;
        this.cancelledAt = cancelledAt;
    }

    public Long getBiletId() { return biletId; }
    public Long getEtkinlikId() { return etkinlikId; }
    public Integer getKalanKoltuk() { return kalanKoltuk; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
}
