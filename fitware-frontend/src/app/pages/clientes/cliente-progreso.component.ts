import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { ClienteService } from '../../core/services/cliente.service';
import { Progreso } from '../../core/models/progreso.model';

@Component({
  selector: 'app-cliente-progreso',
  templateUrl: './cliente-progreso.component.html',
  styleUrls: ['./cliente-progreso.component.scss']
})
export class ClienteProgresoComponent implements OnInit {
  cedula!: string;
  progresos: Progreso[] = [];
  form: FormGroup;
  loading = false;
  saving = false;
  error: string | null = null;
  editing: Progreso | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private clienteService: ClienteService
  ) {
    this.form = this.fb.group({
      fecha: ['', Validators.required],
      peso: ['', [Validators.required, Validators.min(20)]],
      imc: ['', [Validators.required, Validators.min(10)]],
      observaciones: ['']
    });
  }

  ngOnInit(): void {
    this.cedula = this.route.snapshot.paramMap.get('cedula') ?? '';
    if (!this.cedula) {
      this.router.navigate(['/clientes']);
      return;
    }
    this.cargarProgresos();
  }

  cargarProgresos(): void {
    this.loading = true;
    this.error = null;
    this.clienteService.listarProgreso(this.cedula)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: data => this.progresos = data,
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  editarProgreso(item: Progreso): void {
    this.editing = item;
    this.form.patchValue(item);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelarEdicion(): void {
    this.editing = null;
    this.form.reset();
  }

  eliminarProgreso(item: Progreso): void {
    if (!item.id || !confirm('¿Eliminar registro de progreso?')) {
      return;
    }
    this.saving = true;
    this.clienteService.eliminarProgreso(item.id)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => this.cargarProgresos(),
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const payload: Progreso = this.form.value;
    this.saving = true;
    const request$ = this.editing && this.editing.id
      ? this.clienteService.editarProgreso(this.editing.id, payload)
      : this.clienteService.agregarProgreso(this.cedula, payload);

    request$
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => {
          this.cancelarEdicion();
          this.cargarProgresos();
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  private obtenerMensajeError(error: any): string {
    if (error && error.error && error.error.message) {
      return error.error.message;
    }
    return 'No se pudo completar la operación.';
  }
}
