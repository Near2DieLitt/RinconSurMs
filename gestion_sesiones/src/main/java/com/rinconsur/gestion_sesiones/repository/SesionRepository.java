package com.rinconsur.gestion_sesiones.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rinconsur.gestion_sesiones.model.Sesion;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findByFechaOrderByIdAsc(String fecha);
    List<Sesion> findByFechaBetweenOrderByFechaAscIdAsc(String desde, String hasta);
    Optional<Sesion> findFirstByIdLocalOrderByIdDesc(Long idLocal);
}

