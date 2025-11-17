package com.example.fitware.repository;

import com.example.fitware.domain.Alimento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlimentoRepository extends JpaRepository<Alimento, Integer> {}