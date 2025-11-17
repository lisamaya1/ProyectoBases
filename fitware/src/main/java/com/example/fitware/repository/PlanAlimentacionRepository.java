

package com.example.fitware.repository;

import com.example.fitware.domain.PlanAlimentacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanAlimentacionRepository extends JpaRepository<PlanAlimentacion, Integer> {
    List<PlanAlimentacion> findByRutina_Id(Integer rutinaId);
}
