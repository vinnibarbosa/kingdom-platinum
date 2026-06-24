import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

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
        <a class="ficha-card" *ngFor="let ficha of fichas()" [routerLink]="fichaLink(ficha)">
          <div class="ficha-card-content">
            <span class="card-kicker">#{{ ficha.id }}</span>
            <h2>{{ ficha.nome }}</h2>
            <p>{{ ficha.classePersonagem || ficha.ocupacao || 'Personagem' }}</p>
            <dl>
              <div>
                <dt>Player</dt>
                <dd>{{ ficha.player || '-' }}</dd>
              </div>
            </dl>
          </div>

          <div class="ficha-card-thumb">
            <img *ngIf="ficha.photoplayer" [src]="ficha.photoplayer" [alt]="ficha.nome" />
            <span *ngIf="!ficha.photoplayer">{{ initials(ficha.nome) }}</span>
          </div>

          <div class="ficha-card-team" aria-label="Equipe principal">
            <span class="ficha-card-team-label">Equipe</span>
            <div class="ficha-card-team-sprites" *ngIf="ficha.pokemonsEquipe?.length; else emptyTeam">
              <span
                class="ficha-card-team-sprite"
                *ngFor="let pokemon of ficha.pokemonsEquipe"
                [title]="pokemon.apelido || pokemon.especie"
              >
                <img
                  *ngIf="pokemon.sprite"
                  [class.custom-pokemon-art]="pokemon.sprite.startsWith('data:image/')"
                  [src]="pokemon.sprite"
                  [alt]="pokemon.apelido || pokemon.especie"
                />
                <span *ngIf="!pokemon.sprite">?</span>
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
  protected readonly ownFichaCount = computed(() => {
    const idOrganizacao = this.auth.currentUser()?.idOrganizacao;
    return this.fichas().filter((ficha) => ficha.idOrganizacao === idOrganizacao).length;
  });
  protected readonly canCreate = computed(() => this.ownFichaCount() < 2);

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
      next: (ficha) => this.router.navigate(['/ficha', ficha.id]),
      error: () => {
        this.error.set('Não foi possível criar uma ficha agora.');
        this.creating.set(false);
      },
    });
  }

  protected fichaLink(ficha: FichaResumo): (string | number)[] {
    return this.isOwner(ficha) || this.isAdmin() ? ['/ficha', ficha.id] : ['/ficha', ficha.id, 'visualizar'];
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
        this.fichas.set(page.content ?? []);
        this.loading.set(false);
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
}
