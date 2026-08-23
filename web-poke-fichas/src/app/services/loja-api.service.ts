import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { CatalogoLojaImportacao, CompraLojaItem, CompraLojaResponse, FichaCompra, LojaCupom, LojaCupomPayload, LojaItem, LojaItemPayload } from '../models/loja.model';

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

    importCatalog(items: LojaItemPayload[]): Observable<CatalogoLojaImportacao> {
      return this.http.post<CatalogoLojaImportacao>(`${API_BASE}/itens/importar-catalogo`, {
        itens: items,
      }, { withCredentials: true });
    }

  update(id: number, payload: LojaItemPayload): Observable<LojaItem> {
    return this.http.put<LojaItem>(`${API_BASE}/itens/${id}`, payload, { withCredentials: true });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/itens/${id}`, { withCredentials: true });
  }

  buy(itens: CompraLojaItem[], idFicha: number, cupom?: string): Observable<CompraLojaResponse> {
    return this.http.post<CompraLojaResponse>(`${API_BASE}/compras`, { idFicha, itens, cupom: cupom?.trim() || null }, { withCredentials: true });
  }

  listCoupons(): Observable<LojaCupom[]> { return this.http.get<LojaCupom[]>(`${API_BASE}/cupons/administracao`, { withCredentials: true }); }
  createCoupon(payload: LojaCupomPayload): Observable<LojaCupom> { return this.http.post<LojaCupom>(`${API_BASE}/cupons`, payload, { withCredentials: true }); }
  updateCoupon(id: number, payload: LojaCupomPayload): Observable<LojaCupom> { return this.http.put<LojaCupom>(`${API_BASE}/cupons/${id}`, payload, { withCredentials: true }); }
  deleteCoupon(id: number): Observable<void> { return this.http.delete<void>(`${API_BASE}/cupons/${id}`, { withCredentials: true }); }
}
