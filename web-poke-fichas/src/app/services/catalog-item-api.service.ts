import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, shareReplay } from 'rxjs';

import { environment } from '../../environments/environment';

export interface CatalogItem {
  name: string;
  description: string;
  category: string;
  sprite: string;
  code?: string;
  price?: number;
}

interface SupabaseItemRow {
  name?: string | null;
  item_desc?: string | null;
  category?: string | null;
  sprite?: string | null;
}

interface PokeApiItemRow { name?: string | null; }

interface RemoteCatalogRow {
  id?: string | number; code?: string; codigo?: string; name?: string; nome?: string; label?: string;
  description?: string; descricao?: string; desc?: string; item_desc?: string; category?: string; categoria?: string;
  sprite?: string; icon?: string; icone?: string; image?: string; imagem?: string;
  price?: number | string; preco?: number | string; valor?: number | string;
}

const KINGDOM_CATALOG_URL = '/data/items.json';
const KINGDOM_CATALOG_SOURCE_URL = 'https://raw.githubusercontent.com/alphx-r/kingdomplatinum/main/items.json';

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

      this.catalog$ = forkJoin({ officialItems, githubItems: this.loadKingdomCatalog(), customItems: this.loadCustomItems() }).pipe(
        map(({ officialItems, githubItems, customItems }) => this.merge(officialItems, githubItems, customItems)),
        shareReplay({ bufferSize: 1, refCount: false }),
      );
    }
    return this.catalog$;
  }

  /** Curated items explicitly published for the Kingdom Platinum store. */
  listKingdomCatalog(): Observable<CatalogItem[]> {
    return this.loadKingdomCatalog();
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

  private loadKingdomCatalog(): Observable<CatalogItem[]> {
    return this.http.get<unknown>(KINGDOM_CATALOG_URL).pipe(
      map((payload) => this.extractRows(payload)
        .map((row) => this.toRemoteItem(row))
        .filter((item): item is CatalogItem => !!item)),
      catchError(() => of([] as CatalogItem[])),
    );
  }

  private extractRows(payload: unknown): RemoteCatalogRow[] {
    const rows: RemoteCatalogRow[] = [];
    const visit = (value: unknown): void => {
      if (Array.isArray(value)) {
        value.forEach(visit);
        return;
      }
      if (!value || typeof value !== 'object') return;
      const row = value as RemoteCatalogRow;
      if (typeof row.name === 'string' || typeof row.nome === 'string' || typeof row.label === 'string') {
        rows.push(row);
        return;
      }
      Object.values(value as Record<string, unknown>).forEach(visit);
    };
    visit(payload);
    return rows;
  }

  private toRemoteItem(row: RemoteCatalogRow): CatalogItem | null {
    const name = String(row.name ?? row.nome ?? row.label ?? '').trim();
    if (!name) return null;
    const image = String(row.sprite ?? row.icon ?? row.icone ?? row.image ?? row.imagem ?? '').trim();
    return {
      name,
      description: String(row.description ?? row.descricao ?? row.desc ?? row.item_desc ?? '').trim(),
      category: String(row.category ?? row.categoria ?? 'Item').trim() || 'Item',
      sprite: image && !/^https?:|^data:/i.test(image) ? new URL(image, KINGDOM_CATALOG_SOURCE_URL).toString() : image,
      code: String(row.code ?? row.codigo ?? row.id ?? '').trim() || undefined,
      price: numericValue(row.price ?? row.preco ?? row.valor),
    };
  }

  private toOfficialItem(name: string): CatalogItem {
    return {
      name: displayName(name),
      description: '',
      category: 'Item',
      sprite: `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/${name}.png`,
    };
  }

  private merge(officialItems: CatalogItem[], githubItems: CatalogItem[], customItems: CatalogItem[]): CatalogItem[] {
    const byName = new Map<string, CatalogItem>();
    officialItems.forEach((item) => byName.set(normalize(item.name), item));
    [...githubItems, ...customItems].forEach((item) => {
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

function numericValue(value: unknown): number | undefined {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : undefined;
}
