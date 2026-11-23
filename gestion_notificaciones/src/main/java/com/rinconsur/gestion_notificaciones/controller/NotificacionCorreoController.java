package com.rinconsur.gestion_notificaciones.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rinconsur.gestion_notificaciones.dto.CorreoRequest;
import com.rinconsur.gestion_notificaciones.dto.CorreoResponse;
import com.rinconsur.gestion_notificaciones.service.NotificacionCorreoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Envío de correos electrónicos por SMTP Gmail")
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionCorreoController {

    private final NotificacionCorreoService service;

    @Operation(summary = "Enviar correo de seguridad", description = "Envía una clave temporal o un código de recuperación de 6 dígitos al correo indicado.")
    @PostMapping("/correo")
    public ResponseEntity<CorreoResponse> enviar(@RequestBody CorreoRequest request) {
        log.info("[NotificacionCorreoController] POST /api/notificaciones/correo");
        try {
            return ResponseEntity.ok(service.enviarCorreo(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new CorreoResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new CorreoResponse(false, "Error SMTP al enviar correo: " + e.getMessage()));
        }
    }
}
