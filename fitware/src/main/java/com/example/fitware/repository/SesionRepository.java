package com.example.fitware.repository;

import com.example.fitware.domain.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SesionRepository extends JpaRepository<Sesion, Integer> {

    // Ruta: Sesion -> cliente -> usuario -> cedula
    List<Sesion> findByCliente_Usuario_CedulaOrderByFechaInicioDesc(String cedula);
}
