package com.rinconsur.gestion_notificaciones.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinconsur.gestion_notificaciones.dto.CorreoRequest;
import com.rinconsur.gestion_notificaciones.dto.CorreoResponse;
import com.rinconsur.gestion_notificaciones.service.NotificacionCorreoService;

@WebMvcTest(NotificacionCorreoController.class)
@DisplayName("Tests del NotificacionCorreoController con MockMvc")
public class NotificacionCorreoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacionCorreoService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CorreoRequest requestValido;

    @BeforeEach
    void setUp() {
        requestValido = new CorreoRequest(
                "renata@gmail.com",
                "Rincón Sur - Código de verificación",
                "Renata",
                "123456",
                "RECUPERACION"
        );
    }

    @Test
    @DisplayName("POST /api/notificaciones/correo debe retornar 200 cuando el correo se envia")
    void enviar_debeRetornar200() throws Exception {
        CorreoResponse response = new CorreoResponse(true, "Se ha enviado un mensaje al propietario del correo renata@gmail.com");
        when(service.enviarCorreo(any(CorreoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/notificaciones/correo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enviado").value(true));
    }

    @Test
    @DisplayName("POST /api/notificaciones/correo debe retornar 400 cuando los datos son invalidos")
    void enviar_debeRetornar400_cuandoDatosInvalidos() throws Exception {
        when(service.enviarCorreo(any(CorreoRequest.class)))
                .thenThrow(new IllegalArgumentException("El correo del destinatario no es válido"));

        mockMvc.perform(post("/api/notificaciones/correo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.enviado").value(false))
                .andExpect(jsonPath("$.mensaje").value("El correo del destinatario no es válido"));
    }

    @Test
    @DisplayName("POST /api/notificaciones/correo debe retornar 500 cuando falla SMTP")
    void enviar_debeRetornar500_cuandoFallaSmtp() throws Exception {
        when(service.enviarCorreo(any(CorreoRequest.class)))
                .thenThrow(new RuntimeException("SMTP timeout"));

        mockMvc.perform(post("/api/notificaciones/correo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.enviado").value(false));
    }
}
