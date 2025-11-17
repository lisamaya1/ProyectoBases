import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Rutina } from '../models/rutina.model';
import { PlanAlimentacion } from '../models/plan-alimentacion.model';
import { PlanAlimentos } from '../models/plan-alimentos.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RutinaPlanService {
  private readonly baseUrl = environment.apiUrl + '/rutinas';

  constructor(private http: HttpClient) { }

  listarRutinas(entrenadorCedula?: string): Observable<Rutina[]> {
    let params = new HttpParams();
    if (entrenadorCedula) {
      params = params.set('entrenadorCedula', entrenadorCedula);
    }
    return this.http.get<Rutina[]>(this.baseUrl, { params });
  }

  obtenerRutina(id: number): Observable<Rutina> {
    return this.http.get<Rutina>(this.baseUrl + '/' + id);
  }

  crearRutina(entrenadorCedula: string, rutina: Rutina): Observable<Rutina> {
    return this.http.post<Rutina>(this.baseUrl + '/' + entrenadorCedula, rutina);
  }

  actualizarRutina(id: number, data: Rutina, entrenadorCedula?: string): Observable<Rutina> {
    let params = new HttpParams();
    if (entrenadorCedula) {
      params = params.set('entrenadorCedula', entrenadorCedula);
    }
    return this.http.put<Rutina>(this.baseUrl + '/' + id, data, { params });
  }

  eliminarRutina(id: number): Observable<void> {
    return this.http.delete<void>(this.baseUrl + '/' + id);
  }

  listarPlanes(rutinaId?: number): Observable<PlanAlimentacion[]> {
    let params = new HttpParams();
    if (rutinaId !== undefined && rutinaId !== null) {
      params = params.set('rutinaId', rutinaId.toString());
    }
    return this.http.get<PlanAlimentacion[]>(this.baseUrl + '/planes', { params });
  }

  obtenerPlan(planId: number): Observable<PlanAlimentacion> {
    return this.http.get<PlanAlimentacion>(this.baseUrl + '/planes/' + planId);
  }

  crearPlan(rutinaId: number, plan: PlanAlimentacion): Observable<PlanAlimentacion> {
    return this.http.post<PlanAlimentacion>(this.baseUrl + '/' + rutinaId + '/planes', plan);
  }

  actualizarPlan(planId: number, data: PlanAlimentacion, rutinaId?: number): Observable<PlanAlimentacion> {
    let params = new HttpParams();
    if (rutinaId !== undefined && rutinaId !== null) {
      params = params.set('rutinaId', rutinaId.toString());
    }
    return this.http.put<PlanAlimentacion>(this.baseUrl + '/planes/' + planId, data, { params });
  }

  eliminarPlan(planId: number): Observable<void> {
    return this.http.delete<void>(this.baseUrl + '/planes/' + planId);
  }

  listarAlimentos(planId: number): Observable<PlanAlimentos[]> {
    return this.http.get<PlanAlimentos[]>(this.baseUrl + '/planes/' + planId + '/alimentos');
  }

  obtenerAlimento(id: number): Observable<PlanAlimentos> {
    return this.http.get<PlanAlimentos>(this.baseUrl + '/planes/alimentos/' + id);
  }

  agregarAlimento(planId: number, alimentoId: number, cantidad: string, comida: string): Observable<PlanAlimentos> {
    const params = new HttpParams()
      .set('cantidad', cantidad)
      .set('comida', comida);
    return this.http.post<PlanAlimentos>(this.baseUrl + '/planes/' + planId + '/alimentos/' + alimentoId, null, { params });
  }

  actualizarAlimento(id: number, payload: PlanAlimentos, planId?: number, alimentoId?: number): Observable<PlanAlimentos> {
    let params = new HttpParams();
    if (planId) {
      params = params.set('planId', planId.toString());
    }
    if (alimentoId) {
      params = params.set('alimentoId', alimentoId.toString());
    }
    return this.http.put<PlanAlimentos>(this.baseUrl + '/planes/alimentos/' + id, payload, { params });
  }

  eliminarAlimento(id: number): Observable<void> {
    return this.http.delete<void>(this.baseUrl + '/planes/alimentos/' + id);
  }
}
