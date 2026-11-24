package com.rinconsur.gestion_sesiones.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinconsur.gestion_sesiones.dto.SesionRequest;
import com.rinconsur.gestion_sesiones.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_sesiones.model.Sesion;
import com.rinconsur.gestion_sesiones.service.SesionService;

@WebMvcTest(SesionController.class)
@DisplayName("Tests del SesionController con MockMvc")
public class SesionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SesionService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Sesion sesion;
    private SesionRequest request;

    @BeforeEach
    void setUp() {
        sesion = new Sesion(1L, "A1", "Turno Mañana", "2026-11-25", true, false, 4L, 1L);

        request = new SesionRequest();
        request.setCodigo("A1");
        request.setNombre("Turno Mañana");
        request.setFecha("2026-11-25");
        request.setIdUsuarioAsignado(4L);
        request.setIdLocal(1L);
    }

    @Test
    @DisplayName("GET /api/sesiones debe retornar 200 con la lista de sesiones")
    void listar_debeRetornar200() throws Exception {
        when(service.listar("2026-11-25", null, null, true, 4L)).thenReturn(List.of(sesion));

        mockMvc.perform(get("/api/sesiones")
                .param("fecha", "2026-11-25")
                .param("idUsuario", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("A1"));
    }

    @Test
    @DisplayName("POST /api/sesiones debe retornar 200 al crear y 400 si falla validacion")
    void crear_debeRetornar200y400() throws Exception {
        when(service.crear(any(Sesion.class), eq("SUPERVISOR"))).thenReturn(sesion);

        mockMvc.perform(post("/api/sesiones")
                .param("rolSolicitante", "SUPERVISOR")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Turno Mañana"));

        when(service.crear(any(Sesion.class), eq("ADMIN")))
                .thenThrow(new IllegalArgumentException("Solo el perfil Supervisor puede crear y programar sesiones."));

        mockMvc.perform(post("/api/sesiones")
                .param("rolSolicitante", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/sesiones/{id}/activo debe retornar 200")
    void cambiarActivo_debeRetornar200() throws Exception {
        sesion.setActivo(false);
        when(service.cambiarActivo(1L, false)).thenReturn(sesion);

        mockMvc.perform(patch("/api/sesiones/1/activo")
                .param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @DisplayName("PATCH /api/sesiones/{id}/completada debe retornar 200")
    void cambiarCompletada_debeRetornar200() throws Exception {
        sesion.setCompletada(true);
        when(service.cambiarCompletada(1L, true, "OBSERVADOR")).thenReturn(sesion);

        mockMvc.perform(patch("/api/sesiones/1/completada")
                .param("completada", "true")
                .param("rolSolicitante", "OBSERVADOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completada").value(true));
    }

    @Test
    @DisplayName("PATCH /api/sesiones/{id}/finalizar debe retornar 200 y 404 si no existe")
    void finalizar_debeRetornar200y404() throws Exception {
        sesion.setActivo(false);
        sesion.setCompletada(true);
        when(service.finalizar(1L)).thenReturn(sesion);
        when(service.finalizar(99L)).thenThrow(new RecursoNoEncontradoException("No existe la sesión con ID 99"));

        mockMvc.perform(patch("/api/sesiones/1/finalizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completada").value(true));

        mockMvc.perform(patch("/api/sesiones/99/finalizar"))
                .andExpect(status().isNotFound());
    }
}
