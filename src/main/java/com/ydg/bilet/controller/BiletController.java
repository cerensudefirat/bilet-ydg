package com.ydg.bilet.controller;

import com.ydg.bilet.dto.bilet.BiletIptalResponse;
import com.ydg.bilet.dto.bilet.BiletResponse;
import com.ydg.bilet.dto.bilet.BiletSatinAlRequest;
import com.ydg.bilet.dto.bilet.BiletSatinAlResponse;
import com.ydg.bilet.service.BiletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bilet")
public class BiletController {

    private final BiletService biletService;

    public BiletController(BiletService biletService) {
        this.biletService = biletService;
    }

    @PostMapping
    public ResponseEntity<BiletSatinAlResponse> satinAl(
            @RequestBody BiletSatinAlRequest request
    ) {
        BiletSatinAlResponse res = biletService.satinAl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/me")
    public List<BiletResponse> benimBiletlerim() {
        return biletService.benimBiletlerim();
    }

    @DeleteMapping("/{biletId}")
    public ResponseEntity<BiletIptalResponse> iptal(@PathVariable Long biletId) {
        return ResponseEntity.ok(biletService.iptalEt(biletId));
    }
    @GetMapping("/admin/all")
    public List<BiletResponse> tumBiletler() {
        return biletService.tumBiletler();
    }

}
