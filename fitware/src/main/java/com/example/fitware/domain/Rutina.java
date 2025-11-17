package com.example.fitware.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "rutina")
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // FK a Entrenador (hereda 'cedula' de Usuario)
    @ManyToOne(optional = false)
    @JoinColumn(name = "entrenador_cedula")
    private Entrenador entrenador;

    private String nombre;

    private String objetivo;

    @Column(name = "duracion_semanas")
    private Integer duracionSemanas;

    // ---------------- getters / setters ----------------

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Entrenador getEntrenador() {
        return entrenador;
    }

    public void setEntrenador(Entrenador entrenador) {
        this.entrenador = entrenador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public Integer getDuracionSemanas() {
        return duracionSemanas;
    }

    public void setDuracionSemanas(Integer duracionSemanas) {
        this.duracionSemanas = duracionSemanas;
    }

    // ---------------- helpers JSON ----------------

    @JsonProperty("entrenadorCedula")
    public String getEntrenadorCedula() {
        return (entrenador != null) ? entrenador.getCedula() : null;
    }

    // Opcional: permite enviar/recibir sólo la cédula del entrenador en JSON
    @JsonProperty("entrenadorCedula")
    public void setEntrenadorCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            this.entrenador = null;
        } else {
            Entrenador e = new Entrenador();
            e.setCedula(cedula.trim());
            this.entrenador = e;
        }
    }
}
