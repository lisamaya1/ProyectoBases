package com.example.fitware.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "sesion_ejercicios",
    uniqueConstraints = @UniqueConstraint(columnNames = {"sesion_id", "ejercicio_id"})
)
public class SesionEjercicios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "sesion_id")
    private Sesion sesion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ejercicio_id")
    private Ejercicio ejercicio;

    private Integer repeticiones;

    private Integer series;

    @Column(length = 20)
    private String estado;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Sesion getSesion() {
        return sesion;
    }

    public void setSesion(Sesion sesion) {
        this.sesion = sesion;
    }

    public Ejercicio getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(Ejercicio ejercicio) {
        this.ejercicio = ejercicio;
    }

    public Integer getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(Integer repeticiones) {
        this.repeticiones = repeticiones;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado == null ? null : estado.trim();
    }

    @JsonProperty("sesionId")
    public Integer getSesionId() {
        return sesion != null ? sesion.getId() : null;
    }

    @JsonProperty("ejercicioId")
    public Integer getEjercicioId() {
        return ejercicio != null ? ejercicio.getId() : null;
    }

    @JsonProperty("ejercicioNombre")
    public String getEjercicioNombre() {
        return ejercicio != null ? ejercicio.getNombre() : null;
    }
}
