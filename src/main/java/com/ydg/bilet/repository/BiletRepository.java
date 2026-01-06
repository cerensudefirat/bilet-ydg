package com.ydg.bilet.repository;

import com.ydg.bilet.entity.Bilet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BiletRepository extends JpaRepository<Bilet, Long> {

    List<Bilet> findByKullaniciIdOrderByCreatedAtDesc(Long kullaniciId);

    long countByEtkinlikId(Long etkinlikId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bilet b join fetch b.etkinlik e join fetch b.kullanici k where b.id = :id")
    Optional<Bilet> findByIdForUpdate(@Param("id") Long id);
}
