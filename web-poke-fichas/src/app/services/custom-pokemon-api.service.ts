import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

const API_BASE = '/api/public';

export interface CustomPokemonMove {
  name: string;
  category?: string;
  type?: string;
  style?: string;
  power?: number;
  accuracy?: number;
}

export interface CustomPokemonDetails {
  name: string;
  sprite?: string;
  types?: string[];
  abilities?: string[];
  moves?: CustomPokemonMove[];
  stats?: Partial<Record<'hp' | 'atk' | 'def' | 'satk' | 'sdef' | 'speed', number>>;
}

@Injectable({ providedIn: 'root' })
export class CustomPokemonApiService {
  private readonly http = inject(HttpClient);

  search(term: string): Observable<CustomPokemonDetails[]> {
    return this.http.get<CustomPokemonDetails[]>(`${API_BASE}/pokemon/custom`, {
      params: { termo: term },
    });
  }

  findByName(name: string): Observable<CustomPokemonDetails> {
    return this.http.get<CustomPokemonDetails>(`${API_BASE}/pokemon/custom/${encodeURIComponent(name)}`);
  }
}
