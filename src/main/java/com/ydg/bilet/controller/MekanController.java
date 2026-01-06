package com.ydg.bilet.controller;

import com.ydg.bilet.dto.MekanDto;
import com.ydg.bilet.service.MekanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/mekan")
@RequiredArgsConstructor
public class MekanController {

    private final MekanService mekanService;

    @PostMapping
    public ResponseEntity<MekanDto> create(@RequestBody MekanDto dto) {
        MekanDto created = mekanService.create(dto);
        return ResponseEntity.created(URI.create("/api/mekan/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MekanDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mekanService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<MekanDto>> listAll() {
        return ResponseEntity.ok(mekanService.listAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MekanDto> update(@PathVariable Long id, @RequestBody MekanDto dto) {
        return ResponseEntity.ok(mekanService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mekanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

