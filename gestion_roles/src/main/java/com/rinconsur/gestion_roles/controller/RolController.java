package com.rinconsur.gestion_roles.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rinconsur.gestion_roles.dto.MensajeResponse;
import com.rinconsur.gestion_roles.dto.RolRequest;
import com.rinconsur.gestion_roles.model.Rol;
import com.rinconsur.gestion_roles.service.RolService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Operaciones CRUD para la gestión de los roles de acceso")
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RolController {

    private final RolService rolService;

    @Operation(summary = "Listar todos los roles", description = "Retorna la lista completa de roles existentes en la base de datos.")
    @GetMapping
    public ResponseEntity<List<Rol>> listar() {
        log.info("[RolController] GET /api/roles");
        return ResponseEntity.ok(rolService.listarTodos());
    }

    @Operation(summary = "Obtener rol por código", description = "Busca un perfil por su código (ej: P-01).")
    @GetMapping("/{codigo}")
    public ResponseEntity<?> obtenerPorCodigo(@PathVariable String codigo) {
        try {
            return ResponseEntity.ok(rolService.buscarPorCodigo(codigo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new MensajeResponse(e.getMessage()));
        }
    }

    @Operation(summary = "Crear un nuevo rol", description = "Guarda un nuevo perfil en la base de datos.")
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody RolRequest rol) {
        return ResponseEntity.ok(rolService.guardar(rol.toEntity()));
    }
}
