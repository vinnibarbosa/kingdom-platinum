import { CommonModule } from '@angular/common';
import { Component, Input, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { FichaApiService } from '../../services/ficha-api.service';

@Component({
  selector: 'app-ficha-delete',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button type="button" class="button danger" *ngIf="canDelete()" (click)="opened.set(true)">Excluir</button>

    <div class="modal-backdrop" *ngIf="opened()" (click)="close()">
      <div class="delete-modal" (click)="$event.stopPropagation()">
        <div>
          <span class="eyebrow">Confirmação</span>
          <h3>Excluir ficha?</h3>
          <p>
            A ficha <strong>{{ fichaNome }}</strong> e todo o seu conteúdo serão apagados permanentemente.
          </p>
        </div>

        <p class="error" *ngIf="error()">{{ error() }}</p>

        <div class="modal-actions">
          <button type="button" class="button ghost" (click)="close()" [disabled]="deleting()">Cancelar</button>
          <button type="button" class="button danger" (click)="confirmDelete()" [disabled]="deleting()">
            {{ deleting() ? 'Excluindo...' : 'Excluir definitivamente' }}
          </button>
        </div>
      </div>
    </div>
  `,
})
export class FichaDeleteComponent {
  @Input({ required: true }) fichaId!: number;
  @Input({ required: true }) fichaIdOrganizacao!: number;
  @Input({ required: true }) fichaNome!: string;

  private readonly api = inject(FichaApiService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly opened = signal(false);
  protected readonly deleting = signal(false);
  protected readonly error = signal('');

  protected canDelete(): boolean {
    const user = this.auth.currentUser();
    return Boolean(
      user
      && (['ADMIN', 'A'].includes(user.perfil) || user.idOrganizacao === this.fichaIdOrganizacao)
    );
  }

  protected close(): void {
    if (this.deleting()) {
      return;
    }
    this.opened.set(false);
    this.error.set('');
  }

  protected confirmDelete(): void {
    if (!this.canDelete() || this.deleting()) {
      return;
    }

    this.deleting.set(true);
    this.error.set('');
    this.api.delete(this.fichaId).subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: () => {
        this.error.set('Não foi possível excluir esta ficha.');
        this.deleting.set(false);
      },
    });
  }
}
