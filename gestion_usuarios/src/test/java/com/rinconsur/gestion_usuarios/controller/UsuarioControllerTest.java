package com.rinconsur.gestion_usuarios.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinconsur.gestion_usuarios.dto.ActualizarDatosRequest;
import com.rinconsur.gestion_usuarios.dto.CambiarClaveRequest;
import com.rinconsur.gestion_usuarios.dto.ColaboradorRequest;
import com.rinconsur.gestion_usuarios.dto.LoginRequest;
import com.rinconsur.gestion_usuarios.dto.PerfilPropioRequest;
import com.rinconsur.gestion_usuarios.dto.RecuperacionRequest;
import com.rinconsur.gestion_usuarios.model.Usuario;
import com.rinconsur.gestion_usuarios.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
@DisplayName("Tests del UsuarioController con MockMvc")
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Daniela Gonzales");
        usuario.setCorreo("daniela@rinconsur.cl");
        usuario.setRut("20123456-5");
        usuario.setPerfil("P-01");
        usuario.setRol("ADMIN");
        usuario.setActivo(true);
    }

    @Test
    @DisplayName("GET /api/usuarios debe retornar 200 con la lista de usuarios")
    void listar_debeRetornar200() throws Exception {
        when(service.listar()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("daniela@rinconsur.cl"));
    }

    @Test
    @DisplayName("GET /api/usuarios/correo debe retornar 200 si existe y 404 si no existe")
    void buscarPorCorreo_debeRetornar200y404() throws Exception {
        when(service.buscarPorCorreo("daniela@rinconsur.cl")).thenReturn(usuario);
        when(service.buscarPorCorreo("no@existe.cl")).thenThrow(new IllegalArgumentException("Usuario no encontrado."));

        mockMvc.perform(get("/api/usuarios/correo")
                .param("correo", "daniela@rinconsur.cl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Daniela Gonzales"));

        mockMvc.perform(get("/api/usuarios/correo")
                .param("correo", "no@existe.cl"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Usuario no encontrado."));
    }

@Test
    @DisplayName("POST /api/usuarios/login debe retornar 200, 401 y 403 segun el caso")
    void login_debeRetornar200_401_403() throws Exception {
        LoginRequest reqOk = new LoginRequest();
        reqOk.setCorreo("daniela@rinconsur.cl");
        reqOk.setPassword("Daniela123!");

        when(service.login("daniela@rinconsur.cl", "Daniela123!")).thenReturn(usuario);
        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqOk)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("daniela@rinconsur.cl"));

        LoginRequest req401 = new LoginRequest();
        req401.setCorreo("daniela@rinconsur.cl");
        req401.setPassword("Incorrecta!");

        when(service.login("daniela@rinconsur.cl", "Incorrecta!"))
                .thenThrow(new IllegalArgumentException("Contraseña incorrecta."));
        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req401)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Contraseña incorrecta."));

        LoginRequest req403 = new LoginRequest();
        req403.setCorreo("inactivo@rinconsur.cl");
        req403.setPassword("Daniela123!");

        when(service.login("inactivo@rinconsur.cl", "Daniela123!"))
                .thenThrow(new IllegalStateException("Tu cuenta está deshabilitada."));
        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req403)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Tu cuenta está deshabilitada."));
    }

    @Test
    @DisplayName("POST /api/usuarios/colaboradores y /{id}/clave-temporal deben retornar 200")
    void crearColaboradorYClaveTemporal() throws Exception {
        ColaboradorRequest req = new ColaboradorRequest();
        req.setNombre("Renata Valdés");
        req.setCorreo("renata@gmail.com");
        req.setRut("21045678-3");
        req.setPerfil("P-04");
        req.setRol("OBSERVADOR");

        when(service.crearColaboradorConClaveTemporal("Renata Valdés", "renata@gmail.com", "21045678-3", "P-04", "OBSERVADOR"))
                .thenReturn(Map.of("id", 2L, "correo", "renata@gmail.com", "mensaje", "Enviado"));

        mockMvc.perform(post("/api/usuarios/colaboradores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("renata@gmail.com"));

        when(service.generarClaveTemporal(2L, 1L))
                .thenReturn(Map.of("correo", "renata@gmail.com", "mensaje", "Clave temporal enviada"));

        mockMvc.perform(post("/api/usuarios/2/clave-temporal")
                .param("idSolicitante", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("renata@gmail.com"));
    }

    @Test
    @DisplayName("POST /api/usuarios/cambiar-clave debe retornar 200")
    void cambiarClave_debeRetornar200() throws Exception {
        CambiarClaveRequest req = new CambiarClaveRequest();
        req.setCorreo("daniela@rinconsur.cl");
        req.setClaveActual("Daniela123!");
        req.setClaveNueva("NuevaClave2026!");
        req.setEsPrimerInicio(false);

        doNothing().when(service).cambiarClave("daniela@rinconsur.cl", "Daniela123!", "NuevaClave2026!", false);

        mockMvc.perform(post("/api/usuarios/cambiar-clave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña actualizada correctamente."));
    }

    @Test
    @DisplayName("PATCH activo, perfil, datos y perfil-propio deben retornar 200")
    void endpointsPatch_debenRetornar200() throws Exception {
        usuario.setActivo(false);
        when(service.cambiarEstadoActivo(2L, 1L, false)).thenReturn(usuario);
        mockMvc.perform(patch("/api/usuarios/2/activo")
                .param("idSolicitante", "1")
                .param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        usuario.setPerfil("P-02");
        when(service.cambiarPerfil(2L, 1L, "P-02", "SUPERVISOR")).thenReturn(usuario);
        mockMvc.perform(patch("/api/usuarios/2/perfil")
                .param("idSolicitante", "1")
                .param("perfil", "P-02")
                .param("rol", "SUPERVISOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("P-02"));

        ActualizarDatosRequest datos = new ActualizarDatosRequest();
        datos.setNombre("Daniela Editada");
        datos.setCorreo("nuevo@gmail.com");
        usuario.setCorreo("nuevo@gmail.com");
        when(service.actualizarDatos(1L, "Daniela Editada", "nuevo@gmail.com")).thenReturn(usuario);
        mockMvc.perform(patch("/api/usuarios/1/datos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("nuevo@gmail.com"));

        PerfilPropioRequest perfil = new PerfilPropioRequest();
        perfil.setCorreo("daniela@rinconsur.cl");
        perfil.setDescripcion("Hola");
        perfil.setAvatarBase64("AAAA");
        usuario.setDescripcion("Hola");
        when(service.actualizarPerfilPropio("daniela@rinconsur.cl", "Hola", "AAAA")).thenReturn(usuario);
        mockMvc.perform(patch("/api/usuarios/perfil-propio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(perfil)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Hola"));
    }

    @Test
    @DisplayName("POST /recuperar/solicitar, /verificar y /confirmar deben retornar 200")
    void endpointsRecuperacion_debenRetornar200() throws Exception {
        RecuperacionRequest req = new RecuperacionRequest();
        req.setCorreo("daniela@rinconsur.cl");
        req.setCodigo("123456");
        req.setNuevaClave("NuevaClave2026!");

        when(service.solicitarRecuperacionNeutral("daniela@rinconsur.cl"))
                .thenReturn(Map.of("mensaje", "Mensaje neutro"));
        doNothing().when(service).verificarCodigoRecuperacion("daniela@rinconsur.cl", "123456");
        doNothing().when(service).confirmarRecuperacion("daniela@rinconsur.cl", "123456", "NuevaClave2026!");

        mockMvc.perform(post("/api/usuarios/recuperar/solicitar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Mensaje neutro"));

        mockMvc.perform(post("/api/usuarios/recuperar/verificar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Código válido."));

        mockMvc.perform(post("/api/usuarios/recuperar/confirmar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña restablecida con éxito."));
    }
}
