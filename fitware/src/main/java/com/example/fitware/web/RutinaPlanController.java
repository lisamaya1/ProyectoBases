package com.example.fitware.web;

import com.example.fitware.domain.PlanAlimentacion;
import com.example.fitware.domain.PlanAlimentos;
import com.example.fitware.domain.Rutina;
import com.example.fitware.service.RutinaPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rutinas")
public class RutinaPlanController {

    private final RutinaPlanService service;

    public RutinaPlanController(RutinaPlanService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Rutina>> listarRutinas(@RequestParam(required = false) String entrenadorCedula) {
        return ResponseEntity.ok(service.listarRutinas(entrenadorCedula));
    }

    @GetMapping("/{rutinaId}")
    public ResponseEntity<Rutina> obtenerRutina(@PathVariable Integer rutinaId) {
        return ResponseEntity.ok(service.obtenerRutina(rutinaId));
    }

    @PostMapping("/{entrenadorCedula}")
    public ResponseEntity<Rutina> crearRutina(@PathVariable String entrenadorCedula, @RequestBody Rutina r) {
        Rutina created = service.crearRutina(entrenadorCedula, r);
        return ResponseEntity.created(URI.create("/api/rutinas/" + created.getId())).body(created);
    }

    @PutMapping("/{rutinaId}")
    public ResponseEntity<Rutina> actualizarRutina(@PathVariable Integer rutinaId,
                                                   @RequestParam(required = false) String entrenadorCedula,
                                                   @RequestBody Rutina r) {
        return ResponseEntity.ok(service.actualizarRutina(rutinaId, entrenadorCedula, r));
    }

    @DeleteMapping("/{rutinaId}")
    public ResponseEntity<Void> eliminarRutina(@PathVariable Integer rutinaId) {
        service.eliminarRutina(rutinaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/planes")
    public ResponseEntity<List<PlanAlimentacion>> listarPlanes(@RequestParam(required = false) Integer rutinaId) {
        return ResponseEntity.ok(service.listarPlanes(rutinaId));
    }

    @GetMapping("/planes/{planId}")
    public ResponseEntity<PlanAlimentacion> obtenerPlan(@PathVariable Integer planId) {
        return ResponseEntity.ok(service.obtenerPlan(planId));
    }

    @PostMapping("/{rutinaId}/planes")
    public ResponseEntity<PlanAlimentacion> crearPlan(@PathVariable Integer rutinaId, @RequestBody PlanAlimentacion p) {
        PlanAlimentacion created = service.crearPlan(rutinaId, p);
        return ResponseEntity.created(URI.create("/api/rutinas/planes/" + created.getId())).body(created);
    }

    @PutMapping("/planes/{planId}")
    public ResponseEntity<PlanAlimentacion> actualizarPlan(@PathVariable Integer planId,
                                                           @RequestParam(required = false) Integer rutinaId,
                                                           @RequestBody PlanAlimentacion p) {
        return ResponseEntity.ok(service.actualizarPlan(planId, rutinaId, p));
    }

    @DeleteMapping("/planes/{planId}")
    public ResponseEntity<Void> eliminarPlan(@PathVariable Integer planId) {
        service.eliminarPlan(planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/planes/{planId}/alimentos")
    public ResponseEntity<List<PlanAlimentos>> listarAlimentos(@PathVariable Integer planId) {
        return ResponseEntity.ok(service.listarAlimentos(planId));
    }

    @GetMapping("/planes/alimentos/{id}")
    public ResponseEntity<PlanAlimentos> obtenerAlimento(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerPlanAlimentos(id));
    }

    @PostMapping("/planes/{planId}/alimentos/{alimentoId}")
    public ResponseEntity<PlanAlimentos> agregarAlimento(@PathVariable Integer planId,
                                                         @PathVariable Integer alimentoId,
                                                         @RequestParam String cantidad,
                                                         @RequestParam String comida) {
        PlanAlimentos created = service.agregarAlimento(planId, alimentoId, cantidad, comida);
        return ResponseEntity.created(URI.create("/api/rutinas/planes/alimentos/" + created.getId())).body(created);
    }

    @PutMapping("/planes/alimentos/{id}")
    public ResponseEntity<PlanAlimentos> actualizarAlimento(@PathVariable Integer id,
                                                            @RequestParam(required = false) Integer planId,
                                                            @RequestParam(required = false) Integer alimentoId,
                                                            @RequestBody PlanAlimentos pa) {
        return ResponseEntity.ok(service.actualizarPlanAlimentos(id, planId, alimentoId, pa));
    }

    @DeleteMapping("/planes/alimentos/{id}")
    public ResponseEntity<Void> eliminarAlimento(@PathVariable Integer id) {
        service.eliminarPlanAlimentos(id);
        return ResponseEntity.noContent().build();
    }
}
