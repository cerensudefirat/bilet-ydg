package com.ydg.bilet.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminRequestsSecurityIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void pending_anonim_401Donmeli() throws Exception {
        mockMvc.perform(get("/api/admin-requests/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pending_user_403Donmeli() throws Exception {
        mockMvc.perform(
                        get("/api/admin-requests/pending")
                                .with(user("user1@test.com").roles("USER"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void pending_admin_200Donmeli() throws Exception {
        mockMvc.perform(
                        get("/api/admin-requests/pending")
                                .with(user("admin@test.com").roles("ADMIN"))
                )
                .andExpect(status().isOk());
    }
}
