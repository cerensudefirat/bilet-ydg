package com.ydg.bilet.service.bilet;

import com.ydg.bilet.dto.bilet.*;
import com.ydg.bilet.entity.*;
import com.ydg.bilet.exception.NotFoundException;
import com.ydg.bilet.repository.*;
import com.ydg.bilet.service.BiletService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BiletServiceTest {

    @Mock BiletRepository biletRepository;
    @Mock EtkinlikRepository etkinlikRepository;
    @Mock KullaniciRepository kullaniciRepository;

    @InjectMocks BiletService biletService;

    @BeforeEach
    void setupAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "pass", List.of())
        );
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void iptalEt_basarili_satilanAzalir_veBiletCancelledOlur() {
        Kullanici user = new Kullanici();
        user.setId(10L);
        user.setEmail("user1");
        when(kullaniciRepository.findByEmail("user1")).thenReturn(Optional.of(user));

        Etkinlik e = new Etkinlik();
        e.setId(5L);
        e.setKapasite(100);
        e.setSatilan(7);
        e.setTarih(LocalDateTime.now().plusDays(7)); // 24 saat kuralı için gelecek tarih

        Bilet b = new Bilet();
        b.setId(99L);
        b.setEtkinlik(e);
        b.setKullanici(user);
        b.setDurum(BiletDurum.ACTIVE);

        when(biletRepository.findById(99L)).thenReturn(Optional.of(b));

        BiletIptalResponse res = biletService.iptalEt(99L);

        assertEquals(99L, res.getBiletId());
        assertEquals(6, e.getSatilan()); // 7 idi, 6 oldu mu?
        assertEquals(BiletDurum.CANCELLED, b.getDurum()); // İptal oldu mu?
        assertEquals(94, res.getKalanKoltuk()); // 100 - 6 = 94 mü?
    }

    @Test
    void iptalEt_biletYoksa_NotFound() {
        when(kullaniciRepository.findByEmail("user1")).thenReturn(Optional.of(new Kullanici()));
        when(biletRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> biletService.iptalEt(123L));
    }

    @Test
    void iptalEt_zatenIptalse_IllegalState() {
        Kullanici user = new Kullanici();
        user.setId(10L);
        user.setEmail("user1");
        when(kullaniciRepository.findByEmail("user1")).thenReturn(Optional.of(user));

        Etkinlik e = new Etkinlik();
        e.setTarih(LocalDateTime.now().plusDays(7));

        Bilet b = new Bilet();
        b.setId(1L);
        b.setKullanici(user);
        b.setEtkinlik(e);
        b.setDurum(BiletDurum.CANCELLED); // Zaten iptal edilmiş

        when(biletRepository.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(IllegalStateException.class, () -> biletService.iptalEt(1L));
    }
}