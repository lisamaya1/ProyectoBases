import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Progreso } from '../models/progreso.model';
import { Cliente, RegistroClienteDTO } from '../models/cliente.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly baseUrl = environment.apiUrl + '/clientes';

  constructor(private http: HttpClient) { }

  registrarCliente(payload: RegistroClienteDTO): Observable<Cliente> {
    return this.http.post<Cliente>(this.baseUrl, payload);
  }

  listarClientes(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.baseUrl);
  }

  obtenerCliente(cedula: string): Observable<Cliente> {
    return this.http.get<Cliente>(this.baseUrl + '/' + cedula);
  }

  actualizarCliente(cedula: string, payload: RegistroClienteDTO): Observable<Cliente> {
    return this.http.put<Cliente>(this.baseUrl + '/' + cedula, payload);
  }

  eliminarCliente(cedula: string): Observable<void> {
    return this.http.delete<void>(this.baseUrl + '/' + cedula);
  }

  listarProgreso(cedula: string): Observable<Progreso[]> {
    return this.http.get<Progreso[]>(this.baseUrl + '/' + cedula + '/progreso');
  }

  agregarProgreso(cedula: string, progreso: Progreso): Observable<Progreso> {
    return this.http.post<Progreso>(this.baseUrl + '/' + cedula + '/progreso', progreso);
  }

  editarProgreso(id: number, progreso: Progreso): Observable<Progreso> {
    return this.http.put<Progreso>(this.baseUrl + '/progreso/' + id, progreso);
  }

  eliminarProgreso(id: number): Observable<void> {
    return this.http.delete<void>(this.baseUrl + '/progreso/' + id);
  }
}
