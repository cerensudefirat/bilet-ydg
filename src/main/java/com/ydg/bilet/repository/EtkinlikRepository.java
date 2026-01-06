package com.ydg.bilet.repository;

import com.ydg.bilet.entity.Etkinlik;
import com.ydg.bilet.entity.EtkinlikDurum;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtkinlikRepository extends JpaRepository<Etkinlik, Long> {

    List<Etkinlik> findBySehirIgnoreCaseAndTurIgnoreCase(String sehir, String tur);

    // SATIN ALMA sırasında aynı etkinliği eş zamanlı güncellemeyi engeller
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Etkinlik e where e.id = :id")
    Optional<Etkinlik> findByIdForUpdate(@Param("id") Long id);

    List<Etkinlik> findByDurumOrderByTarihAsc(EtkinlikDurum durum);

}
