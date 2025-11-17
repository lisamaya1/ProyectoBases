package com.example.fitware.domain;
import jakarta.persistence.*;
import java.time.LocalDate;


@Entity
@Table(name = "progreso")
public class Progreso {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;


@ManyToOne(optional = false)
@JoinColumn(name = "cliente_cedula")
private Cliente cliente;


private LocalDate fecha;
private Double peso;
private Double imc;
private String observaciones;
// getters/setters
public Integer getId() {
    return id;
}
public void setId(Integer id) {
    this.id = id;
}
public Cliente getCliente() {
    return cliente;
}
public void setCliente(Cliente cliente) {
    this.cliente = cliente;
}
public LocalDate getFecha() {
    return fecha;
}
public void setFecha(LocalDate fecha) {
    this.fecha = fecha;
}
public Double getPeso() {
    return peso;
}
public void setPeso(Double peso) {
    this.peso = peso;
}
public Double getImc() {
    return imc;
}
public void setImc(Double imc) {
    this.imc = imc;
}
public String getObservaciones() {
    return observaciones;
}
public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
}
}