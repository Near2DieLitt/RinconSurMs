package com.rinconsur.gestion_visitantes.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.rinconsur.gestion_visitantes.controller.VisitanteController;
import com.rinconsur.gestion_visitantes.dto.BoletaRequest;
import com.rinconsur.gestion_visitantes.dto.VisitanteRequest;
import com.rinconsur.gestion_visitantes.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_visitantes.model.Visitante;
import com.rinconsur.gestion_visitantes.repository.VisitanteRepository;

@ExtendWith(MockitoExtension.class)
class VisitanteServiceTest {

    @Mock
    private VisitanteRepository repository;
    @InjectMocks
    private VisitanteService service;
    private VisitanteController controller;

    @BeforeEach
    void setUp() {
        controller = new VisitanteController(service);
    }

    @Test
    void listarResumenYRegistrarEntrada() {
        Visitante v1 = new Visitante(10L, "VIS-001", "2026-11-19", "10:15:00", "A2", "JOVEN", "En_Tienda", null, 1L, 10L);
        Visitante v2 = new Visitante(11L, "VIS-002", "2026-11-19", "10:20:00", "A2", "ADULTO", "Compro", "12345678", 1L, 11L);
        Visitante v3 = new Visitante(12L, "VIS-003", "2026-11-19", "10:25:00", "A2", "ADULTO", "No_Compro", null, 1L, 12L);

        when(repository.findByFechaAndEstadoOrderByIdAsc("2026-11-19", "En_Tienda")).thenReturn(List.of(v1));
        when(repository.findByFechaOrderByIdAsc("2026-11-19")).thenReturn(List.of(v1, v2, v3));
        when(repository.countByFecha("2026-11-19")).thenReturn(0L);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(1, controller.listarEnTienda("2026-11-19", 1L).getBody().size());
        Map<String, Integer> res = controller.resumen("2026-11-19", 1L).getBody();
        assertEquals(3, res.get("total"));
        assertEquals(1, res.get("enTienda"));
        assertEquals(1, res.get("compro"));
        assertEquals(1, res.get("noCompro"));

        assertEquals(200, registrarEntrada(v1).getStatusCode().value());
        assertThrows(IllegalArgumentException.class, () -> registrarEntrada(new Visitante(null, null, "2026-11-19", "10:00", "Z9", "JOVEN", "En_Tienda", null, 1L, null)));
        assertThrows(IllegalArgumentException.class, () -> registrarEntrada(new Visitante(null, null, "2026-11-19", "10:00", "A1", "MALO", "En_Tienda", null, 1L, null)));
        assertThrows(IllegalArgumentException.class, () -> registrarEntrada(new Visitante(null, null, "", "10:00", "A1", "JOVEN", "En_Tienda", null, 1L, null)));
        assertThrows(IllegalArgumentException.class, () -> registrarEntrada(new Visitante(null, null, "2026-11-19", "", "A1", "JOVEN", "En_Tienda", null, 1L, null)));
    }

    @Test
    void salidaBoletaYCierreDiario() {
        Visitante v = new Visitante(10L, "VIS-001", "2026-11-19", "10:15:00", "A2", "JOVEN", "En_Tienda", null, 1L, 10L);
        when(repository.findById(10L)).thenReturn(Optional.of(v));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.findFirstByIdLocalOrderByIdDesc(99L)).thenReturn(Optional.empty());
        when(repository.existsByFolioBoletaIgnoreCase("12345678")).thenReturn(false);
        when(repository.existsByFolioBoletaIgnoreCase("11111111")).thenReturn(true);
        when(repository.findByFechaAndEstadoOrderByIdAsc("2026-11-19", "En_Tienda")).thenReturn(List.of(v));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(200, vincularBoleta(10L, Map.of("folio", "12345678")).getStatusCode().value());
        assertThrows(IllegalArgumentException.class, () -> vincularBoleta(10L, Map.of("folio", "abc")));
        assertThrows(IllegalArgumentException.class, () -> vincularBoleta(10L, Map.of("folio", "11111111")));
        assertThrows(RecursoNoEncontradoException.class, () -> vincularBoleta(99L, Map.of("folio", "12345678")));
        assertThrows(IllegalArgumentException.class, () -> controller.registrarSalidaEspecifica(10L));

        v.setEstado("En_Tienda");
        assertEquals(200, controller.registrarSalidaEspecifica(10L).getStatusCode().value());
        assertThrows(RecursoNoEncontradoException.class, () -> controller.registrarSalidaEspecifica(99L));

        v.setEstado("En_Tienda");
        assertEquals(1, controller.cierreDiario("2026-11-19", 1L).getBody().get("actualizados"));
    }

    private ResponseEntity<?> registrarEntrada(Visitante v) {
        VisitanteRequest r = new VisitanteRequest();
        r.setCodigo(v.getCodigo());
        r.setFecha(v.getFecha());
        r.setHora(v.getHora());
        r.setAcceso(v.getAcceso());
        r.setRangoEtario(v.getRangoEtario());
        r.setEstado(v.getEstado());
        r.setFolioBoleta(v.getFolioBoleta());
        r.setIdSesion(v.getIdSesion());
        r.setIdLocal(v.getIdLocal());
        return controller.registrarEntrada(r);
    }

    private ResponseEntity<?> vincularBoleta(Long id, Map<String, String> datos) {
        BoletaRequest b = new BoletaRequest();
        b.setFolio(datos.get("folio"));
        return controller.vincularBoleta(id, b);
    }
}
