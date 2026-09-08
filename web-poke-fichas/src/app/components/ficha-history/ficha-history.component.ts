import { CommonModule } from '@angular/common';
import { Component, Input, computed, inject, signal } from '@angular/core';

import { FichaHistorico, FichaPokemon } from '../../models/ficha.model';
import { AuthService } from '../../services/auth.service';
import { FichaApiService } from '../../services/ficha-api.service';

interface HistoryGroup {
  lote: string;
  entries: FichaHistorico[];
  createdAt: string;
  createdBy?: string;
}

@Component({
  selector: 'app-ficha-history',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button type="button" class="button ghost" *ngIf="isAdmin()" (click)="open()">Registros</button>

    <div class="modal-backdrop" *ngIf="opened()" (click)="close()">
      <div class="history-modal" (click)="$event.stopPropagation()">
        <div class="modal-head">
          <div>
            <span class="eyebrow">Administração</span>
            <h3>Registros da ficha</h3>
          </div>
          <button type="button" class="button ghost" (click)="close()">Fechar</button>
        </div>

        <div class="state-card" *ngIf="loading()">Carregando registros...</div>
        <div class="state-card error" *ngIf="error()">{{ error() }}</div>
        <div class="state-card" *ngIf="!loading() && !error() && !history().length">
          Nenhuma alteração registrada ainda.
        </div>

        <div class="history-list" *ngIf="!loading() && history().length">
          <article class="history-entry history-group" *ngFor="let group of groupedHistory(); trackBy: trackByHistoryGroup">
            <div class="history-entry-head">
              <span class="history-action" [class]="'history-action action-' + groupActionClass(group)">
                {{ groupAction(group) }}
              </span>
              <time>{{ group.createdAt | date:'dd/MM/yyyy HH:mm' }}</time>
            </div>
            <small class="history-group-count" *ngIf="group.entries.length > 1">
              {{ group.entries.length }} alterações neste salvamento
            </small>
            <div class="history-group-changes">
              <div class="history-change" *ngFor="let entry of group.entries; trackBy: trackByHistory">
                <strong>{{ historyField(entry.campo) }}</strong>
                <div class="history-values" *ngIf="entry.acao === 'ALTERADO'">
                  <span>{{ entry.valorAnterior || 'Vazio' }}</span>
                  <span aria-hidden="true">→</span>
                  <span>{{ entry.valorNovo || 'Vazio' }}</span>
                </div>
                <p *ngIf="entry.acao === 'ADICIONADO' || entry.acao === 'COMPRA'">{{ entry.valorNovo || 'Item adicionado' }}</p>
                <p *ngIf="entry.acao === 'REMOVIDO'">{{ entry.valorAnterior || 'Item removido' }}</p>
              </div>
            </div>
            <small class="history-group-author">por {{ group.createdBy || 'sistema' }}</small>
          </article>
          <button
            type="button"
            class="button secondary history-load-more"
            *ngIf="hasMore()"
            [disabled]="loadingMore()"
            (click)="loadMore()"
          >
            {{ loadingMore() ? 'Carregando...' : 'Carregar registros anteriores' }}
          </button>
        </div>
      </div>
    </div>
  `,
})
export class FichaHistoryComponent {
  private static readonly HISTORY_PAGE_SIZE = 150;

  @Input({ required: true }) fichaId!: number;
  @Input() pokemons: FichaPokemon[] = [];

  private readonly api = inject(FichaApiService);
  private readonly auth = inject(AuthService);

  protected readonly opened = signal(false);
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly history = signal<FichaHistorico[]>([]);
  protected readonly loadingMore = signal(false);
  protected readonly hasMore = signal(false);
  protected readonly isAdmin = computed(() => ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? ''));
  protected readonly groupedHistory = computed<HistoryGroup[]>(() => {
    const groups = new Map<string, HistoryGroup>();
    this.history().forEach((entry) => {
      const key = entry.lote || `registro-${entry.id}`;
      const group = groups.get(key);
      if (group) {
        group.entries.push(entry);
        return;
      }
      groups.set(key, {
        lote: key,
        entries: [entry],
        createdAt: entry.createdAt,
        createdBy: entry.createdBy,
      });
    });
    return [...groups.values()];
  });

  protected open(): void {
    if (!this.isAdmin()) {
      return;
    }

    this.opened.set(true);
    this.loading.set(true);
    this.error.set('');
    this.history.set([]);
    this.loadHistory(0, false);
  }

  protected loadMore(): void {
    if (this.loadingMore() || !this.hasMore()) return;
    this.loadingMore.set(true);
    this.error.set('');
    this.loadHistory(this.history().length, true);
  }

  private loadHistory(offset: number, append: boolean): void {
    this.api.getHistory(this.fichaId, offset, FichaHistoryComponent.HISTORY_PAGE_SIZE).subscribe({
      next: (entries) => {
        const page = entries ?? [];
        this.history.update((current) => append ? [...current, ...page] : page);
        this.hasMore.set(page.length === FichaHistoryComponent.HISTORY_PAGE_SIZE);
        this.loading.set(false);
        this.loadingMore.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar os registros desta ficha.');
        this.loading.set(false);
        this.loadingMore.set(false);
      },
    });
  }

  protected close(): void {
    this.opened.set(false);
  }

  protected trackByHistory(_: number, entry: FichaHistorico): number {
    return entry.id;
  }

  protected trackByHistoryGroup(_: number, group: HistoryGroup): string {
    return group.lote;
  }

  protected groupAction(group: HistoryGroup): string {
    const actions = new Set(group.entries.map((entry) => entry.acao));
    return actions.size === 1 ? this.historyAction(group.entries[0].acao) : 'Atualização';
  }

  protected groupActionClass(group: HistoryGroup): string {
    const actions = new Set(group.entries.map((entry) => entry.acao));
    return actions.size === 1 ? group.entries[0].acao.toLowerCase() : 'atualizacao';
  }

  protected historyAction(action: FichaHistorico['acao']): string {
    return {
      ADICIONADO: 'Adicionado',
      REMOVIDO: 'Removido',
      ALTERADO: 'Alterado',
      COMPRA: 'Compra',
    }[action];
  }

  protected historyField(path: string): string {
    const labels: Record<string, string> = {
      ficha: 'Ficha', nome: 'Nome', frase: 'Frase', idade: 'Idade', naturalidade: 'Naturalidade',
      classePersonagem: 'Classe', alturaCm: 'Altura', pesoKg: 'Peso', tipoFisico: 'Tipo Físico',
      indole: 'Índole', ranking: 'Pontos de Ranking', ocupacao: 'Ocupação',
      reputacao: 'Pontos de Reputação', dinheiro: 'Dinheiro', pontosVida: 'Pontos de Vida',
      equipe: 'Equipe', pontos: 'Pontos', photoplayer: 'Imagem do Personagem', avatar: 'Avatar',
      player: 'Player', biografia: 'Biografia', anotacoes: 'Anotações', pokemons: 'Pokémon',
      movimentos: 'Movimento', itens: 'Item', conquistas: 'Conquista', relacionados: 'Relacionado',
      habilidades: 'Habilidade', registros: 'Registro', apelido: 'Apelido', especie: 'Espécie',
      sprite: 'Sprite', box: 'Localização', descricao: 'Descrição', quantidade: 'Quantidade',
      mecanica: 'Mecânica', ordem: 'Ordem',
    };

    return path.split('.').map((segment) => {
      const indexed = segment.match(/^([^[]+)\[(\d+)]$/);
      if (!indexed) {
        return labels[segment] ?? this.capitalize(segment);
      }
      const [, field, index] = indexed;
      if (field === 'pokemons') {
        return this.pokemonName(Number(index));
      }
      return `${labels[field] ?? this.capitalize(field)} ${Number(index) + 1}`;
    }).join(' › ');
  }

  private pokemonName(index: number): string {
    const pokemon = this.pokemons[index];
    return pokemon?.apelido?.trim() || pokemon?.especie?.trim() || `Pokémon ${index + 1}`;
  }

  private capitalize(value: string): string {
    const words = value.replace(/([a-z])([A-Z])/g, '$1 $2');
    return words ? words[0].toUpperCase() + words.slice(1) : 'Campo';
  }
}
