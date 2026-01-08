package com.ydg.bilet.service;

import com.ydg.bilet.dto.AdminRequestResponse;
import com.ydg.bilet.entity.*;
import com.ydg.bilet.repository.AdminRequestRepository;
import com.ydg.bilet.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Önemli: Veritabanı commit işlemi için

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRequestService {

    private final AdminRequestRepository adminRequestRepository;
    private final KullaniciRepository kullaniciRepository;

    private String currentEmail(Authentication auth) {
        return auth.getName();
    }

    public AdminRequestResponse createRequest(Authentication auth) {
        String email = currentEmail(auth);

        Kullanici user = kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Zaten adminsin");
        }

        if (adminRequestRepository.existsByKullanici_IdAndStatus(user.getId(), AdminRequestStatus.PENDING)) {
            throw new RuntimeException("Zaten bekleyen bir admin isteğin var");
        }

        AdminRequest req = new AdminRequest();
        req.setKullanici(user);
        req.setStatus(AdminRequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());

        AdminRequest saved = adminRequestRepository.save(req);
        return toResponse(saved);
    }

    public List<AdminRequestResponse> listPending() {
        return adminRequestRepository.findByStatusOrderByCreatedAtAsc(AdminRequestStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AdminRequestResponse approve(Long requestId) {
        AdminRequest req = adminRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("İstek bulunamadı"));

        if (req.getStatus() != AdminRequestStatus.PENDING) {
            throw new RuntimeException("Bu istek zaten karara bağlanmış");
        }

        Kullanici user = req.getKullanici();
        user.setRole(Role.ADMIN);
        kullaniciRepository.save(user);

        req.setStatus(AdminRequestStatus.APPROVED);
        req.setDecidedAt(LocalDateTime.now());
        AdminRequest saved = adminRequestRepository.save(req);

        return toResponse(saved);
    }

    @Transactional
    public AdminRequestResponse reject(Long requestId) {
        AdminRequest req = adminRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("İstek bulunamadı"));

        if (req.getStatus() != AdminRequestStatus.PENDING) {
            throw new RuntimeException("Bu istek zaten karara bağlanmış");
        }

        req.setStatus(AdminRequestStatus.REJECTED);
        req.setDecidedAt(LocalDateTime.now());
        AdminRequest saved = adminRequestRepository.save(req);

        return toResponse(saved);
    }

    private AdminRequestResponse toResponse(AdminRequest r) {
        return new AdminRequestResponse(
                r.getId(),
                r.getKullanici().getId(),
                r.getKullanici().getEmail(),
                r.getStatus().name(),
                r.getCreatedAt(),
                r.getDecidedAt()
        );
    }
}