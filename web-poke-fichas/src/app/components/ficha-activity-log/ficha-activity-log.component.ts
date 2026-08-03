import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, computed, inject, signal } from '@angular/core';

import { FichaHistorico, FichaHistoricoGlobal } from '../../models/ficha.model';
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
            <h3>Registros de todas as fichas</h3>
          </div>
          <button type="button" class="button ghost" (click)="close()">Fechar</button>
        </div>

        <div class="state-card" *ngIf="loading()">Carregando registros...</div>
        <div class="state-card error" *ngIf="error()">{{ error() }}</div>
        <div class="state-card" *ngIf="!loading() && !error() && !history().length">
          Nenhuma alteracao registrada ainda.
        </div>

        <div class="history-list" *ngIf="!loading() && history().length">
          <article class="history-entry" *ngFor="let entry of history(); trackBy: trackByHistory">
            <div class="history-entry-head">
              <div class="global-history-heading">
                <span class="history-action" [class]="'history-action action-' + entry.acao.toLowerCase()">
                  {{ historyAction(entry.acao) }}
                </span>
                <strong>{{ entry.nomeFicha }}</strong>
              </div>
              <time>{{ entry.createdAt | date:'dd/MM/yyyy HH:mm' }}</time>
            </div>
            <strong>{{ historyField(entry.campo) }}</strong>
            <div class="history-values" *ngIf="entry.acao === 'ALTERADO'">
              <span>{{ entry.valorAnterior || 'Vazio' }}</span>
              <span aria-hidden="true">&rarr;</span>
              <span>{{ entry.valorNovo || 'Vazio' }}</span>
            </div>
            <p *ngIf="entry.acao === 'ADICIONADO'">{{ entry.valorNovo || 'Item adicionado' }}</p>
            <p *ngIf="entry.acao === 'REMOVIDO'">{{ entry.valorAnterior || 'Item removido' }}</p>
            <small>por {{ entry.createdBy || 'sistema' }}</small>
          </article>
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
      this.load();
    }
  }

  protected readonly openedState = signal(false);
  protected readonly opened = this.openedState.asReadonly();
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly history = signal<FichaHistoricoGlobal[]>([]);
  private readonly isAdmin = computed(() => ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? ''));

  protected close(): void {
    this.openedState.set(false);
    this.closed.emit();
  }

  protected trackByHistory(_: number, entry: FichaHistoricoGlobal): number {
    return entry.id;
  }

  protected historyAction(action: FichaHistorico['acao']): string {
    return { ADICIONADO: 'Adicionado', REMOVIDO: 'Removido', ALTERADO: 'Alterado' }[action];
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

  private load(): void {
    if (!this.isAdmin()) {
      this.close();
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.api.getAllHistory().subscribe({
      next: (entries) => {
        this.history.set(entries ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Nao foi possivel carregar os registros administrativos.');
        this.loading.set(false);
      },
    });
  }

  private capitalize(value: string): string {
    const words = value.replace(/([a-z])([A-Z])/g, '$1 $2');
    return words ? words[0].toUpperCase() + words.slice(1) : 'Campo';
  }
}
