package com.rinconsur.gestion_usuarios.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rinconsur.gestion_usuarios.model.Usuario;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByRutIgnoreCase(String rut);
    long countByRolIgnoreCaseAndActivoTrue(String rol);
}
