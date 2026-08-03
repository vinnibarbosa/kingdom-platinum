import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';

import { AuthService } from './services/auth.service';
import { FichaActivityLogComponent } from './components/ficha-activity-log/ficha-activity-log.component';
import { PasswordResetComponent } from './components/password-reset/password-reset.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FichaActivityLogComponent, PasswordResetComponent, RouterLink, RouterOutlet],
  template: `
    <div class="shell">
      <header class="topbar" *ngIf="isLoggedIn()">
        <a
          class="brand"
          href="https://kingdomplatinum.vercel.app/"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="Abrir o site Kingdom Platinum"
        >
          <img class="brand-logo" src="/assets/kingdom-platinum-logo.png" alt="Kingdom Platinum KP" />
        </a>

        <nav class="topbar-actions">
          <a class="topbar-library-link" routerLink="/">Fich&aacute;rio</a>
          <button
            type="button"
            class="topbar-admin-link"
            *ngIf="isAdmin()"
            title="Ver todas as edicoes das fichas"
            (click)="activityLogOpen.set(true)"
          >
            Registros
          </button>
          <button
            type="button"
            class="topbar-admin-link"
            *ngIf="isAdmin()"
            title="Redefinir senha de usuario"
            (click)="passwordResetOpen.set(true)"
          >
            Senha
          </button>
          <span class="topbar-user-chip">
            <span>{{ username() || 'Conta' }}</span>
            <button type="button" title="Sair da conta" aria-label="Sair da conta" (click)="logout()">x</button>
          </span>
        </nav>
      </header>

      <app-password-reset
        *ngIf="isAdmin()"
        [opened]="passwordResetOpen()"
        (closed)="passwordResetOpen.set(false)"
      />

      <app-ficha-activity-log
        *ngIf="isAdmin()"
        [opened]="activityLogOpen()"
        (closed)="activityLogOpen.set(false)"
      />

      <main>
        <router-outlet />
      </main>
    </div>
  `,
})
export class AppComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly isLoggedIn = computed(() => this.auth.isLoggedIn());
  protected readonly isAdmin = computed(() => ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? ''));
  protected readonly username = computed(() => this.auth.currentUser()?.nome ?? this.auth.currentUser()?.username ?? '');
  protected readonly passwordResetOpen = signal(false);
  protected readonly activityLogOpen = signal(false);

  protected logout(): void {
    this.auth.logout().subscribe({
      next: () => this.router.navigateByUrl('/login'),
      error: () => this.router.navigateByUrl('/login'),
    });
  }
}
