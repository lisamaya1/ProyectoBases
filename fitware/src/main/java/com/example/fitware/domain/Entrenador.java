package com.example.fitware.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "entrenador") // permitido con JOINED
@PrimaryKeyJoinColumn(name = "usuario_cedula", referencedColumnName = "cedula")
public class Entrenador extends Usuario {

    @Column(name = "especializacion")
    private String especializacion;

    public String getEspecializacion() {
        return especializacion;
    }
    public void setEspecializacion(String especializacion) {
        this.especializacion = especializacion;
    }

    // Accesores alternos para exponer/recibir la cédula como "usuarioCedula" en JSON
    @JsonProperty("usuarioCedula")
    public String getUsuarioCedula() {
        return getCedula();
    }

    @JsonProperty("usuarioCedula")
    public void setUsuarioCedula(String cedula) {
        setCedula(cedula);
    }
}
