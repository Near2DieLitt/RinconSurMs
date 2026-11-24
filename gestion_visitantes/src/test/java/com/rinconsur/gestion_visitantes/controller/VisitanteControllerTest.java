package com.rinconsur.gestion_visitantes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinconsur.gestion_visitantes.dto.BoletaRequest;
import com.rinconsur.gestion_visitantes.dto.VisitanteRequest;
import com.rinconsur.gestion_visitantes.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_visitantes.model.Visitante;
import com.rinconsur.gestion_visitantes.service.VisitanteService;

@WebMvcTest(VisitanteController.class)
@DisplayName("Tests del VisitanteController con MockMvc")
public class VisitanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VisitanteService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Visitante visitante;
    private VisitanteRequest request;

    @BeforeEach
    void setUp() {
        visitante = new Visitante(10L, "VIS-001", "2026-11-19", "10:15:00", "A2", "JOVEN", "En_Tienda", null, 1L, 10L);

        request = new VisitanteRequest();
        request.setFecha("2026-11-19");
        request.setHora("10:15:00");
        request.setAcceso("A2");
        request.setRangoEtario("JOVEN");
        request.setIdSesion(1L);
        request.setIdLocal(10L);
    }

    @Test
    @DisplayName("GET /api/visitantes/en-tienda debe retornar 200 con visitantes activos")
    void listarEnTienda_debeRetornar200() throws Exception {
        when(service.listarEnTienda("2026-11-19", 1L)).thenReturn(List.of(visitante));

        mockMvc.perform(get("/api/visitantes/en-tienda")
                .param("fecha", "2026-11-19")
                .param("idSesion", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("VIS-001"));
    }

    @Test
    @DisplayName("GET /api/visitantes/resumen debe retornar 200 con el resumen del dia")
    void resumen_debeRetornar200() throws Exception {
        when(service.obtenerResumen("2026-11-19", 1L))
                .thenReturn(Map.of("enTienda", 1, "compro", 2, "noCompro", 1, "total", 4));

        mockMvc.perform(get("/api/visitantes/resumen")
                .param("fecha", "2026-11-19")
                .param("idSesion", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4));
    }

    @Test
    @DisplayName("POST /api/visitantes/entrada debe retornar 200 al registrar entrada")
    void registrarEntrada_debeRetornar200() throws Exception {
        when(service.registrarEntrada(any(Visitante.class))).thenReturn(visitante);

        mockMvc.perform(post("/api/visitantes/entrada")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("VIS-001"));
    }

    @Test
    @DisplayName("PATCH /api/visitantes/{id}/salida-sin-compra debe retornar 200 y 404 si no existe")
    void registrarSalidaEspecifica_debeRetornar200y404() throws Exception {
        visitante.setEstado("No_Compro");
        when(service.registrarSalidaEspecificaSinCompra(10L)).thenReturn(visitante);
        when(service.registrarSalidaEspecificaSinCompra(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe el visitante con ID 99"));

        mockMvc.perform(patch("/api/visitantes/10/salida-sin-compra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("No_Compro"));

        mockMvc.perform(patch("/api/visitantes/99/salida-sin-compra"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/visitantes/{id}/boleta debe retornar 200 al vincular boleta")
    void vincularBoleta_debeRetornar200() throws Exception {
        BoletaRequest body = new BoletaRequest();
        body.setFolio("12345678");
        visitante.setEstado("Compro");
        visitante.setFolioBoleta("12345678");
        when(service.vincularBoletaAVisitanteEspecifico(10L, "12345678")).thenReturn(visitante);

        mockMvc.perform(post("/api/visitantes/10/boleta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folioBoleta").value("12345678"));
    }

    @Test
    @DisplayName("POST /api/visitantes/cierre-diario debe retornar 200 con cantidad actualizada")
    void cierreDiario_debeRetornar200() throws Exception {
        when(service.ejecutarCierreDiario("2026-11-19", 1L)).thenReturn(3);

        mockMvc.perform(post("/api/visitantes/cierre-diario")
                .param("fecha", "2026-11-19")
                .param("idSesion", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actualizados").value(3));
    }
}
