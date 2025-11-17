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
    name = "plan_alimentos",
    uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "alimento_id", "comida_del_dia"})
)
public class PlanAlimentos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id")
    private PlanAlimentacion plan;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "alimento_id")
    private Alimento alimento;

    private String cantidad;

    @Column(name = "comida_del_dia")
    private String comidaDelDia;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PlanAlimentacion getPlan() {
        return plan;
    }

    public void setPlan(PlanAlimentacion plan) {
        this.plan = plan;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public void setAlimento(Alimento alimento) {
        this.alimento = alimento;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad == null ? null : cantidad.trim();
    }

    public String getComidaDelDia() {
        return comidaDelDia;
    }

    public void setComidaDelDia(String comidaDelDia) {
        this.comidaDelDia = comidaDelDia == null ? null : comidaDelDia.trim();
    }

    @JsonProperty("planId")
    public Integer getPlanId() {
        return plan != null ? plan.getId() : null;
    }

    @JsonProperty("alimentoId")
    public Integer getAlimentoId() {
        return alimento != null ? alimento.getId() : null;
    }

    @JsonProperty("alimentoNombre")
    public String getAlimentoNombre() {
        return alimento != null ? alimento.getNombre() : null;
    }
}
