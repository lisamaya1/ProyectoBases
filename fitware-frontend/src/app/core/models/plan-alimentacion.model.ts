import { PlanAlimentos } from './plan-alimentos.model';

export interface PlanAlimentacion {
  id?: number;
  rutina?: any;
  rutinaId?: number;
  nombre: string;
  descripcion: string;
  duracionDias: number;
  fechaInicio: string;
  fechaFin: string;
  alimentos?: PlanAlimentos[];
  [key: string]: any;
}
