package com.example.fitware.service;

import com.example.fitware.domain.Ejercicio;
import com.example.fitware.repository.EjercicioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class EjercicioServiceImpl implements EjercicioService {

    private final EjercicioRepository repo;
    private final Set<String> tiposPermitidos;
    private final Map<String, String> mapaNormalizado;

    public EjercicioServiceImpl(EjercicioRepository repo) {
        this.repo = repo;
        this.tiposPermitidos = Set.of("Cardio", "Fuerza", "Resistencia", "Movilidad", "Flexibilidad", "HIIT", "Equilibrio", "Potencia");
        this.mapaNormalizado = tiposPermitidos.stream()
            .collect(Collectors.toMap(valor -> valor.toLowerCase(), valor -> valor));
    }

    @Override
    public Ejercicio create(Ejercicio e) {
        if (e == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos del ejercicio no enviados");
        }
        validar(e);
        e.setId(null);
        return repo.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ejercicio> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Ejercicio findById(Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ejercicio no encontrado"));
    }

    @Override
    public Ejercicio update(Integer id, Ejercicio e) {
        Ejercicio db = findById(id);
        if (e == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos del ejercicio no enviados");
        }
        validar(e);
        db.setNombre(e.getNombre());
        db.setDescripcion(e.getDescripcion());
        db.setTipo(e.getTipo());
        db.setEquipamiento(e.getEquipamiento());
        return repo.save(db);
    }

    @Override
    public void delete(Integer id) {
        repo.delete(findById(id));
    }

    private void validar(Ejercicio ejercicio) {
        if (ejercicio.getNombre() == null || ejercicio.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (ejercicio.getDescripcion() == null || ejercicio.getDescripcion().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripción es obligatoria");
        }
        if (ejercicio.getTipo() == null || ejercicio.getTipo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo es obligatorio");
        }
        if (ejercicio.getEquipamiento() == null || ejercicio.getEquipamiento().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipamiento es obligatorio");
        }

        ejercicio.setNombre(normalizarCapitalizacion(ejercicio.getNombre()));
        ejercicio.setDescripcion(ejercicio.getDescripcion().trim());
        ejercicio.setEquipamiento(normalizarCapitalizacion(ejercicio.getEquipamiento()));

        String tipoNormalizado = mapaNormalizado.getOrDefault(ejercicio.getTipo().trim().toLowerCase(), null);
        if (tipoNormalizado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Tipo de ejercicio inválido. Use uno de: " + String.join(", ", tiposPermitidos));
        }
        ejercicio.setTipo(tipoNormalizado);
    }

    private String normalizarCapitalizacion(String valor) {
        String trimmed = valor.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}
