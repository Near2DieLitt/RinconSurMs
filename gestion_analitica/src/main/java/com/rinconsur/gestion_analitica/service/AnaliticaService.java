package com.rinconsur.gestion_analitica.service;


import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

import com.rinconsur.gestion_analitica.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_analitica.model.RegistroFlujo;
import com.rinconsur.gestion_analitica.repository.RegistroFlujoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnaliticaService {

    private static final Set<String> ACCESOS_VALIDOS = Set.of("A1", "A2", "A3", "A4");
    private final RegistroFlujoRepository flujoRep;

    public List<RegistroFlujo> obtenerPorRango(String desde, String hasta, boolean incluirInactivos) {
        if (parsear(desde).isAfter(parsear(hasta))) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'.");
        }
        return incluirInactivos
                ? flujoRep.findByFechaBetweenOrderByFechaDesc(desde, hasta)
                : flujoRep.findByFechaBetweenAndActivoTrueOrderByFechaDesc(desde, hasta);
    }

    public RegistroFlujo registrar(RegistroFlujo r) {
        if (r.getAcceso() == null || !ACCESOS_VALIDOS.contains(r.getAcceso().trim().toUpperCase())) {
            throw new IllegalArgumentException("El acceso debe ser A1, A2, A3 o A4.");
        }
        parsear(r.getFecha());
        if (r.getIngresos() < 0 || r.getCompradores() < 0 || r.getTransitoJoven() < 0 || r.getTransitoAdulto() < 0 || r.getTransitoMayor() < 0) {
            throw new IllegalArgumentException("Los valores no pueden ser negativos.");
        }
        if (r.getIngresos() == 0 && r.getCompradores() == 0) {
            throw new IllegalArgumentException("El total de ingresos debe ser mayor a 0.");
        }
        if (r.getIngresos() > 0 && r.getCompradores() > r.getIngresos()) {
            throw new IllegalArgumentException("Los compradores no pueden superar a los ingresos.");
        }
        int sumaEdades = r.getTransitoJoven() + r.getTransitoAdulto() + r.getTransitoMayor();
        if (r.getIngresos() > 0 && sumaEdades > r.getIngresos()) {
            throw new IllegalArgumentException("La suma por rango etario no puede superar a los ingresos.");
        }
        r.setId(null);
        r.setAcceso(r.getAcceso().trim().toUpperCase());
        r.setActivo(true);
        return flujoRep.save(r);
    }

    public int registrarLote(List<RegistroFlujo> lista) {
        if (flujoRep.count() > 0) {
            return 0;
        }
        int guardados = 0;
        for (RegistroFlujo r : lista) {
            registrar(r);
            guardados++;
        }
        return guardados;
    }

    public RegistroFlujo cambiarActivo(Long id, boolean activo) {
        RegistroFlujo r = flujoRep.findById(id)
                .or(() -> flujoRep.findFirstByIdLocalOrderByIdDesc(id))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el registro de flujo con ID " + id));
        r.setActivo(activo);
        return flujoRep.save(r);
    }

    private LocalDate parsear(String fecha) {
        try {
            return LocalDate.parse(fecha == null ? "" : fecha.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha debe tener formato aaaa-mm-dd.");
        }
    }
}
