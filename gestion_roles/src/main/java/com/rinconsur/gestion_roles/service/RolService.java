package com.rinconsur.gestion_roles.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.rinconsur.gestion_roles.model.Rol;
import com.rinconsur.gestion_roles.repository.RolRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRep;

    public List<Rol> listarTodos() {
        log.info("[RolService] Consultando todos los roles registrados");
        return rolRep.findAll();
    }

    public Rol buscarPorCodigo(String codigo) {
        log.info("[RolService] Buscando rol con codigo: {}", codigo);
        return rolRep.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado: " + codigo));
    }

    public Rol guardar(Rol rol) {
        log.info("[RolService] Guardando rol: {}", rol.getCodigo());
        if (rol.getCodigo() == null || rol.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código del perfil es obligatorio");
        }
        return rolRep.save(rol);
    }
}
