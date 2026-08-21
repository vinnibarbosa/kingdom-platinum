import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FichaCompra, LojaItem, LojaItemPayload } from '../../models/loja.model';
import { AuthService } from '../../services/auth.service';
import { CatalogItem, CatalogItemApiService } from '../../services/catalog-item-api.service';
import { LojaApiService } from '../../services/loja-api.service';

interface ItemDraft extends LojaItemPayload { id?: number; }

@Component({
  selector: 'app-loja-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-wrap store-page">
      <div class="section-head">
        <div>
          <span class="eyebrow">Comércio</span>
          <h1>Loja</h1>
          <p class="section-copy">Itens disponíveis para a jornada.</p>
        </div>
        <button type="button" class="button primary" *ngIf="isAdmin()" (click)="openEditor()">Adicionar item</button>
      </div>

      <div class="state-card" *ngIf="loading()">Carregando itens...</div>
      <div class="state-card error" *ngIf="error()">{{ error() }}</div>

      <div class="store-grid" *ngIf="!loading()">
        <article class="store-card" *ngFor="let item of items()">
          <div class="store-item-icon">
            <img *ngIf="item.icone && !brokenItemIcons().has(item.id)" [src]="item.icone" [alt]="item.nome" (error)="markItemImageBroken(item)" />
            <span *ngIf="!item.icone || brokenItemIcons().has(item.id)">?</span>
          </div>
          <div class="store-card-content">
            <span class="eyebrow">{{ item.categoria }}</span>
            <h2>{{ item.nome }}</h2>
            <p>{{ item.descricao || 'Sem descrição cadastrada.' }}</p>
          </div>
          <div class="store-card-footer">
            <strong>{{ money(item.preco) }}</strong>
            <div class="store-card-actions">
              <button type="button" class="button secondary" *ngIf="isAdmin()" (click)="openEditor(item)">Editar</button>
              <button type="button" class="button primary" [disabled]="!item.ativo" (click)="openPurchase(item)">{{ item.ativo ? 'Comprar' : 'Indisponível' }}</button>
            </div>
          </div>
        </article>
      </div>

      <p class="store-empty" *ngIf="!loading() && !items().length">Nenhum item disponível no momento.</p>
    </section>

    <div class="modal-backdrop" *ngIf="purchaseItem()" (click)="closePurchase()">
      <section class="store-modal" (click)="$event.stopPropagation()" aria-modal="true" role="dialog">
        <div class="modal-head"><div><span class="eyebrow">Compra</span><h3>{{ purchaseItem()?.nome }}</h3></div><button type="button" class="button secondary" (click)="closePurchase()">Fechar</button></div>
        <div class="store-purchase-summary"><span>{{ purchaseItem()?.descricao }}</span><strong>{{ money(purchaseTotal()) }}</strong></div>
        <label>Quantidade <input type="number" min="1" [(ngModel)]="purchaseQuantity" (ngModelChange)="normalizeQuantity()" /></label>
        <div class="store-character-picker">
          <p>Escolha o personagem que pagará pela compra.</p>
          <button type="button" *ngFor="let ficha of buyableFichas()" [class.selected]="selectedFichaId() === ficha.id" (click)="selectedFichaId.set(ficha.id)">
            <span>{{ ficha.nome }}</span><small>{{ money(ficha.dinheiro || 0) }}</small>
          </button>
          <p class="store-empty" *ngIf="!buyableFichas().length">Você ainda não possui fichas disponíveis para comprar.</p>
        </div>
        <p class="inline-error" *ngIf="purchaseError()">{{ purchaseError() }}</p>
        <button type="button" class="button primary store-confirm" [disabled]="buying() || !selectedFichaId()" (click)="buy()">{{ buying() ? 'Comprando...' : 'Confirmar compra' }}</button>
      </section>
    </div>

    <div class="modal-backdrop" *ngIf="editorOpen()" (click)="closeEditor()">
      <section class="store-modal store-editor-modal" (click)="$event.stopPropagation()" aria-modal="true" role="dialog">
        <div class="modal-head"><div><span class="eyebrow">Administração</span><h3>{{ editingId() ? 'Editar item' : 'Novo item' }}</h3></div><button type="button" class="button secondary" (click)="closeEditor()">Fechar</button></div>
        <button type="button" class="button secondary" (click)="openCatalogPicker()">Escolher item do catálogo</button>
        <div class="store-form-grid">
          <label>Nome <input [(ngModel)]="draft.nome" /></label>
          <label>Preço <input type="number" min="0" step="1" [(ngModel)]="draft.preco" /></label>
          <label>Categoria <select [(ngModel)]="draft.categoria"><option *ngFor="let category of categories" [value]="category">{{ category }}</option></select></label>
          <label>Código <input [(ngModel)]="draft.codigo" placeholder="Opcional" /></label>
          <label class="store-form-full">Descrição <textarea rows="3" [(ngModel)]="draft.descricao"></textarea></label>
          <label>Ícone por URL <input [(ngModel)]="draft.icone" placeholder="Opcional" /></label>
          <label class="store-upload-label">Enviar ícone <input type="file" accept="image/*" (change)="readIcon($event)" /></label>
          <label class="store-checkbox"><input type="checkbox" [(ngModel)]="draft.ativo" /> Disponível na loja</label>
        </div>
        <p class="inline-error" *ngIf="editorError()">{{ editorError() }}</p>
        <div class="modal-actions">
          <button type="button" class="button danger" *ngIf="editingId()" (click)="removeItem()">Excluir</button>
          <button type="button" class="button primary" [disabled]="saving()" (click)="saveItem()">{{ saving() ? 'Salvando...' : 'Salvar item' }}</button>
        </div>
      </section>
    </div>

    <div class="modal-backdrop inventory-picker-backdrop" *ngIf="catalogPickerOpen()" (click)="closeCatalogPicker()">
      <section class="inventory-modal" (click)="$event.stopPropagation()" aria-modal="true" role="dialog">
        <div class="modal-head">
          <div><span class="eyebrow">Catálogo</span><h3>Escolher item</h3></div>
          <button type="button" class="button secondary" (click)="closeCatalogPicker()">Fechar</button>
        </div>
        <input [ngModel]="catalogSearch()" (ngModelChange)="updateCatalogSearch($event)" placeholder="Buscar item" autocomplete="off" />
        <p *ngIf="catalogLoading()" class="store-catalog-state">Carregando itens...</p>
        <p *ngIf="catalogError()" class="inline-error">{{ catalogError() }}</p>
        <p *ngIf="!catalogLoading() && !filteredCatalogItems().length" class="store-catalog-state">Nenhum item encontrado.</p>
        <div class="inventory-modal-grid" *ngIf="!catalogLoading()" (scroll)="onCatalogScroll($event)">
          <button type="button" class="inventory-modal-option" *ngFor="let item of visibleCatalogItems(); trackBy: trackByCatalogItem" (click)="useCatalogItem(item)">
            <img *ngIf="item.sprite" [src]="item.sprite" [alt]="item.name" loading="lazy" decoding="async" (error)="hideBrokenPreview($event)" />
            <span class="item-empty-dot" *ngIf="!item.sprite">?</span>
            <strong>{{ item.name }}</strong>
            <small>{{ item.category || 'Item' }}</small>
          </button>
        </div>
      </section>
    </div>
  `,
})
export class LojaPageComponent implements OnInit {
  private readonly api = inject(LojaApiService);
  private readonly auth = inject(AuthService);
  private readonly catalog = inject(CatalogItemApiService);

  protected readonly items = signal<LojaItem[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly purchaseItem = signal<LojaItem | null>(null);
  protected readonly buyableFichas = signal<FichaCompra[]>([]);
  protected readonly selectedFichaId = signal<number | null>(null);
  protected readonly purchaseError = signal('');
  protected readonly buying = signal(false);
  protected purchaseQuantity = 1;
  protected readonly editorOpen = signal(false);
  protected readonly editingId = signal<number | null>(null);
  protected readonly saving = signal(false);
  protected readonly editorError = signal('');
  protected readonly catalogPickerOpen = signal(false);
  protected readonly catalogSearch = signal('');
  protected readonly catalogItems = signal<CatalogItem[]>([]);
  protected readonly catalogLoading = signal(false);
  protected readonly catalogError = signal('');
  protected readonly brokenItemIcons = signal<Set<number>>(new Set());
  protected readonly catalogVisibleLimit = signal(120);
  protected readonly categories = ['Restauração HP / PP', 'Restaurar status', 'Pokébolas', 'Itens de batalha', 'Contest itens', 'Evolutionary', 'Berries', 'Treasure', 'Thrash itens', 'Trainer itens (Keys)', 'TM / Pill case'];
  protected draft: ItemDraft = this.emptyDraft();
  protected readonly isAdmin = computed(() => ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? ''));
  protected readonly purchaseTotal = computed(() => (this.purchaseItem()?.preco ?? 0) * this.purchaseQuantity);

  ngOnInit(): void { this.load(); }

  protected openPurchase(item: LojaItem): void {
    this.purchaseItem.set(item); this.purchaseQuantity = 1; this.purchaseError.set(''); this.selectedFichaId.set(null);
    this.api.listOwnedFichas().subscribe({
      next: (fichas) => this.buyableFichas.set(fichas),
      error: () => this.purchaseError.set('Não foi possível carregar suas fichas para esta compra.'),
    });
  }

  protected closePurchase(): void { this.purchaseItem.set(null); this.buyableFichas.set([]); }
  protected normalizeQuantity(): void { this.purchaseQuantity = Math.max(1, Math.floor(Number(this.purchaseQuantity) || 1)); }

  protected buy(): void {
    const item = this.purchaseItem(); const fichaId = this.selectedFichaId();
    if (!item || !fichaId) return;
    this.buying.set(true); this.purchaseError.set('');
    this.api.buy(item.id, fichaId, this.purchaseQuantity).subscribe({
      next: () => { this.buying.set(false); this.closePurchase(); },
      error: (error) => { this.buying.set(false); this.purchaseError.set(error?.error?.message || 'Não foi possível concluir a compra.'); },
    });
  }

  protected openEditor(item?: LojaItem): void {
    this.editingId.set(item?.id ?? null);
    this.draft = item ? { categoria: item.categoria, codigo: item.codigo || '', icone: item.icone || '', nome: item.nome, descricao: item.descricao || '', preco: item.preco, ativo: item.ativo, ordem: item.ordem || 0 } : this.emptyDraft();
    this.editorError.set(''); this.closeCatalogPicker(); this.editorOpen.set(true);
  }
  protected closeEditor(): void { this.editorOpen.set(false); this.closeCatalogPicker(); }

  protected openCatalogPicker(): void {
    this.catalogPickerOpen.set(true);
    this.catalogSearch.set('');
    this.catalogVisibleLimit.set(120);
    this.catalogError.set('');
    if (this.catalogItems().length) return;
    this.catalogLoading.set(true);
    this.catalog.list().subscribe({
      next: (items) => { this.catalogItems.set(items); this.catalogLoading.set(false); },
      error: () => { this.catalogError.set('Não foi possível carregar o catálogo de itens.'); this.catalogLoading.set(false); },
    });
  }

  protected closeCatalogPicker(): void {
    this.catalogPickerOpen.set(false);
    this.catalogSearch.set('');
    this.catalogVisibleLimit.set(120);
    this.catalogError.set('');
  }

  protected updateCatalogSearch(term: string): void {
    this.catalogSearch.set(term);
    this.catalogVisibleLimit.set(120);
  }

  protected readonly filteredCatalogItems = computed(() => {
    const search = this.itemCode(this.catalogSearch());
    if (!search) return this.catalogItems();
    return this.catalogItems().filter((item) => this.itemCode(`${item.name} ${item.category}`).includes(search));
  });
  protected readonly visibleCatalogItems = computed(() => this.filteredCatalogItems().slice(0, this.catalogVisibleLimit()));

  protected onCatalogScroll(event: Event): void {
    const element = event.currentTarget as HTMLElement;
    if (element.scrollTop + element.clientHeight < element.scrollHeight - 60 || this.visibleCatalogItems().length >= this.filteredCatalogItems().length) return;
    this.catalogVisibleLimit.update((limit) => limit + 120);
  }

  protected trackByCatalogItem(_: number, item: CatalogItem): string { return item.name; }

  protected useCatalogItem(item: CatalogItem): void {
    const code = this.itemCode(item.name);
    this.draft = {
      ...this.draft,
      nome: item.name,
      descricao: item.description,
      categoria: this.storeCategory(item.category),
      codigo: code,
      icone: item.sprite,
    };
    this.closeCatalogPicker();
    this.catalog.details(item).subscribe((details) => {
      if (this.draft.codigo !== code) return;
      this.draft = {
        ...this.draft,
        descricao: details.description || this.draft.descricao,
        categoria: this.storeCategory(details.category),
        icone: details.sprite || this.draft.icone,
      };
    });
  }

  protected markItemImageBroken(item: LojaItem): void {
    this.brokenItemIcons.update((ids) => new Set(ids).add(item.id));
  }

  protected hideBrokenPreview(event: Event): void {
    (event.target as HTMLImageElement).style.display = 'none';
  }

  protected saveItem(): void {
    if (!this.draft.nome.trim() || !this.draft.categoria || Number(this.draft.preco) < 0) { this.editorError.set('Informe nome, categoria e um preço válido.'); return; }
    this.saving.set(true); this.editorError.set('');
    const payload: LojaItemPayload = { ...this.draft, nome: this.draft.nome.trim(), preco: Number(this.draft.preco), ativo: Boolean(this.draft.ativo) };
    const request = this.editingId() ? this.api.update(this.editingId()!, payload) : this.api.create(payload);
    request.subscribe({ next: () => { this.saving.set(false); this.closeEditor(); this.load(); }, error: (error) => { this.saving.set(false); this.editorError.set(error?.error?.message || 'Não foi possível salvar o item.'); } });
  }

  protected removeItem(): void {
    const id = this.editingId();
    if (!id || !confirm('Excluir este item da loja?')) return;
    this.api.delete(id).subscribe({ next: () => { this.closeEditor(); this.load(); }, error: () => this.editorError.set('Não foi possível excluir o item.') });
  }

  protected readIcon(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0]; if (!file) return;
    const reader = new FileReader(); reader.onload = () => this.draft = { ...this.draft, icone: String(reader.result || '') }; reader.readAsDataURL(file);
  }

  protected money(value: number): string { return `${new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 }).format(value || 0)}C$`; }
  private emptyDraft(): ItemDraft { return { nome: '', categoria: this.categories[0], codigo: '', icone: '', descricao: '', preco: 0, ativo: true, ordem: 0 }; }
  private storeCategory(value: string): string {
    const normalized = value.trim().toLocaleLowerCase('pt-BR');
    const apiCategoryMap: Record<string, string> = {
      medicine: 'Restauração HP / PP',
      status: 'Restaurar status',
      'standard-balls': 'Pokébolas',
      'special-balls': 'Pokébolas',
      'battle-items': 'Itens de batalha',
      'held-items': 'Itens de batalha',
      'evolution-items': 'Evolutionary',
      berries: 'Berries',
      'valuable-items': 'Treasure',
      'key-items': 'Trainer itens (Keys)',
      'all-machines': 'TM / Pill case',
      'type-enhancement': 'Itens de batalha',
    };
    if (apiCategoryMap[normalized]) return apiCategoryMap[normalized];
    return this.categories.find((category) => category.toLocaleLowerCase('pt-BR') === normalized)
      ?? this.categories.find((category) => normalized.includes(category.toLocaleLowerCase('pt-BR').split(' ')[0]))
      ?? this.draft.categoria;
  }

  private itemCode(value: string): string {
    return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
      .replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
  }

  private load(): void {
    this.loading.set(true); this.error.set('');
    (this.isAdmin() ? this.api.listAdmin() : this.api.list()).subscribe({ next: (items) => { this.items.set(items); this.loading.set(false); }, error: () => { this.error.set('Não foi possível carregar a loja.'); this.loading.set(false); } });
  }
}
