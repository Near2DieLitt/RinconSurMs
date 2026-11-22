package com.rinconsur.gestion_usuarios.client;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NotificacionesClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notificaciones.url:http://localhost:8083}")
    private String baseUrl;

    /** Devuelve true si el microservicio de correos acepto el envio. */
    public boolean enviarCorreo(String destinatario, String asunto, String nombre, String codigoOClave, String tipo) {
        try {
            restTemplate.postForEntity(
                    baseUrl + "/api/notificaciones/correo",
                    Map.of(
                            "destinatario", destinatario,
                            "asunto", asunto,
                            "nombreColaborador", nombre,
                            "codigoOClave", codigoOClave,
                            "tipo", tipo
                    ),
                    Map.class
            );
            return true;
        } catch (Exception e) {
            log.error("[NotificacionesClient] No se pudo enviar el correo a {}: {}", destinatario, e.getMessage());
            return false;
        }
    }
}
