import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, shareReplay } from 'rxjs';

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

interface PokeApiItemRow { name?: string | null; }

@Injectable({ providedIn: 'root' })
export class CatalogItemApiService {
  private readonly http = inject(HttpClient);
  private catalog$?: Observable<CatalogItem[]>;

  /** Same broad catalog used by the inventory, cached after its first load. */
  list(): Observable<CatalogItem[]> {
    if (!this.catalog$) {
      const officialItems = this.http.get<{ results?: PokeApiItemRow[] }>('https://pokeapi.co/api/v2/item?limit=10000').pipe(
        map((response) => (response.results ?? [])
          .map((item) => item.name?.trim() ?? '')
          .filter(Boolean)
          .filter((name) => !this.isHiddenCatalogItem(name))
          .map((name) => this.toOfficialItem(name))),
        catchError(() => of([] as CatalogItem[])),
      );

      this.catalog$ = forkJoin({ officialItems, customItems: this.loadCustomItems() }).pipe(
        map(({ officialItems, customItems }) => this.merge(officialItems, customItems)),
        shareReplay({ bufferSize: 1, refCount: false }),
      );
    }
    return this.catalog$;
  }

  details(item: CatalogItem): Observable<CatalogItem> {
    const code = slugify(item.name);
    if (!code) return of(item);

    return this.http.get<{
      category?: { name?: string | null };
      effect_entries?: { effect?: string | null; short_effect?: string | null; language?: { name?: string | null } }[];
      flavor_text_entries?: { text?: string | null; language?: { name?: string | null } }[];
      sprites?: { default?: string | null };
    }>(`https://pokeapi.co/api/v2/item/${code}`).pipe(
      map((data) => {
        const effect = data.effect_entries?.find((entry) => ['pt-BR', 'pt'].includes(entry.language?.name ?? ''));
        const flavor = data.flavor_text_entries?.find((entry) => ['pt-BR', 'pt'].includes(entry.language?.name ?? ''));
        return {
          ...item,
          description: item.description || effect?.short_effect || effect?.effect || flavor?.text?.replace(/\s+/g, ' ') || '',
          category: item.category === 'Item' ? data.category?.name || item.category : item.category,
          sprite: item.sprite || data.sprites?.default || '',
        };
      }),
      catchError(() => of(item)),
    );
  }

  private loadCustomItems(): Observable<CatalogItem[]> {
    if (!this.isConfigured()) return of([]);

    const url = `${trimTrailingSlash(environment.supabasePokemonUrl)}/rest/v1/items`;
    return this.http.get<SupabaseItemRow[]>(url, {
      headers: new HttpHeaders({
        apikey: environment.supabasePokemonAnonKey,
        Authorization: `Bearer ${environment.supabasePokemonAnonKey}`,
        Accept: 'application/json',
      }),
      params: { select: 'name,item_desc,category,sprite', approved: 'eq.true', order: 'name.asc' },
    }).pipe(
      map((items) => items
        .filter((item) => item.name?.trim())
        .map((item) => ({
          name: item.name!.trim(),
          description: item.item_desc?.trim() ?? '',
          category: item.category?.trim() ?? 'Item',
          sprite: item.sprite?.trim() ?? '',
        }))),
      catchError(() => of([])),
    );
  }

  private toOfficialItem(name: string): CatalogItem {
    return {
      name: displayName(name),
      description: '',
      category: 'Item',
      sprite: `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/${name}.png`,
    };
  }

  private merge(officialItems: CatalogItem[], customItems: CatalogItem[]): CatalogItem[] {
    const byName = new Map<string, CatalogItem>();
    officialItems.forEach((item) => byName.set(normalize(item.name), item));
    customItems.forEach((item) => {
      const key = normalize(item.name);
      const official = byName.get(key);
      byName.set(key, {
        ...official,
        ...item,
        description: item.description || official?.description || '',
        category: item.category || official?.category || 'Item',
        sprite: item.sprite || official?.sprite || '',
      });
    });
    return [...byName.values()].sort((first, second) => first.name.localeCompare(second.name, 'pt-BR'));
  }

  private isHiddenCatalogItem(name: string): boolean {
    return /^(?:tm|tr)(?:\d|-)/i.test(name)
      || /^data-card(?:-\d+)?$/i.test(name)
      || /^dynamax-crystal(?:-|$)/i.test(name);
  }

  private isConfigured(): boolean {
    return !!environment.supabasePokemonUrl && !!environment.supabasePokemonAnonKey;
  }
}

function displayName(value: string): string {
  return value.split('-').map((part) => part ? `${part[0].toUpperCase()}${part.slice(1)}` : '').join(' ');
}

function normalize(value: string): string {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/[^a-z0-9]+/g, '');
}

function slugify(value: string): string {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
    .replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
}

function trimTrailingSlash(value: string): string {
  return value.replace(/\/+$/, '');
}
