package com.example.fitware.domain;
import jakarta.persistence.*;


@Entity
@Table(name = "alimento")
public class Alimento {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;
private String nombre;
private Double calorias;
@Column(name = "macros_proteinas")
private Double macrosProteinas;
@Column(name = "macros_carbs")
private Double macrosCarbs;
@Column(name = "macros_grasas")
private Double macrosGrasas;
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
public Double getCalorias() {
    return calorias;
}
public void setCalorias(Double calorias) {
    this.calorias = calorias;
}
public Double getMacrosProteinas() {
    return macrosProteinas;
}
public void setMacrosProteinas(Double macrosProteinas) {
    this.macrosProteinas = macrosProteinas;
}
public Double getMacrosCarbs() {
    return macrosCarbs;
}
public void setMacrosCarbs(Double macrosCarbs) {
    this.macrosCarbs = macrosCarbs;
}
public Double getMacrosGrasas() {
    return macrosGrasas;
}
public void setMacrosGrasas(Double macrosGrasas) {
    this.macrosGrasas = macrosGrasas;
}
}
