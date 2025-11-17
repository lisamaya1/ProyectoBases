package com.example.fitware.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "plan_alimentacion")
public class PlanAlimentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rutina_id")
    private Rutina rutina;

    private String nombre;

    private String descripcion;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Rutina getRutina() {
        return rutina;
    }

    public void setRutina(Rutina rutina) {
        this.rutina = rutina;
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

    public Integer getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    @JsonProperty("rutinaId")
    public Integer getRutinaId() {
        return rutina != null ? rutina.getId() : null;
    }

    @JsonProperty("rutinaNombre")
    public String getRutinaNombre() {
        return rutina != null ? rutina.getNombre() : null;
    }
}
