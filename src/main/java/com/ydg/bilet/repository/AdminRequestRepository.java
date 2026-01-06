package com.ydg.bilet.repository;

import com.ydg.bilet.entity.AdminRequest;
import com.ydg.bilet.entity.AdminRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRequestRepository extends JpaRepository<AdminRequest, Long> {

    Optional<AdminRequest> findTopByKullanici_IdOrderByCreatedAtDesc(Long kullaniciId);

    boolean existsByKullanici_IdAndStatus(Long kullaniciId, AdminRequestStatus status);

    List<AdminRequest> findByStatusOrderByCreatedAtAsc(AdminRequestStatus status);
}
