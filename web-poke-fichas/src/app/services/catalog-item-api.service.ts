import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, of } from 'rxjs';

import { environment } from '../../environments/environment';

export interface CatalogItem {
  name: string;
  description: string;
  category: string;
  sprite: string;
}

interface SupabaseItemRow {
  name?: string | null;
  item_desc?: string | null;
  category?: string | null;
  sprite?: string | null;
}

@Injectable({ providedIn: 'root' })
export class CatalogItemApiService {
  private readonly http = inject(HttpClient);

  search(term: string): Observable<CatalogItem[]> {
    const query = term.trim();
    if (!query || !this.isConfigured()) {
      return of([]);
    }

    const url = `${trimTrailingSlash(environment.supabasePokemonUrl)}/rest/v1/items`;
    return this.http.get<SupabaseItemRow[]>(url, {
      headers: new HttpHeaders({
        apikey: environment.supabasePokemonAnonKey,
        Authorization: `Bearer ${environment.supabasePokemonAnonKey}`,
        Accept: 'application/json',
      }),
      params: {
        select: 'name,item_desc,category,sprite',
        approved: 'eq.true',
        or: `(name.ilike.*${escapeSearch(query)}*,category.ilike.*${escapeSearch(query)}*)`,
        order: 'name.asc',
        limit: 40,
      },
    }).pipe(
      map((items) => items
        .filter((item) => item.name?.trim())
        .map((item) => ({
          name: item.name!.trim(),
          description: item.item_desc?.trim() ?? '',
          category: item.category?.trim() ?? '',
          sprite: item.sprite?.trim() ?? '',
        }))),
    );
  }

  private isConfigured(): boolean {
    return !!environment.supabasePokemonUrl && !!environment.supabasePokemonAnonKey;
  }
}

function escapeSearch(value: string): string {
  return value.replace(/[(),.*]/g, ' ').trim();
}

function trimTrailingSlash(value: string): string {
  return value.replace(/\/+$/, '');
}
