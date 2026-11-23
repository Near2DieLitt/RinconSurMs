package com.rinconsur.gestion_sesiones.service;


import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

import com.rinconsur.gestion_sesiones.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_sesiones.model.Sesion;
import com.rinconsur.gestion_sesiones.repository.SesionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SesionService {

    private static final Set<String> ACCESOS_VALIDOS = Set.of("A1", "A2", "A3", "A4");
    private static final ZoneId ZONA_CHILE = ZoneId.of("America/Santiago");
    private final SesionRepository sesionRep;

    public List<Sesion> listar(String fecha, String desde, String hasta, boolean incluirInactivas, Long idUsuario) {
        List<Sesion> base;
        if (desde != null && !desde.isBlank() && hasta != null && !hasta.isBlank()) {
            base = sesionRep.findByFechaBetweenOrderByFechaAscIdAsc(desde.trim(), hasta.trim());
        } else if (fecha != null && !fecha.isBlank()) {
            base = sesionRep.findByFechaOrderByIdAsc(fecha.trim());
        } else {
            base = sesionRep.findAll();
        }
        return base.stream()
                .filter(s -> incluirInactivas || s.isActivo())
                .filter(s -> idUsuario == null || s.getIdUsuarioAsignado() == null || idUsuario.equals(s.getIdUsuarioAsignado()))
                .toList();
    }

    public Sesion crear(Sesion sesion, String rolSolicitante) {
        if (rolSolicitante != null && !rolSolicitante.isBlank() && !"SUPERVISOR".equalsIgnoreCase(rolSolicitante)) {
            throw new IllegalArgumentException("Solo el perfil Supervisor puede crear y programar sesiones.");
        }
        if (sesion.getCodigo() == null || !ACCESOS_VALIDOS.contains(sesion.getCodigo().trim().toUpperCase())) {
            throw new IllegalArgumentException("El código de acceso debe ser A1, A2, A3 o A4.");
        }
        if (sesion.getNombre() == null || sesion.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la sesión es obligatorio.");
        }
        if (sesion.getNombre().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres.");
        }
        validarFechaNoPasada(sesion.getFecha());

        sesion.setId(null);
        sesion.setCodigo(sesion.getCodigo().trim().toUpperCase());
        sesion.setNombre(sesion.getNombre().trim());
        sesion.setFecha(sesion.getFecha().trim());
        sesion.setActivo(true);
        sesion.setCompletada(false);
        return sesionRep.save(sesion);
    }

    public Sesion cambiarActivo(Long id, boolean activo) {
        Sesion s = buscar(id);
        s.setActivo(activo);
        return sesionRep.save(s);
    }

    public Sesion cambiarCompletada(Long id, boolean completada, String rolSolicitante) {
        if (rolSolicitante != null && !rolSolicitante.isBlank() && !"OBSERVADOR".equalsIgnoreCase(rolSolicitante)) {
            throw new IllegalArgumentException("Solo el perfil Observador puede marcar conteos de sesiones.");
        }
        Sesion s = buscar(id);
        s.setCompletada(completada);
        return sesionRep.save(s);
    }

    public Sesion finalizar(Long id) {
        Sesion s = buscar(id);
        s.setActivo(false);
        s.setCompletada(true);
        return sesionRep.save(s);
    }

    private Sesion buscar(Long id) {
        return sesionRep.findById(id)
                .or(() -> sesionRep.findFirstByIdLocalOrderByIdDesc(id))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la sesión con ID " + id));
    }

    private void validarFechaNoPasada(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            throw new IllegalArgumentException("La fecha es obligatoria (aaaa-mm-dd).");
        }
        LocalDate dia;
        try {
            dia = LocalDate.parse(fecha.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha debe tener formato aaaa-mm-dd.");
        }
        if (dia.isBefore(LocalDate.now(ZONA_CHILE))) {
            throw new IllegalArgumentException("No se pueden crear sesiones en fechas pasadas.");
        }
    }
}
