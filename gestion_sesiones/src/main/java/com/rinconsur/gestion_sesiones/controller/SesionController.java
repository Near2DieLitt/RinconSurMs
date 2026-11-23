package com.rinconsur.gestion_sesiones.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rinconsur.gestion_sesiones.dto.SesionRequest;
import com.rinconsur.gestion_sesiones.model.Sesion;
import com.rinconsur.gestion_sesiones.service.SesionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Sesiones", description = "Operaciones CRUD para programar sesiones y persistir el checkbox de completado")
@RequestMapping("/api/sesiones")
@CrossOrigin(origins = "*")
public class SesionController {

    private final SesionService service;

    @Operation(summary = "Listar sesiones")
    @GetMapping
    public ResponseEntity<List<Sesion>> listar(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(defaultValue = "true") boolean incluirInactivas,
            @RequestParam(required = false) Long idUsuario) {
        return ResponseEntity.ok(service.listar(fecha, desde, hasta, incluirInactivas, idUsuario));
    }

    @Operation(summary = "Programar nueva sesión")
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody SesionRequest sesion,
                                   @RequestParam(required = false) String rolSolicitante) {
        return ResponseEntity.ok(service.crear(sesion.toEntity(), rolSolicitante));
    }

    @Operation(summary = "Habilitar o deshabilitar sesión")
    @PatchMapping("/{id}/activo")
    public ResponseEntity<?> cambiarActivo(@PathVariable Long id, @RequestParam boolean activo) {
        return ResponseEntity.ok(service.cambiarActivo(id, activo));
    }

    @Operation(summary = "Actualizar checkbox de sesión completada")
    @PatchMapping("/{id}/completada")
    public ResponseEntity<?> cambiarCompletada(@PathVariable Long id,
                                               @RequestParam boolean completada,
                                               @RequestParam(required = false) String rolSolicitante) {
        return ResponseEntity.ok(service.cambiarCompletada(id, completada, rolSolicitante));
    }

    @Operation(summary = "Finalizar sesión activa")
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(service.finalizar(id));
    }
}

