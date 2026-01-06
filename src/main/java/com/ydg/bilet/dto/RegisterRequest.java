package com.ydg.bilet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String sifre;

    @NotBlank
    private String ad;

    @NotBlank
    private String soyad;
}
