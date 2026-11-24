package com.rinconsur.gestion_roles.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rinconsur.gestion_roles.model.Rol;
import com.rinconsur.gestion_roles.repository.RolRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Unit de RolService")
public class RolServiceTest {

    @Mock
    private RolRepository rolRep;

    @InjectMocks
    private RolService rolService;

    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        rolAdmin = new Rol(1L, "P-01", "Administrador", "ADMIN", true, false, false);
    }

    @Test
    @DisplayName("listarTodos() debe retornar la lista de roles registrados")
    void listarTodos_debeRetornarLista() {
        when(rolRep.findAll()).thenReturn(List.of(rolAdmin));

        List<Rol> resultado = rolService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("P-01", resultado.get(0).getCodigo());
        verify(rolRep, times(1)).findAll();
    }

    @Test
    @DisplayName("buscarPorCodigo() debe retornar el rol cuando existe")
    void buscarPorCodigo_debeRetornarRol_cuandoExiste() {
        when(rolRep.findByCodigoIgnoreCase("P-01")).thenReturn(Optional.of(rolAdmin));

        Rol resultado = rolService.buscarPorCodigo("P-01");

        assertNotNull(resultado);
        assertEquals("Administrador", resultado.getNombre());
    }

    @Test
    @DisplayName("buscarPorCodigo() debe lanzar IllegalArgumentException cuando no existe")
    void buscarPorCodigo_debeLanzarExcepcion_cuandoNoExiste() {
        when(rolRep.findByCodigoIgnoreCase("P-99")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> rolService.buscarPorCodigo("P-99"));
    }

    @Test
    @DisplayName("guardar() debe guardar el rol cuando el codigo es valido")
    void guardar_debeGuardarRol_cuandoEsValido() {
        when(rolRep.save(any(Rol.class))).thenReturn(rolAdmin);

        Rol resultado = rolService.guardar(rolAdmin);

        assertNotNull(resultado);
        assertEquals("P-01", resultado.getCodigo());
        verify(rolRep, times(1)).save(rolAdmin);
    }

    @Test
    @DisplayName("guardar() debe lanzar IllegalArgumentException cuando el codigo es nulo o vacio")
    void guardar_debeLanzarExcepcion_cuandoCodigoInvalido() {
        Rol codigoNulo = new Rol();
        codigoNulo.setCodigo(null);
        assertThrows(IllegalArgumentException.class, () -> rolService.guardar(codigoNulo));

        Rol codigoVacio = new Rol();
        codigoVacio.setCodigo("   ");
        assertThrows(IllegalArgumentException.class, () -> rolService.guardar(codigoVacio));
    }
}
