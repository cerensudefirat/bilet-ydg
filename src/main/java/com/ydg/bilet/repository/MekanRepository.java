package com.ydg.bilet.repository;

import com.ydg.bilet.entity.Mekan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MekanRepository extends JpaRepository<Mekan, Long> {}
