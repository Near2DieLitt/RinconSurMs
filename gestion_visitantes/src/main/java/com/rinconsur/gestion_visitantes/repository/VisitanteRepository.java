package com.rinconsur.gestion_visitantes.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rinconsur.gestion_visitantes.model.Visitante;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, Long> {
    long countByFecha(String fecha);
    Optional<Visitante> findFirstByIdLocalOrderByIdDesc(Long idLocal);
    List<Visitante> findByFechaOrderByIdAsc(String fecha);
    List<Visitante> findByFechaAndEstadoOrderByIdAsc(String fecha, String estado);
    boolean existsByFolioBoletaIgnoreCase(String folioBoleta);
}
