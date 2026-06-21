import { CommonModule } from '@angular/common';
import { Component, Input, computed, inject, signal } from '@angular/core';

import { FichaHistorico } from '../../models/ficha.model';
import { AuthService } from '../../services/auth.service';
import { FichaApiService } from '../../services/ficha-api.service';

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
              <span aria-hidden="true">→</span>
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
export class FichaHistoryComponent {
  @Input({ required: true }) fichaId!: number;

  private readonly api = inject(FichaApiService);
  private readonly auth = inject(AuthService);

  protected readonly opened = signal(false);
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly history = signal<FichaHistorico[]>([]);
  protected readonly isAdmin = computed(() => ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? ''));

  protected open(): void {
    if (!this.isAdmin()) {
      return;
    }

    this.opened.set(true);
    this.loading.set(true);
    this.error.set('');
    this.api.getHistory(this.fichaId).subscribe({
      next: (entries) => {
        this.history.set(entries ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar os registros desta ficha.');
        this.loading.set(false);
      },
    });
  }

  protected close(): void {
    this.opened.set(false);
  }

  protected trackByHistory(_: number, entry: FichaHistorico): number {
    return entry.id;
  }

  protected historyAction(action: FichaHistorico['acao']): string {
    return {
      ADICIONADO: 'Adicionado',
      REMOVIDO: 'Removido',
      ALTERADO: 'Alterado',
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
      return `${labels[field] ?? this.capitalize(field)} ${Number(index) + 1}`;
    }).join(' › ');
  }

  private capitalize(value: string): string {
    const words = value.replace(/([a-z])([A-Z])/g, '$1 $2');
    return words ? words[0].toUpperCase() + words.slice(1) : 'Campo';
  }
}
