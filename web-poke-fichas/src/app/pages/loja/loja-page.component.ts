import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CompraLojaItem, FichaCompra, LojaCupom, LojaCupomPayload, LojaItem, LojaItemPayload } from '../../models/loja.model';
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
        <div class="store-head-actions">
          <button type="button" class="button secondary" *ngIf="isAdmin()" (click)="openCouponEditor()">Cupons</button>
          <button type="button" class="button primary" *ngIf="isAdmin()" (click)="openEditor()">Adicionar item</button>
          <button
            type="button"
            class="button secondary store-cart-button"
            [class.has-items]="cartCount() > 0"
            (click)="openCart()"
            [attr.aria-label]="cartAriaLabel()"
          >
            Carrinho
            <span class="store-cart-count" *ngIf="cartCount()">{{ cartCount() > 99 ? '99+' : cartCount() }}</span>
          </button>
        </div>
      </div>

      <div class="state-card" *ngIf="loading()">Carregando itens...</div>
      <div class="state-card error" *ngIf="error()">{{ error() }}</div>

      <div class="store-grid" *ngIf="!loading()">
        <article
          class="store-card"
          *ngFor="let item of items()"
          [class.in-cart]="cartQuantity(item.id) > 0"
          [class.just-added]="lastAddedItemId() === item.id"
        >
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
            <div class="store-price-block">
              <small>Preço</small>
              <strong>{{ item.preco > 0 ? money(item.preco) : 'Não definido' }}</strong>
            </div>
            <div class="store-card-actions">
              <button type="button" class="store-edit-button" *ngIf="isAdmin()" (click)="openEditor(item)">Editar</button>
              <button
                *ngIf="cartQuantity(item.id) === 0 && item.ativo && item.preco > 0"
                type="button"
                class="button primary store-add-button"
                (click)="addToCart(item)"
              >
                Adicionar
              </button>
              <span class="store-unavailable" *ngIf="cartQuantity(item.id) === 0 && (!item.ativo || item.preco <= 0)">Indisponível</span>
              <div class="store-quantity-control" *ngIf="cartQuantity(item.id) > 0" [attr.aria-label]="item.nome + ': ' + cartQuantity(item.id) + ' no carrinho'">
                <button type="button" (click)="decreaseCartItem(item.id)" [attr.aria-label]="'Diminuir quantidade de ' + item.nome">−</button>
                <span><strong>{{ cartQuantity(item.id) }}</strong><small>no carrinho</small></span>
                <button type="button" (click)="addToCart(item)" [attr.aria-label]="'Aumentar quantidade de ' + item.nome">+</button>
              </div>
            </div>
          </div>
        </article>
      </div>

      <p class="store-empty" *ngIf="!loading() && !items().length">Nenhum item disponível no momento.</p>
    </section>

    <div class="store-cart-feedback" *ngIf="cartFeedback()" role="status" aria-live="polite">
      <div>
        <strong>{{ cartFeedback() }}</strong>
        <small>{{ cartCount() }} {{ cartCount() === 1 ? 'item' : 'itens' }} no carrinho</small>
      </div>
      <button type="button" (click)="openCart()">Ver carrinho</button>
    </div>

    <div class="modal-backdrop" *ngIf="cartOpen()" (click)="closeCart()">
      <section class="store-modal" (click)="$event.stopPropagation()" aria-modal="true" role="dialog">
        <div class="modal-head"><div><span class="eyebrow">Compra</span><h3>Carrinho</h3></div><button type="button" class="button secondary" (click)="closeCart()">Fechar</button></div>
        <p class="store-empty" *ngIf="!cart().length">Seu carrinho está vazio.</p>
        <div class="store-cart-lines" *ngIf="cart().length">
          <article *ngFor="let line of cart()"><div><strong>{{ line.item.nome }}</strong><small>{{ money(line.item.preco) }} cada</small></div><label>Qtd. <input type="number" min="1" [ngModel]="line.quantidade" (ngModelChange)="setCartQuantity(line.item.id, $event)" /></label><strong>{{ money(line.item.preco * line.quantidade) }}</strong><button type="button" class="icon-button" aria-label="Remover item" (click)="removeFromCart(line.item.id)">×</button></article>
        </div>
        <label *ngIf="cart().length">Cupom de desconto <input [(ngModel)]="couponCode" placeholder="Ex.: NENDO10" /></label>
        <div class="store-purchase-summary" *ngIf="cart().length"><span>Subtotal</span><strong>{{ money(cartSubtotal()) }}</strong></div>
        <div class="store-character-picker">
          <p>Escolha o personagem que pagará pela compra.</p>
          <button type="button" *ngFor="let ficha of buyableFichas()" [class.selected]="selectedFichaId() === ficha.id" (click)="selectedFichaId.set(ficha.id)">
            <span>{{ ficha.nome }}</span><small>{{ money(ficha.dinheiro || 0) }}</small>
          </button>
          <p class="store-empty" *ngIf="!buyableFichas().length">Você ainda não possui fichas disponíveis para comprar.</p>
        </div>
        <p class="inline-error" *ngIf="purchaseError()">{{ purchaseError() }}</p>
        <button type="button" class="button primary store-confirm" [disabled]="buying() || !selectedFichaId() || !cart().length" (click)="buy()">{{ buying() ? 'Comprando...' : 'Confirmar compra' }}</button>
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

    <div class="modal-backdrop" *ngIf="couponEditorOpen()" (click)="closeCouponEditor()">
      <section class="store-modal" (click)="$event.stopPropagation()" aria-modal="true" role="dialog">
        <div class="modal-head"><div><span class="eyebrow">Administração</span><h3>Cupons</h3></div><button type="button" class="button secondary" (click)="closeCouponEditor()">Fechar</button></div>
        <form class="store-coupon-form" (ngSubmit)="saveCoupon()"><label>Código <input [(ngModel)]="couponDraft.codigo" name="codigo" placeholder="Ex.: NENDO10" /></label><label>Desconto (%) <input type="number" min="1" max="100" [(ngModel)]="couponDraft.percentual" name="percentual" /></label><label class="store-checkbox"><input type="checkbox" [(ngModel)]="couponDraft.ativo" name="ativo" /> Ativo</label><button type="submit" class="button primary">{{ editingCouponId() ? 'Atualizar cupom' : 'Criar cupom' }}</button></form>
        <p class="inline-error" *ngIf="couponError()">{{ couponError() }}</p>
        <div class="store-coupon-list"><article *ngFor="let coupon of coupons()"><div><strong>{{ coupon.codigo }}</strong><small>{{ coupon.percentual }}% de desconto</small></div><span [class.inactive]="!coupon.ativo">{{ coupon.ativo ? 'Ativo' : 'Inativo' }}</span><button type="button" class="button secondary" (click)="editCoupon(coupon)">Editar</button><button type="button" class="button danger" (click)="deleteCoupon(coupon)">Excluir</button></article></div>
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
            <img *ngIf="item.sprite" [src]="item.sprite" [alt]="item.name" loading="lazy" decoding="async" (error)="preserveBrokenPreviewSpace($event)" />
            <span class="item-empty-dot" *ngIf="!item.sprite">?</span>
            <strong>{{ item.name }}</strong>
            <small>{{ item.category || 'Item' }}</small>
          </button>
        </div>
      </section>
    </div>
  `,
})
export class LojaPageComponent implements OnInit, OnDestroy {
  private readonly api = inject(LojaApiService);
  private readonly auth = inject(AuthService);
  private readonly catalog = inject(CatalogItemApiService);

  protected readonly items = signal<LojaItem[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly cartOpen = signal(false);
  protected readonly cart = signal<{ item: LojaItem; quantidade: number }[]>([]);
  protected readonly cartFeedback = signal('');
  protected readonly lastAddedItemId = signal<number | null>(null);
  protected readonly buyableFichas = signal<FichaCompra[]>([]);
  protected readonly selectedFichaId = signal<number | null>(null);
  protected readonly purchaseError = signal('');
  protected readonly buying = signal(false);
  protected couponCode = '';
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
  protected readonly couponEditorOpen = signal(false);
  protected readonly coupons = signal<LojaCupom[]>([]);
  protected readonly editingCouponId = signal<number | null>(null);
  protected readonly couponError = signal('');
  protected couponDraft: LojaCupomPayload = { codigo: '', percentual: 10, ativo: true };
  private initialCatalogImportAttempted = false;
  private cartFeedbackTimer?: ReturnType<typeof setTimeout>;
  protected readonly categories = ['Restauração HP / PP', 'Restaurar status', 'Pokébolas', 'Itens de batalha', 'Contest itens', 'Evolutionary', 'Berries', 'Treasure', 'Thrash itens', 'Trainer itens (Keys)', 'TM / Pill case'];
  protected draft: ItemDraft = this.emptyDraft();
  protected readonly isAdmin = computed(() => ['ADMIN', 'A'].includes(this.auth.currentUser()?.perfil ?? ''));
  protected readonly cartCount = computed(() => this.cart().reduce((total, line) => total + line.quantidade, 0));
  protected readonly cartSubtotal = computed(() => this.cart().reduce((total, line) => total + (line.item.preco * line.quantidade), 0));
  protected readonly cartAriaLabel = computed(() => {
    const count = this.cartCount();
    return count ? `Abrir carrinho com ${count} ${count === 1 ? 'item' : 'itens'}` : 'Abrir carrinho vazio';
  });

  ngOnInit(): void { this.load(); }

  ngOnDestroy(): void {
    if (this.cartFeedbackTimer) clearTimeout(this.cartFeedbackTimer);
  }

  protected addToCart(item: LojaItem): void {
    this.cart.update((cart) => {
      const existing = cart.find((line) => line.item.id === item.id);
      return existing ? cart.map((line) => line.item.id === item.id ? { ...line, quantidade: line.quantidade + 1 } : line) : [...cart, { item, quantidade: 1 }];
    });
    this.lastAddedItemId.set(item.id);
    this.cartFeedback.set(`${item.nome} adicionado`);
    if (this.cartFeedbackTimer) clearTimeout(this.cartFeedbackTimer);
    this.cartFeedbackTimer = setTimeout(() => {
      this.cartFeedback.set('');
      this.lastAddedItemId.set(null);
    }, 3600);
  }

  protected cartQuantity(id: number): number {
    return this.cart().find((line) => line.item.id === id)?.quantidade ?? 0;
  }

  protected decreaseCartItem(id: number): void {
    this.cart.update((cart) => cart.flatMap((line) => {
      if (line.item.id !== id) return [line];
      return line.quantidade > 1 ? [{ ...line, quantidade: line.quantidade - 1 }] : [];
    }));
  }

  protected openCart(): void {
    this.cartFeedback.set('');
    this.lastAddedItemId.set(null);
    this.purchaseError.set(''); this.selectedFichaId.set(null); this.cartOpen.set(true);
    this.api.listOwnedFichas().subscribe({
      next: (fichas) => this.buyableFichas.set(fichas),
      error: () => this.purchaseError.set('Não foi possível carregar suas fichas para esta compra.'),
    });
  }

  protected closeCart(): void { this.cartOpen.set(false); this.buyableFichas.set([]); }
  protected setCartQuantity(id: number, value: number): void { const quantity = Math.max(1, Math.floor(Number(value) || 1)); this.cart.update((cart) => cart.map((line) => line.item.id === id ? { ...line, quantidade: quantity } : line)); }
  protected removeFromCart(id: number): void { this.cart.update((cart) => cart.filter((line) => line.item.id !== id)); }

  protected buy(): void {
    const fichaId = this.selectedFichaId();
    if (!fichaId || !this.cart().length) return;
    this.buying.set(true); this.purchaseError.set('');
    const items: CompraLojaItem[] = this.cart().map((line) => ({ idItem: line.item.id, quantidade: line.quantidade }));
    this.api.buy(items, fichaId, this.couponCode).subscribe({
      next: () => { this.buying.set(false); this.cart.set([]); this.couponCode = ''; this.closeCart(); },
      error: (error) => { this.buying.set(false); this.purchaseError.set(this.apiErrorMessage(error, 'Não foi possível concluir a compra.')); },
    });
  }

  protected openEditor(item?: LojaItem): void {
    this.editingId.set(item?.id ?? null);
    this.draft = item ? { categoria: item.categoria, codigo: item.codigo || '', icone: item.icone || '', nome: item.nome, descricao: item.descricao || '', preco: item.preco, ativo: item.ativo, ordem: item.ordem || 0 } : this.emptyDraft();
    this.editorError.set(''); this.closeCatalogPicker(); this.editorOpen.set(true);
  }

  private syncStoreCatalog(): void {
    this.catalog.listKingdomCatalog().subscribe({
      next: (catalog) => {
        const items: LojaItemPayload[] = catalog.map((item, index) => ({
          nome: item.name,
          categoria: this.storeCategory(item.category),
          codigo: item.code || this.itemCode(item.name),
          descricao: item.description || '',
          icone: item.sprite || '',
          preco: item.price ?? 0,
          ativo: true,
          ordem: index,
        }));
        if (!items.length) {
          this.error.set('Não foi possível obter os itens do catálogo Kingdom Platinum.');
          return;
        }
        this.api.importCatalog(items).subscribe({
          next: () => {
            this.load();
          },
          error: (error) => {
            this.error.set(this.apiErrorMessage(error, 'Não foi possível importar o catálogo da loja.'));
          },
        });
      },
      error: () => {
        this.error.set('Não foi possível obter os itens do catálogo Kingdom Platinum.');
      },
    });
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
    const code = item.code || this.itemCode(item.name);
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

  protected preserveBrokenPreviewSpace(event: Event): void {
    const image = event.target as HTMLImageElement;
    image.style.visibility = 'hidden';
    image.setAttribute('aria-hidden', 'true');
  }

  protected saveItem(): void {
    if (!this.draft.nome.trim() || !this.draft.categoria || Number(this.draft.preco) < 0) { this.editorError.set('Informe nome, categoria e um preço válido.'); return; }
    this.saving.set(true); this.editorError.set('');
    const payload: LojaItemPayload = { ...this.draft, nome: this.draft.nome.trim(), preco: Number(this.draft.preco), ativo: Boolean(this.draft.ativo) };
    const request = this.editingId() ? this.api.update(this.editingId()!, payload) : this.api.create(payload);
    request.subscribe({ next: () => { this.saving.set(false); this.closeEditor(); this.load(); }, error: (error) => { this.saving.set(false); this.editorError.set(this.apiErrorMessage(error, 'Não foi possível salvar o item.')); } });
  }

  protected removeItem(): void {
    const id = this.editingId();
    if (!id || !confirm('Excluir este item da loja?')) return;
    this.api.delete(id).subscribe({ next: () => { this.closeEditor(); this.load(); }, error: () => this.editorError.set('Não foi possível excluir o item.') });
  }

  protected openCouponEditor(): void { this.couponEditorOpen.set(true); this.couponError.set(''); this.editingCouponId.set(null); this.couponDraft = { codigo: '', percentual: 10, ativo: true }; this.loadCoupons(); }
  protected closeCouponEditor(): void { this.couponEditorOpen.set(false); }
  protected editCoupon(coupon: LojaCupom): void { this.editingCouponId.set(coupon.id); this.couponDraft = { codigo: coupon.codigo, percentual: coupon.percentual, ativo: coupon.ativo }; }
  protected saveCoupon(): void {
    if (!this.couponDraft.codigo.trim() || this.couponDraft.percentual < 1 || this.couponDraft.percentual > 100) { this.couponError.set('Informe um código e um desconto entre 1% e 100%.'); return; }
    const request = this.editingCouponId() ? this.api.updateCoupon(this.editingCouponId()!, this.couponDraft) : this.api.createCoupon(this.couponDraft);
    request.subscribe({ next: () => { this.couponError.set(''); this.editingCouponId.set(null); this.couponDraft = { codigo: '', percentual: 10, ativo: true }; this.loadCoupons(); }, error: (error) => this.couponError.set(this.apiErrorMessage(error, 'Não foi possível salvar o cupom.')) });
  }
  protected deleteCoupon(coupon: LojaCupom): void { if (!confirm(`Excluir o cupom ${coupon.codigo}?`)) return; this.api.deleteCoupon(coupon.id).subscribe({ next: () => this.loadCoupons(), error: () => this.couponError.set('Não foi possível excluir o cupom.') }); }
  private loadCoupons(): void { this.api.listCoupons().subscribe({ next: (coupons) => this.coupons.set(coupons), error: () => this.couponError.set('Não foi possível carregar os cupons.') }); }

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

  private apiErrorMessage(error: unknown, fallback: string): string {
    if (!error || typeof error !== 'object') return fallback;
    const response = error as { error?: unknown };
    if (!response.error || typeof response.error !== 'object' || response.error instanceof Error) return fallback;
    const message = (response.error as { message?: unknown }).message;
    return typeof message === 'string' && message.trim() ? message : fallback;
  }

  private load(): void {
    this.loading.set(true); this.error.set('');
    (this.isAdmin() ? this.api.listAdmin() : this.api.list()).subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
        if (this.isAdmin() && !this.initialCatalogImportAttempted) {
          this.initialCatalogImportAttempted = true;
          this.syncStoreCatalog();
        }
      },
      error: () => { this.error.set('Não foi possível carregar a loja.'); this.loading.set(false); },
    });
  }
}
