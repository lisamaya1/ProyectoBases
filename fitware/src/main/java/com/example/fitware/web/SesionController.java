package com.example.fitware.web;

import com.example.fitware.domain.Sesion;
import com.example.fitware.domain.SesionEjercicios;
import com.example.fitware.service.SesionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sesiones")
public class SesionController {

    private final SesionService service;

    public SesionController(SesionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Sesion>> listar(@RequestParam(required = false) String clienteCedula) {
        return ResponseEntity.ok(service.listarSesiones(clienteCedula));
    }

    @GetMapping("/{sesionId}")
    public ResponseEntity<Sesion> obtener(@PathVariable Integer sesionId) {
        return ResponseEntity.ok(service.obtenerSesion(sesionId));
    }

    @PostMapping("/{clienteCedula}")
    public ResponseEntity<Sesion> crear(@PathVariable String clienteCedula, @RequestBody Sesion s) {
        Sesion created = service.crearSesion(clienteCedula, s);
        return ResponseEntity.created(URI.create("/api/sesiones/" + created.getId())).body(created);
    }

    @PutMapping("/{sesionId}")
    public ResponseEntity<Sesion> actualizar(@PathVariable Integer sesionId,
                                             @RequestParam(required = false) String clienteCedula,
                                             @RequestBody Sesion s) {
        return ResponseEntity.ok(service.actualizarSesion(sesionId, clienteCedula, s));
    }

    @DeleteMapping("/{sesionId}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer sesionId) {
        service.eliminarSesion(sesionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sesionId}/ejercicios")
    public ResponseEntity<List<SesionEjercicios>> listarEjercicios(@PathVariable Integer sesionId) {
        return ResponseEntity.ok(service.listarEjercicios(sesionId));
    }

    @GetMapping("/ejercicios/{id}")
    public ResponseEntity<SesionEjercicios> obtenerEjercicio(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerSesionEjercicio(id));
    }

    @PostMapping("/{sesionId}/ejercicios/{ejercicioId}")
    public ResponseEntity<SesionEjercicios> asignar(@PathVariable Integer sesionId,
                                                    @PathVariable Integer ejercicioId,
                                                    @RequestParam Integer repeticiones,
                                                    @RequestParam Integer series,
                                                    @RequestParam String estado) {
        SesionEjercicios created = service.asignarEjercicio(sesionId, ejercicioId, repeticiones, series, estado);
        return ResponseEntity.created(URI.create("/api/sesiones/ejercicios/" + created.getId())).body(created);
    }

    @PutMapping("/ejercicios/{id}")
    public ResponseEntity<SesionEjercicios> actualizarAsignacion(@PathVariable Integer id,
                                                                 @RequestParam(required = false) Integer ejercicioId,
                                                                 @RequestParam(required = false) Integer repeticiones,
                                                                 @RequestParam(required = false) Integer series,
                                                                 @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(service.actualizarSesionEjercicio(id, ejercicioId, repeticiones, series, estado));
    }

    @DeleteMapping("/ejercicios/{id}")
    public ResponseEntity<Void> eliminarAsignacion(@PathVariable Integer id) {
        service.eliminarSesionEjercicio(id);
        return ResponseEntity.noContent().build();
    }
}
