package com.example.fitware.domain;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;
import java.time.LocalDate;

@Entity
@Table(name = "cliente")
public class Cliente implements Persistable<String> {

    @Id
    @Column(name = "usuario_cedula")
    private String usuarioCedula;

    @OneToOne(optional = false)
    @MapsId // ← comparte la PK con usuario.cedula
    @JoinColumn(name = "usuario_cedula", referencedColumnName = "cedula")
    private Usuario usuario;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @Column(name = "peso_inicial")
    private Double pesoInicial;

    @Column(name = "altura_inicial")
    private Double alturaInicial;

    @Transient
    private boolean isNew = true;

    // --- getters / setters ---
    public String getUsuarioCedula() {
        return usuarioCedula;
    }
    public void setUsuarioCedula(String usuarioCedula) {
        this.usuarioCedula = usuarioCedula;
    }
    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.usuarioCedula = usuario.getCedula();
        } else {
            this.usuarioCedula = null;
        }
    }
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    public Double getPesoInicial() {
        return pesoInicial;
    }
    public void setPesoInicial(Double pesoInicial) {
        this.pesoInicial = pesoInicial;
    }
    public Double getAlturaInicial() {
        return alturaInicial;
    }
    public void setAlturaInicial(Double alturaInicial) {
        this.alturaInicial = alturaInicial;
    }

    // --- Persistable ---
    @Override
    @Transient
    public String getId() {
        return getUsuarioCedula();
    }

    @Override
    @Transient
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    private void markNotNew() {
        this.isNew = false;
    }

    @PrePersist
    private void markNew() {
        this.isNew = true;
    }
}
