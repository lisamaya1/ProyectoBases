package com.example.fitware.web;


import com.example.fitware.domain.Cliente;
import com.example.fitware.domain.Progreso;
import com.example.fitware.service.ClienteService;
import com.example.fitware.web.dto.RegistroClienteDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteProgresoController {

    private final ClienteService service;

    public ClienteProgresoController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Cliente> registrar(@RequestBody RegistroClienteDTO dto) {
        Cliente created = service.registrarCliente(dto.usuario(), dto.cliente());
        return ResponseEntity.created(URI.create("/api/clientes/" + created.getUsuarioCedula())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(service.listarClientes());
    }

    @GetMapping("/{cedula}")
    public ResponseEntity<Cliente> obtenerCliente(@PathVariable String cedula) {
        return ResponseEntity.ok(service.obtenerCliente(cedula));
    }

    @PutMapping("/{cedula}")
    public ResponseEntity<Cliente> actualizar(@PathVariable String cedula, @RequestBody RegistroClienteDTO dto) {
        Cliente actualizado = service.actualizarCliente(cedula, dto.usuario(), dto.cliente());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{cedula}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable String cedula) {
        service.eliminarCliente(cedula);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cedula}/progreso")
    public ResponseEntity<List<Progreso>> listar(@PathVariable String cedula) {
        return ResponseEntity.ok(service.listarProgreso(cedula));
    }

    @PostMapping("/{cedula}/progreso")
    public ResponseEntity<Progreso> agregar(@PathVariable String cedula, @RequestBody Progreso p) {
        Progreso created = service.agregarProgreso(cedula, p);
        return ResponseEntity.created(URI.create("/api/clientes/" + cedula + "/progreso/" + created.getId())).body(created);
    }

    @PutMapping("/progreso/{id}")
    public ResponseEntity<Progreso> editar(@PathVariable Integer id, @RequestBody Progreso p) {
        return ResponseEntity.ok(service.editarProgreso(id, p));
    }

    @DeleteMapping("/progreso/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminarProgreso(id);
        return ResponseEntity.noContent().build();
    }
}
