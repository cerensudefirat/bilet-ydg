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
@RequiredArgsConstructor // Constructor yerine Lombok kullanımı kodu sadeleştirir
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
        // 1. Adet kontrolü ve varsayılan değer
        int adet = (req.getAdet() == null || req.getAdet() < 1) ? 1 : req.getAdet();

        Kullanici user = currentUser();

        // 2. Etkinliği veritabanı kilidiyle çek (Race Condition engelleme)
        Etkinlik etkinlik = etkinlikRepository.findByIdForUpdate(req.getEtkinlikId())
                .orElseThrow(() -> new NotFoundException("Etkinlik bulunamadı: " + req.getEtkinlikId()));

        // 3. Etkinlik durum kontrolü
        if (etkinlik.getDurum() == EtkinlikDurum.CANCELLED) {
            throw new IllegalStateException("İptal edilmiş etkinlikten bilet alınamaz.");
        }

        // 4. Kapasite hesaplaması (Null güvenli)
        int mevcutSatilan = (etkinlik.getSatilan() == null) ? 0 : etkinlik.getSatilan();
        int kalanOnce = etkinlik.getKapasite() - mevcutSatilan;

        if (kalanOnce < adet) {
            throw new IllegalStateException("Kapasite yetersiz. Kalan koltuk: " + kalanOnce);
        }

        // 5. Satılan sayısını güncelle
        etkinlik.setSatilan(mevcutSatilan + adet);

        // 6. Biletleri üret ve listeye ekle
        List<Bilet> biletler = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < adet; i++) {
            Bilet b = new Bilet();
            b.setEtkinlik(etkinlik);
            b.setKullanici(user);
            b.setCreatedAt(now);
            b.setDurum(BiletDurum.ACTIVE); // Bilet durumunu set etmeyi unutma
            biletler.add(b);
        }

        // 7. Toplu kaydet
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

        // Bileti çek
        Bilet bilet = biletRepository.findById(biletId)
                .orElseThrow(() -> new NotFoundException("Bilet bulunamadı: " + biletId));

        // Sahiplik kontrolü
        if (!bilet.getKullanici().getId().equals(user.getId())) {
            throw new RuntimeException("Bu bileti iptal etme yetkiniz yok.");
        }

        if (bilet.getDurum() == BiletDurum.CANCELLED) {
            throw new IllegalStateException("Bilet zaten iptal edilmiş.");
        }

        Etkinlik etkinlik = bilet.getEtkinlik();

        // Zaman kontrolü (24 saat kuralı)
        if (etkinlik.getTarih() == null) {
            throw new IllegalStateException("Etkinlik tarihi belirlenmemiş.");
        }
        if (etkinlik.getTarih().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new IllegalStateException("Etkinliğe 24 saatten az süre kaldığı için iptal yapılamaz.");
        }

        // Kapasiteyi geri iade et (Null safe)
        int güncelSatilan = (etkinlik.getSatilan() == null) ? 0 : etkinlik.getSatilan();
        etkinlik.setSatilan(Math.max(0, güncelSatilan - 1));

        // Bilet durumunu güncelle
        bilet.setDurum(BiletDurum.CANCELLED);
        bilet.setCancelledAt(LocalDateTime.now());

        // Değişiklikleri kaydet
        biletRepository.save(bilet);
        etkinlikRepository.save(etkinlik);

        // Güncel kalan koltuk sayısını hesapla
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
        // Admin yetki kontrolü genellikle Controller seviyesinde @PreAuthorize ile yapılır,
        // ancak servis katmanında tüm listeyi dönen bu metod admin paneli için şarttır.
        return biletRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
}