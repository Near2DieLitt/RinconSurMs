package com.rinconsur.gestion_visitantes.controller;


import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rinconsur.gestion_visitantes.dto.BoletaRequest;
import com.rinconsur.gestion_visitantes.dto.VisitanteRequest;
import com.rinconsur.gestion_visitantes.model.Visitante;
import com.rinconsur.gestion_visitantes.service.VisitanteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Visitantes", description = "Operaciones para el conteo de visitantes, boletas y salida por persona concreta")
@RequestMapping("/api/visitantes")
@CrossOrigin(origins = "*")
public class VisitanteController {

    private final VisitanteService service;

    @Operation(summary = "Listar visitantes en tienda")
    @GetMapping("/en-tienda")
    public ResponseEntity<List<Visitante>> listarEnTienda(
            @RequestParam String fecha,
            @RequestParam(required = false) Long idSesion) {
        return ResponseEntity.ok(service.listarEnTienda(fecha, idSesion));
    }

    @Operation(summary = "Resumen de visitantes del día")
    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Integer>> resumen(
            @RequestParam String fecha,
            @RequestParam(required = false) Long idSesion) {
        return ResponseEntity.ok(service.obtenerResumen(fecha, idSesion));
    }

    @Operation(summary = "Registrar entrada de visitante")
    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestBody VisitanteRequest visitante) {
        return ResponseEntity.ok(service.registrarEntrada(visitante.toEntity()));
    }

    @Operation(summary = "Registrar salida de visitante específico sin compra")
    @PatchMapping("/{id}/salida-sin-compra")
    public ResponseEntity<?> registrarSalidaEspecifica(@PathVariable Long id) {
        return ResponseEntity.ok(service.registrarSalidaEspecificaSinCompra(id));
    }

    @Operation(summary = "Asociar boleta a visitante específico")
    @PostMapping("/{id}/boleta")
    public ResponseEntity<?> vincularBoleta(@PathVariable Long id, @RequestBody BoletaRequest body) {
        return ResponseEntity.ok(service.vincularBoletaAVisitanteEspecifico(id, body.getFolio()));
    }

    @Operation(summary = "Ejecutar cierre diario")
    @PostMapping("/cierre-diario")
    public ResponseEntity<Map<String, Integer>> cierreDiario(
            @RequestParam String fecha,
            @RequestParam(required = false) Long idSesion) {
        int cerrados = service.ejecutarCierreDiario(fecha, idSesion);
        return ResponseEntity.ok(Map.of("actualizados", cerrados));
    }
}
