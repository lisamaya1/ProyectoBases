import { SesionEjercicio } from './sesion-ejercicio.model';

export interface Sesion {
  id?: number;
  estado: string;
  fechaInicio: string;
  fechaFin: string | null;
  gastoCalorico: number | null;
  clienteCedula?: string;
  cliente?: any;

  ejercicios?: SesionEjercicio[];
  [key: string]: any;
}
