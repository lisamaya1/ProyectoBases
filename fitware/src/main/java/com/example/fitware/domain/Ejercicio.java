package com.example.fitware.domain;
import jakarta.persistence.*;


@Entity
@Table(name = "ejercicio")
public class Ejercicio {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;
private String nombre;
private String descripcion;
private String tipo;
private String equipamiento;
// getters/setters
public Integer getId() {
    return id;
}
public void setId(Integer id) {
    this.id = id;
}
public String getNombre() {
    return nombre;
}
public void setNombre(String nombre) {
    this.nombre = nombre;
}
public String getDescripcion() {
    return descripcion;
}
public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
}
public String getTipo() {
    return tipo;
}
public void setTipo(String tipo) {
    this.tipo = tipo;
}
public String getEquipamiento() {
    return equipamiento;
}
public void setEquipamiento(String equipamiento) {
    this.equipamiento = equipamiento;
}
}