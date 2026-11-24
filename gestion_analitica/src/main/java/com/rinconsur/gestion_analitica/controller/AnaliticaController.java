package com.rinconsur.gestion_analitica.controller;


import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rinconsur.gestion_analitica.dto.RegistroFlujoRequest;
import com.rinconsur.gestion_analitica.service.AnaliticaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Analitica", description = "Operaciones CRUD para los registros de flujo y métricas de conversión")
@RequestMapping("/api/analitica/flujo")
@CrossOrigin(origins = "*")
public class AnaliticaController {

    private final AnaliticaService service;

    @Operation(summary = "Listar flujo por rango de fechas")
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam String desde,
            @RequestParam String hasta,
            @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return ResponseEntity.ok(service.obtenerPorRango(desde, hasta, incluirInactivos));
    }

    @Operation(summary = "Registrar nuevo conteo de flujo")
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody RegistroFlujoRequest registro) {
        return ResponseEntity.ok(service.registrar(registro.toEntity()));
    }

    @Operation(summary = "Cargar lote de registros de prueba si la tabla está vacía")
    @PostMapping("/lote")
    public ResponseEntity<Map<String, Integer>> registrarLote(@RequestBody List<RegistroFlujoRequest> registros) {
        return ResponseEntity.ok(Map.of("insertados", service.registrarLote(registros.stream().map(RegistroFlujoRequest::toEntity).toList())));
    }

    @Operation(summary = "Habilitar o deshabilitar registro de flujo")
    @PatchMapping("/{id}/activo")
    public ResponseEntity<?> cambiarActivo(@PathVariable Long id, @RequestParam boolean activo) {
        return ResponseEntity.ok(service.cambiarActivo(id, activo));
    }
}
