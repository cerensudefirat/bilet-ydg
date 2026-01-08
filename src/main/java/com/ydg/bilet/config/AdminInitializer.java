package com.ydg.bilet.config;

import com.ydg.bilet.entity.*;
import com.ydg.bilet.repository.EtkinlikRepository;
import com.ydg.bilet.repository.KullaniciRepository;
import com.ydg.bilet.repository.MekanRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final KullaniciRepository repo;
    private final MekanRepository mekanRepo;
    private final EtkinlikRepository etkinlikRepo; // Bunu ekledik
    private final PasswordEncoder encoder;

    @PostConstruct
    public void init() {
        // 1. Admin Kullanıcısı Oluşturma
        if (!repo.existsByEmail("admin@ydg.com")) {
            Kullanici a = new Kullanici();
            a.setEmail("admin@ydg.com");
            a.setSifre(encoder.encode("admin123"));
            a.setAd("Admin");
            a.setSoyad("YDG");
            a.setAktif(true);
            a.setRole(Role.ADMIN);
            repo.save(a);
            System.out.println("TEST ADMIN KULLANICISI OLUŞTURULDU: admin@ydg.com");
        }

        // 2. Mekan Oluşturma
        Mekan m;
        if (mekanRepo.count() == 0) {
            m = new Mekan();
            m.setAd("Test Sahnesi");
            m.setAdres("Test Mah. No:1 İstanbul");
            m.setSehir("İstanbul");
            m.setKapasite(100);
            m = mekanRepo.save(m);
            System.out.println("TEST MEKANI OLUŞTURULDU: Test Sahnesi");
        } else {
            m = mekanRepo.findAll().get(0);
        }

        // 3. Etkinlik Oluşturma (Entity alan isimlerine göre düzeltildi)
        if (etkinlikRepo.count() == 0) {
            Etkinlik e = new Etkinlik();
            e.setBaslik("Test Konseri"); // 'ad' yerine 'baslik'
            e.setTur("Konser");         // Zorunlu alan
            e.setSehir("İstanbul");      // Zorunlu alan
            e.setTarih(LocalDateTime.now().plusDays(7));
            e.setTemelFiyat(new BigDecimal("150.00")); // 'fiyat' yerine 'temelFiyat'
            e.setKapasite(50);
            e.setSatilan(0);
            e.setDurum(EtkinlikDurum.ACTIVE);
            e.setMekan(m);

            etkinlikRepo.save(e);
            System.out.println("✅ TEST ETKİNLİĞİ OLUŞTURULDU: Test Konseri");
        }
    }
}