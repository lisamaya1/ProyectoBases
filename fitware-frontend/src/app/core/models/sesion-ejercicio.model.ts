export interface SesionEjercicio {
  id?: number;
  sesionId?: number;
  ejercicioId: number;
  repeticiones: number;
  series: number;
  estado: string;
  ejercicioNombre?: string;
}
