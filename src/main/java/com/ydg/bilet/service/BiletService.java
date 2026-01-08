package com.ydg.bilet.service;

import com.ydg.bilet.dto.bilet.BiletIptalResponse;
import com.ydg.bilet.dto.bilet.BiletResponse;
import com.ydg.bilet.dto.bilet.BiletSatinAlRequest;
import com.ydg.bilet.dto.bilet.BiletSatinAlResponse;
import com.ydg.bilet.entity.*;
import com.ydg.bilet.exception.NotFoundException;
import com.ydg.bilet.repository.BiletRepository;
import com.ydg.bilet.repository.EtkinlikRepository;
import com.ydg.bilet.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BiletService {

    private final BiletRepository biletRepository;
    private final EtkinlikRepository etkinlikRepository;
    private final KullaniciRepository kullaniciRepository;

    private Kullanici currentUser() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return kullaniciRepository.findByEmail(principal)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + principal));
    }

    @Transactional
    public BiletSatinAlResponse satinAl(BiletSatinAlRequest req) {
        int adet = (req.getAdet() == null || req.getAdet() < 1) ? 1 : req.getAdet();

        Kullanici user = currentUser();

        Etkinlik etkinlik = etkinlikRepository.findByIdForUpdate(req.getEtkinlikId())
                .orElseThrow(() -> new NotFoundException("Etkinlik bulunamadı: " + req.getEtkinlikId()));

        if (etkinlik.getDurum() == EtkinlikDurum.CANCELLED) {
            throw new IllegalStateException("İptal edilmiş etkinlikten bilet alınamaz.");
        }

        int mevcutSatilan = (etkinlik.getSatilan() == null) ? 0 : etkinlik.getSatilan();
        int kalanOnce = etkinlik.getKapasite() - mevcutSatilan;

        if (kalanOnce < adet) {
            throw new IllegalStateException("Kapasite yetersiz. Kalan koltuk: " + kalanOnce);
        }

        etkinlik.setSatilan(mevcutSatilan + adet);

        List<Bilet> biletler = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < adet; i++) {
            Bilet b = new Bilet();
            b.setEtkinlik(etkinlik);
            b.setKullanici(user);
            b.setCreatedAt(now);
            b.setDurum(BiletDurum.ACTIVE);
            biletler.add(b);
        }

        biletRepository.saveAll(biletler);

        int kalanKoltuk = etkinlik.getKapasite() - etkinlik.getSatilan();

        return new BiletSatinAlResponse(
                etkinlik.getId(),
                adet,
                kalanKoltuk,
                biletler.stream().map(Bilet::getId).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<BiletResponse> benimBiletlerim() {
        Kullanici user = currentUser();
        return biletRepository.findByKullaniciIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private BiletResponse toResponse(Bilet b) {
        Etkinlik e = b.getEtkinlik();
        return new BiletResponse(
                b.getId(),
                e.getId(),
                e.getBaslik(),
                e.getTur(),
                e.getSehir(),
                e.getTarih(),
                e.getTemelFiyat(),
                b.getCreatedAt()
        );
    }

    @Transactional
    public BiletIptalResponse iptalEt(Long biletId) {
        Kullanici user = currentUser();

        Bilet bilet = biletRepository.findById(biletId)
                .orElseThrow(() -> new NotFoundException("Bilet bulunamadı: " + biletId));

        if (!bilet.getKullanici().getId().equals(user.getId())) {
            throw new RuntimeException("Bu bileti iptal etme yetkiniz yok.");
        }

        if (bilet.getDurum() == BiletDurum.CANCELLED) {
            throw new IllegalStateException("Bilet zaten iptal edilmiş.");
        }

        Etkinlik etkinlik = bilet.getEtkinlik();

        if (etkinlik.getTarih() == null) {
            throw new IllegalStateException("Etkinlik tarihi belirlenmemiş.");
        }
        if (etkinlik.getTarih().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new IllegalStateException("Etkinliğe 24 saatten az süre kaldığı için iptal yapılamaz.");
        }

        int güncelSatilan = (etkinlik.getSatilan() == null) ? 0 : etkinlik.getSatilan();
        etkinlik.setSatilan(Math.max(0, güncelSatilan - 1));

        bilet.setDurum(BiletDurum.CANCELLED);
        bilet.setCancelledAt(LocalDateTime.now());

        biletRepository.save(bilet);
        etkinlikRepository.save(etkinlik);

        int yeniKalanKoltuk = etkinlik.getKapasite() - etkinlik.getSatilan();

        return new BiletIptalResponse(
                bilet.getId(),
                etkinlik.getId(),
                yeniKalanKoltuk,
                bilet.getCancelledAt()
        );
    }
    @Transactional(readOnly = true)
    public List<BiletResponse> tumBiletler() {
        return biletRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
}