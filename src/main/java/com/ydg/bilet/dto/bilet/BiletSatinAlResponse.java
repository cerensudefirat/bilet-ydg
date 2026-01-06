package com.ydg.bilet.dto.bilet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BiletSatinAlResponse {

    private Long etkinlikId;
    private Integer adet;
    private Integer kalanKoltuk;
    private List<Long> biletIdList;
}
