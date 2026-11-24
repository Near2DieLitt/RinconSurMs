package com.rinconsur.gestion_sesiones.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rinconsur.gestion_sesiones.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_sesiones.model.Sesion;
import com.rinconsur.gestion_sesiones.repository.SesionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Unit de SesionService")
public class SesionServiceTest {

    @Mock
    private SesionRepository sesionRep;

    @InjectMocks
    private SesionService sesionService;

    private String hoy;

    @BeforeEach
    void setUp() {
        hoy = LocalDate.now(ZoneId.of("America/Santiago")).toString();
    }

    @Test
    @DisplayName("listar() filtra por rango, fecha, todos, estado activo y usuario")
    void listar_debeAplicarTodosLosFiltros() {
        Sesion activaUser4 = new Sesion(1L, "A1", "Turno Mañana", hoy, true, false, 4L, 1L);
        Sesion inactivaSinUser = new Sesion(2L, "A2", "Turno Tarde", hoy, false, false, null, 2L);
        Sesion activaOtroUser = new Sesion(3L, "A3", "Turno Noche", hoy, true, false, 5L, 3L);

        when(sesionRep.findAll()).thenReturn(List.of(activaUser4, inactivaSinUser, activaOtroUser));
        when(sesionRep.findByFechaOrderByIdAsc(hoy)).thenReturn(List.of(activaUser4, inactivaSinUser, activaOtroUser));
        when(sesionRep.findByFechaBetweenOrderByFechaAscIdAsc(hoy, hoy)).thenReturn(List.of(activaUser4, inactivaSinUser));

        assertEquals(3, sesionService.listar(null, null, null, true, null).size());
        assertEquals(2, sesionService.listar(" ", " ", null, false, null).size());
        assertEquals(2, sesionService.listar(hoy, null, " ", true, 4L).size());
        assertEquals(1, sesionService.listar(null, hoy, hoy, false, 4L).size());
    }

    @Test
    @DisplayName("crear() guarda sesion valida y valida rol, acceso, nombre y fecha")
    void crear_debeGuardarYValidarReglas() {
        when(sesionRep.save(any(Sesion.class))).thenAnswer(i -> i.getArgument(0));

        Sesion valida = new Sesion(10L, " a1 ", " Turno Mañana ", hoy, false, true, 4L, 1L);
        Sesion guardada = sesionService.crear(valida, "SUPERVISOR");
        assertNull(guardada.getId());
        assertEquals("A1", guardada.getCodigo());
        assertEquals("Turno Mañana", guardada.getNombre());
        assertTrue(guardada.isActivo());
        assertFalse(guardada.isCompletada());

        assertNotNull(sesionService.crear(new Sesion(null, "A2", "Turno", hoy, true, false, 4L, null), null));
        assertNotNull(sesionService.crear(new Sesion(null, "A2", "Turno", hoy, true, false, 4L, null), "  "));

        String nombreLargo = "A".repeat(101);
        assertThrows(IllegalArgumentException.class, () -> sesionService.crear(new Sesion(null, "A1", "Ok", hoy, true, false, 4L, null), "ADMIN"));
        assertThrows(IllegalArgumentException.class, () -> sesionService.crear(new Sesion(null, null, "Ok", hoy, true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> sesionService.crear(new Sesion(null, "Z9", "Ok", hoy, true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> sesionService.crear(new Sesion(null, "A1", null, hoy, true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> sesionService.crear(new Sesion(null, "A1", "   ", hoy, true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> sesionService.crear(new Sesion(null, "A1", nombreLargo, hoy, true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> sesionService.crear(new Sesion(null, "A1", "Turno", null, true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> sesionService.crear(new Sesion(null, "A1", "Turno", "  ", true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> sesionService.crear(new Sesion(null, "A1", "Turno", "mal-fecha", true, false, 4L, null), "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> sesionService.crear(new Sesion(null, "A1", "Turno", "2020-01-01", true, false, 4L, null), "SUPERVISOR"));
    }

    @Test
    @DisplayName("cambiarActivo(), cambiarCompletada() y finalizar() actualizan estado y validan existencia")
    void cambiarEstadosYFinalizar() {
        Sesion s = new Sesion(1L, "A1", "Turno Mañana", hoy, true, false, 4L, 10L);
        when(sesionRep.findById(1L)).thenReturn(Optional.of(s));
        when(sesionRep.findById(10L)).thenReturn(Optional.empty());
        when(sesionRep.findFirstByIdLocalOrderByIdDesc(10L)).thenReturn(Optional.of(s));
        when(sesionRep.findById(99L)).thenReturn(Optional.empty());
        when(sesionRep.findFirstByIdLocalOrderByIdDesc(99L)).thenReturn(Optional.empty());
        when(sesionRep.save(any(Sesion.class))).thenAnswer(i -> i.getArgument(0));

        assertFalse(sesionService.cambiarActivo(1L, false).isActivo());
        assertTrue(sesionService.cambiarActivo(10L, true).isActivo());
        assertThrows(RecursoNoEncontradoException.class, () -> sesionService.cambiarActivo(99L, false));

        assertTrue(sesionService.cambiarCompletada(1L, true, "OBSERVADOR").isCompletada());
        assertFalse(sesionService.cambiarCompletada(1L, false, null).isCompletada());
        assertTrue(sesionService.cambiarCompletada(1L, true, "  ").isCompletada());
        assertThrows(IllegalArgumentException.class, () -> sesionService.cambiarCompletada(1L, true, "ADMIN"));
        assertThrows(RecursoNoEncontradoException.class, () -> sesionService.cambiarCompletada(99L, true, "OBSERVADOR"));

        Sesion finalizada = sesionService.finalizar(1L);
        assertFalse(finalizada.isActivo());
        assertTrue(finalizada.isCompletada());
        assertThrows(RecursoNoEncontradoException.class, () -> sesionService.finalizar(99L));
    }
}
