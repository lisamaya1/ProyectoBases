import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { ReportesService } from '../../core/services/reportes.service';
import { ReportCategory, ReportSection, ReportSummary } from '../../core/models/reportes.model';

interface ReportOption {
  id: string;
  label: string;
  category: ReportCategory;
  helper?: string;
}

@Component({
  selector: 'app-reportes',
  templateUrl: './reportes.component.html',
  styleUrls: ['./reportes.component.scss']
})
export class ReportesComponent implements OnInit {
  filtroForm: FormGroup;
  resumen: ReportSummary | null = null;
  loading = false;
  downloading = false;
  error: string | null = null;
  selectedSections = new Set<string>();

  readonly categorias: { id: ReportCategory; titulo: string; descripcion: string; }[] = [
    { id: 'SIMPLE', titulo: 'Consultas simples', descripcion: 'Listados directos para una visión rápida.' },
    { id: 'INTERMEDIO', titulo: 'Consultas intermedias', descripcion: 'Agrupaciones con joins y consolidaciones.' },
    { id: 'AVANZADO', titulo: 'Consultas avanzadas', descripcion: 'KPIs con subconsultas y lógica analítica.' },
  ];
  readonly opciones: ReportOption[] = [
    { id: 'clientes-registrados', label: 'Clientes inscritos en el periodo', category: 'SIMPLE', helper: 'Altas por fecha de inscripción.' },
    { id: 'sesiones-periodo', label: 'Sesiones programadas en el rango', category: 'SIMPLE', helper: 'Últimas 25 sesiones registradas.' },
    { id: 'progresos-periodo', label: 'Registros de progreso', category: 'SIMPLE', helper: 'Últimos registros corporales de clientes.' },
    { id: 'sesiones-estado', label: 'Volumen de sesiones por estado', category: 'INTERMEDIO', helper: 'Conteo agrupado por estado.' },
    { id: 'top-clientes-sesiones', label: 'Top clientes por sesiones completadas', category: 'INTERMEDIO', helper: 'Ranking con calorías promedio.' },
    { id: 'rutinas-entrenador', label: 'Producción de rutinas por entrenador', category: 'INTERMEDIO' },
    { id: 'peso-promedio-mensual', label: 'Evolución de peso promedio mensual', category: 'INTERMEDIO' },
    { id: 'mes-activo-calorias', label: 'Mes con más sesiones completadas', category: 'AVANZADO' },
    { id: 'clientes-sin-progreso', label: 'Clientes sin seguimiento en 45 días', category: 'AVANZADO' },
    { id: 'variacion-peso', label: 'Clientes con mayor variación de peso', category: 'AVANZADO' }
  ];
  readonly rangosRapidos = [
    { id: 'custom', label: 'Personalizado' },
    { id: 'current-month', label: 'Mes actual' },
    { id: 'previous-month', label: 'Mes anterior' },
    { id: 'last-30', label: 'Últimos 30 días' }
  ];

  constructor(private fb: FormBuilder, private reportesService: ReportesService) {
    const hoy = new Date();
    const haceTreinta = this.addDays(new Date(), -30);
    this.filtroForm = this.fb.group({
      desde: [this.toInputValue(haceTreinta)],
      hasta: [this.toInputValue(hoy)],
      preset: ['last-30']
    });
    this.selectedSections.add('clientes-registrados');
  }

  ngOnInit(): void {
    this.aplicarPreset(this.filtroForm.value.preset);
    this.consultar();
  }

  consultar(): void {
    const { desde, hasta } = this.filtroForm.value;
    const sections = this.seccionesSeleccionadas();
    if (!sections.length) {
      this.error = 'Selecciona al menos un reporte para generar los resultados.';
      this.resumen = null;
      return;
    }
    this.loading = true;
    this.error = null;
    this.reportesService.obtenerResumen(desde, hasta, sections)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: resumen => this.resumen = resumen,
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  descargar(): void {
    if (this.downloading) return;
    const { desde, hasta } = this.filtroForm.value;
    const sections = this.seccionesSeleccionadas();
    if (!sections.length) {
      this.error = 'Selecciona al menos un reporte antes de descargar.';
      return;
    }
    this.error = null;
    this.downloading = true;
    this.reportesService.descargarPdf(desde, hasta, sections)
      .pipe(finalize(() => this.downloading = false))
      .subscribe({
        next: blob => this.descargarBlob(blob, this.nombreArchivo(desde, hasta)),
        error: err => this.error = this.obtenerMensajeError(err)
      });
  }

  sectionsBy(category: ReportCategory): ReportSection[] {
    return (this.resumen?.sections || []).filter(section => section.category === category);
  }

  opcionesPorCategoria(category: ReportCategory): ReportOption[] {
    return this.opciones.filter(op => op.category === category);
  }

  tieneFilas(section: ReportSection): boolean {
    return section.rows && section.rows.length > 0;
  }

  isSelected(id: string): boolean {
    return this.selectedSections.has(id);
  }

  toggleSectionFromEvent(id: string, event: Event): void {
    const checked = (event.target as HTMLInputElement | null)?.checked ?? false;
    this.toggleSection(id, checked);
  }

  toggleSection(id: string, checked: boolean): void {
    if (checked) {
      this.selectedSections.add(id);
    } else {
      this.selectedSections.delete(id);
    }
  }

  seleccionarTodo(): void {
    this.opciones.forEach(op => this.selectedSections.add(op.id));
  }

  limpiarSeleccion(): void {
    this.selectedSections.clear();
    this.resumen = null;
  }

  get ultimaActualizacion(): string | null {
    if (!this.resumen) return null;
    const fecha = new Date(this.resumen.generatedAt);
    return fecha.toLocaleString();
  }

  private descargarBlob(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  private nombreArchivo(desde?: string | null, hasta?: string | null): string {
    const inicio = desde || this.resumen?.from || 'inicio';
    const fin = hasta || this.resumen?.to || 'fin';
    return `reportes-fitware-${inicio}-a-${fin}.pdf`;
  }

  onPresetChange(): void {
    const preset = this.filtroForm.value.preset;
    this.aplicarPreset(preset);
  }

  marcarPersonalizado(): void {
    if (this.filtroForm.value.preset !== 'custom') {
      this.filtroForm.patchValue({ preset: 'custom' }, { emitEvent: false });
    }
  }

  private aplicarPreset(preset?: string | null): void {
    if (!preset || preset === 'custom') {
      return;
    }
    const rango = this.obtenerRangoPreset(preset);
    if (!rango) return;
    this.filtroForm.patchValue({
      desde: this.toInputValue(rango.desde),
      hasta: this.toInputValue(rango.hasta)
    }, { emitEvent: false });
  }

  private obtenerRangoPreset(preset: string): { desde: Date; hasta: Date; } | null {
    const hoy = new Date();
    switch (preset) {
      case 'current-month': {
        const start = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
        const end = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
        return { desde: start, hasta: end };
      }
      case 'previous-month': {
        const start = new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1);
        const end = new Date(hoy.getFullYear(), hoy.getMonth(), 0);
        return { desde: start, hasta: end };
      }
      case 'last-30': {
        return { desde: this.addDays(new Date(), -30), hasta: hoy };
      }
      default:
        return null;
    }
  }

  private addDays(date: Date, days: number): Date {
    const copy = new Date(date);
    copy.setDate(copy.getDate() + days);
    return copy;
  }

  private toInputValue(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private obtenerMensajeError(err: any): string {
    if (err?.error?.message) return err.error.message;
    return 'No se pudo generar el reporte, intenta nuevamente.';
  }

  private seccionesSeleccionadas(): string[] {
    return Array.from(this.selectedSections);
  }
}
