package com.rinconsur.gestion_usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.rinconsur.gestion_usuarios.client.NotificacionesClient;
import com.rinconsur.gestion_usuarios.model.Usuario;
import com.rinconsur.gestion_usuarios.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Unit de UsuarioService")
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRep;

    @Mock
    private ValidadorSeguridadClave validadorClave;

    @Mock
    private NotificacionesClient notificacionesClient;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioAdmin;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNombre("Daniela Gonzales");
        usuarioAdmin.setCorreo("daniela@rinconsur.cl");
        usuarioAdmin.setRut("20123456-5");
        usuarioAdmin.setPerfil("P-01");
        usuarioAdmin.setRol("ADMIN");
        usuarioAdmin.setActivo(true);
        usuarioAdmin.setDebeCambiarClave(false);
        usuarioAdmin.setPasswordHash("hash_daniela");
    }

    @Test
    @DisplayName("listar(), buscarPorCorreo() y login() funcionan y validan errores")
    void listarBuscarYLogin() {
        when(usuarioRep.findAll()).thenReturn(List.of(usuarioAdmin));
        when(usuarioRep.findByCorreoIgnoreCase("daniela@rinconsur.cl")).thenReturn(Optional.of(usuarioAdmin));
        when(usuarioRep.findByCorreoIgnoreCase("")).thenReturn(Optional.empty());
        when(usuarioRep.findByCorreoIgnoreCase("no@existe.cl")).thenReturn(Optional.empty());
        when(encoder.matches("Daniela123!", "hash_daniela")).thenReturn(true);
        when(encoder.matches("Mala!", "hash_daniela")).thenReturn(false);
        when(encoder.matches("", "hash_daniela")).thenReturn(false);

        assertEquals(1, usuarioService.listar().size());
        assertEquals("Daniela Gonzales", usuarioService.buscarPorCorreo(" daniela@rinconsur.cl ").getNombre());
        assertThrows(IllegalArgumentException.class, () -> usuarioService.buscarPorCorreo(null));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.buscarPorCorreo("no@existe.cl"));

        assertEquals(usuarioAdmin, usuarioService.login("daniela@rinconsur.cl", "Daniela123!"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.login("daniela@rinconsur.cl", "Mala!"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.login("daniela@rinconsur.cl", null));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.login(null, "Daniela123!"));

        usuarioAdmin.setActivo(false);
        assertThrows(IllegalStateException.class, () -> usuarioService.login("daniela@rinconsur.cl", "Daniela123!"));
    }

    @Test
    @DisplayName("crearColaboradorConClaveTemporal() valida datos, duplicados y fallo de correo")
    void crearColaboradorConClaveTemporal() {
        when(usuarioRep.existsByCorreoIgnoreCase("nuevo@gmail.com")).thenReturn(false);
        when(usuarioRep.existsByRutIgnoreCase("21045678-3")).thenReturn(false);
        when(validadorClave.generarClaveTemporalSegura()).thenReturn("Temp1234!");
        when(encoder.encode("Temp1234!")).thenReturn("hash_temp");
        when(usuarioRep.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(notificacionesClient.enviarCorreo(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true, false);

        Map<String, Object> ok = usuarioService.crearColaboradorConClaveTemporal(
                "Renata Valdés", "nuevo@gmail.com", "21045678-3", "P-04", "OBSERVADOR");
        assertEquals(10L, ok.get("id"));
        assertEquals("nuevo@gmail.com", ok.get("correo"));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal(
                "Renata Valdés", "nuevo@gmail.com", "21045678-3", "P-04", "OBSERVADOR"));
        verify(usuarioRep, times(1)).delete(any(Usuario.class));

        when(usuarioRep.existsByCorreoIgnoreCase("dup@gmail.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal(
                "Renata", "dup@gmail.com", "21045678-3", "P-04", "OBSERVADOR"));

        when(usuarioRep.existsByCorreoIgnoreCase("otro@gmail.com")).thenReturn(false);
        when(usuarioRep.existsByRutIgnoreCase("11111111-1")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal(
                "Renata", "otro@gmail.com", "11111111-1", "P-04", "OBSERVADOR"));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal(null, "a@b.cl", "21045678-3", "P-04", "OBSERVADOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal(" ", "a@b.cl", "21045678-3", "P-04", "OBSERVADOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal("Ana", null, "21045678-3", "P-04", "OBSERVADOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal("Ana", "mal", "21045678-3", "P-04", "OBSERVADOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal("Ana", "a@b.cl", null, "P-04", "OBSERVADOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal("Ana", "a@b.cl", "mal", "P-04", "OBSERVADOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal("Ana", "a@b.cl", "21045678-3", null, "OBSERVADOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal("Ana", "a@b.cl", "21045678-3", "P-99", "OBSERVADOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal("Ana", "a@b.cl", "21045678-3", "P-04", null));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crearColaboradorConClaveTemporal("Ana", "a@b.cl", "21045678-3", "P-04", "JEFE"));
    }

    @Test
    @DisplayName("generarClaveTemporal() valida permisos, estado activo y envio de correo")
    void generarClaveTemporal() {
        Usuario objetivo = new Usuario();
        objetivo.setId(2L);
        objetivo.setNombre("Matías");
        objetivo.setCorreo("otra@gmail.com");
        objetivo.setRol("OBSERVADOR");
        objetivo.setActivo(true);

        Usuario inactivo = new Usuario();
        inactivo.setId(3L);
        inactivo.setRol("ADMIN");
        inactivo.setActivo(false);

        Usuario noAdmin = new Usuario();
        noAdmin.setId(4L);
        noAdmin.setRol("SUPERVISOR");
        noAdmin.setActivo(true);

        when(usuarioRep.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(usuarioRep.findById(2L)).thenReturn(Optional.of(objetivo));
        when(usuarioRep.findById(3L)).thenReturn(Optional.of(inactivo));
        when(usuarioRep.findById(4L)).thenReturn(Optional.of(noAdmin));
        when(usuarioRep.findById(-1L)).thenReturn(Optional.empty());
        when(usuarioRep.findById(99L)).thenReturn(Optional.empty());
        when(validadorClave.generarClaveTemporalSegura()).thenReturn("Temp1234!");
        when(encoder.encode("Temp1234!")).thenReturn("hash_temp");
        when(notificacionesClient.enviarCorreo(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true, false);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.generarClaveTemporal(2L, null));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.generarClaveTemporal(2L, 99L));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.generarClaveTemporal(2L, 4L));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.generarClaveTemporal(2L, 3L));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.generarClaveTemporal(1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.generarClaveTemporal(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.generarClaveTemporal(99L, 1L));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.generarClaveTemporal(3L, 1L));

        Map<String, Object> ok = usuarioService.generarClaveTemporal(2L, 1L);
        assertEquals("otra@gmail.com", ok.get("correo"));
        assertTrue(objetivo.isDebeCambiarClave());
        verify(usuarioRep, times(1)).save(objetivo);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.generarClaveTemporal(2L, 1L));
    }

    @Test
    @DisplayName("cambiarClave() valida requisitos, clave actual y clave repetida")
    void cambiarClave() {
        usuarioAdmin.setDebeCambiarClave(true);
        when(validadorClave.validar5Requisitos("debil")).thenReturn("La contraseña debe tener al menos 8 caracteres.");
        when(validadorClave.validar5Requisitos("Nueva123!")).thenReturn(null);
        when(validadorClave.validar5Requisitos("Daniela123!")).thenReturn(null);
        when(usuarioRep.findByCorreoIgnoreCase("daniela@rinconsur.cl")).thenReturn(Optional.of(usuarioAdmin));
        when(usuarioRep.findByCorreoIgnoreCase("")).thenReturn(Optional.empty());
        when(encoder.matches("Mala!", "hash_daniela")).thenReturn(false);
        when(encoder.matches("", "hash_daniela")).thenReturn(false);
        when(encoder.matches("Daniela123!", "hash_daniela")).thenReturn(true);
        when(encoder.matches("Nueva123!", "hash_daniela")).thenReturn(false);
        when(encoder.encode("Nueva123!")).thenReturn("hash_nueva");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarClave("daniela@rinconsur.cl", "Daniela123!", "debil", false));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarClave(null, "Daniela123!", "Nueva123!", false));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarClave("daniela@rinconsur.cl", "Mala!", "Nueva123!", false));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarClave("daniela@rinconsur.cl", null, "Nueva123!", false));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarClave("daniela@rinconsur.cl", "Daniela123!", "Daniela123!", false));

        usuarioService.cambiarClave("daniela@rinconsur.cl", "", "Nueva123!", true);
        assertFalse(usuarioAdmin.isDebeCambiarClave());
        assertEquals("hash_nueva", usuarioAdmin.getPasswordHash());

        usuarioAdmin.setPasswordHash("hash_daniela");
        usuarioService.cambiarClave("daniela@rinconsur.cl", "Daniela123!", "Nueva123!", false);
        verify(usuarioRep, times(2)).save(usuarioAdmin);
    }

    @Test
    @DisplayName("cambiarEstadoActivo(), cambiarPerfil(), actualizarDatos() y actualizarPerfilPropio()")
    void administrarColaboradoresYPerfil() {
        Usuario otro = new Usuario();
        otro.setId(2L);
        otro.setNombre("Matías");
        otro.setCorreo("matias@rinconsur.cl");
        otro.setRol("OBSERVADOR");
        otro.setPerfil("P-04");
        otro.setActivo(true);

        when(usuarioRep.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(usuarioRep.findById(2L)).thenReturn(Optional.of(otro));
        when(usuarioRep.findById(99L)).thenReturn(Optional.empty());
        when(usuarioRep.countByRolIgnoreCaseAndActivoTrue("ADMIN")).thenReturn(1L);
        when(usuarioRep.findByCorreoIgnoreCase("daniela@rinconsur.cl")).thenReturn(Optional.of(usuarioAdmin));
        when(usuarioRep.findByCorreoIgnoreCase("matias@rinconsur.cl")).thenReturn(Optional.of(otro));
        when(usuarioRep.findByCorreoIgnoreCase("nuevo@gmail.com")).thenReturn(Optional.empty());
        when(usuarioRep.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarEstadoActivo(1L, 1L, false));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarEstadoActivo(99L, 1L, false));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarEstadoActivo(1L, 2L, false));
        assertFalse(usuarioService.cambiarEstadoActivo(2L, 1L, false).isActivo());
        assertTrue(usuarioService.cambiarEstadoActivo(1L, 1L, true).isActivo());

        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarPerfil(1L, 1L, "P-02", "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarPerfil(2L, 1L, null, "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarPerfil(2L, 1L, "P-99", "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarPerfil(2L, 1L, "P-02", null));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarPerfil(2L, 1L, "P-02", "INVALIDO"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarPerfil(99L, 1L, "P-02", "SUPERVISOR"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarPerfil(1L, 2L, "P-02", "SUPERVISOR"));
        Usuario cambiado = usuarioService.cambiarPerfil(2L, 1L, "P-02", "SUPERVISOR");
        assertEquals("P-02", cambiado.getPerfil());
        assertEquals("SUPERVISOR", cambiado.getRol());

        assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarDatos(99L, "Nombre", "nuevo@gmail.com"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarDatos(1L, null, "nuevo@gmail.com"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarDatos(1L, " ", "nuevo@gmail.com"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarDatos(1L, "Daniela", null));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarDatos(1L, "Daniela", "invalido"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarDatos(1L, "Daniela", "matias@rinconsur.cl"));
        assertEquals("daniela@rinconsur.cl", usuarioService.actualizarDatos(1L, "Daniela Misma", "daniela@rinconsur.cl").getCorreo());
        assertEquals("nuevo@gmail.com", usuarioService.actualizarDatos(1L, "Daniela Nueva", "nuevo@gmail.com").getCorreo());

        Usuario conPerfil = usuarioService.actualizarPerfilPropio("daniela@rinconsur.cl", "  Hola  ", "BASE64");
        assertEquals("Hola", conPerfil.getDescripcion());
        assertEquals("BASE64", conPerfil.getAvatarBase64());

        Usuario sinCambios = usuarioService.actualizarPerfilPropio("daniela@rinconsur.cl", null, null);
        assertEquals("Hola", sinCambios.getDescripcion());
    }

    @Test
    @DisplayName("flujo de recuperacion de contrasena y limpieza de codigos vencidos")
    void flujoRecuperacionYLimpieza() {
        Usuario inactivo = new Usuario();
        inactivo.setId(2L);
        inactivo.setCorreo("inactivo@rinconsur.cl");
        inactivo.setActivo(false);

        when(usuarioRep.findByCorreoIgnoreCase("daniela@rinconsur.cl")).thenReturn(Optional.of(usuarioAdmin));
        when(usuarioRep.findByCorreoIgnoreCase("inactivo@rinconsur.cl")).thenReturn(Optional.of(inactivo));
        when(usuarioRep.findByCorreoIgnoreCase("")).thenReturn(Optional.empty());
        when(usuarioRep.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(validadorClave.validar5Requisitos("123")).thenReturn("Muy corta");
        when(validadorClave.validar5Requisitos("ClaveNueva2026!")).thenReturn(null);
        when(encoder.encode("ClaveNueva2026!")).thenReturn("hash_restablecida");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.verificarCodigoRecuperacion(null, "123456"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.verificarCodigoRecuperacion("daniela@rinconsur.cl", "123456"));

        assertNotNull(usuarioService.solicitarRecuperacionNeutral(null).get("mensaje"));
        assertNotNull(usuarioService.solicitarRecuperacionNeutral("inactivo@rinconsur.cl").get("mensaje"));
        verify(notificacionesClient, never()).enviarCorreo(anyString(), anyString(), anyString(), anyString(), anyString());

        usuarioService.solicitarRecuperacionNeutral("daniela@rinconsur.cl");
        String codigo = usuarioAdmin.getCodigoRecuperacion();
        assertNotNull(codigo);
        verify(notificacionesClient, times(1)).enviarCorreo(anyString(), anyString(), anyString(), anyString(), anyString());

        assertThrows(IllegalArgumentException.class, () -> usuarioService.verificarCodigoRecuperacion("daniela@rinconsur.cl", "000000"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.verificarCodigoRecuperacion("daniela@rinconsur.cl", "000001"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.verificarCodigoRecuperacion("daniela@rinconsur.cl", "000002"));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.verificarCodigoRecuperacion("daniela@rinconsur.cl", codigo));

        usuarioAdmin.setIntentosRecuperacion(0);
        usuarioAdmin.setCodigoExpiracionMs(1L);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.verificarCodigoRecuperacion("daniela@rinconsur.cl", codigo));

        usuarioAdmin.setCodigoExpiracionMs(System.currentTimeMillis() + 60000L);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.confirmarRecuperacion("daniela@rinconsur.cl", codigo, "123"));

        usuarioService.confirmarRecuperacion("daniela@rinconsur.cl", codigo, "ClaveNueva2026!");
        assertNull(usuarioAdmin.getCodigoRecuperacion());
        assertEquals("hash_restablecida", usuarioAdmin.getPasswordHash());
    }

    @Test
    @DisplayName("limpiarCodigosVencidos() elimina codigos vencidos y conserva vigentes")
    void limpiarCodigosVencidos() {
        Usuario vencido = new Usuario();
        vencido.setId(1L);
        vencido.setCodigoRecuperacion("392239");
        vencido.setCodigoExpiracionMs(1L);

        Usuario sinExpiracion = new Usuario();
        sinExpiracion.setId(2L);
        sinExpiracion.setCodigoRecuperacion("222222");
        sinExpiracion.setCodigoExpiracionMs(null);

        Usuario vigente = new Usuario();
        vigente.setId(3L);
        vigente.setCodigoRecuperacion("111111");
        vigente.setCodigoExpiracionMs(System.currentTimeMillis() + 600000L);

        Usuario sinCodigo = new Usuario();
        sinCodigo.setId(4L);

        when(usuarioRep.findAll()).thenReturn(List.of(vencido, sinExpiracion, vigente, sinCodigo));

        usuarioService.limpiarCodigosVencidos();

        assertNull(vencido.getCodigoRecuperacion());
        assertNull(sinExpiracion.getCodigoRecuperacion());
        assertEquals("111111", vigente.getCodigoRecuperacion());
        verify(usuarioRep, times(1)).save(vencido);
        verify(usuarioRep, times(1)).save(sinExpiracion);
    }
}
