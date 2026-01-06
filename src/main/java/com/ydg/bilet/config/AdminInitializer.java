package com.ydg.bilet.config;

import com.ydg.bilet.entity.Kullanici;
import com.ydg.bilet.entity.Role;
import com.ydg.bilet.repository.KullaniciRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final KullaniciRepository repo;
    private final PasswordEncoder encoder;

    @PostConstruct
    public void init() {
        if (!repo.existsByEmail("admin@ydg.com")) {
            Kullanici a = new Kullanici();
            a.setEmail("admin@ydg.com");
            a.setSifre(encoder.encode("admin123"));
            a.setAd("Admin");
            a.setSoyad("YDG");
            a.setAktif(true);
            a.setRole(Role.ADMIN);
            repo.save(a);
        }
    }
}
