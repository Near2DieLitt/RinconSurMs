package com.rinconsur.gestion_visitantes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.rinconsur.gestion_visitantes.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_visitantes.model.Visitante;
import com.rinconsur.gestion_visitantes.repository.VisitanteRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Unit de VisitanteService")
public class VisitanteServiceTest {

    @Mock
    private VisitanteRepository visitanteRep;

    @InjectMocks
    private VisitanteService visitanteService;

    @Test
    @DisplayName("listarEnTienda() y obtenerResumen() filtran por sesion y calculan totales")
    void listarYResumen_debenCalcularTotales() {
        Visitante v1 = new Visitante(10L, "VIS-001", "2026-11-19", "10:15:00", "A2", "JOVEN", "En_Tienda", null, 1L, 10L);
        Visitante v2 = new Visitante(11L, "VIS-002", "2026-11-19", "10:20:00", "A2", "ADULTO", "Compro", "12345678", 1L, 11L);
        Visitante v3 = new Visitante(12L, "VIS-003", "2026-11-19", "10:25:00", "A2", "ADULTO", "No_Compro", null, 2L, 12L);

        when(visitanteRep.findByFechaAndEstadoOrderByIdAsc("2026-11-19", "En_Tienda")).thenReturn(List.of(v1));
        when(visitanteRep.findByFechaOrderByIdAsc("2026-11-19")).thenReturn(List.of(v1, v2, v3));

        assertEquals(1, visitanteService.listarEnTienda("2026-11-19", null).size());
        assertEquals(1, visitanteService.listarEnTienda("2026-11-19", 1L).size());
        assertEquals(0, visitanteService.listarEnTienda("2026-11-19", 2L).size());

        Map<String, Integer> resumenTodos = visitanteService.obtenerResumen("2026-11-19", null);
        assertEquals(3, resumenTodos.get("total"));
        assertEquals(1, resumenTodos.get("enTienda"));
        assertEquals(1, resumenTodos.get("compro"));
        assertEquals(1, resumenTodos.get("noCompro"));

        Map<String, Integer> resumenSesion1 = visitanteService.obtenerResumen("2026-11-19", 1L);
        assertEquals(2, resumenSesion1.get("total"));
    }

    @Test
    @DisplayName("registrarEntrada() genera codigo correlativo y valida todos los campos")
    void registrarEntrada_debeGuardarYValidarCampos() {
        when(visitanteRep.countByFecha("2026-11-19")).thenReturn(0L);
        when(visitanteRep.save(any(Visitante.class))).thenAnswer(i -> i.getArgument(0));

        Visitante nuevo = new Visitante(5L, null, "2026-11-19", "10:15:00", " a2 ", " joven ", "Otro", "999", 1L, 10L);
        Visitante guardado = visitanteService.registrarEntrada(nuevo);
        assertNull(guardado.getId());
        assertEquals("A2", guardado.getAcceso());
        assertEquals("JOVEN", guardado.getRangoEtario());
        assertEquals("VIS-001", guardado.getCodigo());
        assertEquals("En_Tienda", guardado.getEstado());
        assertNull(guardado.getFolioBoleta());

        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrarEntrada(new Visitante(null, null, "2026-11-19", "10:00", null, "JOVEN", "En_Tienda", null, 1L, null)));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrarEntrada(new Visitante(null, null, "2026-11-19", "10:00", "Z9", "JOVEN", "En_Tienda", null, 1L, null)));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrarEntrada(new Visitante(null, null, "2026-11-19", "10:00", "A1", null, "En_Tienda", null, 1L, null)));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrarEntrada(new Visitante(null, null, "2026-11-19", "10:00", "A1", "MALO", "En_Tienda", null, 1L, null)));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrarEntrada(new Visitante(null, null, null, "10:00", "A1", "JOVEN", "En_Tienda", null, 1L, null)));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrarEntrada(new Visitante(null, null, "", "10:00", "A1", "JOVEN", "En_Tienda", null, 1L, null)));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrarEntrada(new Visitante(null, null, "2026-11-19", null, "A1", "JOVEN", "En_Tienda", null, 1L, null)));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrarEntrada(new Visitante(null, null, "2026-11-19", " ", "A1", "JOVEN", "En_Tienda", null, 1L, null)));
    }

    @Test
    @DisplayName("salida sin compra, vincular boleta y cierre diario operan correctamente")
    void salidaBoletaYCierreDiario() {
        Visitante v = new Visitante(10L, "VIS-001", "2026-11-19", "10:15:00", "A2", "JOVEN", "En_Tienda", null, 1L, 50L);
        when(visitanteRep.findById(10L)).thenReturn(Optional.of(v));
        when(visitanteRep.findById(50L)).thenReturn(Optional.empty());
        when(visitanteRep.findFirstByIdLocalOrderByIdDesc(50L)).thenReturn(Optional.of(v));
        when(visitanteRep.findById(99L)).thenReturn(Optional.empty());
        when(visitanteRep.findFirstByIdLocalOrderByIdDesc(99L)).thenReturn(Optional.empty());
        when(visitanteRep.existsByFolioBoletaIgnoreCase("12345678")).thenReturn(false);
        when(visitanteRep.existsByFolioBoletaIgnoreCase("11111111")).thenReturn(true);
        when(visitanteRep.existsByFolioBoletaIgnoreCase("87654321")).thenReturn(false);
        when(visitanteRep.findByFechaAndEstadoOrderByIdAsc("2026-11-19", "En_Tienda")).thenReturn(List.of(v));
        when(visitanteRep.save(any(Visitante.class))).thenAnswer(i -> i.getArgument(0));

        Visitante compro = visitanteService.vincularBoletaAVisitanteEspecifico(10L, "12345678");
        assertEquals("Compro", compro.getEstado());
        assertEquals("12345678", compro.getFolioBoleta());

        assertThrows(IllegalArgumentException.class, () -> visitanteService.vincularBoletaAVisitanteEspecifico(10L, "87654321"));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.registrarSalidaEspecificaSinCompra(10L));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.vincularBoletaAVisitanteEspecifico(10L, null));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.vincularBoletaAVisitanteEspecifico(10L, "abc"));
        assertThrows(IllegalArgumentException.class, () -> visitanteService.vincularBoletaAVisitanteEspecifico(10L, "11111111"));
        assertThrows(RecursoNoEncontradoException.class, () -> visitanteService.vincularBoletaAVisitanteEspecifico(99L, "12345678"));

        v.setEstado("En_Tienda");
        Visitante salida = visitanteService.registrarSalidaEspecificaSinCompra(50L);
        assertEquals("No_Compro", salida.getEstado());
        assertThrows(RecursoNoEncontradoException.class, () -> visitanteService.registrarSalidaEspecificaSinCompra(99L));

        v.setEstado("En_Tienda");
        assertEquals(1, visitanteService.ejecutarCierreDiario("2026-11-19", 1L));
        assertEquals("No_Compro", v.getEstado());
    }

    @Test
    @DisplayName("vincularBoletaAVisitanteEspecifico() captura DataIntegrityViolationException")
    void vincularBoleta_duplicadaEnBD_debeLanzarIllegalArgument() {
        Visitante v = new Visitante(10L, "VIS-001", "2026-11-19", "10:15:00", "A2", "JOVEN", "En_Tienda", null, 1L, 10L);
        when(visitanteRep.existsByFolioBoletaIgnoreCase("99999999")).thenReturn(false);
        when(visitanteRep.findById(10L)).thenReturn(Optional.of(v));
        when(visitanteRep.save(any(Visitante.class))).thenThrow(new DataIntegrityViolationException("duplicada"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> visitanteService.vincularBoletaAVisitanteEspecifico(10L, "99999999")
        );
        assertTrue(ex.getMessage().contains("YA_PROCESADA"));
    }
}
