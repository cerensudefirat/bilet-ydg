package com.ydg.bilet.controller;

import com.ydg.bilet.dto.etkinlik.EtkinlikCreateRequest;
import com.ydg.bilet.dto.etkinlik.EtkinlikIptalResponse;
import com.ydg.bilet.dto.etkinlik.EtkinlikResponse;
import com.ydg.bilet.dto.etkinlik.EtkinlikUpdateRequest;
import com.ydg.bilet.service.EtkinlikService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/etkinlik")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEtkinlikController {

    private final EtkinlikService service;

    public AdminEtkinlikController(EtkinlikService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EtkinlikResponse> olustur(@RequestBody EtkinlikCreateRequest req) {
        EtkinlikResponse body = service.olustur(req);
        return ResponseEntity
                .created(URI.create("/api/admin/etkinlik/" + body.getId()))
                .body(body);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EtkinlikResponse guncelle(@PathVariable Long id,
                                     @RequestBody EtkinlikUpdateRequest req) {
        return service.guncelle(id, req);
    }

    @DeleteMapping("/{id}")
    public EtkinlikIptalResponse iptal(@PathVariable Long id) {
        return service.iptalEt(id);
    }
}
