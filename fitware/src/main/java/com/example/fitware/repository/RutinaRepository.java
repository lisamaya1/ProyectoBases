package com.example.fitware.repository;

import com.example.fitware.domain.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RutinaRepository extends JpaRepository<Rutina, Integer> {

    // Ruta correcta: rutina -> entrenador (Entidad) -> cedula (PK heredada de Usuario)
    List<Rutina> findByEntrenador_Cedula(String cedula);

    // (Opcional) Si quieres las más recientes primero y tienes fecha:
    // List<Rutina> findByEntrenador_CedulaOrderByFechaDesc(String cedula);
}
