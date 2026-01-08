package com.ydg.bilet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MekanDto {
    private Long id;
    private String ad;
    private String adres;
    private String sehir;
    private Integer kapasite;
}

