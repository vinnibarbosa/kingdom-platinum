import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, finalize, map, of, shareReplay, tap } from 'rxjs';

import { AuthResponse, RegisterRequest, Usuario } from '../models/ficha.model';

const API_BASE = '/api';
const TOKEN_KEY = 'accessToken';
const USER_KEY = 'currentUser';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenSignal = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly userSignal = signal<Usuario | null>(this.readUser());
  private refreshRequest?: Observable<string>;

  readonly isLoggedIn = computed(() => Boolean(this.tokenSignal()));
  readonly currentUser = computed(() => this.userSignal());

  get token(): string | null {
    return this.tokenSignal();
  }

  login(username: string, senha: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_BASE}/auth/login`, { username, senha }, { withCredentials: true }).pipe(
      tap((response) => this.storeSession(response)),
    );
  }

  refreshAccessToken(): Observable<string> {
    if (!this.refreshRequest) {
      this.refreshRequest = this.http.post<AuthResponse>(`${API_BASE}/auth/refresh`, {}, { withCredentials: true }).pipe(
        tap((response) => this.storeSession(response)),
        map((response) => response.accessToken),
        finalize(() => {
          this.refreshRequest = undefined;
        }),
        shareReplay({ bufferSize: 1, refCount: false }),
      );
    }

    return this.refreshRequest;
  }

  shouldRefreshAccessToken(leewaySeconds = 60): boolean {
    const expiration = this.readTokenPayload()?.exp;
    return typeof expiration !== 'number' || expiration * 1000 <= Date.now() + leewaySeconds * 1000;
  }

  register(request: RegisterRequest): Observable<void> {
    return this.http.post<void>(`${API_BASE}/auth/registrar`, request, { withCredentials: true });
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${API_BASE}/auth/logout`, {}, { withCredentials: true }).pipe(
      catchError(() => of(undefined)),
      tap(() => this.clearSession()),
      map(() => undefined),
    );
  }

  clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.tokenSignal.set(null);
    this.userSignal.set(null);
  }

  private storeSession(response: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, response.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify(response.usuario));
    this.tokenSignal.set(response.accessToken);
    this.userSignal.set(response.usuario);
  }

  private readUser(): Usuario | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) {
      return null;
    }

    try {
      const user = JSON.parse(raw) as Usuario;
      const tokenInfo = this.readTokenContext();
      const hydrated = {
        ...user,
        idEntidade: user.idEntidade ?? tokenInfo.idEntidade,
        idOrganizacao: user.idOrganizacao ?? tokenInfo.idOrganizacao,
      };
      localStorage.setItem(USER_KEY, JSON.stringify(hydrated));
      return hydrated;
    } catch {
      localStorage.removeItem(USER_KEY);
      return null;
    }
  }

  private readTokenContext(): Pick<Usuario, 'idEntidade' | 'idOrganizacao'> {
    const payload = this.readTokenPayload(localStorage.getItem(TOKEN_KEY));
    if (!payload) {
      return {};
    }

    return {
      idEntidade: payload.idEntidade ? Number(payload.idEntidade) : undefined,
      idOrganizacao: payload.idOrganizacao ? Number(payload.idOrganizacao) : undefined,
    };
  }

  private readTokenPayload(token = this.token): { exp?: number; idEntidade?: string; idOrganizacao?: string } | null {
    if (!token) {
      return null;
    }

    try {
      const encodedPayload = token.split('.')[1] ?? '';
      const base64 = encodedPayload.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
      return JSON.parse(atob(padded)) as { exp?: number; idEntidade?: string; idOrganizacao?: string };
    } catch {
      return null;
    }
  }
}
