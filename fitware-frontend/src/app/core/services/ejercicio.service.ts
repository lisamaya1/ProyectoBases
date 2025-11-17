import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ejercicio } from '../models/ejercicio.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EjercicioService {
  private readonly baseUrl = environment.apiUrl + '/ejercicios';

  constructor(private http: HttpClient) { }

  findAll(): Observable<Ejercicio[]> {
    return this.http.get<Ejercicio[]>(this.baseUrl);
  }

  findById(id: number): Observable<Ejercicio> {
    return this.http.get<Ejercicio>(this.baseUrl + '/' + id);
  }

  create(payload: Ejercicio): Observable<Ejercicio> {
    return this.http.post<Ejercicio>(this.baseUrl, payload);
  }

  update(id: number, payload: Ejercicio): Observable<Ejercicio> {
    return this.http.put<Ejercicio>(this.baseUrl + '/' + id, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.baseUrl + '/' + id);
  }
}
