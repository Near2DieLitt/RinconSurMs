package com.rinconsur.gestion_analitica.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import com.rinconsur.gestion_analitica.controller.AnaliticaController;
import com.rinconsur.gestion_analitica.dto.RegistroFlujoRequest;
import com.rinconsur.gestion_analitica.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_analitica.model.RegistroFlujo;
import com.rinconsur.gestion_analitica.repository.RegistroFlujoRepository;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AnaliticaServiceTest {

    @Mock
    private RegistroFlujoRepository repository;
    @InjectMocks
    private AnaliticaService service;
    private AnaliticaController controller;

    @BeforeEach
    void setUp() {
        controller = new AnaliticaController(service);
    }

    @Test
    void listarRegistrarLoteYValidaciones() {
        RegistroFlujo r = new RegistroFlujo(1L, "A1", "2026-11-19", 100, 40, 20, 20, 20, true, 1L);
        when(repository.findByFechaBetweenAndActivoTrueOrderByFechaDesc("2026-11-01", "2026-11-30")).thenReturn(List.of(r));
        when(repository.findByFechaBetweenOrderByFechaDesc("2026-11-01", "2026-11-30")).thenReturn(List.of(r));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(repository.count()).thenReturn(0L);

        assertEquals(200, controller.listar("2026-11-01", "2026-11-30", false).getStatusCode().value());
        assertEquals(200, controller.listar("2026-11-01", "2026-11-30", true).getStatusCode().value());
        assertThrows(IllegalArgumentException.class, () -> controller.listar("2026-12-01", "2026-11-01", false));
        assertThrows(IllegalArgumentException.class, () -> controller.listar("mal", "2026-11-01", false));

        assertEquals(200, registrar(r).getStatusCode().value());
        assertEquals(1, registrarLote(List.of(r)).getBody().get("insertados"));

        when(repository.count()).thenReturn(5L);
        assertEquals(0, registrarLote(List.of(r)).getBody().get("insertados"));

        assertThrows(IllegalArgumentException.class, () -> registrar(new RegistroFlujo(null, "Z9", "2026-11-19", 10, 1, 1, 1, 1, true, null)));
        assertThrows(IllegalArgumentException.class, () -> registrar(new RegistroFlujo(null, "A1", "2026-11-19", 0, 0, 0, 0, 0, true, null)));
        assertThrows(IllegalArgumentException.class, () -> registrar(new RegistroFlujo(null, "A1", "2026-11-19", -1, 0, 0, 0, 0, true, null)));
        assertThrows(IllegalArgumentException.class, () -> registrar(new RegistroFlujo(null, "A1", "2026-11-19", 10, 20, 0, 0, 0, true, null)));
        assertThrows(IllegalArgumentException.class, () -> registrar(new RegistroFlujo(null, "A1", "2026-11-19", 10, 1, 5, 5, 5, true, null)));
    }

    @Test
    void cambiarActivoExistenteYNoExistente() {
        RegistroFlujo r = new RegistroFlujo(1L, "A1", "2026-11-19", 100, 40, 20, 20, 20, true, 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(r));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.findFirstByIdLocalOrderByIdDesc(99L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(200, controller.cambiarActivo(1L, false).getStatusCode().value());
        assertThrows(RecursoNoEncontradoException.class, () -> controller.cambiarActivo(99L, false));
    }

    private RegistroFlujoRequest req(RegistroFlujo f) {
        RegistroFlujoRequest r = new RegistroFlujoRequest();
        r.setAcceso(f.getAcceso());
        r.setFecha(f.getFecha());
        r.setIngresos(f.getIngresos());
        r.setCompradores(f.getCompradores());
        r.setTransitoJoven(f.getTransitoJoven());
        r.setTransitoAdulto(f.getTransitoAdulto());
        r.setTransitoMayor(f.getTransitoMayor());
        r.setActivo(f.isActivo());
        r.setIdLocal(f.getIdLocal());
        return r;
    }

    private ResponseEntity<?> registrar(RegistroFlujo f) {
        return controller.registrar(req(f));
    }

    private ResponseEntity<Map<String, Integer>> registrarLote(List<RegistroFlujo> lista) {
        return controller.registrarLote(lista.stream().map(this::req).toList());
    }
}
