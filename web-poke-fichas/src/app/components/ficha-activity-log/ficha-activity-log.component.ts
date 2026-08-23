import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, computed, inject, signal } from '@angular/core';

import { FichaHistorico, FichaResumo } from '../../models/ficha.model';
import { AuthService } from '../../services/auth.service';
import { FichaApiService } from '../../services/ficha-api.service';

@Component({
  selector: 'app-ficha-activity-log',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="modal-backdrop" *ngIf="opened()" (click)="close()">
      <div class="history-modal global-history-modal" (click)="$event.stopPropagation()">
        <div class="modal-head">
          <div>
            <span class="eyebrow">Administracao</span>
            <h3>Registros das fichas</h3>
          </div>
          <button type="button" class="button ghost" (click)="close()">Fechar</button>
        </div>

        <div class="state-card" *ngIf="loadingFichas()">Carregando fichas...</div>
        <div class="state-card error" *ngIf="error()">{{ error() }}</div>

        <div class="admin-history-layout" *ngIf="!loadingFichas() && !error()">
          <aside class="history-ficha-picker">
            <span class="history-picker-label">Fichas</span>
            <button
              type="button"
              class="history-ficha-option"
              *ngFor="let ficha of fichas(); trackBy: trackByFicha"
              [class.selected]="selectedFicha()?.id === ficha.id"
              (click)="selectFicha(ficha)"
            >
              <span>{{ ficha.nome }}</span>
              <small>{{ ficha.player || 'Sem player' }}</small>
            </button>
            <p class="empty-picker" *ngIf="!fichas().length">Nenhuma ficha encontrada.</p>
          </aside>

          <section class="history-details">
            <div class="state-card" *ngIf="!selectedFicha()">Selecione uma ficha para ver os registros.</div>
            <div class="state-card" *ngIf="selectedFicha() && loadingHistory()">Carregando registros...</div>
            <div class="state-card" *ngIf="selectedFicha() && !loadingHistory() && !history().length">
              Nenhuma alteracao registrada nesta ficha.
            </div>

            <div class="history-list" *ngIf="selectedFicha() && !loadingHistory() && history().length">
              <article class="history-entry" *ngFor="let entry of history(); trackBy: trackByHistory">
                <div class="history-entry-head">
                  <span class="history-action" [class]="'history-action action-' + entry.acao.toLowerCase()">
                    {{ historyAction(entry.acao) }}
                  </span>
                  <time>{{ entry.createdAt | date:'dd/MM/yyyy HH:mm' }}</time>
                </div>
                <strong>{{ historyField(entry.campo) }}</strong>
                <div class="history-values" *ngIf="entry.acao === 'ALTERADO'">
                  <span>{{ entry.valorAnterior || 'Vazio' }}</span>
                  <span aria-hidden="true">&rarr;</span>
                  <span>{{ entry.valorNovo || 'Vazio' }}</span>
                </div>
                <p *ngIf="entry.acao === 'ADICIONADO' || entry.acao === 'COMPRA'">{{ entry.valorNovo || 'Item adicionado' }}</p>
                <p *ngIf="entry.acao === 'REMOVIDO'">{{ entry.valorAnterior || 'Item removido' }}</p>
                <small>por {{ entry.createdBy || 'sistema' }}</small>
              </article>
            </div>
          </section>
        </div>
      </div>
    </div>
  `,
})
export class FichaActivityLogComponent {
  private readonly api = inject(FichaApiService);
  private readonly auth = inject(AuthService);

  @Output() readonly closed = new EventEmitter<void>();

  @Input('opened')
  set isOpened(value: boolean) {
    this.openedState.set(value);
    if (value) {
      this.loadFichas();
    }
  }

  protected readonly openedState = signal(false);
  protected readonly opened = this.openedState.asReadonly();
  protected readonly loadingFichas = signal(false);
  protected readonly loadingHistory = signal(false);
  protected readonly error = signal('');
  protected readonly fichas = signal<FichaResumo[]>([]);
  protected readonly selectedFicha = signal<FichaResumo | null>(null);
  protected readonly history = signal<FichaHistorico[]>([]);
  private readonly isAdmin = computed(() => ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? ''));

  protected close(): void {
    this.openedState.set(false);
    this.closed.emit();
  }

  protected trackByFicha(_: number, ficha: FichaResumo): number {
    return ficha.id;
  }

  protected trackByHistory(_: number, entry: FichaHistorico): number {
    return entry.id;
  }

  protected historyAction(action: FichaHistorico['acao']): string {
    return { ADICIONADO: 'Adicionado', REMOVIDO: 'Removido', ALTERADO: 'Alterado', COMPRA: 'Compra' }[action];
  }

  protected historyField(path: string): string {
    const labels: Record<string, string> = {
      ficha: 'Ficha', nome: 'Nome', frase: 'Frase', idade: 'Idade', naturalidade: 'Naturalidade',
      classePersonagem: 'Classe', alturaCm: 'Altura', pesoKg: 'Peso', tipoFisico: 'Tipo fisico',
      indole: 'Indole', ranking: 'Pontos de Ranking', ocupacao: 'Ocupacao',
      reputacao: 'Pontos de Reputacao', dinheiro: 'Dinheiro', pontosVida: 'Pontos de Vida',
      equipe: 'Equipe', pontos: 'Pontos', photoplayer: 'Imagem do personagem', avatar: 'Avatar',
      player: 'Player', biografia: 'Biografia', anotacoes: 'Anotacoes', pokemons: 'Pokemon',
      movimentos: 'Movimento', itens: 'Item', conquistas: 'Conquista', relacionados: 'Relacionado',
      habilidades: 'Habilidade', registros: 'Registro', apelido: 'Apelido', especie: 'Especie',
      sprite: 'Sprite', box: 'Localizacao', descricao: 'Descricao', quantidade: 'Quantidade',
      mecanica: 'Mecanica', ordem: 'Ordem',
    };

    return path.split('.').map((segment) => {
      const indexed = segment.match(/^([^[]+)\[(\d+)]$/);
      if (!indexed) {
        return labels[segment] ?? this.capitalize(segment);
      }
      const [, field, index] = indexed;
      return `${labels[field] ?? this.capitalize(field)} ${Number(index) + 1}`;
    }).join(' > ');
  }

  protected selectFicha(ficha: FichaResumo): void {
    if (this.selectedFicha()?.id === ficha.id) {
      return;
    }

    this.selectedFicha.set(ficha);
    this.history.set([]);
    this.error.set('');
    this.loadingHistory.set(true);
    this.api.getHistory(ficha.id).subscribe({
      next: (entries) => {
        this.history.set([...(entries ?? [])].sort((first, second) => {
          const dateDifference = new Date(second.createdAt).getTime() - new Date(first.createdAt).getTime();
          return dateDifference || second.id - first.id;
        }));
        this.loadingHistory.set(false);
      },
      error: () => {
        this.error.set(`Nao foi possivel carregar os registros de ${ficha.nome}.`);
        this.loadingHistory.set(false);
      },
    });
  }

  private loadFichas(): void {
    if (!this.isAdmin()) {
      this.close();
      return;
    }

    this.loadingFichas.set(true);
    this.loadingHistory.set(false);
    this.error.set('');
    this.selectedFicha.set(null);
    this.history.set([]);
    this.api.list(0, 500).subscribe({
      next: (page) => {
        this.fichas.set([...(page.content ?? [])].sort((first, second) => {
          const dateDifference = this.updatedAt(second) - this.updatedAt(first);
          return dateDifference || second.id - first.id;
        }));
        this.loadingFichas.set(false);
      },
      error: () => {
        this.error.set('Nao foi possivel carregar as fichas.');
        this.loadingFichas.set(false);
      },
    });
  }

  private capitalize(value: string): string {
    const words = value.replace(/([a-z])([A-Z])/g, '$1 $2');
    return words ? words[0].toUpperCase() + words.slice(1) : 'Campo';
  }

  private updatedAt(ficha: FichaResumo): number {
    const timestamp = ficha.updatedAt ? new Date(ficha.updatedAt).getTime() : 0;
    return Number.isFinite(timestamp) ? timestamp : 0;
  }
}
