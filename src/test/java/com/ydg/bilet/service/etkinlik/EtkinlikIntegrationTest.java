package com.ydg.bilet.service.etkinlik;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydg.bilet.dto.etkinlik.EtkinlikCreateRequest;
import com.ydg.bilet.dto.etkinlik.EtkinlikUpdateRequest;
import com.ydg.bilet.entity.Etkinlik;
import com.ydg.bilet.entity.EtkinlikDurum;
import com.ydg.bilet.entity.Mekan;
import com.ydg.bilet.repository.EtkinlikRepository;
import com.ydg.bilet.repository.MekanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class EtkinlikIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired EtkinlikRepository etkinlikRepository;
    @Autowired MekanRepository mekanRepository;

    private Mekan mekan;
    private Etkinlik active1;
    private Etkinlik active2;
    private Etkinlik cancelled;

    @BeforeEach
    void setup() {
        etkinlikRepository.deleteAll();
        mekanRepository.deleteAll();

        mekan = new Mekan();
        mekan.setAd("Kongre Merkezi");
        mekan.setAdres("Adres 1");
        mekan.setSehir("Malatya");
        mekan.setKapasite(1200);
        mekan = mekanRepository.save(mekan);

        active1 = new Etkinlik();
        active1.setBaslik("Konser 1");
        active1.setTur("Konser");
        active1.setSehir("Malatya");
        active1.setTarih(LocalDateTime.now().plusDays(1));
        active1.setTemelFiyat(new BigDecimal("200.00"));
        active1.setSatilan(0);
        active1.setDurum(EtkinlikDurum.ACTIVE);
        active1.setMekan(mekan);

        active1.setKapasite(100);

        active1 = etkinlikRepository.save(active1);

        active2 = new Etkinlik();
        active2.setBaslik("Konser 2");
        active2.setTur("Konser");
        active2.setSehir("Malatya");
        active2.setTarih(LocalDateTime.now().plusDays(2));
        active2.setTemelFiyat(new BigDecimal("250.00"));
        active2.setSatilan(0);
        active2.setDurum(EtkinlikDurum.ACTIVE);
        active2.setMekan(mekan);
        active2.setKapasite(200);

        active2 = etkinlikRepository.save(active2);

        cancelled = new Etkinlik();
        cancelled.setBaslik("İptal Edildi");
        cancelled.setTur("Konser");
        cancelled.setSehir("Malatya");
        cancelled.setTarih(LocalDateTime.now().plusDays(3));
        cancelled.setTemelFiyat(new BigDecimal("300.00"));
        cancelled.setSatilan(5);
        cancelled.setDurum(EtkinlikDurum.CANCELLED);
        cancelled.setMekan(mekan);

        cancelled.setKapasite(300);

        cancelled = etkinlikRepository.save(cancelled);
    }

    @Test
    void public_liste_sadeceActive_doner_ve_tarihe_gore_sirali() throws Exception {
        mockMvc.perform(get("/api/etkinlik"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(active1.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(active2.getId().intValue())))
                .andExpect(jsonPath("$[0].mekanId", is(mekan.getId().intValue())))
                .andExpect(jsonPath("$[0].mekanAd", is("Kongre Merkezi")));
    }

    @Test
    void public_detay_active_200() throws Exception {
        mockMvc.perform(get("/api/etkinlik/{id}", active1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(active1.getId().intValue())))
                .andExpect(jsonPath("$.durum", is("ACTIVE")));
    }

    @Test
    void public_detay_cancelled_404() throws Exception {
        mockMvc.perform(get("/api/etkinlik/{id}", cancelled.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_iptal_200() throws Exception {
        mockMvc.perform(delete("/api/admin/etkinlik/{id}", active1.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etkinlikId", is(active1.getId().intValue())))
                .andExpect(jsonPath("$.durum", is("CANCELLED")));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_olustur_201() throws Exception {
        EtkinlikCreateRequest req = new EtkinlikCreateRequest();
        req.setBaslik("Yeni Etkinlik");
        req.setTur("Tiyatro");
        req.setSehir("Malatya");
        req.setTarih(LocalDateTime.now().plusDays(10));
        req.setTemelFiyat(new BigDecimal("150.00"));
        req.setMekanId(mekan.getId());

        req.setKapasite(250);

        mockMvc.perform(post("/api/admin/etkinlik")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/admin/etkinlik/\\d+")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.durum", is("ACTIVE")))
                .andExpect(jsonPath("$.baslik", is("Yeni Etkinlik")))
                .andExpect(jsonPath("$.mekanId", is(mekan.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void admin_guncelle_adminDegilse_403() throws Exception {
        EtkinlikUpdateRequest req = new EtkinlikUpdateRequest();
        req.setBaslik("X");

        mockMvc.perform(put("/api/admin/etkinlik/{id}", active1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_guncelle_notFound_404() throws Exception {
        EtkinlikUpdateRequest req = new EtkinlikUpdateRequest();
        req.setBaslik("X");

        mockMvc.perform(put("/api/admin/etkinlik/{id}", 999999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_guncelle_cancelledIse_409() throws Exception {
        EtkinlikUpdateRequest req = new EtkinlikUpdateRequest();
        req.setBaslik("X");

        mockMvc.perform(put("/api/admin/etkinlik/{id}", cancelled.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }
}
