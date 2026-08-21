import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { CompraLojaResponse, FichaCompra, LojaItem, LojaItemPayload } from '../models/loja.model';

const API_BASE = '/api/loja';

@Injectable({ providedIn: 'root' })
export class LojaApiService {
  private readonly http = inject(HttpClient);

  list(): Observable<LojaItem[]> {
    return this.http.get<LojaItem[]>(`${API_BASE}/itens`, { withCredentials: true });
  }

  listAdmin(): Observable<LojaItem[]> {
    return this.http.get<LojaItem[]>(`${API_BASE}/itens/administracao`, { withCredentials: true });
  }

  listOwnedFichas(): Observable<FichaCompra[]> {
    return this.http.get<FichaCompra[]>(`${API_BASE}/fichas`, { withCredentials: true });
  }

  create(payload: LojaItemPayload): Observable<LojaItem> {
    return this.http.post<LojaItem>(`${API_BASE}/itens`, payload, { withCredentials: true });
  }

  update(id: number, payload: LojaItemPayload): Observable<LojaItem> {
    return this.http.put<LojaItem>(`${API_BASE}/itens/${id}`, payload, { withCredentials: true });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/itens/${id}`, { withCredentials: true });
  }

  buy(idItem: number, idFicha: number, quantidade: number): Observable<CompraLojaResponse> {
    return this.http.post<CompraLojaResponse>(`${API_BASE}/compras`, { idItem, idFicha, quantidade }, { withCredentials: true });
  }
}
