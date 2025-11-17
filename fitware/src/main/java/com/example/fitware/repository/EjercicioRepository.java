package com.example.fitware.repository;


import com.example.fitware.domain.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EjercicioRepository extends JpaRepository<Ejercicio,Integer> {}