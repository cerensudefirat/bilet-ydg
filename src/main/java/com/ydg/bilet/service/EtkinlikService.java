package com.ydg.bilet.service;

import com.ydg.bilet.dto.etkinlik.EtkinlikCreateRequest;
import com.ydg.bilet.dto.etkinlik.EtkinlikIptalResponse;
import com.ydg.bilet.dto.etkinlik.EtkinlikResponse;
import com.ydg.bilet.dto.etkinlik.EtkinlikUpdateRequest;
import com.ydg.bilet.entity.Etkinlik;
import com.ydg.bilet.entity.EtkinlikDurum;
import com.ydg.bilet.entity.Mekan;
import com.ydg.bilet.exception.NotFoundException;
import com.ydg.bilet.repository.EtkinlikRepository;
import com.ydg.bilet.repository.MekanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EtkinlikService {

    private final EtkinlikRepository etkinlikRepository;
    private final MekanRepository mekanRepository;

    public EtkinlikService(EtkinlikRepository etkinlikRepository,
                           MekanRepository mekanRepository) {
        this.etkinlikRepository = etkinlikRepository;
        this.mekanRepository = mekanRepository;
    }

    // ---------- PUBLIC ----------

    @Transactional(readOnly = true)
    public List<EtkinlikResponse> publicListele() {
        return etkinlikRepository
                .findByDurumOrderByTarihAsc(EtkinlikDurum.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EtkinlikResponse publicDetay(Long id) {
        Etkinlik e = etkinlikRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Etkinlik bulunamadı: " + id));

        if (e.getDurum() != EtkinlikDurum.ACTIVE) {
            // public taraf iptal edilmiş etkinliği "yokmuş" gibi dönsün
            throw new NotFoundException("Etkinlik bulunamadı: " + id);
        }

        return toResponse(e);
    }

    // ---------- ADMIN ----------

    public EtkinlikResponse olustur(EtkinlikCreateRequest req) {
        if (req.getMekanId() == null) {
            throw new IllegalArgumentException("mekanId zorunludur");
        }

        Mekan mekan = mekanRepository.findById(req.getMekanId())
                .orElseThrow(() -> new NotFoundException("Mekan bulunamadı: " + req.getMekanId()));

        Etkinlik e = new Etkinlik();
        e.setBaslik(req.getBaslik());
        e.setTur(req.getTur());
        e.setSehir(req.getSehir());
        e.setTarih(req.getTarih());
        e.setTemelFiyat(req.getTemelFiyat());
        e.setMekan(mekan);
        e.setKapasite(req.getKapasite());
        e.setSatilan(0);
        e.setDurum(EtkinlikDurum.ACTIVE);

        return toResponse(etkinlikRepository.save(e));
    }

    public EtkinlikIptalResponse iptalEt(Long id) {
        Etkinlik e = etkinlikRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Etkinlik bulunamadı: " + id));

        // Hard delete yok: iptal (CANCELLED)
        e.setDurum(EtkinlikDurum.CANCELLED);

        // Şimdilik: iptal edilen bilet sayısı = satılan (Gün2’de gerçek bilet kaydına bağlarız)
        int iptalEdilen = (e.getSatilan() == null ? 0 : e.getSatilan());

        return new EtkinlikIptalResponse(
                e.getId(),
                e.getDurum().name(),
                iptalEdilen
        );
    }

    // ---------- mapper ----------

    private EtkinlikResponse toResponse(Etkinlik e) {
        Mekan m = e.getMekan(); // ManyToOne LAZY ama transaction içinde olduğumuz için sorun yok

        Long mekanId = (m != null ? m.getId() : null);
        String mekanAd = (m != null ? m.getAd() : null);          // Mekan entity alan adın farklıysa düzelt
        Integer mekanKapasite = (m != null ? m.getKapasite() : null); // Mekan entity alan adın farklıysa düzelt

        return new EtkinlikResponse(
                e.getId(),
                e.getBaslik(),
                e.getTur(),
                e.getSehir(),
                e.getTarih(),
                e.getTemelFiyat(),
                e.getSatilan(),
                e.getDurum().name(),
                e.getKapasite(),   // ✅ EKLENDİ
                mekanId,
                mekanAd,
                mekanKapasite
        );

    }
    public EtkinlikResponse guncelle(Long id, EtkinlikUpdateRequest req) {
        Etkinlik e = etkinlikRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Etkinlik bulunamadı: " + id));

        // CANCELLED ise burada kesin kes
        if (EtkinlikDurum.CANCELLED.equals(e.getDurum())) {
            throw new IllegalStateException("İptal edilmiş etkinlik güncellenemez: " + id);
        }

        if (req.getBaslik() != null) e.setBaslik(req.getBaslik());
        if (req.getTur() != null) e.setTur(req.getTur());
        if (req.getSehir() != null) e.setSehir(req.getSehir());
        if (req.getTarih() != null) e.setTarih(req.getTarih());
        if (req.getTemelFiyat() != null) e.setTemelFiyat(req.getTemelFiyat());

        if (req.getMekanId() != null) {
            Mekan mekan = mekanRepository.findById(req.getMekanId())
                    .orElseThrow(() -> new NotFoundException("Mekan bulunamadı: " + req.getMekanId()));
            e.setMekan(mekan);
        }

        Etkinlik saved = etkinlikRepository.save(e);
        if (saved == null) { // pratikte olmaz ama test/mocking güvenliği
            throw new IllegalStateException("Etkinlik kaydedilemedi (save null döndü)");
        }
        return toResponse(saved);
    }

}
