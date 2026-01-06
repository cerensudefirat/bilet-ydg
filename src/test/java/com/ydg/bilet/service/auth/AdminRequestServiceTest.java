package com.ydg.bilet.service.auth;

import com.ydg.bilet.dto.AdminRequestResponse;
import com.ydg.bilet.entity.*;
import com.ydg.bilet.repository.AdminRequestRepository;
import com.ydg.bilet.repository.KullaniciRepository;
import com.ydg.bilet.service.AdminRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRequestServiceTest {

    @Mock
    private AdminRequestRepository adminRequestRepository;

    @Mock
    private KullaniciRepository kullaniciRepository;

    @InjectMocks
    private AdminRequestService adminRequestService;

    @Mock
    private Authentication auth;

    @Test
    void createRequest_kullaniciBulunamazsa_hataFirlatir() {
        when(auth.getName()).thenReturn("nouser@test.com");
        when(kullaniciRepository.findByEmail("nouser@test.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminRequestService.createRequest(auth));
        assertTrue(ex.getMessage().toLowerCase().contains("kullanıcı"));

        verify(adminRequestRepository, never()).save(any());
    }

    @Test
    void createRequest_zatenAdminse_hataFirlatir() {
        when(auth.getName()).thenReturn("admin@test.com");

        Kullanici k = new Kullanici();
        k.setId(1L);
        k.setEmail("admin@test.com");
        k.setRole(Role.ADMIN);

        when(kullaniciRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(k));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminRequestService.createRequest(auth));
        assertTrue(ex.getMessage().toLowerCase().contains("zaten"));

        verify(adminRequestRepository, never()).save(any());
    }

    @Test
    void createRequest_pendingIstekVarken_hataFirlatir() {
        when(auth.getName()).thenReturn("user@test.com");

        Kullanici k = new Kullanici();
        k.setId(5L);
        k.setEmail("user@test.com");
        k.setRole(Role.USER);

        when(kullaniciRepository.findByEmail("user@test.com")).thenReturn(Optional.of(k));
        when(adminRequestRepository.existsByKullanici_IdAndStatus(5L, AdminRequestStatus.PENDING)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminRequestService.createRequest(auth));
        assertTrue(ex.getMessage().toLowerCase().contains("bekleyen"));

        verify(adminRequestRepository, never()).save(any());
    }

    @Test
    void createRequest_basarili_olunca_pendingKaydeder_veResponseDoner() {
        when(auth.getName()).thenReturn("user@test.com");

        Kullanici k = new Kullanici();
        k.setId(5L);
        k.setEmail("user@test.com");
        k.setRole(Role.USER);

        when(kullaniciRepository.findByEmail("user@test.com")).thenReturn(Optional.of(k));
        when(adminRequestRepository.existsByKullanici_IdAndStatus(5L, AdminRequestStatus.PENDING)).thenReturn(false);

        // repo save dönüşü (id setlenmiş gibi)
        AdminRequest saved = new AdminRequest();
        saved.setId(10L);
        saved.setKullanici(k);
        saved.setStatus(AdminRequestStatus.PENDING);
        saved.setCreatedAt(java.time.LocalDateTime.now());

        when(adminRequestRepository.save(any(AdminRequest.class))).thenReturn(saved);

        AdminRequestResponse resp = adminRequestService.createRequest(auth);

        assertEquals(10L, resp.getId());
        assertEquals(5L, resp.getKullaniciId());
        assertEquals("user@test.com", resp.getEmail());
        assertEquals("PENDING", resp.getStatus());
        assertNotNull(resp.getCreatedAt());

        // save çağrısında status pending mi?
        ArgumentCaptor<AdminRequest> captor = ArgumentCaptor.forClass(AdminRequest.class);
        verify(adminRequestRepository).save(captor.capture());
        assertEquals(AdminRequestStatus.PENDING, captor.getValue().getStatus());
        assertEquals(k, captor.getValue().getKullanici());
    }

    @Test
    void listPending_repoSonuclariniResponseaCevirir() {
        Kullanici u1 = new Kullanici();
        u1.setId(1L);
        u1.setEmail("u1@test.com");

        AdminRequest r1 = new AdminRequest();
        r1.setId(100L);
        r1.setKullanici(u1);
        r1.setStatus(AdminRequestStatus.PENDING);
        r1.setCreatedAt(java.time.LocalDateTime.now());

        when(adminRequestRepository.findByStatusOrderByCreatedAtAsc(AdminRequestStatus.PENDING))
                .thenReturn(List.of(r1));

        List<AdminRequestResponse> out = adminRequestService.listPending();

        assertEquals(1, out.size());
        assertEquals(100L, out.get(0).getId());
        assertEquals("u1@test.com", out.get(0).getEmail());
        assertEquals("PENDING", out.get(0).getStatus());
    }

    @Test
    void approve_istekBulunamazsa_hataFirlatir() {
        when(adminRequestRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminRequestService.approve(99L));
        assertTrue(ex.getMessage().toLowerCase().contains("istek"));

        verify(kullaniciRepository, never()).save(any());
    }

    @Test
    void approve_istekPendingDegilse_hataFirlatir() {
        Kullanici u = new Kullanici();
        u.setId(1L);
        u.setEmail("u@test.com");
        u.setRole(Role.USER);

        AdminRequest req = new AdminRequest();
        req.setId(5L);
        req.setKullanici(u);
        req.setStatus(AdminRequestStatus.REJECTED);

        when(adminRequestRepository.findById(5L)).thenReturn(Optional.of(req));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminRequestService.approve(5L));
        assertTrue(ex.getMessage().toLowerCase().contains("karara"));

        verify(kullaniciRepository, never()).save(any());
    }

    @Test
    void approve_pendingIstek_olunca_userRoleAdminYapar_veRequestiApprovedEder() {
        Kullanici u = new Kullanici();
        u.setId(1L);
        u.setEmail("u@test.com");
        u.setRole(Role.USER);

        AdminRequest req = new AdminRequest();
        req.setId(5L);
        req.setKullanici(u);
        req.setStatus(AdminRequestStatus.PENDING);
        req.setCreatedAt(java.time.LocalDateTime.now());

        when(adminRequestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(adminRequestRepository.save(any(AdminRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminRequestResponse resp = adminRequestService.approve(5L);

        // user admin oldu mu?
        assertEquals(Role.ADMIN, u.getRole());
        verify(kullaniciRepository).save(u);

        // request approved oldu mu?
        assertEquals("APPROVED", resp.getStatus());
        assertNotNull(resp.getDecidedAt());

        // request save çağrıldı mı?
        verify(adminRequestRepository).save(req);
    }

    @Test
    void reject_pendingIstek_olunca_requestiRejectedEder() {
        Kullanici u = new Kullanici();
        u.setId(1L);
        u.setEmail("u@test.com");
        u.setRole(Role.USER);

        AdminRequest req = new AdminRequest();
        req.setId(7L);
        req.setKullanici(u);
        req.setStatus(AdminRequestStatus.PENDING);
        req.setCreatedAt(java.time.LocalDateTime.now());

        when(adminRequestRepository.findById(7L)).thenReturn(Optional.of(req));
        when(adminRequestRepository.save(any(AdminRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminRequestResponse resp = adminRequestService.reject(7L);

        assertEquals("REJECTED", resp.getStatus());
        assertNotNull(resp.getDecidedAt());
        verify(adminRequestRepository).save(req);

        // reject user role değiştirmez
        verify(kullaniciRepository, never()).save(any());
    }
}
