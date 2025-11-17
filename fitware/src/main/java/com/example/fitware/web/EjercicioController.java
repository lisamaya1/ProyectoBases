package com.example.fitware.web;

import com.example.fitware.domain.Ejercicio;
import com.example.fitware.service.EjercicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ejercicios")
public class EjercicioController {

    private final EjercicioService service;

    public EjercicioController(EjercicioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Ejercicio> create(@RequestBody Ejercicio e) {
        Ejercicio created = service.create(e);
        return ResponseEntity.created(URI.create("/api/ejercicios/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Ejercicio>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ejercicio> one(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ejercicio> update(@PathVariable Integer id, @RequestBody Ejercicio e) {
        return ResponseEntity.ok(service.update(id, e));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
