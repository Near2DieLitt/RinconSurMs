package com.rinconsur.gestion_roles.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
@DisplayName("Test Unitario de RolService")
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
    @DisplayName("listarTodos() y buscarPorCodigo() funcionan correctamente")
    void listarYBuscarPorCodigo() {
        when(rolRep.findAll()).thenReturn(List.of(rolAdmin));
        when(rolRep.findByCodigoIgnoreCase("P-01")).thenReturn(Optional.of(rolAdmin));
        when(rolRep.findByCodigoIgnoreCase("P-99")).thenReturn(Optional.empty());

        assertEquals(1, rolService.listarTodos().size());
        assertEquals("Administrador", rolService.buscarPorCodigo("P-01").getNombre());
        assertThrows(IllegalArgumentException.class, () -> rolService.buscarPorCodigo("P-99"));
    }

    @Test
    @DisplayName("guardar() valida codigo obligatorio y guarda el rol")
    void guardarRol() {
        when(rolRep.save(any())).thenReturn(rolAdmin);
        assertNotNull(rolService.guardar(rolAdmin));

        Rol invalido = new Rol();
        invalido.setCodigo(" ");
        assertThrows(IllegalArgumentException.class, () -> rolService.guardar(invalido));
    }
}
