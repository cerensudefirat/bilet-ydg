package com.ydg.bilet.service;

import com.ydg.bilet.dto.MekanDto;
import com.ydg.bilet.entity.Mekan;
import com.ydg.bilet.exception.NotFoundException;
import com.ydg.bilet.repository.MekanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MekanService {

    private final MekanRepository mekanRepository;

    public MekanDto create(MekanDto dto) {
        Mekan m = new Mekan();
        m.setAd(dto.getAd());
        m.setSehir(dto.getSehir());
        m.setKapasite(dto.getKapasite());
        Mekan saved = mekanRepository.save(m);
        return toDto(saved);
    }

    public MekanDto getById(Long id) {
        Mekan m = mekanRepository.findById(id).orElseThrow(() -> new NotFoundException("Mekan bulunamadı: " + id));
        return toDto(m);
    }

    public List<MekanDto> listAll() {
        return mekanRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public MekanDto update(Long id, MekanDto dto) {
        Mekan m = mekanRepository.findById(id).orElseThrow(() -> new NotFoundException("Mekan bulunamadı: " + id));
        m.setAd(dto.getAd());
        m.setSehir(dto.getSehir());
        m.setKapasite(dto.getKapasite());
        Mekan saved = mekanRepository.save(m);
        return toDto(saved);
    }

    public void delete(Long id) {
        if (!mekanRepository.existsById(id)) throw new NotFoundException("Mekan bulunamadı: " + id);
        mekanRepository.deleteById(id);
    }

    private MekanDto toDto(Mekan m) {
        return new MekanDto(m.getId(), m.getAd(), m.getSehir(),m.getKapasite());
    }
}

