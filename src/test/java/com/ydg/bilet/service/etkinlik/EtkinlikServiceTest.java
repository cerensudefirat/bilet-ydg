package com.ydg.bilet.service.etkinlik;

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
import com.ydg.bilet.service.EtkinlikService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtkinlikServiceTest {

    @Mock
    EtkinlikRepository etkinlikRepository;

    @Mock
    MekanRepository mekanRepository;

    @InjectMocks
    EtkinlikService etkinlikService;

    private Mekan mekan1;

    @BeforeEach
    void setup() {
        mekan1 = new Mekan();
        mekan1.setId(10L);
        mekan1.setAd("Kongre Merkezi");
        mekan1.setSehir("Malatya");
        mekan1.setKapasite(1200);
    }

    @Test
    void publicListele_activeOlanlariSiraliDoner() {
        Etkinlik e1 = etkinlik(1L, "Konser 1", EtkinlikDurum.ACTIVE, LocalDateTime.now().plusDays(1), mekan1);
        Etkinlik e2 = etkinlik(2L, "Konser 2", EtkinlikDurum.ACTIVE, LocalDateTime.now().plusDays(2), mekan1);

        when(etkinlikRepository.findByDurumOrderByTarihAsc(EtkinlikDurum.ACTIVE))
                .thenReturn(List.of(e1, e2));

        List<EtkinlikResponse> res = etkinlikService.publicListele();

        assertEquals(2, res.size());
        assertEquals(1L, res.get(0).getId());
        assertEquals("Konser 1", res.get(0).getBaslik());
        assertEquals("Kongre Merkezi", res.get(0).getMekanAd());
        assertEquals(10L, res.get(0).getMekanId());
        assertEquals(1200, res.get(0).getMekanKapasite());

        verify(etkinlikRepository, times(1)).findByDurumOrderByTarihAsc(EtkinlikDurum.ACTIVE);
        verifyNoMoreInteractions(etkinlikRepository);
    }

    @Test
    void publicDetay_activeIseDoner() {
        Etkinlik e = etkinlik(5L, "Tiyatro", EtkinlikDurum.ACTIVE, LocalDateTime.now().plusDays(3), mekan1);

        when(etkinlikRepository.findById(5L)).thenReturn(Optional.of(e));

        EtkinlikResponse res = etkinlikService.publicDetay(5L);

        assertEquals(5L, res.getId());
        assertEquals("Tiyatro", res.getBaslik());
        assertEquals("ACTIVE", res.getDurum());
        assertEquals(10L, res.getMekanId());

        verify(etkinlikRepository).findById(5L);
    }

    @Test
    void publicDetay_cancelledIseNotFoundFirlatsin() {
        Etkinlik e = etkinlik(6L, "Iptal", EtkinlikDurum.CANCELLED, LocalDateTime.now().plusDays(1), mekan1);

        when(etkinlikRepository.findById(6L)).thenReturn(Optional.of(e));

        assertThrows(NotFoundException.class, () -> etkinlikService.publicDetay(6L));
        verify(etkinlikRepository).findById(6L);
    }

    @Test
    void publicDetay_yoksaNotFoundFirlatsin() {
        when(etkinlikRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> etkinlikService.publicDetay(99L));
        verify(etkinlikRepository).findById(99L);
    }


    @Test
    void olustur_mekanIdYoksaIllegalArgument() {
        EtkinlikCreateRequest req = new EtkinlikCreateRequest();
        req.setBaslik("A");
        req.setTur("Konser");
        req.setSehir("Malatya");
        req.setTarih(LocalDateTime.now().plusDays(1));
        req.setTemelFiyat(new BigDecimal("250.00"));
        req.setMekanId(null);

        assertThrows(IllegalArgumentException.class, () -> etkinlikService.olustur(req));

        verifyNoInteractions(mekanRepository);
        verifyNoInteractions(etkinlikRepository);
    }

    @Test
    void olustur_mekanYoksaNotFound() {
        EtkinlikCreateRequest req = createReq(10L);

        when(mekanRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> etkinlikService.olustur(req));

        verify(mekanRepository).findById(10L);
        verifyNoInteractions(etkinlikRepository);
    }

    @Test
    void olustur_basariliKaydederVeResponseDoner() {
        EtkinlikCreateRequest req = createReq(10L);

        when(mekanRepository.findById(10L)).thenReturn(Optional.of(mekan1));

        ArgumentCaptor<Etkinlik> captor = ArgumentCaptor.forClass(Etkinlik.class);

        when(etkinlikRepository.save(any(Etkinlik.class))).thenAnswer(inv -> {
            Etkinlik saved = inv.getArgument(0);
            saved.setId(77L);
            return saved;
        });

        EtkinlikResponse res = etkinlikService.olustur(req);

        assertEquals(77L, res.getId());
        assertEquals("Yeni Etkinlik", res.getBaslik());
        assertEquals("Konser", res.getTur());
        assertEquals("Malatya", res.getSehir());
        assertEquals(new BigDecimal("250.00"), res.getTemelFiyat());
        assertEquals("ACTIVE", res.getDurum());
        assertEquals(0, res.getSatilan());

        assertEquals(10L, res.getMekanId());
        assertEquals("Kongre Merkezi", res.getMekanAd());
        assertEquals(1200, res.getMekanKapasite());

        verify(mekanRepository).findById(10L);
        verify(etkinlikRepository).save(captor.capture());

        Etkinlik savedEntity = captor.getValue();
        assertEquals("Yeni Etkinlik", savedEntity.getBaslik());
        assertEquals(EtkinlikDurum.ACTIVE, savedEntity.getDurum());
        assertEquals(0, savedEntity.getSatilan());
        assertNotNull(savedEntity.getMekan());
        assertEquals(10L, savedEntity.getMekan().getId());
    }

    @Test
    void iptalEt_yoksaNotFound() {
        when(etkinlikRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> etkinlikService.iptalEt(123L));

        verify(etkinlikRepository).findById(123L);
    }
    @Test
    void guncelle_yoksaNotFound() {
        EtkinlikUpdateRequest req = new EtkinlikUpdateRequest();
        req.setBaslik("Yeni");

        when(etkinlikRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> etkinlikService.guncelle(404L, req));
        verify(etkinlikRepository).findById(404L);
    }

    @Test
    void guncelle_cancelledIseIllegalState() {
        Etkinlik e = etkinlik(9L, "Eski", EtkinlikDurum.CANCELLED,
                LocalDateTime.now().plusDays(1), mekan1);

        when(etkinlikRepository.findById(9L)).thenReturn(Optional.of(e));

        EtkinlikUpdateRequest req = new EtkinlikUpdateRequest();
        req.setBaslik("Yeni");

        assertThrows(IllegalStateException.class, () -> etkinlikService.guncelle(9L, req));

        verify(etkinlikRepository).findById(9L);

        // kritik: save/toResponse yoluna girmemeli
        verify(etkinlikRepository, never()).save(any());
        verifyNoInteractions(mekanRepository);
    }


    @Test
    void guncelle_kismiAlanlariDegistirir_veResponseDoner() {
        Etkinlik e = etkinlik(7L, "Eski Baslik", EtkinlikDurum.ACTIVE, LocalDateTime.now().plusDays(2), mekan1);
        when(etkinlikRepository.findById(7L)).thenReturn(Optional.of(e));

        when(etkinlikRepository.save(any(Etkinlik.class))).thenAnswer(inv -> inv.getArgument(0));

        EtkinlikUpdateRequest req = new EtkinlikUpdateRequest();
        req.setBaslik("Yeni Baslik");
        req.setTemelFiyat(new BigDecimal("999.99"));

        EtkinlikResponse res = etkinlikService.guncelle(7L, req);

        assertEquals(7L, res.getId());
        assertEquals("Yeni Baslik", res.getBaslik());
        assertEquals(new BigDecimal("999.99"), res.getTemelFiyat());
        assertEquals("ACTIVE", res.getDurum());

        // entity gerçekten değişti mi
        assertEquals("Yeni Baslik", e.getBaslik());
        assertEquals(new BigDecimal("999.99"), e.getTemelFiyat());

        verify(etkinlikRepository).findById(7L);
        verify(etkinlikRepository).save(any(Etkinlik.class));
    }

    @Test
    void iptalEt_basariliIseCancelledYaparVeIptalSayisiniDoner() {
        Etkinlik e = etkinlik(55L, "Konser", EtkinlikDurum.ACTIVE, LocalDateTime.now().plusDays(1), mekan1);
        e.setSatilan(12);

        when(etkinlikRepository.findById(55L)).thenReturn(Optional.of(e));

        EtkinlikIptalResponse res = etkinlikService.iptalEt(55L);

        assertEquals(55L, res.getEtkinlikId());
        assertEquals("CANCELLED", res.getDurum());
        assertEquals(12, res.getIptalEdilenBiletSayisi());

        assertEquals(EtkinlikDurum.CANCELLED, e.getDurum());

        verify(etkinlikRepository).findById(55L);
    }


    private Etkinlik etkinlik(Long id, String baslik, EtkinlikDurum durum, LocalDateTime tarih, Mekan mekan) {
        Etkinlik e = new Etkinlik();
        e.setId(id);
        e.setBaslik(baslik);
        e.setTur("Konser");
        e.setSehir("Malatya");
        e.setTarih(tarih);
        e.setTemelFiyat(new BigDecimal("250.00"));
        e.setSatilan(0);
        e.setDurum(durum);
        e.setMekan(mekan);
        e.setKapasite(100);
        return e;
    }

    private EtkinlikCreateRequest createReq(Long mekanId) {
        EtkinlikCreateRequest req = new EtkinlikCreateRequest();
        req.setBaslik("Yeni Etkinlik");
        req.setTur("Konser");
        req.setSehir("Malatya");
        req.setTarih(LocalDateTime.now().plusDays(1));
        req.setTemelFiyat(new BigDecimal("250.00"));
        req.setMekanId(mekanId);
        req.setKapasite(150);
        return req;
    }
}
