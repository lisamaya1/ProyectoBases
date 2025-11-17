import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Sesion } from '../models/sesion.model';
import { SesionEjercicio } from '../models/sesion-ejercicio.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SesionService {
  private readonly baseUrl = environment.apiUrl + '/sesiones';

  constructor(private http: HttpClient) { }

  listarSesiones(clienteCedula?: string): Observable<Sesion[]> {
    let params = new HttpParams();
    if (clienteCedula) {
      params = params.set('clienteCedula', clienteCedula);
    }
    return this.http.get<Sesion[]>(this.baseUrl, { params });
  }

  obtenerSesion(id: number): Observable<Sesion> {
    return this.http.get<Sesion>(this.baseUrl + '/' + id);
  }

  crearSesion(clienteCedula: string, sesion: Sesion): Observable<Sesion> {
    return this.http.post<Sesion>(this.baseUrl + '/' + clienteCedula, sesion);
  }

  actualizarSesion(id: number, sesion: Sesion, clienteCedula?: string): Observable<Sesion> {
    let params = new HttpParams();
    if (clienteCedula) {
      params = params.set('clienteCedula', clienteCedula);
    }
    return this.http.put<Sesion>(this.baseUrl + '/' + id, sesion, { params });
  }

  eliminarSesion(id: number): Observable<void> {
    return this.http.delete<void>(this.baseUrl + '/' + id);
  }

  listarEjercicios(sesionId: number): Observable<SesionEjercicio[]> {
    return this.http.get<SesionEjercicio[]>(this.baseUrl + '/' + sesionId + '/ejercicios');
  }

  obtenerSesionEjercicio(id: number): Observable<SesionEjercicio> {
    return this.http.get<SesionEjercicio>(this.baseUrl + '/ejercicios/' + id);
  }

  asignarEjercicio(sesionId: number, ejercicioId: number, repeticiones: number, series: number, estado: string): Observable<SesionEjercicio> {
    const params = new HttpParams()
      .set('repeticiones', repeticiones.toString())
      .set('series', series.toString())
      .set('estado', estado);
    return this.http.post<SesionEjercicio>(this.baseUrl + '/' + sesionId + '/ejercicios/' + ejercicioId, null, { params });
  }

  actualizarSesionEjercicio(id: number, cambios: { ejercicioId?: number; repeticiones?: number; series?: number; estado?: string; }): Observable<SesionEjercicio> {
    let params = new HttpParams();
    if (cambios.ejercicioId !== undefined) {
      params = params.set('ejercicioId', cambios.ejercicioId.toString());
    }
    if (cambios.repeticiones !== undefined) {
      params = params.set('repeticiones', cambios.repeticiones.toString());
    }
    if (cambios.series !== undefined) {
      params = params.set('series', cambios.series.toString());
    }
    if (cambios.estado !== undefined && cambios.estado !== null) {
      params = params.set('estado', cambios.estado);
    }
    return this.http.put<SesionEjercicio>(this.baseUrl + '/ejercicios/' + id, {}, { params });
  }

  eliminarSesionEjercicio(id: number): Observable<void> {
    return this.http.delete<void>(this.baseUrl + '/ejercicios/' + id);
  }
}
