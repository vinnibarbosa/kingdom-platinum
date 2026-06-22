import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../services/auth.service';
import { UsuarioApiService } from '../../services/usuario-api.service';

@Component({
  selector: 'app-password-reset',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <ng-container *ngIf="isAdmin() && opened">
      <div class="modal-backdrop password-reset-backdrop" (click)="close()">
        <form class="password-reset-modal" (click)="$event.stopPropagation()" (ngSubmit)="submit()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Administração</span>
              <h3>Redefinir senha de outro usuário</h3>
            </div>
            <button type="button" class="button ghost" (click)="close()" [disabled]="loading()">Fechar</button>
          </div>

          <p>A próxima senha que esse usuário informar no login será cadastrada como a nova senha.</p>

          <label>
            Usuário que terá a senha redefinida
            <input
              name="resetUsername"
              autocomplete="off"
              [(ngModel)]="username"
              required
              minlength="3"
              maxlength="50"
            />
          </label>

          <p class="error" *ngIf="error()">{{ error() }}</p>
          <p class="success" *ngIf="success()">{{ success() }}</p>

          <div class="modal-actions">
            <button type="button" class="button ghost" (click)="close()" [disabled]="loading()">Cancelar</button>
            <button type="submit" class="button primary" [disabled]="loading()">
              {{ loading() ? 'Liberando...' : 'Liberar nova senha' }}
            </button>
          </div>
        </form>
      </div>
    </ng-container>
  `,
})
export class PasswordResetComponent {
  @Input() opened = false;
  @Output() readonly closed = new EventEmitter<void>();

  private readonly auth = inject(AuthService);
  private readonly api = inject(UsuarioApiService);

  protected readonly isAdmin = computed(() => ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? ''));
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');
  protected username = '';

  protected close(): void {
    if (this.loading()) {
      return;
    }
    this.resetState();
    this.closed.emit();
  }

  protected submit(): void {
    this.error.set('');
    this.success.set('');

    const username = this.username.trim();
    if (username.length < 3) {
      this.error.set('Informe um usuário válido.');
      return;
    }

    this.loading.set(true);
    this.api.redefinePassword({ username }).subscribe({
      next: () => {
        this.success.set(`Redefinição liberada para ${username}. A próxima senha usada no login será cadastrada.`);
        this.loading.set(false);
      },
      error: (response: unknown) => {
        this.error.set(this.errorMessage(response));
        this.loading.set(false);
      },
    });
  }

  private resetState(): void {
    this.username = '';
    this.error.set('');
    this.success.set('');
  }

  private errorMessage(response: unknown): string {
    if (response instanceof HttpErrorResponse && response.status === 404) {
      return 'Usuário não encontrado.';
    }
    if (response instanceof HttpErrorResponse && response.status === 403) {
      return 'Somente administradores podem redefinir senhas.';
    }
    if (response instanceof HttpErrorResponse && response.status === 422 && typeof response.error?.message === 'string') {
      return response.error.message;
    }
    return 'Não foi possível redefinir a senha.';
  }
}
