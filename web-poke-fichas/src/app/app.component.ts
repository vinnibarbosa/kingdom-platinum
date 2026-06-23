import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';

import { AuthService } from './services/auth.service';
import { PasswordResetComponent } from './components/password-reset/password-reset.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, PasswordResetComponent, RouterLink, RouterOutlet],
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
          <a routerLink="/">Fichas</a>
          <span>{{ username() }}</span>
          <button type="button" class="button ghost" *ngIf="isAdmin()" (click)="passwordResetOpen.set(true)">
            Redefinir senha de usuário
          </button>
          <button type="button" class="button ghost" (click)="logout()">Sair</button>
        </nav>
      </header>

      <app-password-reset
        *ngIf="isAdmin()"
        [opened]="passwordResetOpen()"
        (closed)="passwordResetOpen.set(false)"
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

  protected logout(): void {
    this.auth.logout().subscribe({
      next: () => this.router.navigateByUrl('/login'),
      error: () => this.router.navigateByUrl('/login'),
    });
  }
}
