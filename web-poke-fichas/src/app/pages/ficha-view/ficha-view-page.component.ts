import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { FichaHistoryComponent } from '../../components/ficha-history/ficha-history.component';
import { FichaDeleteComponent } from '../../components/ficha-delete/ficha-delete.component';
import { Ficha, FichaConquista, FichaItem, FichaPokemon, FichaRelacionado } from '../../models/ficha.model';
import { FichaApiService } from '../../services/ficha-api.service';
import { display, money } from '../../services/ficha-utils';

interface BadgeOption {
  id: string;
  label: string;
  icon?: string;
}

@Component({
  selector: 'app-ficha-view-page',
  standalone: true,
  imports: [CommonModule, FichaDeleteComponent, FichaHistoryComponent, RouterLink],
  template: `
    <section class="page-wrap public-sheet-wrap">
      <a class="back-link" routerLink="/">Voltar</a>

      <div class="state-card" *ngIf="loading()">Abrindo ficha...</div>
      <div class="state-card error" *ngIf="error()">{{ error() }}</div>

      <article class="public-sheet" *ngIf="ficha() as current" [style.--green]="current.corTema || defaultTheme">
        <div class="public-admin-actions">
          <app-ficha-history [fichaId]="current.id" />
          <app-ficha-delete
            [fichaId]="current.id"
            [fichaIdOrganizacao]="current.idOrganizacao"
            [fichaNome]="current.nome"
          />
        </div>
        <header class="public-hero">
          <div class="public-portrait">
            <img *ngIf="current.photoplayer" [src]="current.photoplayer" [alt]="current.nome" />
            <span *ngIf="!current.photoplayer">{{ initials(current.nome) }}</span>
          </div>

          <div class="public-hero-main">
            <span class="eyebrow">{{ current.classePersonagem || 'Personagem' }}</span>
            <h1>{{ current.nome }}</h1>
            <p>{{ current.frase || 'Sem frase cadastrada.' }}</p>
          </div>

          <dl class="public-facts">
            <div><dt>Player</dt><dd>{{ displayValue(current.player) }}</dd></div>
            <div><dt>Ocupação</dt><dd>{{ displayValue(current.ocupacao) }}</dd></div>
            <div><dt>Equipe</dt><dd>{{ displayValue(current.equipe) }}</dd></div>
            <div><dt>Naturalidade</dt><dd>{{ displayValue(current.naturalidade) }}</dd></div>
          </dl>
        </header>

        <section class="public-stats" aria-label="Resumo da ficha">
          <div><span>HP</span><strong>{{ displayValue(current.pontosVida) }}</strong></div>
          <div><span>Ranking</span><strong>{{ displayValue(current.ranking) }}</strong></div>
          <div><span>Reputação</span><strong>{{ displayValue(current.reputacao) }}</strong></div>
          <div><span>Dinheiro</span><strong>{{ moneyValue(current.dinheiro) }}</strong></div>
          <div><span>Pontos</span><strong>{{ displayValue(current.pontos) }}</strong></div>
          <div><span>Pokémon</span><strong>{{ current.pokemons.length }}</strong></div>
        </section>

        <section class="public-section" *ngIf="current.biografia || current.anotacoes">
          <h2>História</h2>
          <p *ngIf="current.biografia">{{ current.biografia }}</p>
          <p *ngIf="current.anotacoes">{{ current.anotacoes }}</p>
        </section>

        <section class="public-section" *ngIf="current.relacionados.length">
          <h2>Relacionados</h2>
          <div class="relacionado-card-grid">
            <button
              type="button"
              class="relacionado-card"
              *ngFor="let pessoa of current.relacionados"
              (click)="selectedRelacionado.set(pessoa)"
            >
              <span class="relacionado-card-image">
                <img *ngIf="pessoa.imagem" [src]="pessoa.imagem" [alt]="pessoa.nome" />
                <span *ngIf="!pessoa.imagem">?</span>
              </span>
              <strong>{{ pessoa.nome }}</strong>
            </button>
          </div>
        </section>

        <section class="public-section">
          <div class="public-section-head">
            <h2>Pokémon</h2>
            <span>{{ teamCount(current) }} na equipe · {{ boxCount(current) }} na box</span>
          </div>

          <div class="public-pokemon-grid">
            <article class="public-pokemon-card" *ngFor="let pokemon of teamPokemons(current)">
              <div class="public-pokemon-sprite">
                <img *ngIf="pokemon.sprite" [src]="pokemon.sprite" [alt]="pokemon.apelido || pokemon.especie" />
                <span *ngIf="!pokemon.sprite">?</span>
              </div>

              <div class="public-pokemon-info">
                <strong>{{ pokemon.apelido || (pokemon.especie ? titleCase(pokemon.especie) : 'Pokémon') }}</strong>
                <small>{{ pokemon.especie ? titleCase(pokemon.especie) : 'Espécie não informada' }}</small>
                <dl>
                  <div><dt>Ability</dt><dd>{{ displayValue(pokemon.ability) }}</dd></div>
                </dl>
              </div>
            </article>
          </div>
        </section>

        <section class="public-section" *ngIf="current.conquistas.length">
          <h2>Conquistas</h2>

          <div class="public-badge-case">
            <div class="public-badge-slot" *ngFor="let badge of badgeOptions; let slot = index" [class.filled]="badgeConquista(current, slot)">
              <span class="badge-icon" [class.empty]="!badgeConquista(current, slot)">
                <img
                  *ngIf="badgeIcon(current, slot, badge) as icon"
                  [class.badge-placeholder]="!badgeConquista(current, slot)"
                  [src]="icon"
                  [alt]="badgeConquista(current, slot)?.nome || badge.label"
                />
              </span>
              <strong>{{ badge.label }}</strong>
            </div>
          </div>

          <div class="public-achievement-columns">
            <div>
              <h3>Ribbons</h3>
              <p *ngIf="!conquistasPorTipo(current, 'ribbon').length">Nenhum ribbon registrado.</p>
              <div class="public-achievement-grid">
                <article class="public-achievement-card" *ngFor="let conquista of conquistasPorTipo(current, 'ribbon')">
                  <span class="public-achievement-image">
                    <img *ngIf="conquista.imagem" [src]="conquista.imagem" [alt]="conquista.nome" />
                    <span *ngIf="!conquista.imagem">?</span>
                  </span>
                  <strong>{{ conquista.nome }}</strong>
                </article>
              </div>
            </div>

            <div>
              <h3>Premiações</h3>
              <p *ngIf="!conquistasPorTipo(current, 'premiacao').length">Nenhuma premiação registrada.</p>
              <div class="public-achievement-grid">
                <article class="public-achievement-card" *ngFor="let conquista of conquistasPorTipo(current, 'premiacao')">
                  <span class="public-achievement-image">
                    <img *ngIf="conquista.imagem" [src]="conquista.imagem" [alt]="conquista.nome" />
                    <span *ngIf="!conquista.imagem">?</span>
                  </span>
                  <strong>{{ conquista.nome }}</strong>
                </article>
              </div>
            </div>
          </div>
        </section>

        <section class="public-section" *ngIf="current.itens.length">
          <div class="public-section-head">
            <h2>Inventário</h2>
            <span>{{ current.itens.length }} itens</span>
          </div>

          <div class="public-item-grid">
            <article class="public-item-card" *ngFor="let item of visibleItems(current.itens)">
              <span class="inventory-card-icon">
                <img *ngIf="item.icone" [src]="item.icone" [alt]="item.nome" />
                <span *ngIf="!item.icone">?</span>
                <small class="inventory-card-qty" *ngIf="(item.quantidade || 1) > 1">x{{ item.quantidade }}</small>
              </span>
              <div>
                <strong>{{ item.nome }}</strong>
                <small>{{ item.descricao || item.categoria }}</small>
              </div>
            </article>
          </div>
        </section>
      </article>

      <div class="modal-backdrop" *ngIf="selectedRelacionado() as pessoa" (click)="selectedRelacionado.set(null)">
        <div class="achievement-editor-modal public-relacionado-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Relacionado</span>
              <h3>{{ pessoa.nome }}</h3>
            </div>
            <button type="button" class="button ghost" (click)="selectedRelacionado.set(null)">Fechar</button>
          </div>

          <div class="public-relacionado-detail">
            <span class="public-relacionado-image">
              <img *ngIf="pessoa.imagem" [src]="pessoa.imagem" [alt]="pessoa.nome" />
              <span *ngIf="!pessoa.imagem">?</span>
            </span>
            <div>
              <strong>{{ pessoa.nome }}</strong>
              <small *ngIf="pessoa.relacao">{{ pessoa.relacao }}</small>
              <p>{{ pessoa.historia || 'Sem história cadastrada.' }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  `,
})
export class FichaViewPageComponent implements OnInit {
  private readonly api = inject(FichaApiService);
  private readonly route = inject(ActivatedRoute);

  protected readonly ficha = signal<Ficha | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly selectedRelacionado = signal<FichaRelacionado | null>(null);
  protected readonly defaultTheme = '#2f6f55';
  protected readonly badgeOptions: BadgeOption[] = [
    { id: 'insignia-1', label: 'Dyna Badge', icon: '/assets/badges/dyna-badge.png' },
    { id: 'insignia-2', label: 'Clay Wing Badge', icon: '/assets/badges/clay-wing-badge.png' },
    { id: 'insignia-3', label: 'Big Wave Badge', icon: '/assets/badges/big-wave-badge.png' },
    { id: 'insignia-4', label: 'Deep Jungle Badge', icon: '/assets/badges/deep-jungle-badge.png' },
    { id: 'insignia-5', label: 'Flame Valor Badge', icon: '/assets/badges/flame-valor-badge.png' },
    { id: 'insignia-6', label: 'Sweet Everest Badge', icon: '/assets/badges/sweet-everest-badge.png' },
    { id: 'insignia-7', label: 'Dark Aura Badge', icon: '/assets/badges/dark-aura-badge.png' },
    { id: 'insignia-8', label: 'Seven Lights Badge', icon: '/assets/badges/seven-lights-badge.png' },
  ];

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getPublic(id).subscribe({
      next: (ficha) => {
        this.ficha.set(this.normalizeFicha(ficha));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível abrir esta ficha.');
        this.loading.set(false);
      },
    });
  }

  protected displayValue(value?: string | number | null): string {
    return display(value);
  }

  protected moneyValue(value?: number): string {
    return money(value);
  }

  protected initials(name: string): string {
    return name
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join('');
  }

  protected titleCase(value: string): string {
    return value
      .split(/([\s-]+)/)
      .map((part) => (/[\s-]+/.test(part) ? part : part.charAt(0).toUpperCase() + part.slice(1).toLowerCase()))
      .join('');
  }

  protected teamCount(ficha: Ficha): number {
    return ficha.pokemons.filter((pokemon) => this.pokemonLocation(pokemon) === 'equipe').length;
  }

  protected teamPokemons(ficha: Ficha): FichaPokemon[] {
    return ficha.pokemons.filter((pokemon) => this.pokemonLocation(pokemon) === 'equipe');
  }

  protected boxCount(ficha: Ficha): number {
    return ficha.pokemons.filter((pokemon) => this.pokemonLocation(pokemon) === 'box').length;
  }

  protected badgeConquista(ficha: Ficha, slot: number): FichaConquista | undefined {
    return ficha.conquistas.find((conquista) => conquista.tipo === `insignia-${slot + 1}`);
  }

  protected selectedBadgeOption(ficha: Ficha, slot: number): BadgeOption | undefined {
    return this.badgeConquista(ficha, slot) ? this.badgeOptions[slot] : undefined;
  }

  protected badgeIcon(ficha: Ficha, slot: number, fallback: BadgeOption): string | undefined {
    return this.selectedBadgeOption(ficha, slot)?.icon ?? fallback.icon;
  }

  protected conquistasPorTipo(ficha: Ficha, tipo: string): FichaConquista[] {
    return ficha.conquistas.filter((conquista) => conquista.tipo === tipo);
  }

  protected visibleItems(itens: FichaItem[]): FichaItem[] {
    return itens.slice(0, 12);
  }

  private pokemonLocation(pokemon: FichaPokemon): 'equipe' | 'box' {
    const location = (pokemon.box ?? '').trim().toLowerCase();
    return location === 'box' || location === 'pc' ? 'box' : 'equipe';
  }

  private normalizeFicha(ficha: Ficha): Ficha {
    return {
      ...ficha,
      relacionados: ficha.relacionados ?? [],
      habilidades: ficha.habilidades ?? [],
      conquistas: ficha.conquistas ?? [],
      pokemons: ficha.pokemons ?? [],
      itens: ficha.itens ?? [],
      registros: ficha.registros ?? [],
    };
  }
}
