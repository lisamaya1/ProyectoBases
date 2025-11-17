package com.example.fitware.service;

import com.example.fitware.domain.PlanAlimentacion;
import com.example.fitware.domain.PlanAlimentos;
import com.example.fitware.domain.Rutina;
import com.example.fitware.repository.AlimentoRepository;
import com.example.fitware.repository.EntrenadorRepository;
import com.example.fitware.repository.PlanAlimentacionRepository;
import com.example.fitware.repository.PlanAlimentosRepository;
import com.example.fitware.repository.RutinaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class RutinaPlanService {

    private final RutinaRepository rRepo;
    private final PlanAlimentacionRepository pRepo;
    private final PlanAlimentosRepository paRepo;
    private final EntrenadorRepository eRepo;
    private final AlimentoRepository aRepo;

    public RutinaPlanService(RutinaRepository r,
                             PlanAlimentacionRepository p,
                             PlanAlimentosRepository pa,
                             EntrenadorRepository e,
                             AlimentoRepository a) {
        this.rRepo = r;
        this.pRepo = p;
        this.paRepo = pa;
        this.eRepo = e;
        this.aRepo = a;
    }

    public Rutina crearRutina(String entrenadorCedula, Rutina r) {
        Rutina rutina = r != null ? r : new Rutina();
        rutina.setId(null);
        rutina.setEntrenador(obtenerEntrenador(entrenadorCedula));
        return rRepo.save(rutina);
    }

    @Transactional(readOnly = true)
    public List<Rutina> listarRutinas(String entrenadorCedula) {
        if (entrenadorCedula == null || entrenadorCedula.isBlank()) {
            return rRepo.findAll();
        }
        verificarEntrenador(entrenadorCedula);
       return rRepo.findByEntrenador_Cedula(entrenadorCedula);
    }

    @Transactional(readOnly = true)
    public Rutina obtenerRutina(Integer id) {
        return rRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rutina no encontrada"));
    }

    public Rutina actualizarRutina(Integer id, String entrenadorCedula, Rutina data) {
        Rutina db = obtenerRutina(id);
        if (entrenadorCedula != null && !entrenadorCedula.isBlank()
             && !Objects.equals(db.getEntrenador().getUsuarioCedula(), entrenadorCedula)) {
            db.setEntrenador(obtenerEntrenador(entrenadorCedula));
        }
        if (data != null) {
            if (data.getNombre() != null) {
                db.setNombre(data.getNombre());
            }
            if (data.getObjetivo() != null) {
                db.setObjetivo(data.getObjetivo());
            }
            if (data.getDuracionSemanas() != null) {
                db.setDuracionSemanas(data.getDuracionSemanas());
            }
        }
        return rRepo.save(db);
    }

    public void eliminarRutina(Integer id) {
        Rutina db = obtenerRutina(id);
        rRepo.delete(db);
    }

    public PlanAlimentacion crearPlan(Integer rutinaId, PlanAlimentacion p) {
        PlanAlimentacion plan = p == null ? new PlanAlimentacion() : p;
        plan.setId(null);
        plan.setRutina(obtenerRutina(rutinaId));
        return pRepo.save(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanAlimentacion> listarPlanes(Integer rutinaId) {
        if (rutinaId == null) {
            return pRepo.findAll();
        }
        obtenerRutina(rutinaId);
        return pRepo.findByRutina_Id(rutinaId);
    }

    @Transactional(readOnly = true)
    public PlanAlimentacion obtenerPlan(Integer id) {
        return pRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan de alimentación no encontrado"));
    }

    public PlanAlimentacion actualizarPlan(Integer id, Integer rutinaId, PlanAlimentacion data) {
        PlanAlimentacion db = obtenerPlan(id);
        if (rutinaId != null && !Objects.equals(db.getRutina().getId(), rutinaId)) {
            db.setRutina(obtenerRutina(rutinaId));
        }
        if (data != null) {
            if (data.getNombre() != null) {
                db.setNombre(data.getNombre());
            }
            if (data.getDescripcion() != null) {
                db.setDescripcion(data.getDescripcion());
            }
            if (data.getDuracionDias() != null) {
                db.setDuracionDias(data.getDuracionDias());
            }
            if (data.getFechaInicio() != null) {
                db.setFechaInicio(data.getFechaInicio());
            }
            if (data.getFechaFin() != null) {
                db.setFechaFin(data.getFechaFin());
            }
        }
        return pRepo.save(db);
    }

    public void eliminarPlan(Integer id) {
        PlanAlimentacion db = obtenerPlan(id);
        pRepo.delete(db);
    }

    public PlanAlimentos agregarAlimento(Integer planId, Integer alimentoId, String cantidad, String comida) {
        var plan = obtenerPlan(planId);
        var ali = aRepo.findById(alimentoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alimento no encontrado"));
        validarCantidad(cantidad);
        String comidaNormalizada = normalizarComida(comida);
        var pa = new PlanAlimentos();
        pa.setPlan(plan);
        pa.setAlimento(ali);
        pa.setCantidad(cantidad.trim());
        pa.setComidaDelDia(comidaNormalizada); // debe respetar el CHECK ('Desayuno','Almuerzo','Cena','Snack')
        return paRepo.save(pa);
    }

    @Transactional(readOnly = true)
    public List<PlanAlimentos> listarAlimentos(Integer planId) {
        if (planId == null) {
            return paRepo.findAll();
        }
        obtenerPlan(planId);
        return paRepo.findByPlan_Id(planId);
    }

    @Transactional(readOnly = true)
    public PlanAlimentos obtenerPlanAlimentos(Integer id) {
        return paRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento del plan no encontrado"));
    }

    public PlanAlimentos actualizarPlanAlimentos(Integer id, Integer planId, Integer alimentoId, PlanAlimentos data) {
        PlanAlimentos db = obtenerPlanAlimentos(id);
        if (planId != null && !Objects.equals(db.getPlan().getId(), planId)) {
            db.setPlan(obtenerPlan(planId));
        }
        if (alimentoId != null && !Objects.equals(db.getAlimento().getId(), alimentoId)) {
            db.setAlimento(aRepo.findById(alimentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alimento no encontrado")));
        }
        if (data != null) {
            if (data.getCantidad() != null) {
                validarCantidad(data.getCantidad());
                db.setCantidad(data.getCantidad().trim());
            }
            if (data.getComidaDelDia() != null && !data.getComidaDelDia().isBlank()) {
                db.setComidaDelDia(normalizarComida(data.getComidaDelDia()));
            }
        }
        return paRepo.save(db);
    }

    public void eliminarPlanAlimentos(Integer id) {
        PlanAlimentos db = obtenerPlanAlimentos(id);
        paRepo.delete(db);
    }

    private void verificarEntrenador(String entrenadorCedula) {
        obtenerEntrenador(entrenadorCedula);
    }

    private com.example.fitware.domain.Entrenador obtenerEntrenador(String entrenadorCedula) {
        return eRepo.findById(entrenadorCedula)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrenador no encontrado"));
    }

    private void validarCantidad(String cantidad) {
        if (cantidad == null || cantidad.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad es obligatoria");
        }
    }

    private String normalizarComida(String comida) {
        if (comida == null || comida.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La comida del día es obligatoria");
        }
        String normalized = comida.trim();
        if (!normalized.equalsIgnoreCase("Desayuno")
            && !normalized.equalsIgnoreCase("Almuerzo")
            && !normalized.equalsIgnoreCase("Cena")
            && !normalized.equalsIgnoreCase("Snack")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Comida inválida. Use Desayuno, Almuerzo, Cena o Snack");
        }
        String lower = normalized.toLowerCase();
        return switch (lower) {
            case "desayuno" -> "Desayuno";
            case "almuerzo" -> "Almuerzo";
            case "cena" -> "Cena";
            default -> "Snack";
        };
    }
}
