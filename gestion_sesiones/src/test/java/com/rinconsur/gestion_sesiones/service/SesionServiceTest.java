package com.rinconsur.gestion_sesiones.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.rinconsur.gestion_sesiones.controller.SesionController;
import com.rinconsur.gestion_sesiones.dto.SesionRequest;
import com.rinconsur.gestion_sesiones.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_sesiones.model.Sesion;
import com.rinconsur.gestion_sesiones.repository.SesionRepository;

@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

    @Mock
    private SesionRepository repository;
    @InjectMocks
    private SesionService service;
    private SesionController controller;
    private String hoy;

    @BeforeEach
    void setUp() {
        controller = new SesionController(service);
        hoy = LocalDate.now(ZoneId.of("America/Santiago")).toString();
    }

    @Test
    void listarYCrearSesionesConValidaciones() {
        Sesion s = new Sesion(1L, "A1", "Turno Mañana", hoy, true, false, 4L, 1L);
        when(repository.findAll()).thenReturn(List.of(s));
        when(repository.findByFechaOrderByIdAsc(hoy)).thenReturn(List.of(s));
        when(repository.findByFechaBetweenOrderByFechaAscIdAsc(hoy, hoy)).thenReturn(List.of(s));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(1, controller.listar(null, null, null, true, null).getBody().size());
        assertEquals(1, controller.listar(hoy, null, null, true, 4L).getBody().size());
        assertEquals(1, controller.listar(null, hoy, hoy, false, 4L).getBody().size());
        assertEquals(200, crear(s, "SUPERVISOR").getStatusCode().value());

        assertThrows(IllegalArgumentException.class, () -> crear(s, "ADMIN"));
        assertThrows(IllegalArgumentException.class, () -> crear(new Sesion(null, "Z9", "Ok", hoy, true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> crear(new Sesion(null, "A1", " ", hoy, true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> crear(new Sesion(null, "A1", "Turno", "2020-01-01", true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> crear(new Sesion(null, "A1", "Turno", "mal-fecha", true, false, 4L, null), "SUPERVISOR"));
    }

    @Test
    void cambiarActivoCompletadaYFinalizar() {
        Sesion s = new Sesion(1L, "A1", "Turno Mañana", hoy, true, false, 4L, 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(s));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.findFirstByIdLocalOrderByIdDesc(99L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(200, controller.cambiarActivo(1L, false).getStatusCode().value());
        assertThrows(RecursoNoEncontradoException.class, () -> controller.cambiarActivo(99L, false));
        assertEquals(200, controller.cambiarCompletada(1L, true, "OBSERVADOR").getStatusCode().value());
        assertThrows(IllegalArgumentException.class, () -> controller.cambiarCompletada(1L, true, "ADMIN"));
        assertThrows(RecursoNoEncontradoException.class, () -> controller.cambiarCompletada(99L, true, "OBSERVADOR"));
        assertEquals(200, controller.finalizar(1L).getStatusCode().value());
        assertThrows(RecursoNoEncontradoException.class, () -> controller.finalizar(99L));
    }

    private ResponseEntity<?> crear(Sesion s, String rol) {
        SesionRequest r = new SesionRequest();
        r.setCodigo(s.getCodigo());
        r.setNombre(s.getNombre());
        r.setFecha(s.getFecha());
        r.setIdUsuarioAsignado(s.getIdUsuarioAsignado());
        r.setIdLocal(s.getIdLocal());
        return controller.crear(r, rol);
    }
}
