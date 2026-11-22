package com.rinconsur.gestion_usuarios.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.rinconsur.gestion_usuarios.client.NotificacionesClient;
import com.rinconsur.gestion_usuarios.model.Usuario;
import com.rinconsur.gestion_usuarios.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final long EXPIRACION_MS = 5 * 60 * 1000L;
    private static final int MAX_INTENTOS = 3;
    private static final Set<String> PERFILES_VALIDOS = Set.of("P-01", "P-02", "P-03", "P-04");
    private static final Set<String> ROLES_VALIDOS = Set.of("ADMIN", "SUPERVISOR", "OBSERVADOR");

    private final UsuarioRepository usuarioRep;
    private final ValidadorSeguridadClave validadorClave;
    private final NotificacionesClient notificacionesClient;
    private final BCryptPasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    public List<Usuario> listar() {
        return usuarioRep.findAll();
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioRep.findByCorreoIgnoreCase(correo == null ? "" : correo.trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    public Usuario login(String correo, String password) {
        Usuario u = usuarioRep.findByCorreoIgnoreCase(correo == null ? "" : correo.trim())
                .orElseThrow(() -> new IllegalArgumentException("Este correo no está registrado."));
        if (!u.isActivo()) {
            throw new IllegalStateException("Tu cuenta está deshabilitada. Contacta a un administrador.");
        }
        if (!encoder.matches(password == null ? "" : password, u.getPasswordHash())) {
            throw new IllegalArgumentException("Contraseña incorrecta.");
        }
        return u;
    }

    public Map<String, Object> crearColaboradorConClaveTemporal(String nombre, String correo, String rut, String perfil, String rol) {
        validarDatosColaborador(nombre, correo, rut, perfil, rol);
        if (usuarioRep.existsByCorreoIgnoreCase(correo.trim())) {
            throw new IllegalArgumentException("Este correo ya está registrado.");
        }
        if (usuarioRep.existsByRutIgnoreCase(rut.trim())) {
            throw new IllegalArgumentException("Este RUT ya está registrado.");
        }
        String claveTemporal = validadorClave.generarClaveTemporalSegura();

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre.trim());
        nuevo.setCorreo(correo.trim().toLowerCase());
        nuevo.setRut(rut.trim().toUpperCase());
        nuevo.setPerfil(perfil);
        nuevo.setRol(rol);
        nuevo.setPasswordHash(encoder.encode(claveTemporal));
        nuevo.setActivo(true);
        nuevo.setDebeCambiarClave(true);

        Usuario guardado = usuarioRep.save(nuevo);

        boolean enviado = notificacionesClient.enviarCorreo(
                guardado.getCorreo(),
                "Rincón Sur - Clave temporal de acceso",
                guardado.getNombre(),
                claveTemporal,
                "CLAVE_TEMPORAL"
        );
        if (!enviado) {
            usuarioRep.delete(guardado);
            throw new IllegalArgumentException("No se pudo enviar el correo con la clave temporal. La persona no fue registrada.");
        }

        return Map.of(
                "id", guardado.getId(),
                "correo", guardado.getCorreo(),
                "mensaje", "Se ha enviado un mensaje con la clave temporal al propietario del correo (" + guardado.getCorreo() + ")"
        );
    }

    public Map<String, Object> generarClaveTemporal(Long idObjetivo, Long idSolicitante) {
        Usuario admin = usuarioRep.findById(idSolicitante == null ? -1L : idSolicitante)
                .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado."));
        if (!admin.isActivo() || !"ADMIN".equalsIgnoreCase(admin.getRol())) {
            throw new IllegalArgumentException("Solo un administrador puede generar claves temporales.");
        }
        if (idObjetivo != null && idObjetivo.equals(idSolicitante)) {
            throw new IllegalArgumentException("Para tu propia cuenta usa Cambiar contraseña en tu perfil.");
        }
        Usuario u = usuarioRep.findById(idObjetivo == null ? -1L : idObjetivo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (!u.isActivo()) {
            throw new IllegalArgumentException("La cuenta está deshabilitada. Habilítala primero.");
        }
        String claveTemporal = validadorClave.generarClaveTemporalSegura();

        boolean enviado = notificacionesClient.enviarCorreo(
                u.getCorreo(),
                "Rincón Sur - Nueva clave temporal de acceso",
                u.getNombre(),
                claveTemporal,
                "CLAVE_TEMPORAL"
        );
        if (!enviado) {
            throw new IllegalArgumentException("No se pudo enviar el correo con la clave temporal. No se hicieron cambios.");
        }

        u.setPasswordHash(encoder.encode(claveTemporal));
        u.setDebeCambiarClave(true);
        usuarioRep.save(u);

        return Map.of(
                "correo", u.getCorreo(),
                "mensaje", "Se envió una clave temporal al correo de " + u.getNombre() + " (" + u.getCorreo() + ")"
        );
    }

    private void validarDatosColaborador(String nombre, String correo, String rut, String perfil, String rol) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (correo == null || !correo.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        if (rut == null || !rut.trim().matches("^[0-9]{7,8}-[0-9kK]$")) {
            throw new IllegalArgumentException("El RUT debe tener el formato 12345678-9.");
        }
        if (perfil == null || !PERFILES_VALIDOS.contains(perfil)) {
            throw new IllegalArgumentException("El perfil debe ser P-01, P-02, P-03 o P-04.");
        }
        if (rol == null || !ROLES_VALIDOS.contains(rol)) {
            throw new IllegalArgumentException("El rol debe ser ADMIN, SUPERVISOR u OBSERVADOR.");
        }
    }

    public void cambiarClave(String correo, String claveActual, String claveNueva, boolean esPrimerInicio) {
        String errorSeguridad = validadorClave.validar5Requisitos(claveNueva);
        if (errorSeguridad != null) {
            throw new IllegalArgumentException(errorSeguridad);
        }
        Usuario u = usuarioRep.findByCorreoIgnoreCase(correo == null ? "" : correo.trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (!esPrimerInicio && !encoder.matches(claveActual == null ? "" : claveActual, u.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (encoder.matches(claveNueva, u.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual.");
        }
        u.setPasswordHash(encoder.encode(claveNueva));
        u.setDebeCambiarClave(false);
        usuarioRep.save(u);
    }

    public Usuario cambiarEstadoActivo(Long idObjetivo, Long idSolicitante, boolean activo) {
        if (idObjetivo != null && idObjetivo.equals(idSolicitante) && !activo) {
            throw new IllegalArgumentException("No puedes deshabilitar tu propia cuenta.");
        }
        Usuario u = usuarioRep.findById(idObjetivo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (!activo && "ADMIN".equalsIgnoreCase(u.getRol()) && u.isActivo()
                && usuarioRep.countByRolIgnoreCaseAndActivoTrue("ADMIN") <= 1) {
            throw new IllegalArgumentException("Debe quedar al menos un administrador activo.");
        }
        u.setActivo(activo);
        return usuarioRep.save(u);
    }

    public Usuario cambiarPerfil(Long idObjetivo, Long idSolicitante, String perfil, String rol) {
        if (idObjetivo != null && idObjetivo.equals(idSolicitante)) {
            throw new IllegalArgumentException("No puedes cambiar tu propio perfil.");
        }
        if (perfil == null || !PERFILES_VALIDOS.contains(perfil)) {
            throw new IllegalArgumentException("El perfil debe ser P-01, P-02, P-03 o P-04.");
        }
        if (rol == null || !ROLES_VALIDOS.contains(rol)) {
            throw new IllegalArgumentException("El rol debe ser ADMIN, SUPERVISOR u OBSERVADOR.");
        }
        Usuario u = usuarioRep.findById(idObjetivo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if ("ADMIN".equalsIgnoreCase(u.getRol()) && u.isActivo() && !"ADMIN".equalsIgnoreCase(rol)
                && usuarioRep.countByRolIgnoreCaseAndActivoTrue("ADMIN") <= 1) {
            throw new IllegalArgumentException("Debe quedar al menos un administrador activo.");
        }
        u.setPerfil(perfil);
        u.setRol(rol);
        return usuarioRep.save(u);
    }

    public Usuario actualizarDatos(Long id, String nuevoNombre, String nuevoCorreo) {
        Usuario u = usuarioRep.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (nuevoCorreo == null || !nuevoCorreo.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        String correoLimpio = nuevoCorreo.trim().toLowerCase();
        usuarioRep.findByCorreoIgnoreCase(correoLimpio).ifPresent(otro -> {
            if (!otro.getId().equals(u.getId())) {
                throw new IllegalArgumentException("Este correo ya está registrado por otro usuario.");
            }
        });
        u.setNombre(nuevoNombre.trim());
        u.setCorreo(correoLimpio);
        return usuarioRep.save(u);
    }

    public Usuario actualizarPerfilPropio(String correo, String descripcion, String avatarBase64) {
        Usuario u = buscarPorCorreo(correo);
        if (descripcion != null) {
            u.setDescripcion(descripcion.trim());
        }
        if (avatarBase64 != null) {
            u.setAvatarBase64(avatarBase64);
        }
        return usuarioRep.save(u);
    }

    public Map<String, String> solicitarRecuperacionNeutral(String correo) {
        usuarioRep.findByCorreoIgnoreCase(correo == null ? "" : correo.trim()).ifPresent(u -> {
            if (u.isActivo()) {
                String codigo = String.valueOf(100000 + random.nextInt(900000));
                u.setCodigoRecuperacion(codigo);
                u.setCodigoExpiracionMs(System.currentTimeMillis() + EXPIRACION_MS);
                u.setIntentosRecuperacion(0);
                usuarioRep.save(u);
                notificacionesClient.enviarCorreo(
                        u.getCorreo(),
                        "Rincón Sur - Código de verificación",
                        u.getNombre(),
                        codigo,
                        "RECUPERACION"
                );
            }
        });
        return Map.of("mensaje", "Si el correo se encuentra registrado, recibirás un código de verificación válido por 5 minutos.");
    }

    @Scheduled(fixedDelay = 60000L)
    public void limpiarCodigosVencidos() {
        long ahora = System.currentTimeMillis();
        for (Usuario u : usuarioRep.findAll()) {
            boolean hayCodigo = u.getCodigoRecuperacion() != null || u.getCodigoExpiracionMs() != null;
            boolean vencido = u.getCodigoExpiracionMs() == null || u.getCodigoExpiracionMs() < ahora;
            if (hayCodigo && vencido) {
                u.setCodigoRecuperacion(null);
                u.setCodigoExpiracionMs(null);
                u.setIntentosRecuperacion(0);
                usuarioRep.save(u);
            }
        }
    }

    public void verificarCodigoRecuperacion(String correo, String codigoIngresado) {
        Usuario u = usuarioRep.findByCorreoIgnoreCase(correo == null ? "" : correo.trim())
                .orElseThrow(() -> new IllegalArgumentException("Código incorrecto o correo no válido."));
        if (u.getCodigoRecuperacion() == null || u.getCodigoExpiracionMs() == null) {
            throw new IllegalArgumentException("No hay un código activo. Solicita uno nuevo.");
        }
        if (System.currentTimeMillis() > u.getCodigoExpiracionMs()) {
            throw new IllegalArgumentException("El código ha vencido (más de 5 minutos). Solicita uno nuevo.");
        }
        if (u.getIntentosRecuperacion() >= MAX_INTENTOS) {
            throw new IllegalArgumentException("Superaste el límite de 3 intentos. Solicita un nuevo código.");
        }
        if (!u.getCodigoRecuperacion().equals(codigoIngresado)) {
            u.setIntentosRecuperacion(u.getIntentosRecuperacion() + 1);
            usuarioRep.save(u);
            int restantes = Math.max(0, MAX_INTENTOS - u.getIntentosRecuperacion());
            if (restantes == 0) {
                throw new IllegalArgumentException("Código inválido. Ya no quedan intentos, solicita un código nuevo.");
            }
            throw new IllegalArgumentException("Código incorrecto. Intentos restantes: " + restantes);
        }
    }

    public void confirmarRecuperacion(String correo, String codigoIngresado, String nuevaClave) {
        String errorClave = validadorClave.validar5Requisitos(nuevaClave);
        if (errorClave != null) {
            throw new IllegalArgumentException(errorClave);
        }
        verificarCodigoRecuperacion(correo, codigoIngresado);
        Usuario u = buscarPorCorreo(correo);
        u.setPasswordHash(encoder.encode(nuevaClave));
        u.setDebeCambiarClave(false);
        u.setCodigoRecuperacion(null);
        u.setCodigoExpiracionMs(null);
        u.setIntentosRecuperacion(0);
        usuarioRep.save(u);
    }
}
