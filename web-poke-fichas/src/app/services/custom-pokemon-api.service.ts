import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, shareReplay } from 'rxjs';

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
  sprite?: string;
  types?: string[];
  abilities?: string[];
  moves?: CustomPokemonMove[];
  stats?: Partial<Record<'hp' | 'atk' | 'def' | 'satk' | 'sdef' | 'speed', number>>;
}

interface SupabasePokemonRow {
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
  private supabaseCatalog$?: Observable<CustomPokemonDetails[]>;

  search(term: string): Observable<CustomPokemonDetails[]> {
    return this.searchSupabase(term);
  }

  findByName(name: string): Observable<CustomPokemonDetails> {
    return this.findSupabaseByName(name);
  }

  private searchSupabase(term: string): Observable<CustomPokemonDetails[]> {
    const normalizedTerm = normalize(term);
    return this.loadSupabaseCatalog().pipe(
      map((catalog) => catalog
        .filter((pokemon) => !normalizedTerm || normalize(pokemon.name).includes(normalizedTerm))
        .slice(0, 50)),
    );
  }

  private findSupabaseByName(name: string): Observable<CustomPokemonDetails> {
    const normalizedName = normalize(name);
    return this.loadSupabaseCatalog().pipe(
      map((catalog) => catalog.find((pokemon) => normalize(pokemon.name) === normalizedName)),
      map((pokemon) => {
        if (!pokemon) {
          throw new Error('Pokemon customizado nao encontrado');
        }
        return pokemon;
      }),
    );
  }

  private loadSupabaseCatalog(): Observable<CustomPokemonDetails[]> {
    if (!this.isSupabaseConfigured()) {
      return of([]);
    }

    if (!this.supabaseCatalog$) {
      this.supabaseCatalog$ = forkJoin({
        moves: this.fetchSupabaseRows<SupabaseMoveRow>(environment.supabaseMoveTable, 5000).pipe(
          catchError(() => of([])),
        ),
        pokemons: this.fetchSupabaseRows<SupabasePokemonRow>(
          environment.supabasePokemonTable,
          environment.supabasePokemonLimit,
        ).pipe(
          catchError(() => of([])),
        ),
      }).pipe(
        map(({ moves, pokemons }) => {
          const movesByName = moves
            .filter(isApproved)
            .map(moveFromSupabase)
            .filter((move) => !!move.name)
            .reduce((index, move) => index.set(normalize(move.name), move), new Map<string, CustomPokemonMove>());

          return pokemons
            .filter(isApproved)
            .map((pokemon) => pokemonFromSupabase(pokemon, movesByName))
            .filter((pokemon) => !!pokemon.name);
        }),
        shareReplay({ bufferSize: 1, refCount: true }),
      );
    }

    return this.supabaseCatalog$;
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
    sprite: firstText(row.sprite, row.sprite_shiny),
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

function isApproved(row: { approved?: boolean | null }): boolean {
  return row.approved !== false;
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

function trimTrailingSlash(value: string): string {
  return value.replace(/\/+$/, '');
}
