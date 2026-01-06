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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BiletService {

    private static final long IPTAL_SON_SAAT = 24; // 24 saat kala iptal yok

    private final BiletRepository biletRepository;
    private final EtkinlikRepository etkinlikRepository;
    private final KullaniciRepository kullaniciRepository;

    public BiletService(BiletRepository biletRepository,
                        EtkinlikRepository etkinlikRepository,
                        KullaniciRepository kullaniciRepository) {
        this.biletRepository = biletRepository;
        this.etkinlikRepository = etkinlikRepository;
        this.kullaniciRepository = kullaniciRepository;
    }

    private Kullanici currentUser() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return kullaniciRepository.findByEmail(principal)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + principal));
    }

    @Transactional
    public BiletSatinAlResponse satinAl(BiletSatinAlRequest req) {

        // ✅ adet yoksa default 1
        int adet = (req.getAdet() == null ? 1 : req.getAdet());

        Kullanici user = currentUser();

        // ✅ Concurrency için kilitli çek
        Etkinlik etkinlik = etkinlikRepository.findByIdForUpdate(req.getEtkinlikId())
                .orElseThrow(() -> new NotFoundException("Etkinlik bulunamadı: " + req.getEtkinlikId()));

        // ✅ İptal kontrolü
        if (etkinlik.getDurum() == EtkinlikDurum.CANCELLED) {
            throw new IllegalStateException("İptal edilmiş etkinlikten bilet alınamaz: " + etkinlik.getId());
        }

        int kalanOnce = etkinlik.getKapasite() - etkinlik.getSatilan();
        if (kalanOnce < adet) {
            throw new IllegalStateException("Kapasite yetersiz. Kalan koltuk: " + kalanOnce);
        }

        // ✅ sadece 1 kez artır
        etkinlik.setSatilan(etkinlik.getSatilan() + adet);

        // ✅ Adet kadar bilet üret
        List<Bilet> biletler = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < adet; i++) {
            Bilet b = new Bilet();
            b.setEtkinlik(etkinlik);
            b.setKullanici(user);
            b.setCreatedAt(now);
            biletler.add(b);
        }

        biletRepository.saveAll(biletler);

        List<Long> ids = biletler.stream().map(Bilet::getId).toList();

        int kalanKoltuk = etkinlik.getKapasite() - etkinlik.getSatilan();

        return new BiletSatinAlResponse(
                etkinlik.getId(),
                adet,
                kalanKoltuk,
                ids
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
                b.getId(),              // biletId
                e.getId(),              // etkinlikId
                e.getBaslik(),          // etkinlikBaslik
                e.getTur(),             // etkinlikTur
                e.getSehir(),           // etkinlikSehir
                e.getTarih(),           // etkinlikTarih
                e.getTemelFiyat(),      // temelFiyat
                b.getCreatedAt()        // satinAlmaTarihi
        );
    }
    @Transactional
    public BiletIptalResponse iptalEt(Long biletId) {
        Kullanici user = currentUser();

        Bilet bilet = biletRepository.findByIdForUpdate(biletId)
                .orElseThrow(() -> new NotFoundException("Bilet bulunamadı: " + biletId));

        // sahiplik kontrolü
        if (!bilet.getKullanici().getId().equals(user.getId())) {
            try {
                throw new AccessDeniedException("Bu bileti iptal edemezsiniz.");
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }

        if (bilet.getDurum() == BiletDurum.CANCELLED) {
            throw new IllegalStateException("Bilet zaten iptal edilmiş: " + biletId);
        }

        Etkinlik etkinlik = bilet.getEtkinlik();

        // 24 saat kuralı (etkinlik geçmişteyse de iptal yok)
        if (etkinlik.getTarih() == null) {
            throw new IllegalStateException("Etkinlik tarihi yok, iptal yapılamaz.");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusHours(24);

        if (etkinlik.getTarih().isBefore(limit)) {
            throw new IllegalStateException("Etkinliğe 24 saatten az kaldı, iptal/iade yapılamaz.");
        }


        // satilan azalt (negatife düşmesin)
        int satilan = etkinlik.getSatilan() == null ? 0 : etkinlik.getSatilan();
        etkinlik.setSatilan(Math.max(0, satilan - 1));

        bilet.setDurum(BiletDurum.CANCELLED);
        bilet.setCancelledAt(LocalDateTime.now());

        // save gerekmez çoğu durumda (managed entity) ama net olsun:
        // biletRepository.save(bilet);

        Integer kalan = etkinlik.getKapasite() - etkinlik.getSatilan();

        return new BiletIptalResponse(
                bilet.getId(),
                etkinlik.getId(),
                kalan,
                bilet.getCancelledAt()
        );
    }


}

