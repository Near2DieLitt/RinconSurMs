package com.rinconsur.gestion_usuarios.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rinconsur.gestion_usuarios.dto.ActualizarDatosRequest;
import com.rinconsur.gestion_usuarios.dto.CambiarClaveRequest;
import com.rinconsur.gestion_usuarios.dto.ColaboradorRequest;
import com.rinconsur.gestion_usuarios.dto.LoginRequest;
import com.rinconsur.gestion_usuarios.dto.MensajeResponse;
import com.rinconsur.gestion_usuarios.dto.PerfilPropioRequest;
import com.rinconsur.gestion_usuarios.dto.RecuperacionRequest;
import com.rinconsur.gestion_usuarios.model.Usuario;
import com.rinconsur.gestion_usuarios.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones CRUD y seguridad para los colaboradores de Rincón Sur")
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService service;

    @Operation(summary = "Listar todos los usuarios")
    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @Operation(summary = "Buscar usuario por correo")
    @GetMapping("/correo")
    public ResponseEntity<?> buscarPorCorreo(@RequestParam String correo) {
        try {
            return ResponseEntity.ok(service.buscarPorCorreo(correo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new MensajeResponse(e.getMessage()));
        }
    }

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        try {
            return ResponseEntity.ok(service.login(body.getCorreo(), body.getPassword()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(new MensajeResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(new MensajeResponse(e.getMessage()));
        }
    }

    @Operation(summary = "Crear colaborador con clave temporal enviada al correo")
    @PostMapping("/colaboradores")
    public ResponseEntity<?> crearColaborador(@RequestBody ColaboradorRequest body) {
        return ResponseEntity.ok(service.crearColaboradorConClaveTemporal(
                body.getNombre(),
                body.getCorreo(),
                body.getRut(),
                body.getPerfil(),
                body.getRol()
        ));
    }

    @Operation(summary = "Generar clave temporal para un colaborador (solo administrador)")
    @PostMapping("/{id}/clave-temporal")
    public ResponseEntity<?> generarClaveTemporal(@PathVariable Long id, @RequestParam Long idSolicitante) {
        return ResponseEntity.ok(service.generarClaveTemporal(id, idSolicitante));
    }

    @Operation(summary = "Cambiar contraseña")
    @PostMapping("/cambiar-clave")
    public ResponseEntity<?> cambiarClave(@RequestBody CambiarClaveRequest body) {
        service.cambiarClave(
                body.getCorreo(),
                body.getClaveActual(),
                body.getClaveNueva(),
                Boolean.TRUE.equals(body.getEsPrimerInicio())
        );
        return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada correctamente."));
    }

    @Operation(summary = "Habilitar o deshabilitar colaborador")
    @PatchMapping("/{id}/activo")
    public ResponseEntity<?> cambiarActivo(@PathVariable Long id,
                                           @RequestParam Long idSolicitante,
                                           @RequestParam boolean activo) {
        return ResponseEntity.ok(service.cambiarEstadoActivo(id, idSolicitante, activo));
    }

    @Operation(summary = "Cambiar perfil y rol del colaborador")
    @PatchMapping("/{id}/perfil")
    public ResponseEntity<?> cambiarPerfil(@PathVariable Long id,
                                           @RequestParam(required = false) Long idSolicitante,
                                           @RequestParam String perfil,
                                           @RequestParam String rol) {
        return ResponseEntity.ok(service.cambiarPerfil(id, idSolicitante, perfil, rol));
    }

    @Operation(summary = "Editar nombre y correo del colaborador")
    @PatchMapping("/{id}/datos")
    public ResponseEntity<?> actualizarDatos(@PathVariable Long id, @RequestBody ActualizarDatosRequest body) {
        return ResponseEntity.ok(service.actualizarDatos(
                id,
                body.getNombre(),
                body.getCorreo()
        ));
    }

    @Operation(summary = "Actualizar descripción y/o foto de perfil propia")
    @PatchMapping("/perfil-propio")
    public ResponseEntity<?> actualizarPerfilPropio(@RequestBody PerfilPropioRequest body) {
        return ResponseEntity.ok(service.actualizarPerfilPropio(
                body.getCorreo(),
                body.getDescripcion(),
                body.getAvatarBase64()
        ));
    }

    @Operation(summary = "Solicitar código de recuperación")
    @PostMapping("/recuperar/solicitar")
    public ResponseEntity<Map<String, String>> solicitarRecuperacion(@RequestBody RecuperacionRequest body) {
        return ResponseEntity.ok(service.solicitarRecuperacionNeutral(body.getCorreo()));
    }

    @Operation(summary = "Verificar código de recuperación de 6 dígitos")
    @PostMapping("/recuperar/verificar")
    public ResponseEntity<?> verificarCodigo(@RequestBody RecuperacionRequest body) {
        service.verificarCodigoRecuperacion(body.getCorreo(), body.getCodigo());
        return ResponseEntity.ok(new MensajeResponse("Código válido."));
    }

    @Operation(summary = "Confirmar recuperación de contraseña")
    @PostMapping("/recuperar/confirmar")
    public ResponseEntity<?> confirmarRecuperacion(@RequestBody RecuperacionRequest body) {
        service.confirmarRecuperacion(
                body.getCorreo(),
                body.getCodigo(),
                body.getNuevaClave()
        );
        return ResponseEntity.ok(new MensajeResponse("Contraseña restablecida con éxito."));
    }
}
