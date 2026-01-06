package com.ydg.bilet.service;

import com.ydg.bilet.dto.LoginRequest;
import com.ydg.bilet.dto.RegisterRequest;
import com.ydg.bilet.entity.Kullanici;
import com.ydg.bilet.entity.Role;
import com.ydg.bilet.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {

        if (kullaniciRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email zaten kayıtlı");
        }

        Kullanici kullanici = new Kullanici();
        kullanici.setEmail(request.getEmail());
        kullanici.setSifre(passwordEncoder.encode(request.getSifre()));
        kullanici.setAd(request.getAd());
        kullanici.setSoyad(request.getSoyad());
        kullanici.setAktif(true);
        kullanici.setRole(Role.USER);


        kullaniciRepository.save(kullanici);
    }

    public void login(LoginRequest request) {

        Kullanici kullanici = kullaniciRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (!kullanici.getAktif()) {
            throw new RuntimeException("Kullanıcı pasif");
        }

        if (!passwordEncoder.matches(request.getSifre(), kullanici.getSifre())) {
            throw new RuntimeException("Şifre hatalı");
        }
    }
}
