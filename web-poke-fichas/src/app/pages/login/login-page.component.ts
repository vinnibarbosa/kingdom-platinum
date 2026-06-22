import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

type AuthMode = 'login' | 'register';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="login-page">
      <div class="login-panel">
        <div>
          <div class="login-brand-logo">
            <img src="/assets/kingdom-platinum-logo.png" alt="Kingdom Platinum KP" />
          </div>
          <h1>{{ mode() === 'login' ? 'Acesse suas fichas' : 'Crie sua conta' }}</h1>
          <p>{{ mode() === 'login' ? 'Entre para consultar e editar seus personagens.' : 'Escolha um usuário e uma senha para começar.' }}</p>
        </div>

        <div class="auth-toggle">
          <button type="button" [class.active]="mode() === 'login'" (click)="setMode('login')">Entrar</button>
          <button type="button" [class.active]="mode() === 'register'" (click)="setMode('register')">Registrar</button>
        </div>

        <form class="form-grid" *ngIf="mode() === 'login'" (ngSubmit)="submitLogin()">
          <label>
            Usuário
            <input name="username" autocomplete="username" [(ngModel)]="username" required />
          </label>

          <label>
            Senha
            <input name="senha" type="password" autocomplete="current-password" [(ngModel)]="senha" required />
          </label>

          <p class="error" *ngIf="error()">{{ error() }}</p>
          <p class="success" *ngIf="success()">{{ success() }}</p>

          <button type="submit" class="button primary" [disabled]="loading()">
            {{ loading() ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>

        <form class="form-grid" *ngIf="mode() === 'register'" (ngSubmit)="submitRegister()">
          <label>
            Usuário
            <input name="registerUsername" autocomplete="username" [(ngModel)]="username" required minlength="3" />
          </label>

          <label>
            Senha
            <input name="registerSenha" type="password" autocomplete="new-password" [(ngModel)]="senha" required minlength="4" />
          </label>

          <p class="error" *ngIf="error()">{{ error() }}</p>
          <p class="success" *ngIf="success()">{{ success() }}</p>

          <button type="submit" class="button primary" [disabled]="loading()">
            {{ loading() ? 'Criando...' : 'Criar conta' }}
          </button>
        </form>
      </div>
    </section>
  `,
})
export class LoginPageComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly mode = signal<AuthMode>('login');
  protected username = '';
  protected senha = '';
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');

  protected setMode(mode: AuthMode): void {
    this.mode.set(mode);
    this.error.set('');
    this.success.set('');
  }

  protected submitLogin(): void {
    this.error.set('');
    this.success.set('');
    this.loading.set(true);

    this.auth.login(this.username, this.senha).subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: () => {
        this.error.set('Não foi possível entrar. Confira usuário e senha.');
        this.loading.set(false);
      },
    });
  }

  protected submitRegister(): void {
    this.error.set('');
    this.success.set('');
    this.loading.set(true);

    this.auth.register({
      username: this.username,
      senha: this.senha,
    }).subscribe({
      next: () => {
        this.success.set('Conta criada. Agora entre com seu usuário e senha.');
        this.mode.set('login');
        this.loading.set(false);
      },
      error: (error: unknown) => {
        const message = error instanceof HttpErrorResponse
          && error.status === 422
          && typeof error.error?.message === 'string'
          ? error.error.message
          : 'Não foi possível criar a conta. Verifique os dados e tente outro usuário.';
        this.error.set(message);
        this.loading.set(false);
      },
    });
  }
}
