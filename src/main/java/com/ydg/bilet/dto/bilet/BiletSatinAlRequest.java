package com.ydg.bilet.dto.bilet;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BiletSatinAlRequest {

    @NotNull
    private Long etkinlikId;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer adet;
}
