package com.rinconsur.gestion_visitantes.service;


import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.rinconsur.gestion_visitantes.exception.RecursoNoEncontradoException;
import com.rinconsur.gestion_visitantes.model.Visitante;
import com.rinconsur.gestion_visitantes.repository.VisitanteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitanteService {

    private static final Set<String> ACCESOS_VALIDOS = Set.of("A1", "A2", "A3", "A4");
    private static final Set<String> RANGOS_VALIDOS = Set.of("NINO", "JOVEN", "ADULTO", "PERSONA_MAYOR", "NO_DETERMINADO");

    private final VisitanteRepository visitanteRep;

    public List<Visitante> listarEnTienda(String fecha, Long idSesion) {
        return visitanteRep.findByFechaAndEstadoOrderByIdAsc(fecha, "En_Tienda").stream()
                .filter(v -> idSesion == null || idSesion.equals(v.getIdSesion()))
                .toList();
    }

    public Map<String, Integer> obtenerResumen(String fecha, Long idSesion) {
        List<Visitante> todos = visitanteRep.findByFechaOrderByIdAsc(fecha).stream()
                .filter(v -> idSesion == null || idSesion.equals(v.getIdSesion()))
                .toList();
        int enTienda = (int) todos.stream().filter(v -> "En_Tienda".equals(v.getEstado())).count();
        int compro = (int) todos.stream().filter(v -> "Compro".equals(v.getEstado())).count();
        int noCompro = (int) todos.stream().filter(v -> "No_Compro".equals(v.getEstado())).count();
        return Map.of("enTienda", enTienda, "compro", compro, "noCompro", noCompro, "total", todos.size());
    }

    public Visitante registrarEntrada(Visitante v) {
        if (v.getAcceso() == null || !ACCESOS_VALIDOS.contains(v.getAcceso().trim().toUpperCase())) {
            throw new IllegalArgumentException("El acceso debe ser A1, A2, A3 o A4.");
        }
        if (v.getRangoEtario() == null || !RANGOS_VALIDOS.contains(v.getRangoEtario().trim().toUpperCase())) {
            throw new IllegalArgumentException("El rango etario no es válido.");
        }
        if (v.getFecha() == null || v.getFecha().isBlank()) {
            throw new IllegalArgumentException("La fecha del visitante es obligatoria.");
        }
        if (v.getHora() == null || v.getHora().isBlank()) {
            throw new IllegalArgumentException("La hora del visitante es obligatoria.");
        }
        long correlativo = visitanteRep.countByFecha(v.getFecha()) + 1;
        v.setId(null);
        v.setAcceso(v.getAcceso().trim().toUpperCase());
        v.setRangoEtario(v.getRangoEtario().trim().toUpperCase());
        v.setCodigo(String.format(Locale.US, "VIS-%03d", correlativo));
        v.setEstado("En_Tienda");
        v.setFolioBoleta(null);
        return visitanteRep.save(v);
    }

    public Visitante registrarSalidaEspecificaSinCompra(Long idVisitante) {
        Visitante v = buscar(idVisitante);
        if (!"En_Tienda".equals(v.getEstado())) {
            throw new IllegalArgumentException("Este visitante ya no está en la tienda.");
        }
        v.setEstado("No_Compro");
        return visitanteRep.save(v);
    }

    public Visitante vincularBoletaAVisitanteEspecifico(Long idVisitante, String folio) {
        if (folio == null || !folio.trim().matches("^[0-9]{6,10}$")) {
            throw new IllegalArgumentException("FORMATO_INVALIDO: El folio debe contener entre 6 y 10 dígitos numéricos.");
        }
        if (visitanteRep.existsByFolioBoletaIgnoreCase(folio.trim())) {
            throw new IllegalArgumentException("YA_PROCESADA: Esta boleta ya fue procesada anteriormente.");
        }
        Visitante v = buscar(idVisitante);
        if (!"En_Tienda".equals(v.getEstado())) {
            throw new IllegalArgumentException("SIN_VISITANTE: Este visitante ya no está en la tienda.");
        }
        v.setEstado("Compro");
        v.setFolioBoleta(folio.trim());
        try {
            return visitanteRep.save(v);
        } catch (DataIntegrityViolationException e) {

            throw new IllegalArgumentException("YA_PROCESADA: Esta boleta ya fue procesada anteriormente.");
        }
    }

    public int ejecutarCierreDiario(String fecha, Long idSesion) {
        List<Visitante> activos = listarEnTienda(fecha, idSesion);
        for (Visitante v : activos) {
            v.setEstado("No_Compro");
            visitanteRep.save(v);
        }
        return activos.size();
    }

    private Visitante buscar(Long id) {
        return visitanteRep.findById(id)
                .or(() -> visitanteRep.findFirstByIdLocalOrderByIdDesc(id))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el visitante con ID " + id));
    }
}
