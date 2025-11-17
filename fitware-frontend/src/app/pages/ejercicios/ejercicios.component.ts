import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { EjercicioService } from '../../core/services/ejercicio.service';
import { Ejercicio } from '../../core/models/ejercicio.model';

@Component({
  selector: 'app-ejercicios',
  templateUrl: './ejercicios.component.html',
  styleUrls: ['./ejercicios.component.scss']
})
export class EjerciciosComponent implements OnInit {
  ejercicios: Ejercicio[] = [];
  filtered: Ejercicio[] = [];
  form: FormGroup;
  editingId: number | null = null;
  loading = false;
  saving = false;
  error: string | null = null;
  tiposDisponibles = ['Cardio', 'Fuerza', 'Resistencia', 'Movilidad', 'Flexibilidad', 'HIIT', 'Equilibrio', 'Potencia'];

  constructor(private fb: FormBuilder, private ejercicioService: EjercicioService) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: ['', [Validators.required, Validators.minLength(5)]],
      tipo: ['', Validators.required],
      equipamiento: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarEjercicios();
  }

  cargarEjercicios(): void {
    this.loading = true;
    this.error = null;
    this.ejercicioService.findAll()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: data => {
          this.ejercicios = data;
          this.filtered = data;
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  buscar(term: string): void {
    const value = term.toLocaleLowerCase();
    this.filtered = this.ejercicios.filter(e =>
      e.nombre.toLocaleLowerCase().includes(value) ||
      e.tipo.toLocaleLowerCase().includes(value)
    );
  }

  editar(ejercicio: Ejercicio): void {
    this.editingId = ejercicio.id ?? null;
    this.form.patchValue({
      nombre: ejercicio.nombre,
      descripcion: ejercicio.descripcion,
      tipo: ejercicio.tipo,
      equipamiento: ejercicio.equipamiento
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelarEdicion(): void {
    this.editingId = null;
    this.form.reset();
  }

  eliminar(id: number | undefined): void {
    if (!id) {
      return;
    }
    if (!confirm('¿Deseas eliminar este ejercicio?')) {
      return;
    }
    this.saving = true;
    this.ejercicioService.delete(id)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => this.cargarEjercicios(),
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const payload: Ejercicio = {
      ...this.form.value,
      tipo: this.form.value.tipo
    };
    this.saving = true;
    const request$ = this.editingId
      ? this.ejercicioService.update(this.editingId, payload)
      : this.ejercicioService.create(payload);

    request$
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => {
          this.cancelarEdicion();
          this.cargarEjercicios();
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  private obtenerMensajeError(error: any): string {
    if (error && error.error && error.error.message) {
      return error.error.message;
    }
    return 'Ocurrió un error inesperado. Intenta de nuevo.';
  }
}
