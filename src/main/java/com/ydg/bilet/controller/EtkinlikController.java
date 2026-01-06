package com.ydg.bilet.controller;

import com.ydg.bilet.dto.etkinlik.EtkinlikResponse;
import com.ydg.bilet.service.EtkinlikService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etkinlik")
public class EtkinlikController {

    private final EtkinlikService service;

    public EtkinlikController(EtkinlikService service) {
        this.service = service;
    }

    @GetMapping
    public List<EtkinlikResponse> liste() {
        return service.publicListele();
    }

    @GetMapping("/{id}")
    public EtkinlikResponse detay(@PathVariable Long id) {
        return service.publicDetay(id);
    }
}
