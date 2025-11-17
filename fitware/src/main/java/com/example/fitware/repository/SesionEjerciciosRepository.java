package com.example.fitware.repository;

import com.example.fitware.domain.SesionEjercicios;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SesionEjerciciosRepository extends JpaRepository<SesionEjercicios, Integer> {

    // Para listar por la FK de sesión (campo: sesion.id)
    List<SesionEjercicios> findBySesion_Id(Integer sesionId);

    // Para eliminar en cascada las asignaciones de una sesión
    void deleteBySesion_Id(Integer sesionId);
}
