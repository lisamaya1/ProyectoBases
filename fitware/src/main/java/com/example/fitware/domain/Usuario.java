package com.example.fitware.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED) // ← importante: herencia JOINED
public class Usuario {

    @Id
    private String cedula;

    private String nombre;

    private String apellidos;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String telefono;

    private String genero;

    // --- getters / setters ---
    public String getCedula() {
        return cedula;
    }
    public void setCedula(String cedula) {
        this.cedula = cedula;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getGenero() {
        return genero;
    }
    public void setGenero(String genero) {
        this.genero = genero;
    }
}
