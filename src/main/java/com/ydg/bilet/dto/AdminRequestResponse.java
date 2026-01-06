package com.ydg.bilet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminRequestResponse {
    private Long id;
    private Long kullaniciId;
    private String email;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
}
