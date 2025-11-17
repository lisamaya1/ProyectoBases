package com.example.fitware.web.dto;

import com.example.fitware.domain.Usuario;
import com.example.fitware.domain.Cliente;

// Java 17 record; Jackson lo serializa/deserializa sin código extra
public record RegistroClienteDTO(Usuario usuario, Cliente cliente) {}
