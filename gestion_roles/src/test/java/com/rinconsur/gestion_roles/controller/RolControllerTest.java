package com.rinconsur.gestion_roles.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinconsur.gestion_roles.dto.RolRequest;
import com.rinconsur.gestion_roles.model.Rol;
import com.rinconsur.gestion_roles.service.RolService;

@WebMvcTest(RolController.class)
@DisplayName("Tests del RolController con MockMvc")
public class RolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RolService rolService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Rol rolAdmin;
    private RolRequest request;

    @BeforeEach
    void setUp() {
        rolAdmin = new Rol(1L, "P-01", "Administrador", "ADMIN", true, false, false);

        request = new RolRequest();
        request.setCodigo("P-01");
        request.setNombre("Administrador");
        request.setRolSistema("ADMIN");
        request.setPuedeGestionarUsuarios(true);
        request.setPuedeCrearSesiones(false);
        request.setPuedeHacerConteos(false);
    }

    @Test
    @DisplayName("GET /api/roles debe retornar 200 con la lista de roles")
    void listar_debeRetornar200() throws Exception {
        when(rolService.listarTodos()).thenReturn(List.of(rolAdmin));

        mockMvc.perform(get("/api/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("P-01"))
                .andExpect(jsonPath("$[0].nombre").value("Administrador"));
    }

    @Test
    @DisplayName("GET /api/roles/{codigo} debe retornar 200 cuando existe")
    void obtenerPorCodigo_debeRetornar200() throws Exception {
        when(rolService.buscarPorCodigo("P-01")).thenReturn(rolAdmin);

        mockMvc.perform(get("/api/roles/P-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("P-01"));
    }

    @Test
    @DisplayName("GET /api/roles/{codigo} debe retornar 404 cuando no existe")
    void obtenerPorCodigo_debeRetornar404() throws Exception {
        when(rolService.buscarPorCodigo("P-99"))
                .thenThrow(new IllegalArgumentException("Perfil no encontrado: P-99"));

        mockMvc.perform(get("/api/roles/P-99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Perfil no encontrado: P-99"));
    }

    @Test
    @DisplayName("POST /api/roles debe retornar 200 al crear un rol valido")
    void crear_debeRetornar200() throws Exception {
        when(rolService.guardar(any(Rol.class))).thenReturn(rolAdmin);

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("P-01"));
    }

    @Test
    @DisplayName("POST /api/roles debe retornar 400 cuando los datos son invalidos")
    void crear_debeRetornar400_cuandoDatosInvalidos() throws Exception {
        when(rolService.guardar(any(Rol.class)))
                .thenThrow(new IllegalArgumentException("El código del perfil es obligatorio"));

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RolRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El código del perfil es obligatorio"));
    }
}
