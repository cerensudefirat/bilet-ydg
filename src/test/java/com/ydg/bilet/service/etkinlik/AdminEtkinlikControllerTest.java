package com.ydg.bilet.service.etkinlik;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydg.bilet.config.SecurityConfig;
import com.ydg.bilet.controller.AdminEtkinlikController;
import com.ydg.bilet.dto.etkinlik.EtkinlikResponse;
import com.ydg.bilet.dto.etkinlik.EtkinlikUpdateRequest;
import com.ydg.bilet.exception.NotFoundException;
import com.ydg.bilet.service.EtkinlikService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminEtkinlikController.class)
@Import(SecurityConfig.class) // senin security config’in
class AdminEtkinlikControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EtkinlikService etkinlikService;


    @Test
    @WithMockUser(roles = "ADMIN")
    void guncelle_200_ok() throws Exception {
        EtkinlikResponse resp = new EtkinlikResponse(1L, "Yeni Baslik", null, null,null, null, null, null, null, null, null, null);

        when(etkinlikService.guncelle(eq(1L), any()))
                .thenReturn(resp);

        mockMvc.perform(put("/api/admin/etkinlik/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
              "baslik": "Yeni Baslik",
              "tur": "Konser",
              "sehir": "Malatya",
              "tarih": "2026-02-01T20:00:00",
              "temelFiyat": 100.00,
              "kapasite": 100,
              "mekanId": 10
            }
        """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void guncelle_404_notFound() throws Exception {
        when(etkinlikService.guncelle(eq(999L), any()))
                .thenThrow(new NotFoundException("Etkinlik bulunamadı"));

        mockMvc.perform(put("/api/admin/etkinlik/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void guncelle_409_cancelledIseConflict() throws Exception {
        when(etkinlikService.guncelle(eq(6L), any()))
                .thenThrow(new IllegalStateException("İptal edilmiş etkinlik"));

        mockMvc.perform(put("/api/admin/etkinlik/6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "USER")
    void guncelle_adminDegilse_403() throws Exception {
        mockMvc.perform(put("/api/admin/etkinlik/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
