package com.example.fitware.repository;

import com.example.fitware.domain.Progreso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgresoRepository extends JpaRepository<Progreso, Integer> {

    // Ruta: Progreso -> cliente -> usuario -> cedula
    List<Progreso> findByCliente_Usuario_CedulaOrderByFechaDesc(String cedula);

    void deleteByCliente_Usuario_Cedula(String cedula);
}
