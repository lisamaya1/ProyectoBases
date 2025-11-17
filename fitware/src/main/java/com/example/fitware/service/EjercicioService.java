package com.example.fitware.service;
import com.example.fitware.domain.Ejercicio;
import java.util.List;


public interface EjercicioService {
Ejercicio create(Ejercicio e);
List<Ejercicio> findAll();
Ejercicio findById(Integer id);
Ejercicio update(Integer id, Ejercicio e);
void delete(Integer id);
}
