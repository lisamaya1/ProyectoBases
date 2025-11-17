import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReportSummary } from '../models/reportes.model';

@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly baseUrl = environment.apiUrl + '/reportes';

  constructor(private http: HttpClient) {}

  obtenerResumen(desde?: string | null, hasta?: string | null, sections?: string[]): Observable<ReportSummary> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', desde);
    if (hasta) params = params.set('hasta', hasta);
    if (sections && sections.length) {
      sections.forEach(section => params = params.append('sections', section));
    }
    return this.http.get<ReportSummary>(this.baseUrl, { params });
  }

  descargarPdf(desde?: string | null, hasta?: string | null, sections?: string[]): Observable<Blob> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', desde);
    if (hasta) params = params.set('hasta', hasta);
    if (sections && sections.length) {
      sections.forEach(section => params = params.append('sections', section));
    }
    return this.http.get(this.baseUrl + '/pdf', { params, responseType: 'blob' });
  }
}
