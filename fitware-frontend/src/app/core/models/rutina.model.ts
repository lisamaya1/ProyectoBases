import { PlanAlimentacion } from './plan-alimentacion.model';

export interface Rutina {
  id?: number;
  nombre: string;
  objetivo: string;
  duracionSemanas: number;
  entrenadorCedula?: string;
  entrenador?: any;
  planes?: PlanAlimentacion[];
  [key: string]: any;
}
