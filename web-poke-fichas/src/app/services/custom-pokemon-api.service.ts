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
  spriteShiny?: string;
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
  formas_alt?: unknown;
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
      const url = this.customPokemonEndpoint();
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

    const url = this.customPokemonEndpoint();
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
          .flatMap((pokemon) => pokemonsFromSupabase(pokemon, movesByName))
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
            .flatMap((pokemon) => pokemonsFromSupabase(pokemon, movesByName))
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

  private customPokemonEndpoint(): string {
    return `${trimTrailingSlash(environment.apiUrl)}/public/pokemon/custom`;
  }
}

function pokemonsFromSupabase(row: SupabasePokemonRow, movesByName: Map<string, CustomPokemonMove>): CustomPokemonDetails[] {
  const base = pokemonFromSupabase(row, movesByName);
  return [base, ...alternativeForms(row.formas_alt).map((form) => pokemonFormFromSupabase(base, form, movesByName))];
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
    spriteShiny: firstText(row.sprite_shiny),
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

function pokemonFormFromSupabase(
  base: CustomPokemonDetails,
  form: Record<string, unknown>,
  movesByName: Map<string, CustomPokemonMove>,
): CustomPokemonDetails {
  const name = firstText(form['name'], form['nome'], form['form_name'], form['form'], form['url_slug'], form['slug']);
  const slug = firstText(form['url_slug'], form['slug'], name);
  const formTypes = Array.isArray(form['types']) ? form['types'] : [form['types']];
  const moves = [
    ...moveList(form['moves_level'], movesByName),
    ...moveList(form['moves_tm'], movesByName),
    ...moveList(form['moves_egg'], movesByName),
    ...moveList(form['moves_tutor'], movesByName),
  ];

  return {
    ...base,
    name: name || base.name,
    // A form is a catalog entry of its own. Do not reuse the base slug/dex,
    // otherwise the catalog merger treats it as the official species.
    slug,
    dex: dexNumber(form['id']),
    sprite: firstText(form['sprite'], form['sprite_url'], form['image'], form['image_url'], form['artwork'], form['imagem']) || base.sprite,
    spriteShiny: firstText(form['sprite_shiny'], form['shiny_sprite'], form['image_shiny']) || base.spriteShiny,
    searchTerms: uniqueTexts(
      name,
      slug,
      form['id'] === undefined || form['id'] === null ? undefined : String(form['id']),
    ),
    types: uniqueTexts(
      firstText(form['tipo1'], form['type1']),
      firstText(form['tipo2'], form['type2']),
      ...formTypes.map((type) => firstText(type)),
      ...(base.types ?? []),
    ),
    abilities: uniqueTexts(
      firstText(form['habilidade1'], form['ability1']),
      firstText(form['habilidade2'], form['ability2']),
      firstText(form['habilidade_oculta'], form['hidden_ability']),
      ...(base.abilities ?? []),
    ),
    moves: moves.length ? uniqueMoves(moves) : base.moves,
    stats: {
      ...(base.stats ?? {}),
      hp: numberOrUndefined(form['hp']) ?? base.stats?.hp,
      atk: numberOrUndefined(form['atk']) ?? base.stats?.atk,
      def: numberOrUndefined(form['def']) ?? base.stats?.def,
      satk: numberOrUndefined(form['spa'], form['satk']) ?? base.stats?.satk,
      sdef: numberOrUndefined(form['spd'], form['sdef']) ?? base.stats?.sdef,
      speed: numberOrUndefined(form['spe'], form['speed']) ?? base.stats?.speed,
    },
  };
}

function alternativeForms(value: unknown): Record<string, unknown>[] {
  const parsed = parseJsonValue(value);
  if (Array.isArray(parsed)) {
    return parsed.flatMap((entry) => alternativeForms(entry));
  }

  if (!isRecord(parsed)) {
    return [];
  }

  const nestedForms = parsed['formas_alt'] ?? parsed['forms'] ?? parsed['formas'];
  if (nestedForms !== undefined) {
    return alternativeForms(nestedForms);
  }

  if (hasFormFields(parsed)) {
    return [parsed];
  }

  return Object.entries(parsed).flatMap(([name, entry]) => {
    if (isRecord(entry)) {
      return [{ ...entry, name: firstText(entry['name'], entry['nome'], name) }];
    }
    if (typeof entry === 'string' && entry.trim()) {
      return [{ name, sprite: entry }];
    }
    return [];
  });
}

function parseJsonValue(value: unknown): unknown {
  if (typeof value !== 'string') {
    return value;
  }
  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function hasFormFields(value: Record<string, unknown>): boolean {
  return ['name', 'nome', 'form_name', 'form', 'sprite', 'sprite_url', 'image', 'url_slug', 'slug']
    .some((key) => value[key] !== undefined && value[key] !== null);
}

function mergeCatalogs(...catalogs: CustomPokemonDetails[][]): CustomPokemonDetails[] {
  const byKey = new Map<string, CustomPokemonDetails>();

  catalogs.flat()
    .map(normalizeCatalogPokemon)
    .filter((pokemon) => !!pokemon.name)
    .forEach((pokemon) => {
      const searchKeys = pokemonSearchTerms(pokemon);
      const identityKeys = pokemonIdentityKeys(pokemon);
      const existingKey = [...identityKeys, ...searchKeys].find((key) => byKey.has(key));
      if (existingKey) {
        byKey.set(existingKey, mergePokemon(byKey.get(existingKey)!, pokemon));
        return;
      }

      byKey.set(identityKeys[0] || searchKeys[0] || normalize(pokemon.name), pokemon);
    });

  return [...byKey.values()];
}

function normalizeCatalogPokemon(pokemon: CustomPokemonDetails): CustomPokemonDetails {
  const name = firstText(pokemon.name, pokemon.slug);
  return {
    ...pokemon,
    name,
    slug: firstText(pokemon.slug),
    spriteShiny: firstText(pokemon.spriteShiny),
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
    spriteShiny: incoming.spriteShiny || base.spriteShiny,
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

function pokemonIdentityKeys(pokemon: CustomPokemonDetails): string[] {
  const variantKey = formVariantKey(pokemon.name) || formVariantKey(pokemon.slug);
  return uniqueTexts(variantKey, pokemon.slug, pokemon.name).map((term) => normalize(term));
}

function formVariantKey(value: string | undefined): string {
  const text = String(value ?? '').trim();
  if (!text) {
    return '';
  }

  const prefix = text.match(/^\(([AGHPN])\)\s*(.+)$/i);
  if (prefix) {
    return `${prefix[2]} ${formRegionName(prefix[1])}`;
  }

  const suffix = text.match(/^(.+?)\s*\((Alola|Galar|Hisui|Paldea|Nendo)\s+Form\)$/i);
  if (suffix) {
    return `${suffix[1]} ${suffix[2]}`;
  }

  return text;
}

function formRegionName(code: string): string {
  return {
    A: 'Alola',
    G: 'Galar',
    H: 'Hisui',
    P: 'Paldea',
    N: 'Nendo',
  }[code.toUpperCase()] ?? code;
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

function numberOrUndefined(...values: unknown[]): number | undefined {
  for (const value of values) {
    if (typeof value === 'number' && Number.isFinite(value)) {
      return value;
    }
    if (typeof value === 'string' && value.trim()) {
      const parsed = Number(value);
      if (Number.isFinite(parsed)) {
        return parsed;
      }
    }
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
