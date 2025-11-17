package com.example.fitware.service;

import com.example.fitware.domain.Cliente;
import com.example.fitware.domain.Ejercicio;
import com.example.fitware.domain.Sesion;
import com.example.fitware.domain.SesionEjercicios;
import com.example.fitware.repository.ClienteRepository;
import com.example.fitware.repository.EjercicioRepository;
import com.example.fitware.repository.SesionEjerciciosRepository;
import com.example.fitware.repository.SesionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class SesionService {

    // Estados EXACTOS que tu BD acepta hoy (no tocamos la BD)
    // Si tu CHECK permite más, añádelos aquí.
    private static final Set<String> ESTADOS_BD = Set.of(
        "Completada", "Pendiente"
    );

    private final SesionRepository sRepo;
    private final SesionEjerciciosRepository seRepo;
    private final ClienteRepository cRepo;
    private final EjercicioRepository eRepo;

    public SesionService(SesionRepository s,
                         SesionEjerciciosRepository se,
                         ClienteRepository c,
                         EjercicioRepository e) {
        this.sRepo = s;
        this.seRepo = se;
        this.cRepo = c;
        this.eRepo = e;
    }

    public Sesion crearSesion(String clienteCedula, Sesion s) {
        if (s == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos de la sesión son obligatorios");
        }
        Cliente cliente = obtenerCliente(clienteCedula);
        Sesion nueva = new Sesion();
        nueva.setCliente(cliente);
        copiarDatosSesion(nueva, s); // normaliza y valida
        return sRepo.save(nueva);
    }

    @Transactional(readOnly = true)
    public List<Sesion> listarSesiones(String clienteCedula) {
        if (clienteCedula == null || clienteCedula.isBlank()) {
            return sRepo.findAll();
        }
        verificarCliente(clienteCedula);
        // Ruta: sesion -> cliente -> usuario -> cedula
        return sRepo.findByCliente_Usuario_CedulaOrderByFechaInicioDesc(clienteCedula);
    }

    @Transactional(readOnly = true)
    public Sesion obtenerSesion(Integer id) {
        return sRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión no encontrada"));
    }

    public Sesion actualizarSesion(Integer id, String clienteCedula, Sesion data) {
        if (data == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos de la sesión son obligatorios");
        }
        Sesion db = obtenerSesion(id);

        if (clienteCedula != null && !clienteCedula.isBlank()
            && !Objects.equals(db.getCliente().getUsuarioCedula(), clienteCedula)) {
            db.setCliente(obtenerCliente(clienteCedula));
        }

        copiarDatosSesion(db, data); // normaliza y valida
        return sRepo.save(db);
    }

    public void eliminarSesion(Integer id) {
        Sesion db = obtenerSesion(id);
        if (db.getId() != null) {
            seRepo.deleteBySesion_Id(db.getId()); // borra asignaciones de ejercicios
        }
        sRepo.delete(db);
    }

    public SesionEjercicios asignarEjercicio(Integer sesionId,
                                             Integer ejercicioId,
                                             Integer reps,
                                             Integer series,
                                             String estado) {
        Sesion s = obtenerSesion(sesionId);
        Ejercicio e = eRepo.findById(ejercicioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ejercicio no encontrado"));

        if (reps == null || reps <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las repeticiones deben ser mayores a cero");
        }
        if (series == null || series <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las series deben ser mayores a cero");
        }

        String estadoNormalizado = normalizarEstadoSesionEjercicio(estado); // 'Completado' | 'Pendiente'

        SesionEjercicios se = new SesionEjercicios();
        se.setSesion(s);
        se.setEjercicio(e);
        se.setRepeticiones(reps);
        se.setSeries(series);
        se.setEstado(estadoNormalizado);
        return seRepo.save(se);
    }

    @Transactional(readOnly = true)
    public List<SesionEjercicios> listarEjercicios(Integer sesionId) {
        if (sesionId == null) {
            return seRepo.findAll();
        }
        obtenerSesion(sesionId); // valida existencia
        return seRepo.findBySesion_Id(sesionId);
    }

    @Transactional(readOnly = true)
    public SesionEjercicios obtenerSesionEjercicio(Integer id) {
        return seRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignación no encontrada"));
    }

    public SesionEjercicios actualizarSesionEjercicio(Integer id,
                                                      Integer ejercicioId,
                                                      Integer repeticiones,
                                                      Integer series,
                                                      String estado) {
        SesionEjercicios db = obtenerSesionEjercicio(id);

        if (ejercicioId != null && !Objects.equals(db.getEjercicio().getId(), ejercicioId)) {
            Ejercicio ejercicio = eRepo.findById(ejercicioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ejercicio no encontrado"));
            db.setEjercicio(ejercicio);
        }

        if (repeticiones != null) {
            if (repeticiones <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las repeticiones deben ser mayores a cero");
            }
            db.setRepeticiones(repeticiones);
        }

        if (series != null) {
            if (series <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las series deben ser mayores a cero");
            }
            db.setSeries(series);
        }

        if (estado != null && !estado.isBlank()) {
            db.setEstado(normalizarEstadoSesionEjercicio(estado));
        }

        return seRepo.save(db);
    }

    public void eliminarSesionEjercicio(Integer id) {
        SesionEjercicios db = obtenerSesionEjercicio(id);
        seRepo.delete(db);
    }

    // ----------------- helpers -----------------

    private void copiarDatosSesion(Sesion destino, Sesion origen) {
        validarSesion(origen);
        destino.setEstado(normalizarEstadoSesion(origen.getEstado())); // literal EXACTO que acepta la BD
        destino.setFechaInicio(origen.getFechaInicio());
        destino.setFechaFin(origen.getFechaFin());
        destino.setGastoCalorico(origen.getGastoCalorico());
    }

    private void validarSesion(Sesion sesion) {
        if (sesion.getEstado() == null || sesion.getEstado().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado de la sesión es obligatorio");
        }
        LocalDateTime inicio = sesion.getFechaInicio();
        LocalDateTime fin = sesion.getFechaFin();
        if (inicio == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de inicio es obligatoria");
        }
        if (fin != null && fin.isBefore(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha fin no puede ser anterior al inicio");
        }
        if (sesion.getGastoCalorico() != null && sesion.getGastoCalorico() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El gasto calórico no puede ser negativo");
        }

        String literalBD = normalizarEstadoSesion(sesion.getEstado());
        if (!ESTADOS_BD.contains(literalBD)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Estado inválido. Use uno de: " + String.join(", ", ESTADOS_BD));
        }
    }

    private Cliente obtenerCliente(String cedula) {
        String c = safe(cedula);
        if (c.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cédula es obligatoria");
        }
        return cRepo.findById(c)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    private void verificarCliente(String cedula) {
        obtenerCliente(cedula);
    }

    /**
     * Normaliza cualquier entrada a los literales EXACTOS que tu BD acepta hoy.
     * Aquí mapeamos variantes comunes:
     *   - cualquier “completa*” -> "Completada"
     *   - Programada / En progreso / En curso -> "Pendiente" (si tu BD no acepta esas)
     */
    private String normalizarEstadoSesion(String estado) {
        String s = estado == null ? "" : estado.trim();
        if (s.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }
        String up = s.toUpperCase().replace(' ', '_');

        // sinónimos -> mapea al literal permitido por la BD
        switch (up) {
            case "COMPLETADA":
            case "COMPLETADO":
            case "COMPLETO":
            case "FINALIZADA":
            case "FINALIZADO":
                return "Completada";
            case "PENDIENTE":
            case "PROGRAMADA":
            case "EN_PROGRESO":
            case "EN-CURSO":
            case "EN_CURSO":
                return "Pendiente";
            default:
                // si ya viene exacto y está permitido, déjalo
                if (ESTADOS_BD.contains(s)) return s;
                // De lo contrario, rechaza
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado inválido. Use uno de: " + String.join(", ", ESTADOS_BD));
        }
    }

    /** Normaliza estado de SesionEjercicios a 'Completado' | 'Pendiente' (como en BD) */
    private String normalizarEstadoSesionEjercicio(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }
        String normalized = estado.trim().toUpperCase();
        if (normalized.equals("COMPLETADO") || normalized.equals("COMPLETA") || normalized.equals("COMPLETO")) {
            return "Completado";
        }
        if (normalized.equals("PENDIENTE")) {
            return "Pendiente";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido. Use 'Completado' o 'Pendiente'");
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
