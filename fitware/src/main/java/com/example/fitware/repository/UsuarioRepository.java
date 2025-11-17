package com.example.fitware.repository;

import com.example.fitware.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}
