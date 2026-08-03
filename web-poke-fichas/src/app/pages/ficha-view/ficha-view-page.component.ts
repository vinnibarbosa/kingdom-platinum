import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { FichaHistoryComponent } from '../../components/ficha-history/ficha-history.component';
import { FichaDeleteComponent } from '../../components/ficha-delete/ficha-delete.component';
import { Ficha, FichaConquista, FichaItem, FichaPokemon, FichaRelacionado } from '../../models/ficha.model';
import { FichaApiService } from '../../services/ficha-api.service';
import { AuthService } from '../../services/auth.service';
import { CustomPokemonApiService } from '../../services/custom-pokemon-api.service';
import { display, money } from '../../services/ficha-utils';
import { loadPokemonMoveStyle, pokemonContestStyleColor, pokemonMoveTypeColor } from '../../services/pokemon-move-utils';

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

      <article class="public-sheet" *ngIf="ficha() as current" [style.--green]="themeAccent(current.corTema)">
        <div class="public-admin-actions">
          <a class="button ghost" *ngIf="canEdit(current)" [routerLink]="['/ficha', fichaSlug(current), 'editar']">Editar ficha</a>
          <app-ficha-history [fichaId]="current.id" />
          <app-ficha-delete
            [fichaId]="current.id"
            [fichaNome]="current.nome"
          />
        </div>
        <header class="public-hero">
          <span class="public-hero-banner" *ngIf="current.banner" aria-hidden="true">
            <img [src]="current.banner" alt="" />
          </span>
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
            <div><dt>Naturalidade</dt><dd>{{ displayValue(current.naturalidade) }}</dd></div>
            <div><dt>Ocupação</dt><dd>{{ displayValue(current.ocupacao) }}</dd></div>
            <div><dt>Equipe</dt><dd>{{ displayValue(current.equipe) }}</dd></div>
            <div><dt>Player</dt><dd>{{ displayValue(current.player) }}</dd></div>
          </dl>
        </header>

        <section class="public-stats" aria-label="Resumo da ficha">
          <div><span>PV</span><strong>{{ displayValue(current.pontosVida) }}</strong></div>
          <div><span>Ranking</span><strong>{{ rankingLabel(current.ranking) }}</strong></div>
          <div><span>Reputação</span><strong>{{ reputationLabel(current.reputacao) }}</strong></div>
          <div><span>Dinheiro</span><strong>{{ moneyValue(current.dinheiro) }}</strong></div>
          <div><span>Pontos</span><strong>{{ displayValue(current.pontos) }}</strong></div>
          <div><span>Pokémon</span><strong>{{ current.pokemons.length }}</strong></div>
        </section>

        <section class="public-section public-data-section">
          <h2>Dados</h2>
          <dl class="public-data-grid">
            <div><dt>Avatar</dt><dd>{{ displayValue(current.avatar) }}</dd></div>
            <div><dt>Índole</dt><dd>{{ displayValue(current.indole) }}</dd></div>
            <div><dt>Idade</dt><dd>{{ displayValue(current.idade) }}</dd></div>
            <div><dt>Altura</dt><dd>{{ displayValue(current.alturaCm) }}</dd></div>
            <div><dt>Peso</dt><dd>{{ displayValue(current.pesoKg) }}</dd></div>
            <div><dt>Tipo Físico</dt><dd>{{ displayValue(current.tipoFisico) }}</dd></div>
          </dl>
        </section>

        <section class="public-section" *ngIf="current.biografia || current.anotacoes">
          <h2>História</h2>
          <p class="justified-text" *ngIf="current.biografia">{{ current.biografia }}</p>
          <p class="justified-text" *ngIf="current.anotacoes">{{ current.anotacoes }}</p>
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
              <span class="relacionado-card-copy">
                <strong>{{ pessoa.nome }}</strong>
                <small *ngIf="pessoa.relacao">{{ pessoa.relacao }}</small>
              </span>
            </button>
          </div>
        </section>

        <section class="public-section">
          <div class="public-section-head">
            <h2>Pokémon</h2>
            <div class="public-section-actions">
              <span>{{ teamCount(current) }} na equipe &middot; {{ boxCount(current) }} na box</span>
              <button type="button" class="button ghost compact" *ngIf="boxCount(current)" (click)="boxOpen.set(true)">Ver Box</button>
            </div>
          </div>

          <div class="public-pokemon-grid">
            <button
              type="button"
              class="public-pokemon-card"
              *ngFor="let pokemon of teamPokemons(current)"
              (click)="openPokemon(pokemon)"
              [attr.aria-label]="'Ver detalhes de ' + (pokemon.apelido || pokemon.especie || 'Pokémon')"
            >
              <div class="public-pokemon-sprite">
                <img
                  *ngIf="pokemonImage(pokemon) as sprite"
                  [class.custom-pokemon-art]="pokemon.sprite?.startsWith('data:image/')"
                  [src]="sprite"
                  [alt]="pokemonTitle(pokemon)"
                />
                <span *ngIf="!pokemonImage(pokemon)">?</span>
              </div>

              <div class="public-pokemon-info">
                <strong>{{ pokemonTitle(pokemon) }}</strong>
                <small>{{ pokemonSpeciesText(pokemon) }}</small>
              </div>
            </button>
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
            <div class="public-section-actions">
              <span>{{ current.itens.length }} itens</span>
              <button type="button" class="button ghost compact" *ngIf="current.itens.length > visibleItems(current.itens).length" (click)="inventoryOpen.set(true)">Ver tudo</button>
            </div>
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

      <div class="modal-backdrop" *ngIf="boxOpen() && ficha() as current" (click)="boxOpen.set(false)">
        <div class="achievement-editor-modal public-collection-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Pokémon</span>
              <h3>Box de {{ current.nome }}</h3>
            </div>
            <button type="button" class="button ghost" (click)="boxOpen.set(false)">Fechar</button>
          </div>

          <div class="public-pokemon-grid public-collection-grid">
            <button
              type="button"
              class="public-pokemon-card"
              *ngFor="let pokemon of boxPokemons(current)"
              (click)="boxOpen.set(false); openPokemon(pokemon)"
              [attr.aria-label]="'Ver detalhes de ' + (pokemon.apelido || pokemon.especie || 'Pokémon')"
            >
              <div class="public-pokemon-sprite">
                <img *ngIf="pokemonImage(pokemon) as sprite" [class.custom-pokemon-art]="pokemon.sprite?.startsWith('data:image/')" [src]="sprite" [alt]="pokemonTitle(pokemon)" />
                <span *ngIf="!pokemonImage(pokemon)">?</span>
              </div>
              <div class="public-pokemon-info">
                <strong>{{ pokemonTitle(pokemon) }}</strong>
                <small>{{ pokemonSpeciesText(pokemon) }}</small>
              </div>
            </button>
          </div>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="inventoryOpen() && ficha() as current" (click)="inventoryOpen.set(false)">
        <div class="achievement-editor-modal public-collection-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Inventário</span>
              <h3>Inventário de {{ current.nome }}</h3>
            </div>
            <button type="button" class="button ghost" (click)="inventoryOpen.set(false)">Fechar</button>
          </div>

          <div class="public-item-grid public-collection-grid">
            <article class="public-item-card" *ngFor="let item of current.itens">
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
        </div>
      </div>

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

      <div
        class="modal-backdrop"
        *ngIf="selectedPokemon() as pokemon"
        [style.--green]="themeAccent(ficha()?.corTema)"
        (click)="selectedPokemon.set(null)"
      >
        <div class="achievement-editor-modal public-pokemon-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Pokémon da equipe</span>
              <h3>{{ pokemonTitle(pokemon) }}</h3>
            </div>
            <button type="button" class="button ghost" (click)="selectedPokemon.set(null)">Fechar</button>
          </div>

          <div class="public-pokemon-modal-hero">
            <span class="public-pokemon-modal-sprite">
              <img
                *ngIf="pokemonImage(pokemon) as sprite"
                [class.custom-pokemon-art]="pokemon.sprite?.startsWith('data:image/')"
                [src]="sprite"
                  [alt]="pokemonTitle(pokemon)"
              />
              <span *ngIf="!pokemonImage(pokemon)">?</span>
            </span>
            <div>
              <span class="eyebrow">{{ pokemonSpeciesText(pokemon) }}</span>
              <strong>{{ pokemonTitle(pokemon) }}</strong>
              <div class="public-pokemon-meta">
                <span class="public-pokeball-meta">
                  <img [src]="pokeballIcon(pokemon)" [alt]="pokeballLabel(pokemon)" />
                  {{ pokeballLabel(pokemon) }}
                </span>
                <span
                  class="public-type-chip"
                  *ngFor="let type of pokemonTypesFor(pokemon)"
                  [style.--pokemon-type-color]="moveTypeColor(type)"
                >{{ titleCase(type) }}</span>
              </div>
            </div>
          </div>

          <dl class="public-pokemon-detail-facts">
            <div><dt>Ability</dt><dd>{{ pokemonText(pokemon.ability) }}</dd></div>
            <div><dt>Gênero</dt><dd>{{ pokemonText(pokemon.genero) }}</dd></div>
            <div><dt>Feature</dt><dd>{{ pokemonText(pokemon.feature) }}</dd></div>
            <div><dt>Nature</dt><dd>{{ pokemonText(pokemon.nature) }}</dd></div>
            <div><dt>Hold Item</dt><dd>{{ pokemonText(pokemon.holdItem) }}</dd></div>
            <div><dt>Happiness</dt><dd>{{ displayValue(pokemon.happinessAtual) }}</dd></div>
          </dl>

          <section class="public-pokemon-moves">
            <h4>Moveset</h4>
            <div class="public-move-list" *ngIf="moveset(pokemon).length; else emptyMoveset">
              <article
                class="public-move-card"
                *ngFor="let move of moveset(pokemon)"
                [style.--public-move-color]="moveTypeColor(move.tipo)"
              >
                <div class="public-move-title">
                  <strong>{{ titleCase(move.nome) }}</strong>
                  <span *ngIf="move.tipo">{{ titleCase(move.tipo) }}</span>
                </div>
                <dl>
                  <div><dt>Power</dt><dd>{{ displayValue(move.poder) }}</dd></div>
                  <div><dt>Accuracy</dt><dd>{{ displayValue(move.accuracy) }}</dd></div>
                  <div><dt>Categoria</dt><dd>{{ pokemonText(move.categoria) }}</dd></div>
                  <div
                    class="public-move-style"
                    [class.has-style]="!!move.style"
                    [style.--contest-style-color]="contestStyleColor(move.style)"
                  ><dt>Style</dt><dd>{{ pokemonText(move.style) }}</dd></div>
                </dl>
              </article>
            </div>
            <ng-template #emptyMoveset><p class="public-empty-copy">Nenhum movimento cadastrado.</p></ng-template>
          </section>

          <section class="public-pokemon-combo" *ngIf="pokemon.combo">
            <h4>Combo</h4>
            <p>{{ pokemon.combo }}</p>
          </section>
        </div>
      </div>
    </section>
  `,
})
export class FichaViewPageComponent implements OnInit {
  private readonly api = inject(FichaApiService);
  private readonly auth = inject(AuthService);
  private readonly customPokemonApi = inject(CustomPokemonApiService);
  private readonly route = inject(ActivatedRoute);

  protected readonly ficha = signal<Ficha | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly selectedRelacionado = signal<FichaRelacionado | null>(null);
  protected readonly selectedPokemon = signal<FichaPokemon | null>(null);
  protected readonly pokemonTypes = signal<Record<string, string[]>>({});
  protected readonly boxOpen = signal(false);
  protected readonly inventoryOpen = signal(false);
  protected readonly defaultTheme = '#aeb5bf';

  protected themeAccent(theme?: string): string {
    const value = theme?.trim();
    return !value || ['#2f6f55', '#586a9b'].includes(value.toLowerCase()) ? this.defaultTheme : value;
  }
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
    const idParam = this.route.snapshot.paramMap.get('id');
    const slug = this.route.snapshot.paramMap.get('slug');
    const request = idParam ? this.api.getPublic(Number(idParam)) : this.api.getPublicBySlug(slug ?? '');
    request.subscribe({
      next: (ficha) => {
        const normalized = this.normalizeFicha(ficha);
        this.ficha.set(normalized);
        this.hydrateMissingMoveStyles(normalized);
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

  protected fichaSlug(ficha: Ficha): string {
    const slug = ficha.nome
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
    return slug || String(ficha.id);
  }

  protected rankingLabel(points?: number | null): string {
    if (points === undefined || points === null) {
      return '-';
    }

    const rankingTable = [
      { threshold: 3000, label: '5' },
      { threshold: 2000, label: '4' },
      { threshold: 1000, label: '3' },
      { threshold: 500, label: '2' },
      { threshold: 0, label: '1' },
    ];

    return rankingTable.find((item) => points >= item.threshold)?.label ?? '1';
  }

  protected reputationLabel(points?: number | null): string {
    if (points === undefined || points === null) {
      return '-';
    }

    if (points === 0) {
      return 'Anônimo';
    }

    const positiveTable = [
      { threshold: 30000, label: 'Astro' },
      { threshold: 20000, label: 'Notório' },
      { threshold: 15000, label: 'Celebridade' },
      { threshold: 10000, label: 'Renomado' },
      { threshold: 5000, label: 'Estrela' },
      { threshold: 3000, label: 'Influencer' },
      { threshold: 1000, label: 'Notado' },
      { threshold: 500, label: 'Conhecido' },
      { threshold: 100, label: 'Familiar' },
    ];
    const negativeTable = [
      { threshold: 30000, label: 'Abominável' },
      { threshold: 20000, label: 'Malquisto' },
      { threshold: 15000, label: 'Indesejado' },
      { threshold: 10000, label: 'Procurado' },
      { threshold: 5000, label: 'Bandido' },
      { threshold: 3000, label: 'Mal visto' },
      { threshold: 1000, label: 'Intolerado' },
    ];

    const table = points > 0 ? positiveTable : negativeTable;
    const absolutePoints = Math.abs(points);
    return table.find((item) => absolutePoints >= item.threshold)?.label ?? 'Anônimo';
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
      .split(/[-_\s\u2010-\u2015]+/)
      .filter(Boolean)
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
      .join(' ');
  }

  protected pokemonText(value?: string): string {
    return value?.trim() ? this.titleCase(value) : '-';
  }

  protected pokemonTitle(pokemon: FichaPokemon): string {
    const nickname = pokemon.apelido?.trim();
    if (nickname && !this.isGeneratedPokemonName(nickname)) {
      return nickname;
    }

    return pokemon.especie?.trim()
      ? this.titleCase(pokemon.especie)
      : 'Pokémon';
  }

  protected pokemonSpeciesText(pokemon: FichaPokemon): string {
    return pokemon.especie?.trim()
      ? this.titleCase(pokemon.especie)
      : 'Espécie não informada';
  }

  protected openPokemon(pokemon: FichaPokemon): void {
    this.selectedPokemon.set(pokemon);
    this.loadPokemonTypes(pokemon);
  }

  protected pokemonTypesFor(pokemon: FichaPokemon): string[] {
    return this.pokemonTypes()[this.pokemonIdentityKey(pokemon)] ?? [];
  }

  protected pokeballLabel(pokemon: FichaPokemon): string {
    const key = (pokemon.pokebola || 'poke-ball').trim().toLowerCase();
    const labels: Record<string, string> = {
      'poke-ball': 'Poké Ball', 'great-ball': 'Great Ball', 'ultra-ball': 'Ultra Ball',
      'master-ball': 'Master Ball', 'premier-ball': 'Premier Ball', 'luxury-ball': 'Luxury Ball',
      'heal-ball': 'Heal Ball', 'quick-ball': 'Quick Ball', 'dusk-ball': 'Dusk Ball',
      'dive-ball': 'Dive Ball', 'net-ball': 'Net Ball', 'nest-ball': 'Nest Ball',
      'repeat-ball': 'Repeat Ball', 'timer-ball': 'Timer Ball', 'level-ball': 'Level Ball',
      'lure-ball': 'Lure Ball', 'friend-ball': 'Friend Ball', 'love-ball': 'Love Ball',
      'safari-ball': 'Safari Ball', 'cherish-ball': 'Cherish Ball', 'beast-ball': 'Beast Ball',
      'fast-ball': 'Fast Ball', 'heavy-ball': 'Heavy Ball', 'moon-ball': 'Moon Ball', 'strange-ball': 'Strange Ball',
    };
    return labels[key] ?? this.titleCase(key);
  }

  protected pokeballIcon(pokemon: FichaPokemon): string {
    const key = (pokemon.pokebola || 'poke-ball').trim().toLowerCase() || 'poke-ball';
    return `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/${encodeURIComponent(key)}.png`;
  }

  protected pokemonImage(pokemon: FichaPokemon): string {
    const sprite = pokemon.sprite?.trim() ?? '';
    if (!sprite) {
      return '';
    }

    if (sprite.startsWith('data:image/')) {
      return sprite;
    }

    const dex = this.dexFromPokemonSprite(sprite);
    if (dex) {
      return this.officialArtworkUrl(dex, pokemon.feature === 'Shiny');
    }

    return sprite;
  }

  private isGeneratedPokemonName(value: string): boolean {
    return /^pokemon\s+\d+$/i.test(this.normalizeText(value).replace(/[-_]+/g, ' '));
  }

  private normalizeText(value: string): string {
    return value
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  private dexFromPokemonSprite(sprite: string): number | undefined {
    const homeMatch = sprite.match(/icon(\d{4})_f00_s[01]\.png/);
    if (homeMatch) {
      return Number(homeMatch[1]);
    }

    const showdownMatch = sprite.match(/\/pokemon\/other\/showdown\/(?:shiny\/)?(\d+)\.gif/);
    return showdownMatch ? Number(showdownMatch[1]) : undefined;
  }

  private officialArtworkUrl(dex: number, shiny: boolean): string {
    const variant = shiny ? 'shiny/' : '';
    return `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${variant}${dex}.png`;
  }

  protected moveset(pokemon: FichaPokemon) {
    return (pokemon.movimentos ?? []).filter((move) => Boolean(move.nome?.trim()));
  }

  protected moveTypeColor(type?: string): string {
    return pokemonMoveTypeColor(type);
  }

  protected contestStyleColor(style?: string): string {
    return pokemonContestStyleColor(style);
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

  protected boxPokemons(ficha: Ficha): FichaPokemon[] {
    return ficha.pokemons.filter((pokemon) => this.pokemonLocation(pokemon) === 'box');
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

  private loadPokemonTypes(pokemon: FichaPokemon): void {
    const key = this.pokemonIdentityKey(pokemon);
    if (this.pokemonTypes()[key] !== undefined) {
      return;
    }

    const species = pokemon.especie?.trim();
    if (!species) {
      this.setPokemonTypes(key, []);
      return;
    }

    this.customPokemonApi.findByName(species).subscribe({
      next: (details) => this.setPokemonTypes(key, details.types ?? []),
      error: () => this.loadOfficialPokemonTypes(key, species, pokemon),
    });
  }

  private loadOfficialPokemonTypes(key: string, species: string, pokemon: FichaPokemon): void {
    const dex = this.dexFromPokemonSprite(pokemon.sprite ?? '');
    const lookup = dex ? String(dex) : this.normalizeText(species);
    if (!lookup) {
      this.setPokemonTypes(key, []);
      return;
    }

    fetch(`https://pokeapi.co/api/v2/pokemon/${encodeURIComponent(lookup)}`)
      .then((response) => response.ok ? response.json() : Promise.reject())
      .then((data: { types?: Array<{ slot?: number; type?: { name?: string } }> }) => {
        const types = (data.types ?? [])
          .sort((first, second) => (first.slot ?? 0) - (second.slot ?? 0))
          .map((entry) => entry.type?.name ?? '')
          .filter(Boolean);
        this.setPokemonTypes(key, types);
      })
      .catch(() => this.setPokemonTypes(key, []));
  }

  private setPokemonTypes(key: string, types: string[]): void {
    this.pokemonTypes.update((current) => ({
      ...current,
      [key]: [...new Set(types.map((type) => type.trim()).filter(Boolean))],
    }));
  }

  private pokemonIdentityKey(pokemon: FichaPokemon): string {
    return String(pokemon.id ?? pokemon.especie ?? pokemon.apelido ?? '').trim().toLowerCase();
  }

  private hydrateMissingMoveStyles(ficha: Ficha): void {
    const movements = this.teamPokemons(ficha)
      .flatMap((pokemon) => pokemon.movimentos ?? [])
      .filter((move) => Boolean(move.nome?.trim()) && !move.style?.trim());

    if (!movements.length) {
      return;
    }

    Promise.all(movements.map(async (move) => ({ move, style: await loadPokemonMoveStyle(move.nome) })))
      .then((results) => {
        const changed = results.some(({ move, style }) => {
          if (!style || move.style?.trim()) {
            return false;
          }
          move.style = style;
          return true;
        });
        if (changed) {
          this.ficha.update((current) => current ? { ...current } : current);
        }
      });
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

  protected isAdmin(): boolean {
    return ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? '');
  }

  protected canEdit(ficha: Ficha): boolean {
    const currentUser = this.auth.currentUser();
    return this.isAdmin() || Boolean(currentUser?.idOrganizacao && currentUser.idOrganizacao === ficha.idOrganizacao);
  }
}
