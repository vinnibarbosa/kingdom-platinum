import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, OnChanges, ViewChild, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FichaTimelineEntry } from '../../models/ficha.model';
import { FichaApiService } from '../../services/ficha-api.service';

const EMPTY_ENTRY = (): FichaTimelineEntry => ({
  secao: 'Acontecimentos históricos',
  periodo: '',
  titulo: '',
  subtitulo: '',
  conteudo: '<p></p>',
  cor: '#aeb5bf',
});

@Component({
  selector: 'app-ficha-timeline',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="timeline-admin-section">
      <header class="timeline-admin-header">
        <div>
          <span class="eyebrow">Administração</span>
          <h3>Timeline do mundo</h3>
          <p>Registros internos de cosmologia, história e acontecimentos jogados.</p>
        </div>
        <button type="button" class="button ghost" (click)="newEntry()">Novo marco</button>
      </header>

      <p class="timeline-state" *ngIf="loading">Carregando timeline...</p>
      <p class="timeline-state error" *ngIf="error">{{ error }}</p>

      <div class="timeline-admin-layout" *ngIf="!loading">
        <aside class="timeline-entry-list" aria-label="Marcos da timeline">
          <button
            type="button"
            *ngFor="let entry of entries; let index = index"
            class="timeline-entry-option"
            [class.active]="selectedIndex === index"
            [style.--entry-color]="entry.cor || '#aeb5bf'"
            (click)="selectEntry(index)"
          >
            <span>{{ entry.periodo || entry.secao }}</span>
            <strong>{{ entry.titulo }}</strong>
          </button>
          <p class="timeline-empty" *ngIf="!entries.length">Nenhum marco cadastrado.</p>
        </aside>

        <form class="timeline-editor" *ngIf="draft" (submit)="$event.preventDefault(); save()">
          <div class="timeline-editor-heading">
            <span class="timeline-dot" [style.background]="draft.cor || '#aeb5bf'"></span>
            <span>{{ draft.id ? 'Editar marco' : 'Novo marco' }}</span>
          </div>

          <div class="timeline-field-grid">
            <label>Seção
              <input [(ngModel)]="draft.secao" name="secao" maxlength="80" list="timeline-sections" required />
            </label>
            <label>Período / Era
              <input [(ngModel)]="draft.periodo" name="periodo" maxlength="120" placeholder="Ex.: I - Criação" />
            </label>
            <label class="wide">Título
              <input [(ngModel)]="draft.titulo" name="titulo" maxlength="180" required />
            </label>
            <label class="wide">Subtítulo
              <input [(ngModel)]="draft.subtitulo" name="subtitulo" maxlength="255" />
            </label>
            <label class="timeline-color-label">Cor do marco
              <input type="color" [(ngModel)]="draft.cor" name="cor" />
            </label>
          </div>

          <datalist id="timeline-sections">
            <option value="Caderno de ideias"></option>
            <option value="Acontecimentos históricos"></option>
            <option value="Acontecimentos jogados"></option>
            <option value="Base velada"></option>
          </datalist>

          <div class="timeline-rich-toolbar" role="toolbar" aria-label="Formatação da timeline">
            <button type="button" title="Negrito" (click)="format('bold')"><strong>B</strong></button>
            <button type="button" title="Itálico" (click)="format('italic')"><em>I</em></button>
            <button type="button" title="Sublinhado" (click)="format('underline')"><u>U</u></button>
            <span class="toolbar-divider"></span>
            <button type="button" title="Título" (click)="format('formatBlock', 'h4')">T</button>
            <button type="button" title="Parágrafo" (click)="format('formatBlock', 'p')">P</button>
            <button type="button" title="Lista" (click)="format('insertUnorderedList')">Lista</button>
            <button type="button" title="Lista numerada" (click)="format('insertOrderedList')">1.</button>
            <button type="button" title="Citação" (click)="format('formatBlock', 'blockquote')">“</button>
            <button type="button" title="Separador" (click)="format('insertHorizontalRule')">Linha</button>
            <label class="toolbar-color" title="Cor do texto">
              <input #fontColor type="color" value="#596474" (input)="format('foreColor', fontColor.value)" />
            </label>
          </div>

          <div
            #contentEditor
            class="timeline-rich-editor"
            contenteditable="true"
            [innerHTML]="draft.conteudo"
            (input)="syncContent(contentEditor.innerHTML)"
          ></div>

          <div class="timeline-editor-actions">
            <button type="button" class="button ghost danger" *ngIf="draft.id" (click)="remove()">Remover</button>
            <button type="submit" class="button primary" [disabled]="saving">{{ saving ? 'Salvando...' : 'Salvar timeline' }}</button>
          </div>
        </form>
      </div>
    </section>
  `,
  styles: `
    .timeline-admin-section { display: grid; gap: 16px; }
    .timeline-admin-header { display: flex; align-items: start; justify-content: space-between; gap: 18px; }
    .timeline-admin-header h3 { margin: 2px 0 4px; }
    .timeline-admin-header p, .timeline-state, .timeline-empty { margin: 0; color: #727b87; }
    .timeline-admin-layout { display: grid; grid-template-columns: minmax(190px, .42fr) minmax(0, 1fr); gap: 14px; align-items: start; }
    .timeline-entry-list { display: grid; gap: 6px; max-height: 620px; overflow: auto; padding-right: 4px; }
    .timeline-entry-option { border: 1px solid #d9dce1; border-left: 4px solid var(--entry-color); border-radius: 7px; background: #fffdf8; padding: 10px 11px; text-align: left; cursor: pointer; display: grid; gap: 3px; color: #303746; }
    .timeline-entry-option:hover, .timeline-entry-option.active { background: #f4f5f7; border-color: var(--entry-color); }
    .timeline-entry-option span { font-size: .74rem; color: #747d89; }
    .timeline-entry-option strong { font-size: .88rem; font-weight: 600; }
    .timeline-editor { border: 1px solid #d9dce1; border-radius: 8px; background: #fffdf8; padding: 16px; display: grid; gap: 14px; min-width: 0; }
    .timeline-editor-heading { display: flex; gap: 8px; align-items: center; color: #586272; font-size: .82rem; font-weight: 600; text-transform: uppercase; }
    .timeline-dot { width: 10px; height: 10px; border-radius: 50%; }
    .timeline-field-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
    .timeline-field-grid label { display: grid; gap: 5px; color: #697381; font-size: .8rem; }
    .timeline-field-grid input { min-width: 0; }
    .timeline-field-grid .wide { grid-column: span 2; }
    .timeline-color-label { max-width: 90px; }
    .timeline-color-label input { height: 38px; padding: 3px; }
    .timeline-rich-toolbar { display: flex; flex-wrap: wrap; gap: 5px; align-items: center; padding: 7px; border: 1px solid #d9dce1; border-radius: 7px; background: #f6f6f3; }
    .timeline-rich-toolbar button { min-width: 31px; height: 30px; border: 0; border-radius: 4px; background: transparent; color: #4d5867; cursor: pointer; font-size: .76rem; }
    .timeline-rich-toolbar button:hover { background: #e4e7eb; }
    .toolbar-divider { width: 1px; height: 21px; background: #d3d7dd; margin: 0 2px; }
    .toolbar-color { width: 30px; height: 30px; overflow: hidden; border-radius: 4px; cursor: pointer; }
    .toolbar-color input { width: 42px; height: 42px; margin: -6px; border: 0; cursor: pointer; }
    .timeline-rich-editor { min-height: 260px; padding: 13px; border: 1px solid #d9dce1; border-radius: 7px; line-height: 1.62; color: #34404d; outline: none; overflow-wrap: anywhere; }
    .timeline-rich-editor:focus { border-color: #aeb5bf; box-shadow: 0 0 0 3px rgb(174 181 191 / .18); }
    .timeline-rich-editor h4 { margin: 0 0 9px; font-size: 1rem; }
    .timeline-rich-editor p { margin: 0 0 10px; }
    .timeline-rich-editor blockquote { margin: 12px 0; padding: 8px 12px; border-left: 3px solid #aeb5bf; color: #657080; background: #f5f6f7; }
    .timeline-rich-editor hr { border: 0; border-top: 1px solid #d9dce1; margin: 16px 0; }
    .timeline-editor-actions { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
    @media (max-width: 700px) { .timeline-admin-layout { grid-template-columns: 1fr; } .timeline-entry-list { max-height: 180px; } }
    @media (max-width: 460px) { .timeline-admin-header { align-items: stretch; flex-direction: column; } .timeline-field-grid { grid-template-columns: 1fr; } .timeline-field-grid .wide { grid-column: auto; } }
  `,
})
export class FichaTimelineComponent implements OnChanges {
  @Input({ required: true }) fichaId!: number;
  @ViewChild('contentEditor') private contentEditor?: ElementRef<HTMLElement>;

  private readonly api = inject(FichaApiService);

  protected entries: FichaTimelineEntry[] = [];
  protected draft: FichaTimelineEntry | null = null;
  protected selectedIndex = -1;
  protected loading = false;
  protected saving = false;
  protected error = '';

  ngOnChanges(): void {
    if (this.fichaId) {
      this.load();
    }
  }

  protected selectEntry(index: number): void {
    this.selectedIndex = index;
    this.draft = this.clone(this.entries[index]);
  }

  protected newEntry(): void {
    this.selectedIndex = -1;
    this.draft = { ...EMPTY_ENTRY(), ordem: this.entries.length ? Math.max(...this.entries.map((entry) => entry.ordem ?? 0)) + 10 : 10 };
  }

  protected syncContent(content: string): void {
    if (this.draft) {
      this.draft.conteudo = content;
    }
  }

  protected format(command: string, value?: string): void {
    this.contentEditor?.nativeElement.focus();
    document.execCommand(command, false, value);
    this.syncContent(this.contentEditor?.nativeElement.innerHTML ?? '');
  }

  protected save(): void {
    if (!this.draft || !this.draft.titulo.trim() || !this.draft.secao.trim() || !this.plainText(this.draft.conteudo)) {
      this.error = 'Preencha seção, título e conteúdo antes de salvar.';
      return;
    }

    const entry = this.clone(this.draft);
    const next = [...this.entries];
    if (this.selectedIndex < 0) {
      next.push(entry);
    } else {
      next[this.selectedIndex] = entry;
    }

    this.persist(next);
  }

  protected remove(): void {
    if (this.selectedIndex < 0) {
      return;
    }
    this.persist(this.entries.filter((_, index) => index !== this.selectedIndex));
  }

  private load(): void {
    this.loading = true;
    this.error = '';
    this.api.getTimeline(this.fichaId).subscribe({
      next: (entries) => {
        this.entries = [...(entries ?? [])].sort((a, b) => (a.ordem ?? 0) - (b.ordem ?? 0));
        this.loading = false;
        this.draft = this.entries.length ? this.clone(this.entries[0]) : null;
        this.selectedIndex = this.entries.length ? 0 : -1;
      },
      error: () => {
        this.error = 'Não foi possível carregar a timeline.';
        this.loading = false;
      },
    });
  }

  private persist(entries: FichaTimelineEntry[]): void {
    this.saving = true;
    this.error = '';
    const payload = entries.map((entry, index) => ({ ...entry, ordem: (index + 1) * 10 }));
    this.api.updateTimeline(this.fichaId, payload).subscribe({
      next: (saved) => {
        this.entries = saved;
        this.saving = false;
        if (!this.entries.length) {
          this.draft = null;
          this.selectedIndex = -1;
          return;
        }
        const selected = Math.min(Math.max(this.selectedIndex, 0), this.entries.length - 1);
        this.selectEntry(selected);
      },
      error: () => {
        this.error = 'Não foi possível salvar a timeline.';
        this.saving = false;
      },
    });
  }

  private clone(entry: FichaTimelineEntry): FichaTimelineEntry {
    return { ...entry };
  }

  private plainText(value: string): string {
    return value.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim();
  }
}
