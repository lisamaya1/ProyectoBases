import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { SesionService } from '../../core/services/sesion.service';
import { Sesion } from '../../core/models/sesion.model';
import { SesionEjercicio } from '../../core/models/sesion-ejercicio.model';

@Component({
  selector: 'app-sesiones',
  templateUrl: './sesiones.component.html',
  styleUrls: ['./sesiones.component.scss']
})
export class SesionesComponent implements OnInit {
  filtroForm: FormGroup;
  sesionForm: FormGroup;
  ejercicioForm: FormGroup;

  sesiones: Sesion[] = [];
  ejercicios: SesionEjercicio[] = [];
  selectedSesion: Sesion | null = null;
  editingSesion: Sesion | null = null;

  // UI (lo que ve el usuario)
  estadosSesion = ['Programada', 'En progreso', 'Completada', 'Cancelada'];

  // Mapa UI -> API (lo que exige la BD)  ⬅️
  private ESTADO_UI_TO_API = new Map<string, string>([
    ['Programada', 'PROGRAMADA'],
    ['En progreso', 'EN_PROGRESO'],
    ['Completada', 'COMPLETADA'],
    ['Cancelada', 'CANCELADA'],
  ]);

  // Mapa API -> UI  ⬅️
  private ESTADO_API_TO_UI = new Map<string, string>([
    ['PROGRAMADA', 'Programada'],
    ['EN_PROGRESO', 'En progreso'],
    ['COMPLETADA', 'Completada'],
    ['CANCELADA', 'Cancelada'],
  ]);

  loadingSesiones = false;
  loadingEjercicios = false;
  saving = false;
  error: string | null = null;
  mensajeExito: string | null = null;

  constructor(private fb: FormBuilder, private sesionService: SesionService) {
    this.filtroForm = this.fb.group({
      clienteCedula: ['']
    });

    this.sesionForm = this.fb.group({
      clienteCedula: ['', Validators.required],
      estado: ['', Validators.required], // UI label
      fechaInicio: ['', Validators.required], // 'YYYY-MM-DDTHH:mm'
      fechaFin: ['', Validators.required],
      gastoCalorico: ['', [Validators.required, Validators.min(0)]]
    });

    this.ejercicioForm = this.fb.group({
      ejercicioId: ['', Validators.required],
      repeticiones: ['', [Validators.required, Validators.min(1)]],
      series: ['', [Validators.required, Validators.min(1)]],
      estado: ['Pendiente', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarSesiones();
  }

  cargarSesiones(): void {
    this.loadingSesiones = true;
    const cedula = this.filtroForm.value.clienteCedula || undefined;
    this.sesionService.listarSesiones(cedula)
      .pipe(finalize(() => this.loadingSesiones = false))
      .subscribe({
        next: data => this.sesiones = data.map(item => ({
          ...item,
          clienteCedula: item.clienteCedula || item?.cliente?.cedula || ''
        })),
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  filtrar(): void {
    this.cargarSesiones();
  }

  seleccionarSesion(item: Sesion): void {
    this.selectedSesion = item;
    this.editingSesion = null;
    const cedula = item.clienteCedula || item?.cliente?.cedula || '';
    this.sesionForm.patchValue({
      clienteCedula: cedula,
      // usa el mapeo API -> UI  ⬅️
      estado: this.estadoApiToUi((item as any).estadoDescripcion ?? item.estado),
      fechaInicio: this.isoToLocalInput(item.fechaInicio), // ⬅️
      fechaFin: this.isoToLocalInput(item.fechaFin),       // ⬅️
      gastoCalorico: item.gastoCalorico
    });
    this.ejercicioForm.reset({ estado: 'Pendiente' });
    this.cargarEjercicios(item);
  }

  iniciarCreacion(): void {
    this.selectedSesion = null;
    this.editingSesion = null;
    const ahora = new Date();
    const dentroDeUnaHora = new Date(ahora.getTime() + 60 * 60 * 1000);
    this.sesionForm.reset({
      estado: '',
      clienteCedula: '',
      gastoCalorico: null,
      fechaInicio: this.dateToLocalInput(ahora),           // ⬅️
      fechaFin: this.dateToLocalInput(dentroDeUnaHora)     // ⬅️
    });
    this.ejercicioForm.reset({ estado: 'Pendiente' });
  }

  cargarEjercicios(sesion: Sesion): void {
    if (!sesion.id) return;

    this.loadingEjercicios = true;
    this.sesionService.listarEjercicios(sesion.id)
      .pipe(finalize(() => this.loadingEjercicios = false))
      .subscribe({
        next: data => this.ejercicios = data,
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  guardarSesion(): void {
    if (this.sesionForm.invalid) {
      this.sesionForm.markAllAsTouched();
      return;
    }

    this.mensajeExito = null;
    this.error = null;

    const estadoUI: string = this.sesionForm.value.estado;
    const estadoAPI = this.estadoUiToApi(estadoUI); // ⬅️ normaliza
    if (!estadoAPI) {
      this.error = 'Estado inválido.';
      return;
    }

    const fechaInicio: string = this.sesionForm.value.fechaInicio; // 'YYYY-MM-DDTHH:mm'
    const fechaFin: string = this.sesionForm.value.fechaFin;
    const gasto = this.sesionForm.value.gastoCalorico;
    const clienteCedula: string = this.sesionForm.value.clienteCedula;

    if (!clienteCedula) {
      this.sesionForm.get('clienteCedula')?.setErrors({ required: true });
      return;
    }

    const payload: Sesion = {
      estado: estadoAPI,                       // ⬅️ manda canónico
      fechaInicio: fechaInicio ? fechaInicio : null,
      fechaFin: fechaFin ? fechaFin : null,
      gastoCalorico: (gasto === null || gasto === '') ? null : Number(gasto)
    } as Sesion;

    this.saving = true;

    const request$ = this.selectedSesion && this.selectedSesion.id
      ? this.sesionService.actualizarSesion(this.selectedSesion.id, payload, clienteCedula)
      : this.sesionService.crearSesion(clienteCedula, payload);

    request$
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: sesion => {
          this.mensajeExito = (this.selectedSesion && this.selectedSesion.id)
            ? 'Sesión actualizada correctamente.'
            : 'Sesión creada correctamente.';
          this.cargarSesiones();

          const normalizada: Sesion = {
            ...sesion,
            clienteCedula: (sesion as any).clienteCedula || (sesion as any)?.cliente?.cedula || clienteCedula,
            // al volver a cargar el form, muestra en UI legible  ⬅️
            estado: sesion.estado
          };
          this.seleccionarSesion(normalizada);
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  eliminarSesion(sesion: Sesion): void {
    if (!sesion.id || !confirm('¿Eliminar sesión?')) return;

    this.saving = true;
    this.sesionService.eliminarSesion(sesion.id)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Sesión eliminada correctamente.';
          this.selectedSesion = null;
          this.ejercicios = [];
          this.cargarSesiones();
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  asignarEjercicio(): void {
    if (!this.selectedSesion || !this.selectedSesion.id || this.ejercicioForm.invalid) {
      this.ejercicioForm.markAllAsTouched();
      return;
    }
    const sesionId = this.selectedSesion.id;
    const ejercicioId = Number(this.ejercicioForm.value.ejercicioId);
    const repeticiones = Number(this.ejercicioForm.value.repeticiones);
    const series = Number(this.ejercicioForm.value.series);
    const estado = this.ejercicioForm.value.estado;

    this.saving = true;
    this.sesionService.asignarEjercicio(sesionId, ejercicioId, repeticiones, series, estado)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: _ => {
          this.mensajeExito = 'Ejercicio asignado a la sesión.';
          this.ejercicioForm.reset({ estado: 'Pendiente' });
          this.cargarEjercicios(this.selectedSesion as Sesion);
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  eliminarAsignacion(item: SesionEjercicio): void {
    if (!item.id || !confirm('¿Eliminar ejercicio de la sesión?')) return;

    this.saving = true;
    this.sesionService.eliminarSesionEjercicio(item.id)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Ejercicio retirado de la sesión.';
          this.cargarEjercicios(this.selectedSesion as Sesion);
        },
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  private obtenerMensajeError(error: any): string {
    if (error?.error?.message) return error.error.message;
    return 'No se pudo completar la operación.';
  }

  // ---------- Utilidades de fecha (SIN UTC) ----------  ⬅️
  private pad(n: number): string { return n < 10 ? '0' + n : '' + n; }

  private dateToLocalInput(d: Date): string {
    const y = d.getFullYear();
    const m = this.pad(d.getMonth() + 1);
    const day = this.pad(d.getDate());
    const hh = this.pad(d.getHours());
    const mm = this.pad(d.getMinutes());
    return `${y}-${m}-${day}T${hh}:${mm}`;
  }

  private isoToLocalInput(iso?: string | null): string | null {
    if (!iso) return null;
    const d = new Date(iso); // interpreta la ISO y la convierte a local
    return this.dateToLocalInput(d);
  }

  // ---------- Mapeos estado ----------  ⬅️
  private estadoUiToApi(ui: string | null | undefined): string | null {
    if (!ui) return null;
    const found = this.ESTADO_UI_TO_API.get(ui.trim());
    if (found) return found;
    // intento robusto (por si vienen variantes)
    const normalized = ui.trim().toUpperCase().replace(' ', '_');
    return this.ESTADO_API_TO_UI.has(normalized) ? normalized : null;
  }

  private estadoApiToUi(api: string | null | undefined): string | null {
    if (!api) return null;
    const key = api.trim().toUpperCase();
    return this.ESTADO_API_TO_UI.get(key) ?? api; // si viene raro, muestra tal cual
  }
}
