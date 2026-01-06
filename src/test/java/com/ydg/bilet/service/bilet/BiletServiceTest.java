package com.ydg.bilet.service.bilet;

import com.ydg.bilet.dto.bilet.BiletSatinAlRequest;
import com.ydg.bilet.dto.bilet.BiletSatinAlResponse;
import com.ydg.bilet.entity.*;
import com.ydg.bilet.exception.NotFoundException;
import com.ydg.bilet.repository.BiletRepository;
import com.ydg.bilet.repository.EtkinlikRepository;
import com.ydg.bilet.repository.KullaniciRepository;
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
    void satinAl_basarili_adetKadarBiletUretir_satilanArtar_veResponseDoner() {
        Kullanici user = new Kullanici();
        user.setId(100L);
        user.setEmail("user1");

        when(kullaniciRepository.findByEmail("user1")).thenReturn(Optional.of(user));

        Etkinlik e = new Etkinlik();
        e.setId(5L);
        e.setDurum(EtkinlikDurum.ACTIVE);
        e.setKapasite(10);
        e.setSatilan(7);

        when(etkinlikRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(e));

        // saveAll geri dönsün
        when(biletRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        BiletSatinAlRequest req = new BiletSatinAlRequest();
        req.setEtkinlikId(5L);
        req.setAdet(2);

        BiletSatinAlResponse res = biletService.satinAl(req);

        assertEquals(5L, res.getEtkinlikId());
        assertEquals(2, res.getAdet());
        assertEquals(9, e.getSatilan());          // 7 + 2
        assertEquals(1, res.getKalanKoltuk());    // 10 - 9

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Bilet>> captor = ArgumentCaptor.forClass(List.class);
        verify(biletRepository, times(1)).saveAll(captor.capture());

        List<Bilet> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertSame(user, saved.get(0).getKullanici());
        assertSame(e, saved.get(0).getEtkinlik());
        assertNotNull(saved.get(0).getCreatedAt());
    }

    @Test
    void satinAl_etkinlikYoksa_404() {
        when(kullaniciRepository.findByEmail("user1")).thenReturn(Optional.of(new Kullanici()));
        when(etkinlikRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        BiletSatinAlRequest req = new BiletSatinAlRequest();
        req.setEtkinlikId(99L);
        req.setAdet(1);

        assertThrows(NotFoundException.class, () -> biletService.satinAl(req));
        verify(biletRepository, never()).saveAll(anyList());
    }

    @Test
    void satinAl_cancelledIse_IllegalState() {
        when(kullaniciRepository.findByEmail("user1")).thenReturn(Optional.of(new Kullanici()));

        Etkinlik e = new Etkinlik();
        e.setId(1L);
        e.setDurum(EtkinlikDurum.CANCELLED);
        e.setKapasite(10);
        e.setSatilan(0);

        when(etkinlikRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(e));

        BiletSatinAlRequest req = new BiletSatinAlRequest();
        req.setEtkinlikId(1L);
        req.setAdet(1);

        assertThrows(IllegalStateException.class, () -> biletService.satinAl(req));
        verify(biletRepository, never()).saveAll(anyList());
    }

    @Test
    void satinAl_kapasiteYetmezse_IllegalState() {
        when(kullaniciRepository.findByEmail("user1")).thenReturn(Optional.of(new Kullanici()));

        Etkinlik e = new Etkinlik();
        e.setId(2L);
        e.setDurum(EtkinlikDurum.ACTIVE);
        e.setKapasite(10);
        e.setSatilan(10);

        when(etkinlikRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(e));

        BiletSatinAlRequest req = new BiletSatinAlRequest();
        req.setEtkinlikId(2L);
        req.setAdet(1);

        assertThrows(IllegalStateException.class, () -> biletService.satinAl(req));
        verify(biletRepository, never()).saveAll(anyList());
    }

    @Test
    void iptalEt_basarili_satilanAzalir_veBiletCancelledOlur() {
        // user
        Kullanici user = new Kullanici();
        user.setId(10L);
        user.setEmail("user1");
        when(kullaniciRepository.findByEmail("user1")).thenReturn(Optional.of(user));

        // etkinlik
        Etkinlik e = new Etkinlik();
        e.setId(5L);
        e.setKapasite(100);
        e.setSatilan(7);
        e.setTarih(LocalDateTime.now().plusDays(2));

        // bilet
        Bilet b = new Bilet();
        b.setId(99L);
        b.setEtkinlik(e);
        b.setKullanici(user);
        b.setDurum(BiletDurum.ACTIVE);

        when(biletRepository.findByIdForUpdate(99L)).thenReturn(Optional.of(b));

        var res = biletService.iptalEt(99L);

        assertEquals(99L, res.getBiletId());
        assertEquals(5L, res.getEtkinlikId());
        assertEquals(6, e.getSatilan());
        assertEquals(BiletDurum.CANCELLED, b.getDurum());
        assertNotNull(b.getCancelledAt());
    }

    @Test
    void iptalEt_zatenIptalse_IllegalState() {
        Kullanici user = new Kullanici(); user.setId(10L); user.setEmail("user1");
        when(kullaniciRepository.findByEmail("user1")).thenReturn(Optional.of(user));

        Etkinlik e = new Etkinlik();
        e.setId(1L);
        e.setKapasite(10);
        e.setSatilan(1);
        e.setTarih(LocalDateTime.now().plusDays(2));

        Bilet b = new Bilet();
        b.setId(1L);
        b.setEtkinlik(e);
        b.setKullanici(user);
        b.setDurum(BiletDurum.CANCELLED);

        when(biletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(b));

        assertThrows(IllegalStateException.class, () -> biletService.iptalEt(1L));
    }


}
