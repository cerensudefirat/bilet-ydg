package com.ydg.bilet.service.bilet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydg.bilet.dto.bilet.BiletSatinAlRequest;
import com.ydg.bilet.entity.*;
import com.ydg.bilet.repository.BiletRepository;
import com.ydg.bilet.repository.EtkinlikRepository;
import com.ydg.bilet.repository.KullaniciRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class BiletIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired BiletRepository biletRepository;
    @Autowired EtkinlikRepository etkinlikRepository;
    @Autowired MekanRepository mekanRepository;
    @Autowired KullaniciRepository kullaniciRepository;

    private Mekan mekan;
    private Etkinlik etkinlik;

    @BeforeEach
    void setup() {
        biletRepository.deleteAll();
        etkinlikRepository.deleteAll();
        mekanRepository.deleteAll();
        kullaniciRepository.deleteAll();

        Kullanici u = new Kullanici();
        u.setAd("User");
        u.setSoyad("One");
        u.setEmail("user1");
        u.setSifre("x");
        u.setAktif(true);
        u.setRole(Role.USER);
        kullaniciRepository.save(u);

        mekan = new Mekan();
        mekan.setAd("Kongre Merkezi");
        mekan.setAdres("Adres 1");        mekan.setSehir("Malatya");
        mekan.setKapasite(1200);
        mekan = mekanRepository.save(mekan);

        etkinlik = new Etkinlik();
        etkinlik.setBaslik("Konser 1");
        etkinlik.setTur("Konser");
        etkinlik.setSehir("Malatya");
        etkinlik.setTarih(LocalDateTime.now().plusDays(1));
        etkinlik.setTemelFiyat(new BigDecimal("200.00"));
        etkinlik.setKapasite(2);
        etkinlik.setSatilan(0);
        etkinlik.setDurum(EtkinlikDurum.ACTIVE);
        etkinlik.setMekan(mekan);
        etkinlik = etkinlikRepository.save(etkinlik);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void satinAl_201_veSatilanArtar() throws Exception {
        BiletSatinAlRequest req = new BiletSatinAlRequest();
        req.setEtkinlikId(etkinlik.getId());
        req.setAdet(1);

        mockMvc.perform(post("/api/bilet")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.etkinlikId", is(etkinlik.getId().intValue())))
                .andExpect(jsonPath("$.adet", is(1)))
                .andExpect(jsonPath("$.kalanKoltuk", is(1)))
                .andExpect(jsonPath("$.biletIdList").isArray())
                .andExpect(jsonPath("$.biletIdList", hasSize(1)))
                .andExpect(jsonPath("$.biletIdList[0]").isNumber());

        Etkinlik fresh = etkinlikRepository.findById(etkinlik.getId()).orElseThrow();
        assertEquals(1, fresh.getSatilan());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void satinAl_kapasiteDoluysa_409() throws Exception {
        etkinlik.setSatilan(2);
        etkinlikRepository.save(etkinlik);

        BiletSatinAlRequest req = new BiletSatinAlRequest();
        req.setEtkinlikId(etkinlik.getId());
        req.setAdet(1);

        mockMvc.perform(post("/api/bilet")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void satinAl_cancelledIse_409() throws Exception {
        etkinlik.setDurum(EtkinlikDurum.CANCELLED);
        etkinlikRepository.save(etkinlik);

        BiletSatinAlRequest req = new BiletSatinAlRequest();
        req.setEtkinlikId(etkinlik.getId());
        req.setAdet(1);

        mockMvc.perform(post("/api/bilet")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void benimBiletlerim_200_listeler() throws Exception {
        BiletSatinAlRequest req = new BiletSatinAlRequest();
        req.setEtkinlikId(etkinlik.getId());
        req.setAdet(1);

        mockMvc.perform(post("/api/bilet")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/bilet")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/bilet/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void biletIptal_200_satilanAzalir_veDurumCancelledOlur() throws Exception {

        etkinlik.setTarih(LocalDateTime.now().plusDays(2));
        etkinlikRepository.save(etkinlik);

        com.ydg.bilet.dto.bilet.BiletSatinAlRequest req =
                new com.ydg.bilet.dto.bilet.BiletSatinAlRequest();
        req.setEtkinlikId(etkinlik.getId());
        req.setAdet(1);

        String resBody = mockMvc.perform(post("/api/bilet")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.biletIdList", hasSize(1)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long biletId = objectMapper.readTree(resBody)
                .path("biletIdList")
                .get(0)
                .asLong();

        mockMvc.perform(delete("/api/bilet/{id}", biletId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.biletId", is(biletId.intValue())))
                .andExpect(jsonPath("$.etkinlikId", is(etkinlik.getId().intValue())))
                .andExpect(jsonPath("$.cancelledAt", notNullValue()));

        Etkinlik fresh = etkinlikRepository.findById(etkinlik.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(0, fresh.getSatilan());

        Bilet b = biletRepository.findById(biletId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(BiletDurum.CANCELLED, b.getDurum());
    }


    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void biletIptal_24SaattenAzKaldiysa_409() throws Exception {
        etkinlik.setTarih(LocalDateTime.now().plusHours(10));
        etkinlikRepository.save(etkinlik);

        var req = new com.ydg.bilet.dto.bilet.BiletSatinAlRequest();
        req.setEtkinlikId(etkinlik.getId());
        req.setAdet(1);

        String resBody = mockMvc.perform(post("/api/bilet")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long biletId = objectMapper.readTree(resBody).get("biletIdList").get(0).asLong();

        mockMvc.perform(delete("/api/bilet/" + biletId).with(csrf()))
                .andExpect(status().isConflict());
    }

}
