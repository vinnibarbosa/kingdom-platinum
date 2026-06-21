import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';

import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterOutlet],
  template: `
    <div class="shell">
      <header class="topbar" *ngIf="isLoggedIn()">
        <a class="brand" routerLink="/" aria-label="Kingdom Platinum KP">
          <img class="brand-logo" src="/assets/kingdom-platinum-logo.png" alt="Kingdom Platinum KP" />
        </a>

        <nav class="topbar-actions">
          <a routerLink="/">Fichas</a>
          <span>{{ username() }}</span>
          <button type="button" class="button ghost" (click)="logout()">Sair</button>
        </nav>
      </header>

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
  protected readonly username = computed(() => this.auth.currentUser()?.nome ?? this.auth.currentUser()?.username ?? '');

  protected logout(): void {
    this.auth.logout().subscribe({
      next: () => this.router.navigateByUrl('/login'),
      error: () => this.router.navigateByUrl('/login'),
    });
  }
}
