import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { RedefinirSenhaRequest } from '../models/ficha.model';

const API_BASE = '/api';

@Injectable({ providedIn: 'root' })
export class UsuarioApiService {
  private readonly http = inject(HttpClient);

  redefinePassword(request: RedefinirSenhaRequest): Observable<void> {
    return this.http.put<void>(`${API_BASE}/usuarios/redefinir-senha`, request, {
      withCredentials: true,
    });
  }
}
