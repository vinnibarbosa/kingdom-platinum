import { environment } from '../environments/environment';
import type { AuthResponse, Ficha, FichaPayload, FichaResumo, Page } from '../types/ficha';

const API_BASE = environment.apiUrl;

class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

function getToken(): string | null {
  return localStorage.getItem('accessToken');
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set('Content-Type', 'application/json');

  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
    credentials: 'include',
  });

  if (!response.ok) {
    let message = response.statusText;
    try {
      const body = await response.json();
      message = body.message ?? body.error ?? message;
    } catch {
      /* ignore */
    }
    throw new ApiError(message, response.status);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  login(username: string, senha: string) {
    return request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, senha }),
    });
  },

  registrar(username: string, senha: string) {
    return request<AuthResponse>('/auth/registrar', {
      method: 'POST',
      body: JSON.stringify({ username, senha }),
    });
  },

  logout() {
    return request<void>('/auth/logout', { method: 'POST' });
  },

  listFichas(offset = 0, limit = 50) {
    return request<Page<FichaResumo>>(`/fichas?offset=${offset}&limit=${limit}`);
  },

  getFicha(id: number) {
    return request<Ficha>(`/fichas/${id}`);
  },

  updateFicha(id: number, payload: FichaPayload) {
    return request<Ficha>(`/fichas/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  createFicha(payload: FichaPayload) {
    return request<Ficha>('/fichas', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
};

export { ApiError };