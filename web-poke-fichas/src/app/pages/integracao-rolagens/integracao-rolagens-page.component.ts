import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

interface AuthorizationResponse {
  codigo: string;
}

@Component({
  selector: 'app-integracao-rolagens-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="login-page">
      <div class="login-panel">
        <span class="eyebrow">Integração</span>
        <h1>Conectar ao Rolador</h1>
        <p *ngIf="!error()">Estamos autorizando o Rolador a consultar suas fichas e consumir Honey.</p>
        <p class="error" *ngIf="error()">{{ error() }}</p>
        <div class="form-actions" *ngIf="error()">
          <button type="button" class="button primary" (click)="authorize()">Tentar novamente</button>
          <a class="button secondary" routerLink="/">Voltar ao Fichário</a>
        </div>
      </div>
    </section>
  `,
})
export class IntegracaoRolagensPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  protected readonly error = signal('');

  ngOnInit(): void {
    this.authorize();
  }

  protected authorize(): void {
    this.error.set('');
    this.http.post<AuthorizationResponse>('/api/integracoes/rolagens/autorizacoes', {}).subscribe({
      next: ({ codigo }) => {
        const callback = new URL('https://kingdomplatinum.vercel.app/');
        callback.searchParams.set('kp_fichas_code', codigo);
        window.location.replace(callback.toString());
      },
      error: () => this.error.set('Não foi possível conectar as contas agora. Tente novamente.'),
    });
  }
}
