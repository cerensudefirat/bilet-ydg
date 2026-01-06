package com.ydg.bilet.service.auth;

import com.ydg.bilet.dto.LoginRequest;
import com.ydg.bilet.dto.RegisterRequest;
import com.ydg.bilet.entity.Kullanici;
import com.ydg.bilet.repository.KullaniciRepository;
import com.ydg.bilet.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KullaniciRepository kullaniciRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // ------------------------
    // REGISTER TESTLERİ
    // ------------------------

    @Test
    void register_emailZatenKayitliysa_hataFirlatir() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@test.com");
        req.setSifre("123456");
        req.setAd("Ali");
        req.setSoyad("Veli");

        when(kullaniciRepository.existsByEmail("user@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));

        verify(kullaniciRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_basarili_olunca_sifreEncodeEdip_kullaniciyiKaydeder() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@test.com");
        req.setSifre("123456");
        req.setAd("Ali");
        req.setSoyad("Veli");

        when(kullaniciRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("ENCODED_PASS");

        authService.register(req);

        ArgumentCaptor<Kullanici> captor = ArgumentCaptor.forClass(Kullanici.class);
        verify(kullaniciRepository).save(captor.capture());

        Kullanici saved = captor.getValue();
        assertEquals("user@test.com", saved.getEmail());
        assertEquals("ENCODED_PASS", saved.getSifre());
        assertEquals("Ali", saved.getAd());
        assertEquals("Veli", saved.getSoyad());
        assertTrue(saved.getAktif());
    }

    // ------------------------
    // LOGIN TESTLERİ
    // ------------------------

    @Test
    void login_kullaniciBulunamazsa_hataFirlatir() {
        LoginRequest req = new LoginRequest();
        req.setEmail("nouser@test.com");
        req.setSifre("123");

        when(kullaniciRepository.findByEmail("nouser@test.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertTrue(ex.getMessage().toLowerCase().contains("bulunamad"));
    }

    @Test
    void login_kullaniciPasifse_hataFirlatir() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setSifre("123");

        Kullanici k = new Kullanici();
        k.setEmail("user@test.com");
        k.setSifre("ENC");
        k.setAktif(false);

        when(kullaniciRepository.findByEmail("user@test.com")).thenReturn(Optional.of(k));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertTrue(ex.getMessage().toLowerCase().contains("pasif"));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_sifreHataliysa_hataFirlatir() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setSifre("wrong");

        Kullanici k = new Kullanici();
        k.setEmail("user@test.com");
        k.setSifre("ENC");
        k.setAktif(true);

        when(kullaniciRepository.findByEmail("user@test.com")).thenReturn(Optional.of(k));
        when(passwordEncoder.matches("wrong", "ENC")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertTrue(ex.getMessage().toLowerCase().contains("şifre") || ex.getMessage().toLowerCase().contains("sifre"));
    }

    @Test
    void login_basarili_olunca_hataFirlatmaz() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setSifre("123456");

        Kullanici k = new Kullanici();
        k.setEmail("user@test.com");
        k.setSifre("ENC");
        k.setAktif(true);

        when(kullaniciRepository.findByEmail("user@test.com")).thenReturn(Optional.of(k));
        when(passwordEncoder.matches("123456", "ENC")).thenReturn(true);

        assertDoesNotThrow(() -> authService.login(req));
    }
}
