package com.example.fitware.service;

import com.example.fitware.domain.Cliente;
import com.example.fitware.domain.Progreso;
import com.example.fitware.domain.Sesion;
import com.example.fitware.domain.Usuario;
import com.example.fitware.repository.ClienteRepository;
import com.example.fitware.repository.ProgresoRepository;
import com.example.fitware.repository.SesionEjerciciosRepository;
import com.example.fitware.repository.SesionRepository;
import com.example.fitware.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class ClienteService {

    private final ClienteRepository cRepo;
    private final UsuarioRepository uRepo;
    private final ProgresoRepository pRepo;
    private final SesionRepository sRepo;
    private final SesionEjerciciosRepository seRepo;

    public ClienteService(ClienteRepository cRepo,
                          UsuarioRepository uRepo,
                          ProgresoRepository pRepo,
                          SesionRepository sRepo,
                          SesionEjerciciosRepository seRepo) {
        this.cRepo = cRepo;
        this.uRepo = uRepo;
        this.pRepo = pRepo;
        this.sRepo = sRepo;
        this.seRepo = seRepo;
    }

    /**
     * Registra un cliente con PK compartida (cedula) usando @MapsId:
     * 1) Crea/actualiza Usuario (tabla usuario)
     * 2) Crea Cliente (tabla cliente) referenciando ese Usuario
     */
    @Transactional
    public Cliente registrarCliente(Usuario u, Cliente c) {
        if (u == null || c == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos para registrar cliente");
        }
        final String cedula = safe(u.getCedula());
        if (cedula.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cédula del usuario es obligatoria");
        }

        // Si viene c.usuarioCedula vacío, lo alineamos con la cédula del usuario
        String cedulaCliente = c.getUsuarioCedula();
        if (cedulaCliente == null || cedulaCliente.isBlank()) {
            cedulaCliente = cedula;
        }
        if (!cedula.equals(cedulaCliente)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PK/FK deben coincidir (usuario.cedula == cliente.usuarioCedula)");
        }

        if (cRepo.existsById(cedula)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El cliente ya está registrado");
        }

        // 1) Asegura/persiste el Usuario (upsert)
        Usuario userDb = uRepo.findById(cedula).orElseGet(Usuario::new);
        userDb.setCedula(cedula);
        userDb.setNombre(u.getNombre());
        userDb.setApellidos(u.getApellidos());
        userDb.setFechaNacimiento(u.getFechaNacimiento());
        userDb.setTelefono(u.getTelefono());
        userDb.setGenero(u.getGenero());
        userDb = uRepo.save(userDb);

        // 2) Crea el Cliente enlazado al Usuario (MapsId)
        Cliente nuevo = new Cliente();
        nuevo.setUsuario(userDb);
        nuevo.setFechaRegistro(c.getFechaRegistro());
        nuevo.setPesoInicial(c.getPesoInicial());
        nuevo.setAlturaInicial(c.getAlturaInicial());

        return cRepo.save(nuevo);
    }

    @Transactional(readOnly = true)
    public List<Progreso> listarProgreso(String cedula) {
        verificarCliente(cedula);
        // Ruta: progreso -> cliente -> usuario -> cedula
        return pRepo.findByCliente_Usuario_CedulaOrderByFechaDesc(cedula);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return cRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente obtenerCliente(String cedula) {
        return verificarCliente(cedula);
    }

    @Transactional
    public Progreso agregarProgreso(String cedula, Progreso p) {
        if (p == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del progreso son obligatorios");
        }
        Cliente c = verificarCliente(cedula);
        p.setId(null);
        p.setCliente(c);
        return pRepo.save(p);
    }

    /**
     * Actualiza datos de Usuario y de Cliente.
     */
    @Transactional
    public Cliente actualizarCliente(String cedula, Usuario datosUsuario, Cliente datosCliente) {
        Cliente existente = verificarCliente(cedula);
        if (datosUsuario == null || datosCliente == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos para actualizar cliente");
        }

        String cedulaCliente = datosCliente.getUsuarioCedula();
        if (cedulaCliente == null || cedulaCliente.isBlank()) {
            cedulaCliente = datosUsuario.getCedula();
        }
        if (!Objects.equals(cedula, datosUsuario.getCedula()) || !Objects.equals(cedula, cedulaCliente)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las identificaciones del cliente deben coincidir");
        }

        // Actualiza primero el Usuario
        Usuario userDb = uRepo.findById(cedula)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario asociado no existe"));
        userDb.setNombre(datosUsuario.getNombre());
        userDb.setApellidos(datosUsuario.getApellidos());
        userDb.setFechaNacimiento(datosUsuario.getFechaNacimiento());
        userDb.setTelefono(datosUsuario.getTelefono());
        userDb.setGenero(datosUsuario.getGenero());
        uRepo.save(userDb);

        // Ahora datos propios de Cliente
        existente.setFechaRegistro(datosCliente.getFechaRegistro());
        existente.setPesoInicial(datosCliente.getPesoInicial());
        existente.setAlturaInicial(datosCliente.getAlturaInicial());

        // Asegura asociación consistente (por si vino nula en el body)
        if (existente.getUsuario() == null || !cedula.equals(existente.getUsuario().getCedula())) {
            existente.setUsuario(userDb);
        }

        return cRepo.save(existente);
    }

    /**
     * Elimina cliente y dependencias (progresos, sesiones/ejercicios asignados).
     * NO elimina el Usuario (queda activo), a menos que tu regla de negocio diga lo contrario.
     */
    @Transactional
    public void eliminarCliente(String cedula) {
        Cliente cliente = verificarCliente(cedula);

        // Primero, ejercicios asignados a sesiones del cliente
        // Ruta: sesion -> cliente -> usuario -> cedula
        List<Sesion> sesiones = sRepo.findByCliente_Usuario_CedulaOrderByFechaInicioDesc(cedula);
        sesiones.forEach(sesion -> {
            if (sesion.getId() != null) {
                seRepo.deleteBySesion_Id(sesion.getId());
            }
        });
        sRepo.deleteAll(sesiones);

        // Luego, progresos
        pRepo.deleteByCliente_Usuario_Cedula(cedula);

        // Finalmente, el cliente
        cRepo.delete(cliente);

        // Si quieres borrar también el Usuario:
        // uRepo.deleteById(cedula);
    }

    @Transactional
    public Progreso editarProgreso(Integer id, Progreso p) {
        Progreso db = pRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Progreso no encontrado"));

        if (p.getFecha() != null) db.setFecha(p.getFecha());
        if (p.getPeso() != null) db.setPeso(p.getPeso());
        if (p.getImc() != null) db.setImc(p.getImc());
        db.setObservaciones(p.getObservaciones());

        return pRepo.save(db);
    }

    @Transactional
    public void eliminarProgreso(Integer id) {
        if (!pRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Progreso no encontrado");
        }
        pRepo.deleteById(id);
    }

    private Cliente verificarCliente(String cedula) {
        String c = safe(cedula);
        if (c.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cédula es obligatoria");
        }
        return cRepo.findById(c)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
