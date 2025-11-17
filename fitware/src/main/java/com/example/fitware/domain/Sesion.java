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
import java.time.LocalDateTime;

@Entity
@Table(name = "sesion")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_cedula")
    private Cliente cliente;

    private String estado;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "gasto_calorico")
    private Double gastoCalorico;

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado == null ? null : estado.trim();
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Double getGastoCalorico() {
        return gastoCalorico;
    }

    public void setGastoCalorico(Double gastoCalorico) {
        this.gastoCalorico = gastoCalorico;
    }

    @JsonProperty("clienteCedula")
    public String getClienteCedula() {
        return cliente != null ? cliente.getUsuarioCedula() : null;
    }



    public static String canonizarEstado(String estado2) {
        if (estado2 == null) {
            return null;
        }
        String s = estado2.trim();
        if (s.isEmpty()) {
            return null;
        }
        // Normalizar: eliminar caracteres no alfabéticos y poner en mayúsculas
        String key = s.replaceAll("[^A-Za-z]+", "_").toUpperCase();

        switch (key) {
            case "PROGRAMADA":
            case "PROGRAMADO":
                return "PROGRAMADA";
            case "EN_PROGRESO":
            case "ENPROGRESO":
            case "EN-PROGRESO":
                return "EN_PROGRESO";
            case "COMPLETADA":
            case "COMPLETADO":
                return "COMPLETADA";
            case "CANCELADA":
            case "CANCELADO":
                return "CANCELADA";
            default:
                // aceptar también las formas ya humanizadas
                String lower = s.toLowerCase();
                if (lower.equals("programada") || lower.equals("programado")) return "PROGRAMADA";
                if (lower.equals("en progreso") || lower.equals("enprogreso")) return "EN_PROGRESO";
                if (lower.equals("completada") || lower.equals("completado")) return "COMPLETADA";
                if (lower.equals("cancelada") || lower.equals("cancelado")) return "CANCELADA";
                return null;
        }
    }
}
