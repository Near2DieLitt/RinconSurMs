package com.rinconsur.gestion_notificaciones.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.rinconsur.gestion_notificaciones.dto.CorreoRequest;
import com.rinconsur.gestion_notificaciones.dto.CorreoResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionCorreoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@rinconsur.cl}")
    private String remitente;

    public CorreoResponse enviarCorreo(CorreoRequest req) {
        log.info("[NotificacionCorreoService] Enviando correo tipo {} a {}", req.getTipo(), req.getDestinatario());
        if (req.getDestinatario() == null || !req.getDestinatario().contains("@")) {
            throw new IllegalArgumentException("El correo del destinatario no es válido");
        }
        if (req.getCodigoOClave() == null || req.getCodigoOClave().isBlank()) {
            throw new IllegalArgumentException("El código o clave temporal es obligatorio");
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(remitente);
        msg.setTo(req.getDestinatario().trim());
        msg.setSubject(req.getAsunto() != null && !req.getAsunto().isBlank()
                ? req.getAsunto()
                : "Rincón Sur - Notificación de Seguridad");
        msg.setText(construirCuerpo(req));

        mailSender.send(msg);

        return new CorreoResponse(true, "Se ha enviado un mensaje al propietario del correo " + req.getDestinatario().trim());
    }

    public String construirCuerpo(CorreoRequest req) {
        if ("RECUPERACION".equalsIgnoreCase(req.getTipo())) {
            return "Hola,\n\n"
                    + "Tu código de verificación para recuperar tu contraseña en Rincón Sur es: "
                    + req.getCodigoOClave() + "\n\n"
                    + "Este código vence en 5 minutos y tiene un máximo de 3 intentos.\n"
                    + "Equipo Rincón Sur.";
        }
        String nombre = (req.getNombreColaborador() == null || req.getNombreColaborador().isBlank())
                ? "Colaborador/a" : req.getNombreColaborador();
        return "Hola " + nombre + ",\n\n"
                + "Tu cuenta en Rincón Sur ha sido creada.\n"
                + "Tu clave temporal privada es: " + req.getCodigoOClave() + "\n\n"
                + "Por seguridad, el sistema te obligará a cambiarla en tu primer inicio de sesión.\n"
                + "Equipo Rincón Sur.";
    }
}
