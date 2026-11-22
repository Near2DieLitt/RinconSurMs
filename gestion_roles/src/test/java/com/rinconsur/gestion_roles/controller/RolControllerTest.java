package com.rinconsur.gestion_roles.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rinconsur.gestion_roles.dto.RolRequest;
import com.rinconsur.gestion_roles.model.Rol;
import com.rinconsur.gestion_roles.service.RolService;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de RolController")
public class RolControllerTest {

    @Mock
    private RolService rolService;

    @InjectMocks
    private RolController rolController;

    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        rolAdmin = new Rol(1L, "P-01", "Administrador", "ADMIN", true, false, false);
    }

    @Test
    @DisplayName("GET y POST /api/roles retornan códigos HTTP esperados")
    void endpointsRolController() {
        when(rolService.listarTodos()).thenReturn(List.of(rolAdmin));
        when(rolService.buscarPorCodigo("P-01")).thenReturn(rolAdmin);
        when(rolService.buscarPorCodigo("P-99")).thenThrow(new IllegalArgumentException("No existe"));
        when(rolService.guardar(any())).thenReturn(rolAdmin);

        assertEquals(200, rolController.listar().getStatusCode().value());
        assertEquals(200, rolController.obtenerPorCodigo("P-01").getStatusCode().value());
        assertEquals(404, rolController.obtenerPorCodigo("P-99").getStatusCode().value());
        assertEquals(200, rolController.crear(new RolRequest()).getStatusCode().value());

        when(rolService.guardar(any())).thenThrow(new IllegalArgumentException("Error"));
        assertEquals(400, rolController.crear(new RolRequest()).getStatusCode().value());
    }
}
