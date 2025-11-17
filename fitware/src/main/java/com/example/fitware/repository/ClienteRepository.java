package com.example.fitware.repository;

import com.example.fitware.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
}
