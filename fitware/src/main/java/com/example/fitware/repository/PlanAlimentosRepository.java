
package com.example.fitware.repository;
import com.example.fitware.domain.PlanAlimentos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanAlimentosRepository extends JpaRepository<PlanAlimentos, Integer> {
    List<PlanAlimentos> findByPlan_Id(Integer planId);
}
