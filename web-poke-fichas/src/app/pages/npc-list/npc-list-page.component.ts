import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { FichaResumo } from '../../models/ficha.model';
import { AuthService } from '../../services/auth.service';
import { FichaApiService } from '../../services/ficha-api.service';

@Component({
  selector: 'app-npc-list-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="page-wrap">
      <div class="section-head">
        <div>
          <span class="eyebrow">Personagens do mundo</span>
          <h1>Fichário NPC</h1>
        </div>
        <button type="button" class="button primary" *ngIf="isAdmin()" (click)="createNpc()" [disabled]="creating()">
          {{ creating() ? 'Criando...' : 'Novo NPC' }}
        </button>
      </div>

      <div class="state-card" *ngIf="loading()">Carregando NPCs...</div>
      <div class="state-card error" *ngIf="error()">{{ error() }}</div>

      <div class="ficha-grid" *ngIf="!loading()">
        <a class="ficha-card" *ngFor="let ficha of fichas()" [routerLink]="['/ficha', fichaSlug(ficha)]" [style.--card-accent]="cardAccent(ficha)">
          <span class="ficha-card-player">NPC</span>
          <div class="ficha-card-thumb">
            <img class="ficha-card-thumb-bg" *ngIf="ficha.photoplayer" [src]="ficha.photoplayer" alt="" aria-hidden="true" />
            <img *ngIf="ficha.photoplayer" [src]="ficha.photoplayer" [alt]="ficha.nome" />
            <span *ngIf="!ficha.photoplayer">{{ initials(ficha.nome) }}</span>
          </div>
          <div class="ficha-card-content">
            <h2>{{ ficha.nome }}</h2>
            <p>{{ ficha.classePersonagem || ficha.ocupacao || 'NPC' }}</p>
          </div>
          <div class="ficha-card-team" aria-label="Equipe principal">
            <div class="ficha-card-team-sprites" *ngIf="ficha.pokemonsEquipe?.length; else emptyTeam">
              <span class="ficha-card-team-sprite" *ngFor="let pokemon of ficha.pokemonsEquipe" [title]="pokemon.apelido || pokemon.especie">
                <img *ngIf="pokemon.sprite" [src]="pokemon.sprite" [alt]="pokemon.apelido || pokemon.especie" />
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
export class NpcListPageComponent implements OnInit {
  private readonly api = inject(FichaApiService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly fichas = signal<FichaResumo[]>([]);
  protected readonly loading = signal(true);
  protected readonly creating = signal(false);
  protected readonly error = signal('');
  protected readonly isAdmin = computed(() => ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? ''));

  ngOnInit(): void {
    this.api.listNpcs().subscribe({
      next: (page) => {
        this.fichas.set(page.content ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar as fichas de NPC.');
        this.loading.set(false);
      },
    });
  }

  protected createNpc(): void {
    this.creating.set(true);
    this.api.createNpc(this.emptyFicha()).subscribe({
      next: (ficha) => this.router.navigate(['/ficha', this.fichaSlug(ficha), 'editar']),
      error: () => {
        this.error.set('Não foi possível criar a ficha de NPC agora.');
        this.creating.set(false);
      },
    });
  }

  protected cardAccent(ficha: FichaResumo): string {
    return /^#[0-9a-f]{6}$/i.test(ficha.corTema ?? '') ? ficha.corTema! : '#aeb5bf';
  }

  protected initials(name: string): string {
    return name.split(' ').filter(Boolean).slice(0, 2).map((part) => part[0]?.toUpperCase()).join('');
  }

  private emptyFicha() {
    return {
      nome: 'Novo NPC', frase: '', naturalidade: '', classePersonagem: '', tipoFisico: '', indole: '', ocupacao: '', equipe: '',
      miniUpgrade: 0, slotUpgrade: 0, corTema: '#aeb5bf', photoplayer: '', banner: '', avatar: '', player: '', biografia: '', anotacoes: '',
      relacionados: [], habilidades: [], conquistas: [], pokemons: [], itens: [], registros: [],
    };
  }

  protected fichaSlug(ficha: Pick<FichaResumo, 'id' | 'nome'>): string {
    const slug = ficha.nome.trim().toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
    return slug || String(ficha.id);
  }
}
