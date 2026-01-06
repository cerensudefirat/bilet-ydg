package com.ydg.bilet.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;
}
