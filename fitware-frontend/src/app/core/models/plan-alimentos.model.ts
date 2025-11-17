export interface PlanAlimentos {
  id?: number;
  planId?: number;
  alimentoId: number;
  cantidad: string;
  comidaDelDia: string;
  alimentoNombre?: string;
  plan?: any;
  alimento?: any;
  [key: string]: any;
}
