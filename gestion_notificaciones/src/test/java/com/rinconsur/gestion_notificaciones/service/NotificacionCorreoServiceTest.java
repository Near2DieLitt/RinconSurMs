package com.rinconsur.gestion_notificaciones.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.rinconsur.gestion_notificaciones.dto.CorreoRequest;
import com.rinconsur.gestion_notificaciones.dto.CorreoResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Unit de NotificacionCorreoService")
public class NotificacionCorreoServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificacionCorreoService service;

    @Test
    @DisplayName("enviarCorreo() debe enviar correo de clave temporal con asunto personalizado")
    void enviarCorreo_claveTemporal_debeEnviarCorrectamente() {
        CorreoRequest req = new CorreoRequest(
                "renata@gmail.com",
                "Rincón Sur - Clave temporal",
                "Renata Valdés",
                "Rincon2026!",
                "CLAVE_TEMPORAL"
        );

        CorreoResponse resp = service.enviarCorreo(req);

        assertNotNull(resp);
        assertTrue(resp.isEnviado());
        assertTrue(resp.getMensaje().contains("renata@gmail.com"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("enviarCorreo() debe usar asunto por defecto cuando asunto es nulo o vacio")
    void enviarCorreo_sinAsunto_debeUsarAsuntoPorDefecto() {
        CorreoRequest reqNulo = new CorreoRequest("matias@gmail.com", null, "", "123456", "RECUPERACION");
        CorreoResponse resp1 = service.enviarCorreo(reqNulo);
        assertTrue(resp1.isEnviado());

        CorreoRequest reqVacio = new CorreoRequest("matias@gmail.com", "   ", null, "654321", "CLAVE_TEMPORAL");
        CorreoResponse resp2 = service.enviarCorreo(reqVacio);
        assertTrue(resp2.isEnviado());

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("enviarCorreo() debe lanzar IllegalArgumentException cuando destinatario o codigo son invalidos")
    void enviarCorreo_datosInvalidos_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.enviarCorreo(new CorreoRequest(null, "Asunto", "Ana", "123456", "RECUPERACION")));
        assertThrows(IllegalArgumentException.class,
                () -> service.enviarCorreo(new CorreoRequest("sin-arroba", "Asunto", "Ana", "123456", "RECUPERACION")));
        assertThrows(IllegalArgumentException.class,
                () -> service.enviarCorreo(new CorreoRequest("ana@gmail.com", "Asunto", "Ana", null, "RECUPERACION")));
        assertThrows(IllegalArgumentException.class,
                () -> service.enviarCorreo(new CorreoRequest("ana@gmail.com", "Asunto", "Ana", "   ", "RECUPERACION")));
    }

    @Test
    @DisplayName("enviarCorreo() debe propagar error cuando falla JavaMailSender")
    void enviarCorreo_falloSmtp_debeLanzarExcepcion() {
        CorreoRequest req = new CorreoRequest("ana@gmail.com", "Asunto", "Ana", "123456", "RECUPERACION");
        doThrow(new RuntimeException("Fallo SMTP")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class, () -> service.enviarCorreo(req));
    }

    @Test
    @DisplayName("construirCuerpo() debe armar el mensaje correcto para recuperacion y clave temporal")
    void construirCuerpo_debeRetornarTextoEsperado() {
        String cuerpoRecup = service.construirCuerpo(
                new CorreoRequest("a@b.cl", "", "", "999888", "RECUPERACION"));
        assertTrue(cuerpoRecup.contains("999888"));

        String cuerpoConNombre = service.construirCuerpo(
                new CorreoRequest("a@b.cl", "", "Renata", "Clave123!", "CLAVE_TEMPORAL"));
        assertTrue(cuerpoConNombre.contains("Renata"));

        String cuerpoSinNombre = service.construirCuerpo(
                new CorreoRequest("a@b.cl", "", "   ", "Clave123!", "CLAVE_TEMPORAL"));
        assertTrue(cuerpoSinNombre.contains("Colaborador/a"));
    }
}
