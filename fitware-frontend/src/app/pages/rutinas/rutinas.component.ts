
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { RutinaPlanService } from '../../core/services/rutina-plan.service';
import { Rutina } from '../../core/models/rutina.model';
import { PlanAlimentacion } from '../../core/models/plan-alimentacion.model';
import { PlanAlimentos } from '../../core/models/plan-alimentos.model';

@Component({
  selector: 'app-rutinas',
  templateUrl: './rutinas.component.html',
  styleUrls: ['./rutinas.component.scss']
})
export class RutinasComponent implements OnInit {
  rutinaForm: FormGroup;
  planForm: FormGroup;
  alimentoForm: FormGroup;

  rutinas: Rutina[] = [];
  planes: PlanAlimentacion[] = [];
  alimentos: PlanAlimentos[] = [];

  selectedRutina: Rutina | null = null;
  selectedPlan: PlanAlimentacion | null = null;

  editRutina = false;
  editPlan = false;
  mensajeExito: string | null = null;

  loadingRutinas = false;
  loadingPlanes = false;
  loadingAlimentos = false;
  saving = false;
  error: string | null = null;

  comidas = ['Desayuno', 'Almuerzo', 'Cena', 'Snack'];

  constructor(private fb: FormBuilder, private rutinaService: RutinaPlanService) {
    this.rutinaForm = this.fb.group({
      entrenadorCedula: ['', Validators.required],
      nombre: ['', Validators.required],
      objetivo: ['', Validators.required],
      duracionSemanas: ['', [Validators.required, Validators.min(1)]]
    });

    this.planForm = this.fb.group({
      nombre: ['', Validators.required],
      descripcion: ['', Validators.required],
      duracionDias: ['', [Validators.required, Validators.min(1)]],
      fechaInicio: ['', Validators.required],
      fechaFin: ['', Validators.required]
    });

    this.alimentoForm = this.fb.group({
      alimentoId: ['', Validators.required],
      cantidad: ['', Validators.required],
      comidaDelDia: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarRutinas();
  }

  cargarRutinas(): void {
    this.loadingRutinas = true;
    this.error = null;
    this.rutinaService.listarRutinas()
      .pipe(finalize(() => this.loadingRutinas = false))
      .subscribe({
        next: data => this.rutinas = data,
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  seleccionarRutina(rutina: Rutina): void {
    this.selectedRutina = rutina;
    this.selectedPlan = null;
    this.planes = [];
    this.alimentos = [];
    this.planForm.reset();
    this.alimentoForm.reset();
    this.editPlan = false;
    this.cargarPlanes(rutina);
  }

  editarRutina(rutina: Rutina): void {
    this.seleccionarRutina(rutina);
    this.editRutina = true;
    this.mensajeExito = null;
    this.rutinaForm.patchValue({
      entrenadorCedula: rutina.entrenador?.usuarioCedula || '',
      nombre: rutina.nombre,
      objetivo: rutina.objetivo,
      duracionSemanas: rutina.duracionSemanas
    });
  }

  cancelarEdicionRutina(): void {
    this.editRutina = false;
    this.rutinaForm.reset();
  }

  guardarRutina(): void {
    this.mensajeExito = null;
    if (this.rutinaForm.invalid) {
      this.rutinaForm.markAllAsTouched();
      return;
    }
    const entrenadorCedula = this.rutinaForm.get('entrenadorCedula')?.value;
    const payload: Rutina = {
      nombre: this.rutinaForm.get('nombre')?.value,
      objetivo: this.rutinaForm.get('objetivo')?.value,
      duracionSemanas: Number(this.rutinaForm.get('duracionSemanas')?.value)
    };

    this.saving = true;
    const request$ = this.editRutina && this.selectedRutina?.id
      ? this.rutinaService.actualizarRutina(this.selectedRutina.id, payload, entrenadorCedula)
      : this.rutinaService.crearRutina(entrenadorCedula, payload);

    request$
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: rutina => {
          this.mensajeExito = this.editRutina ? 'Rutina actualizada correctamente.' : 'Rutina creada correctamente.';
          this.cancelarEdicionRutina();
          this.cargarRutinas();
          if (rutina?.id) {
            this.seleccionarRutina(rutina);
          }
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  eliminarRutina(rutina: Rutina): void {
    if (!rutina.id || !confirm('¿Eliminar rutina seleccionada?')) {
      return;
    }
    this.saving = true;
    this.rutinaService.eliminarRutina(rutina.id)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Rutina eliminada.';
          this.selectedRutina = null;
          this.selectedPlan = null;
          this.editRutina = false;
          this.cargarRutinas();
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  cargarPlanes(rutina: Rutina): void {
    if (!rutina.id) {
      return;
    }
    this.loadingPlanes = true;
    this.rutinaService.listarPlanes(rutina.id)
      .pipe(finalize(() => this.loadingPlanes = false))
      .subscribe({
        next: data => this.planes = data,
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  seleccionarPlan(plan: PlanAlimentacion): void {
    this.selectedPlan = plan;
    this.alimentoForm.reset();
    this.editPlan = false;
    this.cargarAlimentos(plan);
  }

  editarPlan(plan: PlanAlimentacion): void {
    this.selectPlanForEdit(plan);
  }

  private selectPlanForEdit(plan: PlanAlimentacion): void {
    this.selectedPlan = plan;
    this.editPlan = true;
    this.planForm.patchValue({
      nombre: plan.nombre,
      descripcion: plan.descripcion,
      duracionDias: plan.duracionDias,
      fechaInicio: plan.fechaInicio,
      fechaFin: plan.fechaFin
    });
    this.cargarAlimentos(plan);
  }

  cancelarEdicionPlan(): void {
    this.editPlan = false;
    this.planForm.reset();
  }

  guardarPlan(): void {
    if (!this.selectedRutina || !this.selectedRutina.id || this.planForm.invalid) {
      this.planForm.markAllAsTouched();
      return;
    }
    const payload: PlanAlimentacion = {
      nombre: this.planForm.get('nombre')?.value,
      descripcion: this.planForm.get('descripcion')?.value,
      duracionDias: Number(this.planForm.get('duracionDias')?.value),
      fechaInicio: this.planForm.get('fechaInicio')?.value,
      fechaFin: this.planForm.get('fechaFin')?.value
    };

    this.saving = true;
    const request$ = this.editPlan && this.selectedPlan?.id
      ? this.rutinaService.actualizarPlan(this.selectedPlan.id, payload, this.selectedRutina.id)
      : this.rutinaService.crearPlan(this.selectedRutina.id, payload);

    request$
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: plan => {
          this.mensajeExito = this.editPlan ? 'Plan actualizado correctamente.' : 'Plan creado correctamente.';
          this.cancelarEdicionPlan();
          this.cargarPlanes(this.selectedRutina as Rutina);
          if (plan?.id) {
            this.seleccionarPlan(plan);
          }
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  eliminarPlan(plan: PlanAlimentacion): void {
    if (!plan.id || !confirm('¿Eliminar plan de alimentación?')) {
      return;
    }
    this.saving = true;
    this.rutinaService.eliminarPlan(plan.id)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Plan eliminado.';
          this.planes = this.planes.filter(p => p.id !== plan.id);
          if (this.selectedPlan && this.selectedPlan.id === plan.id) {
            this.selectedPlan = null;
            this.alimentos = [];
          }
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  cargarAlimentos(plan: PlanAlimentacion): void {
    if (!plan.id) {
      return;
    }
    this.loadingAlimentos = true;
    this.rutinaService.listarAlimentos(plan.id)
      .pipe(finalize(() => this.loadingAlimentos = false))
      .subscribe({
        next: data => this.alimentos = data,
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  agregarAlimento(): void {
    if (!this.selectedPlan || !this.selectedPlan.id || this.alimentoForm.invalid) {
      this.alimentoForm.markAllAsTouched();
      return;
    }
    const alimentoId = Number(this.alimentoForm.get('alimentoId')?.value);
    const cantidad = this.alimentoForm.get('cantidad')?.value;
    const comida = this.alimentoForm.get('comidaDelDia')?.value;

    this.saving = true;
    this.rutinaService.agregarAlimento(this.selectedPlan.id, alimentoId, cantidad, comida)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: item => {
          this.mensajeExito = 'Alimento agregado al plan.';
          this.alimentos.push(item);
          this.alimentoForm.reset();
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  eliminarAlimento(item: PlanAlimentos): void {
    if (!item.id || !confirm('¿Eliminar alimento del plan?')) {
      return;
    }
    this.saving = true;
    this.rutinaService.eliminarAlimento(item.id)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Alimento eliminado.';
          this.alimentos = this.alimentos.filter(a => a.id !== item.id);
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
