package com.ydg.bilet.controller;

import com.ydg.bilet.dto.AdminRequestResponse;
import com.ydg.bilet.service.AdminRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin-requests")
@RequiredArgsConstructor
public class AdminRequestController {

    private final AdminRequestService adminRequestService;

    // USER -> "Admin olmak istiyorum"
    @PostMapping
    public ResponseEntity<AdminRequestResponse> create(Authentication auth) {
        return ResponseEntity.ok(adminRequestService.createRequest(auth));
    }

    // ADMIN -> Bekleyen istekleri listele
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminRequestResponse>> pending() {
        return ResponseEntity.ok(adminRequestService.listPending());
    }

    // ADMIN -> Onayla
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminRequestResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(adminRequestService.approve(id));
    }

    // ADMIN -> Reddet
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminRequestResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(adminRequestService.reject(id));
    }
}
