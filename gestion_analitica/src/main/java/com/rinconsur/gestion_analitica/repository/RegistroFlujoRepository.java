package com.rinconsur.gestion_analitica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rinconsur.gestion_analitica.model.RegistroFlujo;

@Repository
public interface RegistroFlujoRepository extends JpaRepository<RegistroFlujo, Long> {
    List<RegistroFlujo> findByFechaBetweenAndActivoTrueOrderByFechaDesc(String desde, String hasta);
    List<RegistroFlujo> findByFechaBetweenOrderByFechaDesc(String desde, String hasta);
    Optional<RegistroFlujo> findFirstByIdLocalOrderByIdDesc(Long idLocal);
}
