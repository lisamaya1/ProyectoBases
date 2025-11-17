import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { ClienteService } from '../../core/services/cliente.service';
import { Cliente, RegistroClienteDTO } from '../../core/models/cliente.model';
import { Usuario } from '../../core/models/usuario.model';

@Component({
  selector: 'app-clientes',
  templateUrl: './clientes.component.html',
  styleUrls: ['./clientes.component.scss']
})
export class ClientesComponent implements OnInit {
  registroForm: FormGroup;
  seguimientoForm: FormGroup;
  mensajeExito: string | null = null;
  mensajeError: string | null = null;
  loading = false;
  clientes: Cliente[] = [];
  filtered: Cliente[] = [];
  editMode = false;
  clienteSeleccionado: Cliente | null = null;
  generos = ['Masculino', 'Femenino', 'Otro'];

  constructor(private fb: FormBuilder, private clienteService: ClienteService, private router: Router) {
    this.registroForm = this.fb.group({
      cedula: ['', [Validators.required, Validators.minLength(6)]],
      nombre: ['', Validators.required],
      apellidos: ['', Validators.required],
      fechaNacimiento: ['', Validators.required],
      telefono: ['', Validators.required],
      genero: ['', Validators.required],
      fechaRegistro: ['', Validators.required],
      pesoInicial: ['', [Validators.required, Validators.min(20)]],
      alturaInicial: ['', [Validators.required, Validators.min(1.2)]],
    });

    this.seguimientoForm = this.fb.group({
      cedula: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarClientes();
  }

  guardarCliente(): void {
    this.mensajeError = null;
    this.mensajeExito = null;
    if (this.registroForm.invalid) {
      this.registroForm.markAllAsTouched();
      this.mensajeError = 'Por favor completa todos los campos obligatorios antes de guardar.';
      return;
    }

    const payload = this.armarPayload();
    this.loading = true;
    const identificador = this.clienteSeleccionado?.usuarioCedula
      || this.clienteSeleccionado?.usuario?.cedula;
    const request$ = this.editMode && this.clienteSeleccionado && identificador
      ? this.clienteService.actualizarCliente(identificador, payload)
      : this.clienteService.registrarCliente(payload);

    request$
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: () => {
          this.mensajeExito = this.editMode ? 'Cliente actualizado correctamente.' : 'Cliente registrado correctamente.';
          this.resetFormulario();
          this.cargarClientes();
        },
        error: err => this.mensajeError = this.obtenerMensajeError(err)
      });
  }

  irAProgreso(): void {
    if (this.seguimientoForm.invalid) {
      this.seguimientoForm.markAllAsTouched();
      return;
    }
    const cedula = this.seguimientoForm.value.cedula;
    this.router.navigate(['/clientes', cedula, 'progreso']);
  }

  editar(cliente: Cliente): void {
    const normalizado = this.normalizarCliente(cliente);
    this.editMode = true;
    this.clienteSeleccionado = normalizado;
    const usuario = normalizado.usuario as Usuario;
    const cedula = normalizado.usuarioCedula || usuario.cedula;
    this.registroForm.patchValue({
      cedula,
      nombre: usuario.nombre,
      apellidos: usuario.apellidos,
      fechaNacimiento: usuario.fechaNacimiento,
      telefono: usuario.telefono,
      genero: usuario.genero,
      fechaRegistro: normalizado.fechaRegistro,
      pesoInicial: normalizado.pesoInicial,
      alturaInicial: normalizado.alturaInicial
    });
    this.registroForm.get('cedula')?.disable();
  }

  eliminar(cliente: Cliente): void {
    const normalizado = this.normalizarCliente(cliente);
    const usuario = normalizado.usuario as Usuario;
    const nombreCompleto = [usuario.nombre, usuario.apellidos].filter(Boolean).join(' ');
    if (!confirm('Eliminar al cliente ' + nombreCompleto + '?')) {
      return;
    }
    this.loading = true;
    const identificador = normalizado.usuarioCedula || usuario.cedula;
    this.clienteService.eliminarCliente(identificador)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Cliente eliminado correctamente.';
          if (this.editMode && this.clienteSeleccionado?.usuarioCedula === identificador) {
            this.resetFormulario();
          }
          this.cargarClientes();
        },
        error: err => this.mensajeError = this.obtenerMensajeError(err)
      });
  }

  cancelarEdicion(): void {
    this.resetFormulario();
  }

  filtrar(term: string): void {
    const value = term ? term.trim().toLowerCase() : '';
    this.filtered = this.clientes.filter(cliente => {
      const usuario = cliente.usuario;
      const cedula = (cliente.usuarioCedula || usuario?.cedula || '').toLowerCase();
      const nombre = usuario?.nombre?.toLowerCase() ?? '';
      const apellidos = usuario?.apellidos?.toLowerCase() ?? '';
      return cedula.includes(value) || nombre.includes(value) || apellidos.includes(value);
    });
  }

  private cargarClientes(): void {
    this.clienteService.listarClientes()
      .subscribe({
        next: data => {
          this.clientes = data.map(cliente => this.normalizarCliente(cliente));
          this.filtered = [...this.clientes];
        },
        error: err => this.mensajeError = this.obtenerMensajeError(err)
      });
  }

  private normalizarCliente(cliente: Cliente): Cliente {
    const raw = cliente as Cliente & Record<string, unknown>;
    const plano = (prop: string): string => {
      const value = raw[prop];
      return typeof value === 'string' ? value : '';
    };

    const candidatoCedula = cliente.usuarioCedula
      ?? cliente.usuario?.cedula
      ?? plano('cedula');
    const usuarioCedula = candidatoCedula || '';

    const baseUsuario = cliente.usuario ?? {
      cedula: usuarioCedula,
      nombre: plano('nombre'),
      apellidos: plano('apellidos'),
      fechaNacimiento: plano('fechaNacimiento'),
      telefono: plano('telefono'),
      genero: plano('genero')
    };

    const usuario: Usuario = {
      cedula: (baseUsuario.cedula ?? usuarioCedula) || '',
      nombre: (baseUsuario.nombre ?? plano('nombre')) || '',
      apellidos: (baseUsuario.apellidos ?? plano('apellidos')) || '',
      fechaNacimiento: (baseUsuario.fechaNacimiento ?? plano('fechaNacimiento')) || '',
      telefono: (baseUsuario.telefono ?? plano('telefono')) || '',
      genero: (baseUsuario.genero ?? plano('genero')) || ''
    };

    return {
      ...cliente,
      usuarioCedula: usuarioCedula || usuario.cedula,
      usuario
    };
  }

  private resetFormulario(): void {
    this.editMode = false;
    this.clienteSeleccionado = null;
    this.registroForm.reset();
    this.registroForm.get('cedula')?.enable();
  }

  private armarPayload(): RegistroClienteDTO {
    const raw = this.registroForm.getRawValue();
    return {
      usuario: {
        cedula: raw.cedula,
        nombre: raw.nombre,
        apellidos: raw.apellidos,
        fechaNacimiento: raw.fechaNacimiento,
        telefono: raw.telefono,
        genero: raw.genero
      },
      cliente: {
        usuarioCedula: raw.cedula,
        fechaRegistro: raw.fechaRegistro,
        pesoInicial: Number(raw.pesoInicial),
        alturaInicial: Number(raw.alturaInicial)
      }
    };
  }

  private obtenerMensajeError(error: any): string {
    if (error && error.error && error.error.message) {
      return error.error.message;
    }
    return 'No se pudo completar la operacion.';
  }
}
