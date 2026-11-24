package com.rinconsur.gestion_analitica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rinconsur.gestion_analitica.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_analitica.model.RegistroFlujo;
import com.rinconsur.gestion_analitica.repository.RegistroFlujoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Unit de AnaliticaService")
public class AnaliticaServiceTest {

    @Mock
    private RegistroFlujoRepository flujoRep;

    @InjectMocks
    private AnaliticaService analiticaService;

    @Test
    @DisplayName("obtenerPorRango() consulta activos, todos y valida fechas")
    void obtenerPorRango_debeRetornarRegistrosYValidarFechas() {
        RegistroFlujo r = new RegistroFlujo(1L, "A1", "2026-11-19", 100, 40, 20, 20, 20, true, 1L);
        when(flujoRep.findByFechaBetweenAndActivoTrueOrderByFechaDesc("2026-11-01", "2026-11-30")).thenReturn(List.of(r));
        when(flujoRep.findByFechaBetweenOrderByFechaDesc("2026-11-01", "2026-11-30")).thenReturn(List.of(r));

        assertEquals(1, analiticaService.obtenerPorRango("2026-11-01", "2026-11-30", false).size());
        assertEquals(1, analiticaService.obtenerPorRango("2026-11-01", "2026-11-30", true).size());

        assertThrows(IllegalArgumentException.class, () -> analiticaService.obtenerPorRango("2026-12-01", "2026-11-01", false));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.obtenerPorRango("mal", "2026-11-01", false));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.obtenerPorRango(null, "2026-11-01", false));
    }

    @Test
    @DisplayName("registrar() guarda flujo valido y valida reglas de negocio")
    void registrar_debeGuardarYValidarReglas() {
        when(flujoRep.save(any(RegistroFlujo.class))).thenAnswer(i -> i.getArgument(0));

        RegistroFlujo valido = new RegistroFlujo(5L, " a1 ", "2026-11-19", 100, 40, 20, 20, 20, false, 1L);
        RegistroFlujo guardado = analiticaService.registrar(valido);
        assertNull(guardado.getId());
        assertEquals("A1", guardado.getAcceso());
        assertTrue(guardado.isActivo());

        RegistroFlujo soloCompradores = new RegistroFlujo(null, "A2", "2026-11-19", 0, 5, 0, 0, 0, true, null);
        assertNotNull(analiticaService.registrar(soloCompradores));

        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, null, "2026-11-19", 10, 1, 1, 1, 1, true, null)));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, "Z9", "2026-11-19", 10, 1, 1, 1, 1, true, null)));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, "A1", "mal", 10, 1, 1, 1, 1, true, null)));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, "A1", "2026-11-19", -1, 0, 0, 0, 0, true, null)));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, "A1", "2026-11-19", 10, -1, 0, 0, 0, true, null)));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, "A1", "2026-11-19", 10, 1, -1, 0, 0, true, null)));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, "A1", "2026-11-19", 10, 1, 0, -1, 0, true, null)));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, "A1", "2026-11-19", 10, 1, 0, 0, -1, true, null)));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, "A1", "2026-11-19", 0, 0, 0, 0, 0, true, null)));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, "A1", "2026-11-19", 10, 20, 0, 0, 0, true, null)));
        assertThrows(IllegalArgumentException.class, () -> analiticaService.registrar(new RegistroFlujo(null, "A1", "2026-11-19", 10, 1, 5, 5, 5, true, null)));
    }

    @Test
    @DisplayName("registrarLote() inserta si esta vacia y retorna 0 si ya tiene registros")
    void registrarLote_debeInsertarSoloSiEstaVacia() {
        RegistroFlujo r = new RegistroFlujo(null, "A1", "2026-11-19", 100, 40, 20, 20, 20, true, 1L);
        when(flujoRep.count()).thenReturn(0L);
        when(flujoRep.save(any(RegistroFlujo.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(1, analiticaService.registrarLote(List.of(r)));

        when(flujoRep.count()).thenReturn(3L);
        assertEquals(0, analiticaService.registrarLote(List.of(r)));
    }

    @Test
    @DisplayName("cambiarActivo() busca por id o idLocal y lanza RecursoNoEncontradoException si no existe")
    void cambiarActivo_debeActualizarOLanzarExcepcion() {
        RegistroFlujo r = new RegistroFlujo(1L, "A1", "2026-11-19", 100, 40, 20, 20, 20, true, 10L);
        when(flujoRep.findById(1L)).thenReturn(Optional.of(r));
        when(flujoRep.findById(10L)).thenReturn(Optional.empty());
        when(flujoRep.findFirstByIdLocalOrderByIdDesc(10L)).thenReturn(Optional.of(r));
        when(flujoRep.findById(99L)).thenReturn(Optional.empty());
        when(flujoRep.findFirstByIdLocalOrderByIdDesc(99L)).thenReturn(Optional.empty());
        when(flujoRep.save(any(RegistroFlujo.class))).thenAnswer(i -> i.getArgument(0));

        assertFalse(analiticaService.cambiarActivo(1L, false).isActivo());
        assertTrue(analiticaService.cambiarActivo(10L, true).isActivo());
        verify(flujoRep, times(1)).findFirstByIdLocalOrderByIdDesc(10L);

        assertThrows(RecursoNoEncontradoException.class, () -> analiticaService.cambiarActivo(99L, false));
    }
}
