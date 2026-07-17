import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, shareReplay, switchMap } from 'rxjs';

import { environment } from '../../environments/environment';

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
  slug?: string;
  dex?: number;
  sprite?: string;
  searchTerms?: string[];
  types?: string[];
  abilities?: string[];
  moves?: CustomPokemonMove[];
  stats?: Partial<Record<'hp' | 'atk' | 'def' | 'satk' | 'sdef' | 'speed', number>>;
}

interface SupabasePokemonRow {
  id?: string | number | null;
  name?: string | null;
  url_slug?: string | null;
  sprite?: string | null;
  sprite_shiny?: string | null;
  tipo1?: string | null;
  tipo2?: string | null;
  habilidade1?: string | null;
  habilidade2?: string | null;
  habilidade_oculta?: string | null;
  habilidade_mega?: string | null;
  hp?: number | null;
  atk?: number | null;
  def?: number | null;
  spa?: number | null;
  spd?: number | null;
  spe?: number | null;
  moves_level?: unknown;
  moves_tm?: unknown;
  moves_egg?: unknown;
  moves_tutor?: unknown;
  approved?: boolean | null;
}

interface SupabaseMoveRow {
  name?: string | null;
  type?: string | null;
  damage_class?: string | null;
  power?: number | null;
  accuracy?: number | null;
  contest_style?: string | null;
  approved?: boolean | null;
}

@Injectable({ providedIn: 'root' })
export class CustomPokemonApiService {
  private readonly http = inject(HttpClient);
  private combinedCatalog$?: Observable<CustomPokemonDetails[]>;
  private supabaseCatalog$?: Observable<CustomPokemonDetails[]>;
  private supabaseMoves$?: Observable<Map<string, CustomPokemonMove>>;
  private backendCatalog$?: Observable<CustomPokemonDetails[]>;

  search(term: string): Observable<CustomPokemonDetails[]> {
    return this.searchCatalog(term);
  }

  findByName(name: string): Observable<CustomPokemonDetails> {
    return this.findCatalogByName(name);
  }

  private searchCatalog(term: string): Observable<CustomPokemonDetails[]> {
    const normalizedTerm = normalize(term);
    return forkJoin({
      catalog: this.loadCombinedCatalog().pipe(catchError(() => of([]))),
      backendTargeted: normalizedTerm
        ? this.searchBackendCatalog(term).pipe(catchError(() => of([])))
        : of([]),
      targeted: normalizedTerm
        ? this.searchSupabaseCatalog(term).pipe(catchError(() => of([])))
        : of([]),
    }).pipe(
      map(({ catalog, backendTargeted, targeted }) => mergeCatalogs(catalog, backendTargeted, targeted)
        .filter((pokemon) => !normalizedTerm
          || pokemonSearchTerms(pokemon).some((term) => term.includes(normalizedTerm))
          || String(pokemon.dex ?? '').includes(normalizedTerm.replace('#', '')))
        .slice(0, 50)),
    );
  }

  private findCatalogByName(name: string): Observable<CustomPokemonDetails> {
    const normalizedName = normalize(name);
    return forkJoin({
      catalog: this.loadCombinedCatalog().pipe(catchError(() => of([]))),
      backendTargeted: normalizedName
        ? this.searchBackendCatalog(name).pipe(catchError(() => of([])))
        : of([]),
      targeted: normalizedName
        ? this.searchSupabaseCatalog(name).pipe(catchError(() => of([])))
        : of([]),
    }).pipe(
      map(({ catalog, backendTargeted, targeted }) => mergeCatalogs(catalog, backendTargeted, targeted)
        .find((pokemon) => pokemonSearchTerms(pokemon).includes(normalizedName))),
      map((pokemon) => {
        if (!pokemon) {
          throw new Error('Pokemon customizado nao encontrado');
        }
        return pokemon;
      }),
    );
  }

  private loadCombinedCatalog(): Observable<CustomPokemonDetails[]> {
    if (!this.combinedCatalog$) {
      this.combinedCatalog$ = forkJoin({
        backend: this.loadBackendCatalog().pipe(catchError(() => of([]))),
        supabase: this.loadSupabaseCatalog().pipe(catchError(() => of([]))),
      }).pipe(
        map(({ backend, supabase }) => mergeCatalogs(backend, supabase)),
        shareReplay({ bufferSize: 1, refCount: true }),
      );
    }

    return this.combinedCatalog$;
  }

  private loadBackendCatalog(): Observable<CustomPokemonDetails[]> {
    if (!environment.apiUrl) {
      return of([]);
    }

    if (!this.backendCatalog$) {
      const url = `${trimTrailingSlash(environment.apiUrl)}/pokemon/custom`;
      this.backendCatalog$ = this.http.get<CustomPokemonDetails[]>(url, {
        params: { termo: '' },
      }).pipe(
        map((catalog) => catalog.map(normalizeCatalogPokemon).filter((pokemon) => !!pokemon.name)),
        shareReplay({ bufferSize: 1, refCount: true }),
      );
    }

    return this.backendCatalog$;
  }

  private searchBackendCatalog(term: string): Observable<CustomPokemonDetails[]> {
    if (!environment.apiUrl || !term.trim()) {
      return of([]);
    }

    const url = `${trimTrailingSlash(environment.apiUrl)}/pokemon/custom`;
    return this.http.get<CustomPokemonDetails[]>(url, {
      params: { termo: term.trim() },
    }).pipe(
      map((catalog) => catalog.map(normalizeCatalogPokemon).filter((pokemon) => !!pokemon.name)),
    );
  }

  private searchSupabaseCatalog(term: string): Observable<CustomPokemonDetails[]> {
    const search = supabaseSearchText(term);
    if (!this.isSupabaseConfigured() || !search) {
      return of([]);
    }

    return this.loadSupabaseMoves().pipe(
      switchMap((movesByName) => this.fetchSupabaseRowsBySearch<SupabasePokemonRow>(
        environment.supabasePokemonTable,
        search,
        80,
      ).pipe(
        map((rows) => rows
          .map((pokemon) => pokemonFromSupabase(pokemon, movesByName))
          .filter((pokemon) => !!pokemon.name)),
      )),
    );
  }

  private loadSupabaseCatalog(): Observable<CustomPokemonDetails[]> {
    if (!this.isSupabaseConfigured()) {
      return of([]);
    }

    if (!this.supabaseCatalog$) {
      this.supabaseCatalog$ = forkJoin({
        movesByName: this.loadSupabaseMoves(),
        pokemons: this.fetchSupabaseRows<SupabasePokemonRow>(
          environment.supabasePokemonTable,
          environment.supabasePokemonLimit,
        ).pipe(
          catchError(() => of([])),
        ),
      }).pipe(
        map(({ movesByName, pokemons }) => {
          return pokemons
            .map((pokemon) => pokemonFromSupabase(pokemon, movesByName))
            .filter((pokemon) => !!pokemon.name);
        }),
        shareReplay({ bufferSize: 1, refCount: true }),
      );
    }

    return this.supabaseCatalog$;
  }

  private loadSupabaseMoves(): Observable<Map<string, CustomPokemonMove>> {
    if (!this.isSupabaseConfigured()) {
      return of(new Map<string, CustomPokemonMove>());
    }

    if (!this.supabaseMoves$) {
      this.supabaseMoves$ = this.fetchSupabaseRows<SupabaseMoveRow>(environment.supabaseMoveTable, 5000).pipe(
        map((moves) => moves
          .map(moveFromSupabase)
          .filter((move) => !!move.name)
          .reduce((index, move) => index.set(normalize(move.name), move), new Map<string, CustomPokemonMove>())),
        catchError(() => of(new Map<string, CustomPokemonMove>())),
        shareReplay({ bufferSize: 1, refCount: true }),
      );
    }

    return this.supabaseMoves$;
  }

  private fetchSupabaseRows<T>(table: string, limit: number): Observable<T[]> {
    const url = `${trimTrailingSlash(environment.supabasePokemonUrl)}/rest/v1/${encodeURIComponent(table)}`;
    return this.http.get<T[]>(url, {
      headers: new HttpHeaders({
        apikey: environment.supabasePokemonAnonKey,
        Authorization: `Bearer ${environment.supabasePokemonAnonKey}`,
        Accept: 'application/json',
      }),
      params: {
        select: '*',
        limit,
      },
    });
  }

  private fetchSupabaseRowsBySearch<T>(table: string, term: string, limit: number): Observable<T[]> {
    const url = `${trimTrailingSlash(environment.supabasePokemonUrl)}/rest/v1/${encodeURIComponent(table)}`;
    return this.http.get<T[]>(url, {
      headers: new HttpHeaders({
        apikey: environment.supabasePokemonAnonKey,
        Authorization: `Bearer ${environment.supabasePokemonAnonKey}`,
        Accept: 'application/json',
      }),
      params: {
        select: '*',
        limit,
        or: `(name.ilike.*${term}*,url_slug.ilike.*${term}*,id.ilike.*${term}*)`,
      },
    });
  }

  private isSupabaseConfigured(): boolean {
    return !!environment.supabasePokemonUrl
      && !!environment.supabasePokemonAnonKey
      && !!environment.supabasePokemonTable
      && !!environment.supabaseMoveTable;
  }
}

function pokemonFromSupabase(row: SupabasePokemonRow, movesByName: Map<string, CustomPokemonMove>): CustomPokemonDetails {
  const moves = [
    ...moveList(row.moves_level, movesByName),
    ...moveList(row.moves_tm, movesByName),
    ...moveList(row.moves_egg, movesByName),
    ...moveList(row.moves_tutor, movesByName),
  ];

  return {
    name: firstText(row.name, row.url_slug),
    slug: firstText(row.url_slug),
    dex: dexNumber(row.id),
    sprite: firstText(row.sprite, row.sprite_shiny),
    searchTerms: uniqueTexts(row.name, row.url_slug, row.id === undefined || row.id === null ? undefined : String(row.id)),
    types: uniqueTexts(row.tipo1, row.tipo2),
    abilities: uniqueTexts(row.habilidade1, row.habilidade2, row.habilidade_oculta, row.habilidade_mega),
    moves: uniqueMoves(moves),
    stats: {
      hp: numberOrUndefined(row.hp),
      atk: numberOrUndefined(row.atk),
      def: numberOrUndefined(row.def),
      satk: numberOrUndefined(row.spa),
      sdef: numberOrUndefined(row.spd),
      speed: numberOrUndefined(row.spe),
    },
  };
}

function mergeCatalogs(...catalogs: CustomPokemonDetails[][]): CustomPokemonDetails[] {
  const byKey = new Map<string, CustomPokemonDetails>();

  catalogs.flat()
    .map(normalizeCatalogPokemon)
    .filter((pokemon) => !!pokemon.name)
    .forEach((pokemon) => {
      const keys = pokemonSearchTerms(pokemon);
      const existingKey = keys.find((key) => byKey.has(key));
      if (existingKey) {
        byKey.set(existingKey, mergePokemon(byKey.get(existingKey)!, pokemon));
        return;
      }

      byKey.set(keys[0] || normalize(pokemon.name), pokemon);
    });

  return [...byKey.values()];
}

function normalizeCatalogPokemon(pokemon: CustomPokemonDetails): CustomPokemonDetails {
  const name = firstText(pokemon.name, pokemon.slug);
  return {
    ...pokemon,
    name,
    slug: firstText(pokemon.slug),
    searchTerms: uniqueTexts(
      name,
      pokemon.slug,
      ...(pokemon.searchTerms ?? []),
      pokemon.dex === undefined ? undefined : String(pokemon.dex),
    ),
    types: uniqueTexts(...(pokemon.types ?? [])),
    abilities: uniqueTexts(...(pokemon.abilities ?? [])),
    moves: uniqueMoves(pokemon.moves ?? []),
  };
}

function mergePokemon(base: CustomPokemonDetails, incoming: CustomPokemonDetails): CustomPokemonDetails {
  return {
    ...base,
    ...incoming,
    name: incoming.name || base.name,
    slug: incoming.slug || base.slug,
    dex: incoming.dex ?? base.dex,
    sprite: incoming.sprite || base.sprite,
    searchTerms: uniqueTexts(
      base.name,
      base.slug,
      incoming.name,
      incoming.slug,
      ...(base.searchTerms ?? []),
      ...(incoming.searchTerms ?? []),
      base.dex === undefined ? undefined : String(base.dex),
      incoming.dex === undefined ? undefined : String(incoming.dex),
    ),
    types: uniqueTexts(...(base.types ?? []), ...(incoming.types ?? [])),
    abilities: uniqueTexts(...(base.abilities ?? []), ...(incoming.abilities ?? [])),
    moves: uniqueMoves([...(base.moves ?? []), ...(incoming.moves ?? [])]),
    stats: {
      ...(base.stats ?? {}),
      ...(incoming.stats ?? {}),
    },
  };
}

function pokemonSearchTerms(pokemon: CustomPokemonDetails): string[] {
  return uniqueTexts(
    pokemon.name,
    pokemon.slug,
    ...(pokemon.searchTerms ?? []),
  ).map((term) => normalize(term));
}

function dexNumber(value: unknown): number | undefined {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value;
  }

  const text = String(value ?? '').trim();
  if (!text) {
    return undefined;
  }

  const match = text.match(/^#?(\d+)$/);
  return match ? Number(match[1]) : undefined;
}

function moveFromSupabase(row: SupabaseMoveRow): CustomPokemonMove {
  return {
    name: firstText(row.name),
    category: firstText(row.damage_class),
    type: firstText(row.type),
    style: firstText(row.contest_style),
    power: numberOrUndefined(row.power),
    accuracy: numberOrUndefined(row.accuracy),
  };
}

function moveList(value: unknown, movesByName: Map<string, CustomPokemonMove>): CustomPokemonMove[] {
  if (!value) {
    return [];
  }

  if (Array.isArray(value)) {
    return value.flatMap((item) => moveList(item, movesByName));
  }

  if (typeof value === 'object') {
    const record = value as Record<string, unknown>;
    const name = firstText(record['name'], record['nome'], record['move']);
    if (name) {
      return [enrichMove({ name }, movesByName)];
    }

    return Object.values(record).flatMap((item) => moveList(item, movesByName));
  }

  if (typeof value === 'string') {
    return value
      .split(/[,;\n]/)
      .map((name) => name.trim())
      .filter(Boolean)
      .map((name) => enrichMove({ name }, movesByName));
  }

  return [];
}

function enrichMove(move: CustomPokemonMove, movesByName: Map<string, CustomPokemonMove>): CustomPokemonMove {
  const catalogMove = movesByName.get(normalize(move.name));
  if (!catalogMove) {
    return move;
  }

  return {
    name: move.name || catalogMove.name,
    category: move.category || catalogMove.category,
    type: move.type || catalogMove.type,
    style: move.style || catalogMove.style,
    power: move.power ?? catalogMove.power,
    accuracy: move.accuracy ?? catalogMove.accuracy,
  };
}

function uniqueMoves(moves: CustomPokemonMove[]): CustomPokemonMove[] {
  const byName = new Map<string, CustomPokemonMove>();
  moves.forEach((move) => {
    if (move.name) {
      byName.set(normalize(move.name), move);
    }
  });
  return [...byName.values()];
}

function uniqueTexts(...values: Array<string | null | undefined>): string[] {
  const byKey = new Map<string, string>();
  values
    .map((value) => firstText(value))
    .filter(Boolean)
    .forEach((value) => byKey.set(normalize(value), value));
  return [...byKey.values()];
}

function firstText(...values: unknown[]): string {
  return values
    .map((value) => String(value ?? '').trim())
    .find(Boolean) ?? '';
}

function numberOrUndefined(value: unknown): number | undefined {
  if (typeof value === 'number') {
    return value;
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : undefined;
  }
  return undefined;
}

function normalize(value: string | null | undefined): string {
  return String(value ?? '')
    .normalize('NFD')
    .replace(/\p{M}/gu, '')
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

function supabaseSearchText(value: string): string {
  return normalize(value).replace(/[%*(),]/g, '');
}

function trimTrailingSlash(value: string): string {
  return value.replace(/\/+$/, '');
}
