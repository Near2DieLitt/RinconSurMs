package com.rinconsur.gestion_analitica.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import com.rinconsur.gestion_analitica.dto.RegistroFlujoRequest;
import com.rinconsur.gestion_analitica.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_analitica.model.RegistroFlujo;
import com.rinconsur.gestion_analitica.service.AnaliticaService;

@WebMvcTest(AnaliticaController.class)
@DisplayName("Tests del AnaliticaController con MockMvc")
public class AnaliticaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnaliticaService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RegistroFlujo flujo;
    private RegistroFlujoRequest request;

    @BeforeEach
    void setUp() {
        flujo = new RegistroFlujo(1L, "A1", "2026-11-19", 100, 40, 20, 20, 20, true, 1L);

        request = new RegistroFlujoRequest();
        request.setAcceso("A1");
        request.setFecha("2026-11-19");
        request.setIngresos(100);
        request.setCompradores(40);
        request.setTransitoJoven(20);
        request.setTransitoAdulto(20);
        request.setTransitoMayor(20);
        request.setActivo(true);
        request.setIdLocal(1L);
    }

    @Test
    @DisplayName("GET /api/analitica/flujo debe retornar 200 con registros por rango")
    void listar_debeRetornar200() throws Exception {
        when(service.obtenerPorRango("2026-11-01", "2026-11-30", false)).thenReturn(List.of(flujo));

        mockMvc.perform(get("/api/analitica/flujo")
                .param("desde", "2026-11-01")
                .param("hasta", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].acceso").value("A1"))
                .andExpect(jsonPath("$[0].ingresos").value(100));
    }

    @Test
    @DisplayName("GET /api/analitica/flujo debe retornar 400 cuando el rango es invalido")
    void listar_debeRetornar400_cuandoRangoInvalido() throws Exception {
        when(service.obtenerPorRango("2026-12-01", "2026-11-01", false))
                .thenThrow(new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'."));

        mockMvc.perform(get("/api/analitica/flujo")
                .param("desde", "2026-12-01")
                .param("hasta", "2026-11-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/analitica/flujo debe retornar 200 al registrar un flujo valido")
    void registrar_debeRetornar200() throws Exception {
        when(service.registrar(any(RegistroFlujo.class))).thenReturn(flujo);

        mockMvc.perform(post("/api/analitica/flujo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceso").value("A1"));
    }

    @Test
    @DisplayName("POST /api/analitica/flujo/lote debe retornar 200 con cantidad insertada")
    void registrarLote_debeRetornar200() throws Exception {
        when(service.registrarLote(anyList())).thenReturn(1);

        mockMvc.perform(post("/api/analitica/flujo/lote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insertados").value(1));
    }

    @Test
    @DisplayName("PATCH /api/analitica/flujo/{id}/activo debe retornar 200 al actualizar y 404 si no existe")
    void cambiarActivo_debeRetornar200y404() throws Exception {
        flujo.setActivo(false);
        when(service.cambiarActivo(1L, false)).thenReturn(flujo);
        when(service.cambiarActivo(99L, false))
                .thenThrow(new RecursoNoEncontradoException("No existe el registro de flujo con ID 99"));

        mockMvc.perform(patch("/api/analitica/flujo/1/activo")
                .param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mockMvc.perform(patch("/api/analitica/flujo/99/activo")
                .param("activo", "false"))
                .andExpect(status().isNotFound());
    }
}
