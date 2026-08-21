import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of } from 'rxjs';

import { FichaResumo } from '../../models/ficha.model';
import { AuthService } from '../../services/auth.service';
import { FichaApiService } from '../../services/ficha-api.service';

@Component({
  selector: 'app-ficha-list-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="page-wrap">
      <div class="section-head">
        <div>
          <span class="eyebrow">Biblioteca</span>
          <h1>Fichas</h1>
        </div>
        <button
          type="button"
          class="button primary"
          (click)="createFicha()"
          [disabled]="creating() || !canCreate()"
          [title]="canCreate() ? 'Criar uma nova ficha' : 'Limite de 2 fichas atingido'"
        >
          {{ creating() ? 'Criando...' : 'Nova ficha' }}
        </button>
      </div>

      <div class="state-card" *ngIf="loading()">Carregando fichas...</div>
      <div class="state-card error" *ngIf="error()">{{ error() }}</div>

      <div class="ficha-grid" *ngIf="!loading()">
        <a
          class="ficha-card"
          *ngFor="let ficha of fichas()"
          [routerLink]="fichaLink(ficha)"
          [style.--card-accent]="cardAccent(ficha)"
        >
          <span class="ficha-card-player">{{ ficha.player || '-' }}</span>

          <div class="ficha-card-thumb">
            <img class="ficha-card-thumb-bg" *ngIf="ficha.photoplayer" [src]="ficha.photoplayer" alt="" aria-hidden="true" />
            <img *ngIf="ficha.photoplayer" [src]="ficha.photoplayer" [alt]="ficha.nome" />
            <span *ngIf="!ficha.photoplayer">{{ initials(ficha.nome) }}</span>
          </div>

          <div class="ficha-card-content">
            <h2>{{ ficha.nome }}</h2>
            <p>{{ ficha.classePersonagem || ficha.ocupacao || 'Personagem' }}</p>
          </div>

          <div class="ficha-card-team" aria-label="Equipe principal">
            <div class="ficha-card-team-sprites" *ngIf="ficha.pokemonsEquipe?.length; else emptyTeam">
              <span
                class="ficha-card-team-sprite"
                *ngFor="let pokemon of ficha.pokemonsEquipe"
                [title]="pokemon.apelido || pokemon.especie"
              >
                <img
                  *ngIf="pokemon.sprite && !isPokemonSpriteBroken(pokemon.sprite)"
                  [class.custom-pokemon-art]="pokemon.sprite.startsWith('data:image/')"
                  [src]="pokemon.sprite"
                  [alt]="pokemon.apelido || pokemon.especie"
                  (error)="markBrokenPokemonSprite(pokemon.sprite)"
                />
                <span *ngIf="!pokemon.sprite || isPokemonSpriteBroken(pokemon.sprite)">?</span>
              </span>
            </div>
            <ng-template #emptyTeam><small>Nenhum Pokémon na equipe</small></ng-template>
          </div>
        </a>
      </div>
    </section>
  `,
})
export class FichaListPageComponent implements OnInit {
  private readonly api = inject(FichaApiService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly fichas = signal<FichaResumo[]>([]);
  protected readonly loading = signal(true);
  protected readonly creating = signal(false);
  protected readonly error = signal('');
  private readonly brokenPokemonSprites = signal(new Set<string>());
  // O limite e conferido pela API, que conhece o proprietario real de cada ficha.
  protected readonly canCreate = computed(() => true);

  ngOnInit(): void {
    this.load();
  }

  protected createFicha(): void {
    if (!this.canCreate()) {
      this.error.set('Você já atingiu o limite de 2 fichas por conta.');
      return;
    }
    this.creating.set(true);
    this.api.create({
      nome: 'Nova ficha',
      frase: '',
      naturalidade: '',
      classePersonagem: '',
      tipoFisico: '',
      indole: '',
      ocupacao: '',
      equipe: '',
      miniUpgrade: 0,
      slotUpgrade: 0,
      corTema: '#aeb5bf',
      photoplayer: '',
      banner: '',
      avatar: '',
      player: '',
      biografia: '',
      anotacoes: '',
      relacionados: [],
      habilidades: [],
      conquistas: [],
      pokemons: [],
      itens: [],
      registros: [],
    }).subscribe({
      next: (ficha) => this.router.navigate(['/ficha', this.fichaSlug(ficha), 'editar']),
      error: (error) => {
        this.error.set(error?.error?.message || 'Não foi possível criar uma ficha agora.');
        this.creating.set(false);
      },
    });
  }

  protected fichaLink(ficha: FichaResumo): (string | number)[] {
    return ['/ficha', this.fichaSlug(ficha)];
  }

  protected cardAccent(ficha: FichaResumo): string {
    const rawTheme = ficha.corTema ?? (ficha as FichaResumo & { cor_tema?: string }).cor_tema ?? '';
    const theme = rawTheme.trim();
    return /^#[0-9a-f]{6}$/i.test(theme) ? theme : '#aeb5bf';
  }

  protected isPokemonSpriteBroken(sprite?: string): boolean {
    return Boolean(sprite && this.brokenPokemonSprites().has(sprite));
  }

  protected markBrokenPokemonSprite(sprite?: string): void {
    if (!sprite) {
      return;
    }

    this.brokenPokemonSprites.update((current) => {
      const next = new Set(current);
      next.add(sprite);
      return next;
    });
  }

  private fichaSlug(ficha: Pick<FichaResumo, 'id' | 'nome'>): string {
    const slug = ficha.nome
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
    return slug || String(ficha.id);
  }

  protected initials(name: string): string {
    return name
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join('');
  }

  private load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.list().subscribe({
      next: (page) => {
        const fichas = page.content ?? [];
        this.fichas.set(fichas);
        this.loading.set(false);
        this.enrichThemeColors(fichas);
      },
      error: () => {
        this.error.set('Não foi possível carregar as fichas. Verifique se o backend está rodando.');
        this.loading.set(false);
      },
    });
  }

  private isOwner(ficha: FichaResumo): boolean {
    const currentUser = this.auth.currentUser();
    return Boolean(currentUser?.idOrganizacao && currentUser.idOrganizacao === ficha.idOrganizacao);
  }

  private isAdmin(): boolean {
    return ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? '');
  }

  private enrichThemeColors(fichas: FichaResumo[]): void {
    const missingTheme = fichas.filter((ficha) => !this.cardAccentFromResumo(ficha));
    if (!missingTheme.length) {
      return;
    }

    forkJoin(
      missingTheme.map((ficha) =>
        this.api.getPublicBySlug(this.fichaSlug(ficha)).pipe(
          map((fullFicha) => ({ id: ficha.id, corTema: fullFicha.corTema })),
          catchError(() => of({ id: ficha.id, corTema: undefined }))
        )
      )
    ).subscribe((themes) => {
      const byId = new Map(themes.filter((theme) => theme.corTema).map((theme) => [theme.id, theme.corTema]));
      if (!byId.size) {
        return;
      }

      this.fichas.update((current) =>
        current.map((ficha) => byId.has(ficha.id) ? { ...ficha, corTema: byId.get(ficha.id) } : ficha)
      );
    });
  }

  private cardAccentFromResumo(ficha: FichaResumo): string {
    const rawTheme = ficha.corTema ?? (ficha as FichaResumo & { cor_tema?: string }).cor_tema ?? '';
    const theme = rawTheme.trim();
    return /^#[0-9a-f]{6}$/i.test(theme) ? theme : '';
  }
}
