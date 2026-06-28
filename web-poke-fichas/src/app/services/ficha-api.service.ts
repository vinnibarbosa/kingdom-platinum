import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Ficha, FichaHistorico, FichaPayload, FichaResumo, Page } from '../models/ficha.model';

const API_BASE = '/api';

@Injectable({ providedIn: 'root' })
export class FichaApiService {
  private readonly http = inject(HttpClient);

  list(offset = 0, limit = 50): Observable<Page<FichaResumo>> {
    return this.http.get<Page<FichaResumo>>(`${API_BASE}/fichas?offset=${offset}&limit=${limit}`, {
      withCredentials: true,
    });
  }

  get(id: number): Observable<Ficha> {
    return this.http.get<Ficha>(`${API_BASE}/fichas/${id}`, {
      withCredentials: true,
    });
  }

  getForAdmin(id: number): Observable<Ficha> {
    return this.http.get<Ficha>(`${API_BASE}/fichas/${id}/administracao`, {
      withCredentials: true,
    });
  }

  getPublic(id: number): Observable<Ficha> {
    return this.http.get<Ficha>(`${API_BASE}/fichas/publicas/${id}`);
  }

  getPublicBySlug(slug: string): Observable<Ficha> {
    return this.http.get<Ficha>(`${API_BASE}/fichas/publicas/slug/${encodeURIComponent(slug)}`);
  }

  getHistory(id: number): Observable<FichaHistorico[]> {
    return this.http.get<FichaHistorico[]>(`${API_BASE}/fichas/${id}/historico`, {
      withCredentials: true,
    });
  }

  update(id: number, payload: FichaPayload): Observable<Ficha> {
    return this.http.put<Ficha>(`${API_BASE}/fichas/${id}`, payload, {
      withCredentials: true,
    });
  }

  updateForAdmin(id: number, payload: FichaPayload): Observable<Ficha> {
    return this.http.put<Ficha>(`${API_BASE}/fichas/${id}/administracao`, payload, {
      withCredentials: true,
    });
  }

  create(payload: FichaPayload): Observable<Ficha> {
    return this.http.post<Ficha>(`${API_BASE}/fichas`, payload, {
      withCredentials: true,
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/fichas/${id}`, {
      withCredentials: true,
    });
  }
}
