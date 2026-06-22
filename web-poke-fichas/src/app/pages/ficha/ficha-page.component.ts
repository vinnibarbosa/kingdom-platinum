import { CommonModule } from '@angular/common';
import { CdkDrag, CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import { Component, HostListener, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import {
  Ficha,
  FichaConquista,
  FichaHabilidade,
  FichaItem,
  FichaPokemon,
  FichaPokemonMovimento,
  FichaRegistro,
  FichaRelacionado,
} from '../../models/ficha.model';
import { FichaHistoryComponent } from '../../components/ficha-history/ficha-history.component';
import { FichaDeleteComponent } from '../../components/ficha-delete/ficha-delete.component';
import { FichaApiService } from '../../services/ficha-api.service';
import { AuthService } from '../../services/auth.service';
import { display, fichaToPayload, money } from '../../services/ficha-utils';
import { loadPokemonMoveStyle, pokemonContestStyleColor, pokemonMoveTypeColor } from '../../services/pokemon-move-utils';

type FichaTab = 'dados' | 'historia' | 'pokemon' | 'inventario' | 'conquistas' | 'extras';

interface PokemonSpriteChoice {
  dex: number;
  url: string;
  name?: string;
}

interface PokemonDexMove {
  name: string;
  category?: string;
  type?: string;
  style?: string;
  power?: number;
  accuracy?: number;
}

interface PokemonDexDetails {
  abilities: string[];
  moves: PokemonDexMove[];
  stats?: Partial<Record<'hp' | 'atk' | 'def' | 'satk' | 'sdef' | 'speed', number>>;
}

interface MoveTypeOption {
  name: string;
  label: string;
  color: string;
  icon: string;
}

interface PokemonApiSprites {
  front_default?: string | null;
  front_shiny?: string | null;
  other?: {
    home?: {
      front_default?: string | null;
      front_shiny?: string | null;
    };
    'official-artwork'?: {
      front_default?: string | null;
      front_shiny?: string | null;
    };
  };
}

interface PokeballOption {
  name: string;
  label: string;
  icon: string;
}

interface PokemonMechanicOption {
  name: string;
  label: string;
  icon: string;
  color: string;
}

interface HeldItemOption {
  name: string;
  label: string;
  icon?: string;
}

interface InventoryItemOption {
  name: string;
  label: string;
  icon?: string;
  category: string;
  description?: string;
}

interface BadgeOption {
  id: string;
  label: string;
  icon?: string;
}

interface PokemonEntry {
  pokemon: FichaPokemon;
  index: number;
}

interface PokemonListView {
  equipe: PokemonEntry[];
  box: PokemonEntry[];
}

const ITEMDEX_DETAILS: Record<string, { category: string; description: string }> = {
  'beast-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para a captura exclusiva de Ultrabeast.' },
  'cherish-ball': { category: 'Pokéballs', description: 'EVENTO. Um dispositivo de armazenamento de Pokémon.' },
  'dive-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Captura apenas pokémon com Surf e Dive no movelist. Isso permite que uma vez na batalha use este movimento mesmo sem o possuir.' },
  'dusk-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Aumenta o dano causado por certos climas para 1/6 do HP máximo.' },
  'fast-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para a captura de Pokémon selvagem. Confere +1 de prioridade ao primeiro movimento que o pokémon usar em batalha. Além disso, o capturado fica imune ao status de Paralisia.' },
  'friend-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Torna o capturado imune à Condição Infatuated. Além disso, o Pokémon capturado por esse Pokéball ganha +100 HAPP.' },
  'great-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Permite capturar Pokémon até a forma intermediária no Ranking 2.' },
  'heal-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Aumenta em +10% HP máx do capturado. Além disso, confere uma recuperação natural de 1/16 HP por turno uma vez que seu HP chega a 50%.' },
  'heavy-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Aumenta o peso do pokémon capturado por essa pokéball em 50%. Além disso, aumenta o poder de moves relacionados ao peso em 10%.' },
  'level-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Torna o capturado imune ao status de Confusion. Além disso, se capturar Pokémon que evoluem por LVL, o faz evoluir.' },
  'love-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Em caso de breeding, confere +1 verificação de reprodução extra. Infatuated causado pelo capturado duram a batalha inteira.' },
  'lure-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Torna o capturado imune ao status de Burn. Além disso, se capturar Pokémon que evoluem por Water Stone, o faz evoluir.' },
  'luxury-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Confere +20% C$ ganhos com moves e habilidades de ganho monetário, e em batalhas.' },
  'master-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. BLOQUEADA.' },
  'moon-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Torna o capturado imune ao status de Sleep. Além disso, se capturar Pokémon que evoluem por Moon Stone, permite evolução sem a pedra.' },
  'net-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Captura apenas pokémon com String Shot no movelist. Isso permite que use este move mesmo sem ter. Aumenta o preço da seda em +20% C$.' },
  'poke-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Permite capturar Pokémon na forma básica.' },
  'premier-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Permite capturar Pokémon na forma básica. Quando o Pokémon atinge a forma final ele é liberado dois slots na box.' },
  'quick-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Permite capturar Pokémon na forma básica. Torna o capturado imune ao status de Trapped. Além disso, confere +10% de Evasão ao Pokémon capturado.' },
  'repeat-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Quando o capturado possuir uma habilidade com efeito fora de batalha, poderá a utilizar duas vezes ao dia.' },
  'safari-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Esta Pokéball pode ser substituída a qualquer momento por uma outra de maneira permanente.' },
  'strange-ball': { category: 'Pokéballs', description: 'EVENTO. Um dispositivo de armazenamento de Pokémon.' },
  'timer-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. A cada 3 turnos de batalha, o capturado aumenta de forma aleatória um stat em +1 estágio, com exceção do HP.' },
  'ultra-ball': { category: 'Pokéballs', description: 'CONSUMÍVEL. Um dispositivo para captura e armazenamento de Pokémon. Permite capturar Pokémon até a forma final no Ranking 3.' },
  'amulet-coin': { category: 'Itens de Treinador', description: 'Uma moeda da sorte que adiciona +30% C$ aos ganhos monetários diários.' },
  'ability-capsule': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Uma cápsula que permite alterar a habilidade base de um Pokémon.' },
  'ability-patch': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Um emplastro que permite alterar a habilidade de um Pokémon para a sua Hidden Ability.' },
  'big-bamboo-shot': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. É uma muda de bambu muito apreciada como alimento. Quando deixado em um tópico, todas as interações dentro de 24h rendem +30 HAPP como valor fixo ao fim.' },
  'apricorn-box': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Uma caixa prática para armazenar Apricorn. Contém 2 Apricorn à escolha.' },
  'berry-pouch': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Uma bolsa prática para armazenar Berry. Contém 4 berries à escolha.' },
  'berry-pot': { category: 'Itens de Treinador', description: 'Um contêiner portátil para plantio. Libera +1 campo de plantio. Também confere +5% de chance na lavragem do campo.' },
  'bioskill-key': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Uma chave que permite o Pokémon desbloquear uma habilidade da espécie de forma permanente.' },
  'camping-gear': { category: 'Itens de Treinador', description: 'Uma bolsa de acampamento. Confere +1 rolagem durante o evento Escavação.' },
  'discount-coupon': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Um cupom de 20 a 50% de desconto em qualquer compra do Pokémart. Acumulativo com as skills.' },
  'explorer-kit': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Uma bolsa de ferramentas para explorar. Permite realizar uma rolagem para receber um prêmio de acordo com a última lista divulgada da Escavação.' },
  'escape-rope': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Costuma ser carregado por Rangers. Permite uma re-rolagem em momentos narrativos.' },
  'fluffy-tail': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Um brinquedo que atrai atenção de selvagens. É um brinquedo divertido que aumenta a HAPP do pokémon em proporção ao seu estágio. Na forma básica virá com 100 HAPP, na forma intermediária virá com 250 HAPP e na forma final virá com 400 HAPP.' },
  'old-rod': { category: 'Itens de Treinador', description: 'Vara de pesca antiga. Usada em qualquer corpo de água para pescar Pokémon da raridade comum. Com exceção do Marítimo e Recifes.' },
  'good-rod': { category: 'Itens de Treinador', description: 'Vara de pesca de boa qualidade. Usada em qualquer corpo de água para pescar Pokémon das raridades comum e incomum. Com exceção do Marítimo e Recifes.' },
  'super-rod': { category: 'Itens de Treinador', description: 'Vara de pesca de boa qualidade. Usada em qualquer corpo de água para pescar Pokémon de qualquer raridade. Permite capturas no Marítimo e Recifes.' },
  'mini-slot-upgrade': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Item que permite aumentar em 2 a quantidade de pokémon no Slot.' },
  'slot-upgrade': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Item que permite aumentar em 5 a quantidade de pokémon no Slot.' },
  repel: { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Um perfume que repele pokémon. Repele Pokémon durante 1 hora.' },
  'super-repel': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Um perfume que repele pokémon. Repele Pokémon durante 12 horas.' },
  'max-repel': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Um perfume que repele pokémon. Repele Pokémon durante 24 horas.' },
  'tm-case': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Um estojo que contém TMs. Contém 2 TMs à escolha.' },
  tm: { category: 'Máquinas', description: 'Máquina Técnica genérica. Use o nome personalizado para informar qual TM é esta.' },
  tr: { category: 'Máquinas', description: 'Registro Técnico genérico. Use o nome personalizado para informar qual TR é este.' },
  'pokeblock-case': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Um estojo que armazena pokéblocks. Contém 4 Pokéblocks comuns (+15HAPP) à escolha.' },
  'pokeblock-kit': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Um conjunto de Berry Blender e Pokéblock Case. Aumenta as chances de fazer Pokéblocks mesmo sem a skill para tal.' },
  poketch: { category: 'Itens de Treinador', description: 'Um smartwatch. Serve como uma enciclopédia digital que fornece informações sobre os Pokémon e itens em duas ou três frases.' },
  'alpha-poketch': { category: 'Itens de Treinador', description: 'Um Pokétch avançado. Serve como uma enciclopédia digital aprimorada que fornece informações sobre Pokémon e itens em duas ou três frases.' },
  'poffin-case': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Um estojo que armazena Poffins. Contém 3 Poffins à escolha.' },
  'stun-grenade': { category: 'Itens de Treinador', description: 'CONSUMÍVEL. Item muito utilizado por Criminosos, Policiais e Rangers. É uma bomba de efeito moral que adormece os Pokémon em área durante 2 turnos.' },
  'armorite-ore': { category: 'Treasure Items', description: 'Um metal de coloração peculiar apreciada por intelectuais e estudiosos. Pode ser trocada por uma lição de Move Tutor.' },
  'comet-shard': { category: 'Treasure Items', description: 'Um fragmento de cometa que pode ser vendido por 1.200C$.' },
  'common-stone': { category: 'Treasure Items', description: 'Uma pedra comum que pode parecer valiosa para alguns. Pode ser vendida por 100C$ ou arremessada para ferir uma pessoa causando 15 de dano.' },
  envelope: { category: 'Treasure Items', description: 'CONSUMÍVEL. Um envelope com 400C$.' },
  'envelope-m': { category: 'Treasure Items', description: 'CONSUMÍVEL. Um envelope com 800C$.' },
  'envelope-l': { category: 'Treasure Items', description: 'CONSUMÍVEL. Um envelope com 1.000C$.' },
  'envelope-xl': { category: 'Treasure Items', description: 'CONSUMÍVEL. Um envelope com 2.000C$.' },
  'mysterious-stone': { category: 'Treasure Items', description: 'Uma pedra azulada de procedência duvidosa. Não há informações sobre ela.' },
  'small-nugget': { category: 'Treasure Items', description: 'Uma esfera de ouro pequena que pode ser vendida por 500C$.' },
  'nugget': { category: 'Treasure Items', description: 'Uma esfera de ouro que pode ser vendida por 1.000C$.' },
  'big-nugget': { category: 'Treasure Items', description: 'Uma esfera de ouro que pode ser vendida por 1.500C$.' },
  'small-pearl': { category: 'Treasure Items', description: 'Uma pérola pequena que pode ser vendida por 150C$.' },
  pearl: { category: 'Treasure Items', description: 'Uma pérola que pode ser vendida por 300C$.' },
  'big-pearl': { category: 'Treasure Items', description: 'Uma pérola grande que pode ser vendida por 700C$.' },
  'pearl-string': { category: 'Treasure Items', description: 'Grandes pérolas com um brilho prateado. Podem ser vendidas por 2.100C$.' },
  'qr-code': { category: 'Treasure Items', description: 'Um QR Code contendo XC$.' },
  'rare-bone': { category: 'Treasure Items', description: 'Um achado arqueológico que pode ser vendido por 1.800C$.' },
  stardust: { category: 'Treasure Items', description: 'Areia vermelha adorável que flui entre os dedos de forma sedosa. Pode ser vendido por 1.600C$.' },
  'star-piece': { category: 'Treasure Items', description: 'Um fragmento de estrela que emite um brilho distintamente vermelho. Pode ser vendido por 600C$.' },
  'tropical-shell': { category: 'Treasure Items', description: 'Uma concha branca que flutuou para uma praia. É possível ouvir o som do mar dentro dela. Pode ser vendida por 500C$.' },
};

const ITEMDEX_ICONS: Record<string, string> = {
  'beast-ball': 'image55.png',
  'cherish-ball': 'image72.png',
  'dive-ball': 'image43.png',
  'dusk-ball': 'image1.png',
  'fast-ball': 'image37.png',
  'friend-ball': 'image42.png',
  'great-ball': 'image49.png',
  'heal-ball': 'image2.png',
  'heavy-ball': 'image46.png',
  'level-ball': 'image50.png',
  'love-ball': 'image73.png',
  'lure-ball': 'image34.png',
  'luxury-ball': 'image13.png',
  'master-ball': 'image25.png',
  'moon-ball': 'image30.png',
  'net-ball': 'image47.png',
  'poke-ball': 'image58.png',
  'premier-ball': 'image51.png',
  'quick-ball': 'image22.png',
  'repeat-ball': 'image26.png',
  'safari-ball': 'image5.png',
  'strange-ball': 'image35.png',
  'timer-ball': 'image16.png',
  'ultra-ball': 'image3.png',
  'amulet-coin': 'image67.png',
  'ability-capsule': 'image9.png',
  'ability-patch': 'image11.png',
  'big-bamboo-shot': 'image21.png',
  'apricorn-box': 'image44.png',
  'berry-pouch': 'image59.png',
  'berry-pot': 'image31.png',
  'bioskill-key': 'image4.png',
  'camping-gear': 'image8.png',
  'discount-coupon': 'image45.png',
  'explorer-kit': 'image53.png',
  'escape-rope': 'image38.png',
  'fluffy-tail': 'image19.png',
  'old-rod': 'image61.png',
  'good-rod': 'image17.png',
  'super-rod': 'image54.png',
  'mini-slot-upgrade': 'image32.png',
  'slot-upgrade': 'image48.png',
  repel: 'image70.png',
  'super-repel': 'image6.png',
  'max-repel': 'image60.png',
  'tm-case': 'image29.png',
  'pokeblock-case': 'image62.png',
  'pokeblock-kit': 'image27.png',
  poketch: 'image52.png',
  'alpha-poketch': 'image41.png',
  'poffin-case': 'image56.png',
  'stun-grenade': 'image63.png',
  'armorite-ore': 'image40.png',
  'comet-shard': 'image15.png',
  'common-stone': 'image64.png',
  envelope: 'image65.png',
  'envelope-m': 'image66.png',
  'envelope-l': 'image68.png',
  'envelope-xl': 'image20.png',
  'mysterious-stone': 'image33.png',
  'small-nugget': 'image36.png',
  nugget: 'image36.png',
  'big-nugget': 'image14.png',
  'small-pearl': 'image12.png',
  pearl: 'image12.png',
  'big-pearl': 'image10.png',
  'pearl-string': 'image69.png',
  'qr-code': 'image7.png',
  'rare-bone': 'image24.png',
  stardust: 'image28.png',
  'star-piece': 'image71.png',
  'tropical-shell': 'image18.png',
};

@Component({
  selector: 'app-ficha-page',
  standalone: true,
  imports: [CommonModule, DragDropModule, FichaDeleteComponent, FichaHistoryComponent, FormsModule, RouterLink],
  template: `
    <section class="page-wrap sheet-wrap">
      <a class="back-link" routerLink="/">Voltar para fichas</a>

      <div class="state-card" *ngIf="loading()">Abrindo ficha...</div>
      <div class="state-card error" *ngIf="error()">{{ error() }}</div>

      <article
        class="sheet"
        *ngIf="ficha() as current"
        [style.--green]="themeAccent(current.corTema)"
        [style.--green-dark]="themeDark(current.corTema)"
        [style.--gold]="themeHighlight(current.corTema)"
        (input)="scheduleAutoSave()"
        (change)="scheduleAutoSave()"
      >
        <aside class="profile-rail">
          <button type="button" class="portrait portrait-button" title="Selecionar imagem do personagem" (click)="imageInput.click()">
            <img *ngIf="current.photoplayer" [src]="current.photoplayer" [alt]="current.nome" />
            <span *ngIf="!current.photoplayer">{{ initials(current.nome) }}</span>
          </button>
          <input
            #imageInput
            class="visually-hidden"
            type="file"
            accept="image/png,image/jpeg,image/webp,image/gif"
            (change)="selectCharacterImage($event, current)"
          />

          <div class="rail-block">
            <span class="eyebrow">{{ current.classePersonagem || 'Classe' }}</span>
            <h1>{{ current.nome }}</h1>
            <p>{{ current.frase || 'Sem frase cadastrada.' }}</p>
          </div>

          <div class="rail-stats">
            <div><span>PV</span><strong>{{ displayValue(current.pontosVida) }}</strong></div>
            <div><span>Ranking</span><strong>{{ rankingLabel(current.ranking) }}</strong></div>
            <div><span>Reputação</span><strong>{{ reputationLabel(current.reputacao) }}</strong></div>
            <div><span>Dinheiro</span><strong>{{ moneyValue(current.dinheiro) }}</strong></div>
            <div><span>Pontos</span><strong>{{ displayValue(current.pontos) }}</strong></div>
            <div><span>Pokémon</span><strong>{{ totalPokemonCount(current) }}/{{ totalPokemonCapacity(current) }}</strong></div>
          </div>
        </aside>

        <div class="sheet-body">
          <header class="sheet-header">
            <div>
              <span class="eyebrow">{{ current.classePersonagem || 'Personagem' }}</span>
              <h2>{{ current.nome }}</h2>
              <p>{{ current.ocupacao || current.equipe || 'Ficha de aventura' }}</p>
            </div>
            <div class="sheet-actions">
              <a class="button ghost" [routerLink]="['/ficha', current.id, 'visualizar']">Visualizar</a>
              <app-ficha-history [fichaId]="current.id" />
              <app-ficha-delete
                [fichaId]="current.id"
                [fichaNome]="current.nome"
              />
              <label
                class="theme-picker-button"
                title="Alterar cor da ficha"
                [style.--picked-color]="themeAccent(current.corTema)"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M12 3a9 9 0 0 0 0 18h1.2a1.7 1.7 0 0 0 1.2-2.9 1.7 1.7 0 0 1 1.2-2.9H17a4 4 0 0 0 4-4C21 6.6 17 3 12 3Z"></path>
                  <circle cx="7.5" cy="10" r="1"></circle>
                  <circle cx="10" cy="7.5" r="1"></circle>
                  <circle cx="14" cy="7.5" r="1"></circle>
                  <circle cx="16.5" cy="10" r="1"></circle>
                </svg>
                <input
                  type="color"
                  [ngModel]="themeAccent(current.corTema)"
                  (ngModelChange)="selectTheme(current, $event)"
                />
              </label>
            </div>
          </header>

          <nav class="tabs" aria-label="Seções da ficha">
            <button type="button" [class.active]="tab() === 'dados'" (click)="tab.set('dados')">Dados</button>
            <button type="button" [class.active]="tab() === 'historia'" (click)="tab.set('historia')">História</button>
            <button type="button" [class.active]="tab() === 'pokemon'" (click)="tab.set('pokemon')">Pokémon</button>
            <button type="button" [class.active]="tab() === 'inventario'" (click)="tab.set('inventario')">Inventário</button>
            <button type="button" [class.active]="tab() === 'conquistas'" (click)="tab.set('conquistas')">Conquistas</button>
            <button type="button" [class.active]="tab() === 'extras'" (click)="tab.set('extras')">Extras</button>
          </nav>

          <section class="tab-panel" *ngIf="tab() === 'dados'">
            <div class="form-section">
              <h3>Identidade</h3>
              <div class="edit-panel">
                <label>Nome<input [(ngModel)]="current.nome" /></label>
                <label>Frase<input [(ngModel)]="current.frase" /></label>
                <label>
                  Classe
                  <select [(ngModel)]="current.classePersonagem">
                    <option value="">Selecione</option>
                    <option *ngFor="let classe of classes" [value]="classe">{{ classe }}</option>
                  </select>
                </label>
                <label>Ocupação<input [(ngModel)]="current.ocupacao" /></label>
                <label>
                  Equipe
                  <select [(ngModel)]="current.equipe">
                    <option value="" disabled>Equipe</option>
                    <option *ngFor="let equipe of equipes" [value]="equipe">{{ equipe }}</option>
                  </select>
                </label>
                <label>Player<input [(ngModel)]="current.player" /></label>
                <label>Avatar<input placeholder="Nome usado no avatar" [(ngModel)]="current.avatar" /></label>
                <label>Naturalidade<input [(ngModel)]="current.naturalidade" /></label>
                <label>Índole<input [(ngModel)]="current.indole" /></label>
              </div>
            </div>

            <div class="form-section">
              <h3>Atributos</h3>
              <div class="edit-panel">
                <label>Idade<input type="number" [(ngModel)]="current.idade" /></label>
                <label>Altura<input type="number" [(ngModel)]="current.alturaCm" /></label>
                <label>Peso<input type="number" [(ngModel)]="current.pesoKg" /></label>
                <label>Tipo Físico<input [(ngModel)]="current.tipoFisico" /></label>
                <label>Pontos de Ranking<input type="number" [(ngModel)]="current.ranking" /></label>
                <label>Pontos de Reputação<input type="number" [(ngModel)]="current.reputacao" /></label>
                <label>Dinheiro<input type="number" [(ngModel)]="current.dinheiro" /></label>
                <label>Pontos de Vida<input type="number" [(ngModel)]="current.pontosVida" /></label>
                <label>Pontos<input type="number" [(ngModel)]="current.pontos" /></label>
              </div>
            </div>
          </section>

          <section class="tab-panel" *ngIf="tab() === 'historia'">
            <label class="full-field">Biografia<textarea rows="10" [(ngModel)]="current.biografia"></textarea></label>

            <div class="collection-head">
              <h3>Relacionados</h3>
              <button type="button" class="button ghost" (click)="addRelacionado(current)">Adicionar</button>
            </div>
            <div class="relacionado-card-grid">
              <button
                type="button"
                class="relacionado-card"
                *ngFor="let pessoa of current.relacionados; let i = index"
                (click)="openRelacionadoEditor(pessoa, i)"
              >
                <span class="relacionado-card-image">
                  <img *ngIf="pessoa.imagem" [src]="pessoa.imagem" [alt]="pessoa.nome" />
                  <span *ngIf="!pessoa.imagem">?</span>
                </span>
                <strong>{{ pessoa.nome }}</strong>
              </button>
              <p class="empty-collection" *ngIf="!current.relacionados.length">Nenhuma relação salva.</p>
            </div>

            <button type="button" class="registro-history-trigger" (click)="registroHistoryOpen.set(true)">
              <span>
                <strong>Registros</strong>
                <small>Ver histórico</small>
              </span>
              <strong>{{ current.registros.length }}</strong>
            </button>
          </section>

          <section class="tab-panel" *ngIf="tab() === 'pokemon'">
            <div class="box-upgrade-bar">
              <span class="box-upgrade-title">Slots</span>
              <label class="upgrade-chip mini">
                <span>Mini Slot Upgrade</span>
                <input
                  type="number"
                  min="0"
                  [ngModel]="current.miniUpgrade ?? 0"
                  (ngModelChange)="updateFichaUpgrade(current, 'miniUpgrade', $event)"
                />
                <strong>+{{ miniUpgradeBonus(current) }}</strong>
              </label>
              <label class="upgrade-chip normal">
                <span>Slot Upgrade</span>
                <input
                  type="number"
                  min="0"
                  [ngModel]="current.slotUpgrade ?? 0"
                  (ngModelChange)="updateFichaUpgrade(current, 'slotUpgrade', $event)"
                />
                <strong>+{{ slotUpgradeBonus(current) }}</strong>
              </label>
              <div class="upgrade-total">
                <span>Total</span>
                <strong>{{ pokemonSlotTotal(current) }}</strong>
              </div>
            </div>

            <div class="collection-head pokemon-actions-head">
              <button
                type="button"
                class="button ghost"
                (click)="addPokemon(current)"
                [disabled]="!canAddBoxPokemon(current)"
                [title]="canAddBoxPokemon(current) ? 'Adicionar Pokémon na box' : 'Aumente os slots antes de adicionar mais Pokémon na box'"
              >
                Adicionar Pokémon
              </button>
            </div>

            <ng-container *ngIf="pokemonLists(current) as pokemonList">
            <div class="pokemon-section">
              <div class="pokemon-section-head">
                <h4>Equipe</h4>
              </div>
              <div
                class="pokemon-box-grid drop-zone"
                [class.drop-locked]="!canDropPokemon(current, 'equipe')"
                cdkDropList
                cdkDropListOrientation="mixed"
                id="pokemon-equipe-list"
                [cdkDropListData]="pokemonList.equipe"
                [cdkDropListConnectedTo]="['pokemon-box-list']"
                [cdkDropListEnterPredicate]="canEnterEquipeList"
                (cdkDropListDropped)="dropPokemonCdk($event, current, 'equipe')"
              >
                <div
                  class="pokemon-box-card"
                  *ngFor="let entry of pokemonList.equipe; trackBy: trackByPokemonEntry"
                  cdkDrag
                  [cdkDragData]="entry"
                  [class.active]="selectedPokemonIndex() === entry.index"
                  [class.dragging]="draggingPokemon() === entry.pokemon"
                  (click)="openPokemonCard(entry.pokemon, entry.index)"
                  (cdkDragStarted)="startPokemonCdkDrag(entry.pokemon)"
                  (cdkDragEnded)="endPokemonCdkDrag()"
                >
                  <span
                    *ngIf="selectedMechanic(entry.pokemon) as mechanic"
                    class="mechanic-badge box"
                    [style.--mechanic-color]="mechanic.color"
                    [title]="mechanic.label"
                  >
                    <img [src]="mechanicIcon(mechanic)" [alt]="mechanic.label" draggable="false" />
                  </span>
                  <div class="pokemon-box-main">
                    <span class="pokemon-box-sprite">
                      <img *ngIf="entry.pokemon.sprite" [src]="entry.pokemon.sprite" [alt]="entry.pokemon.apelido || entry.pokemon.especie || 'Pokémon'" draggable="false" />
                      <span *ngIf="!entry.pokemon.sprite">{{ initials(entry.pokemon.apelido || entry.pokemon.especie || 'Pokémon') }}</span>
                    </span>
                    <strong>{{ entry.pokemon.apelido || 'Pokémon ' + (entry.index + 1) }}</strong>
                  </div>
                </div>
              </div>
            </div>

            <div class="pokemon-section">
              <div class="pokemon-section-head">
                <h4>Box</h4>
              </div>
              <div
                class="pokemon-box-grid drop-zone"
                [class.drop-locked]="!canDropPokemon(current, 'box')"
                cdkDropList
                cdkDropListOrientation="mixed"
                id="pokemon-box-list"
                [cdkDropListData]="pokemonList.box"
                [cdkDropListConnectedTo]="['pokemon-equipe-list']"
                [cdkDropListEnterPredicate]="canEnterBoxList"
                (cdkDropListDropped)="dropPokemonCdk($event, current, 'box')"
              >
                <div
                  class="pokemon-box-card"
                  *ngFor="let entry of pokemonList.box; trackBy: trackByPokemonEntry"
                  cdkDrag
                  [cdkDragData]="entry"
                  [class.active]="selectedPokemonIndex() === entry.index"
                  [class.dragging]="draggingPokemon() === entry.pokemon"
                  (click)="openPokemonCard(entry.pokemon, entry.index)"
                  (cdkDragStarted)="startPokemonCdkDrag(entry.pokemon)"
                  (cdkDragEnded)="endPokemonCdkDrag()"
                >
                  <span
                    *ngIf="selectedMechanic(entry.pokemon) as mechanic"
                    class="mechanic-badge box"
                    [style.--mechanic-color]="mechanic.color"
                    [title]="mechanic.label"
                  >
                    <img [src]="mechanicIcon(mechanic)" [alt]="mechanic.label" draggable="false" />
                  </span>
                  <div class="pokemon-box-main">
                    <span class="pokemon-box-sprite">
                      <img *ngIf="entry.pokemon.sprite" [src]="entry.pokemon.sprite" [alt]="entry.pokemon.apelido || entry.pokemon.especie || 'Pokémon'" draggable="false" />
                      <span *ngIf="!entry.pokemon.sprite">{{ initials(entry.pokemon.apelido || entry.pokemon.especie || 'Pokémon') }}</span>
                    </span>
                    <strong>{{ entry.pokemon.apelido || 'Pokémon ' + (entry.index + 1) }}</strong>
                  </div>
                </div>
              </div>
            </div>
            </ng-container>

            <ng-container *ngFor="let pokemon of current.pokemons; let i = index">
            <div class="modal-backdrop pokemon-editor-backdrop" *ngIf="selectedPokemonIndex() === i" (click)="closePokemonEditor()">
            <div class="pokemon-editor-modal" (click)="$event.stopPropagation()">
              <div class="repeat-actions">
                <strong>{{ pokemon.apelido || pokemon.especie || 'Pokémon ' + (i + 1) }}</strong>
                <div class="collection-actions">
                  <button type="button" class="button ghost" (click)="removePokemon(current.pokemons, i)">Remover</button>
                  <button type="button" class="button ghost" (click)="closePokemonEditor()">Fechar</button>
                </div>
              </div>
              <div class="pokemon-sprite-row">
                <button type="button" class="pokemon-sprite-button" (click)="openSpritePicker(pokemon)">
                  <img *ngIf="pokemon.sprite" [src]="pokemon.sprite" [alt]="pokemon.apelido || pokemon.especie || 'Pokémon'" />
                  <span *ngIf="!pokemon.sprite">?</span>
                  <span
                    *ngIf="selectedMechanic(pokemon) as mechanic"
                    class="mechanic-badge sprite"
                    [style.--mechanic-color]="mechanic.color"
                    [title]="mechanic.label"
                  >
                    <img [src]="mechanicIcon(mechanic)" [alt]="mechanic.label" />
                  </span>
                </button>
              </div>
              <div class="edit-panel">
                <label class="pokeball-field">
                  Pokébola
                  <div class="pokeball-select">
                    <button type="button" class="pokeball-selected" (click)="togglePokeballPicker(i)">
                      <ng-container *ngIf="selectedPokeball(pokemon) as ball; else emptyPokeball">
                        <img [src]="ball.icon" [alt]="ball.label" />
                        <span>{{ ball.label }}</span>
                      </ng-container>
                      <ng-template #emptyPokeball>
                        <span>Escolher Pokébola</span>
                      </ng-template>
                    </button>
                    <div class="pokeball-options" *ngIf="pokeballPickerIndex() === i">
                      <button
                        type="button"
                        *ngFor="let ball of pokeballs"
                        [class.active]="pokemon.pokebola === ball.name"
                        (click)="selectPokeball(pokemon, ball)"
                      >
                        <img [src]="ball.icon" [alt]="ball.label" />
                        <span>{{ ball.label }}</span>
                      </button>
                    </div>
                  </div>
                </label>
                <label>
                  Local
                  <select [ngModel]="pokemonLocation(pokemon)" (ngModelChange)="setPokemonLocation(current, pokemon, $event, true)">
                    <option value="equipe">Equipe</option>
                    <option value="box">Box</option>
                  </select>
                </label>
                <label>Apelido<input [(ngModel)]="pokemon.apelido" /></label>
                <label *ngIf="hasCustomSprite(pokemon)">
                  Espécie
                  <input [(ngModel)]="pokemon.especie" placeholder="Nome da espécie" />
                </label>
                <label>
                  Gênero
                  <select [(ngModel)]="pokemon.genero">
                    <option *ngFor="let genero of generos" [value]="genero">{{ genero }}</option>
                  </select>
                </label>
                <label>
                  Ability
                  <select [(ngModel)]="pokemon.ability" (focus)="loadPokemonDexData(pokemon)">
                    <option *ngFor="let ability of pokemonAbilities(pokemon)" [value]="ability">{{ displayPokemonText(ability) }}</option>
                  </select>
                </label>
                <label>
                  Feature
                  <select [ngModel]="pokemon.feature" (ngModelChange)="selectPokemonFeature(pokemon, $event)">
                    <option *ngFor="let feature of features" [value]="feature">{{ feature }}</option>
                  </select>
                </label>
                <label class="mechanic-field">
                  Mecânica
                  <div class="mechanic-select">
                    <button type="button" class="mechanic-selected" (click)="toggleMechanicPicker(i)">
                      <ng-container *ngIf="selectedMechanic(pokemon) as mechanic; else emptyMechanic">
                        <img [src]="mechanicIcon(mechanic)" [alt]="mechanic.label" />
                        <span>{{ mechanic.label }}</span>
                      </ng-container>
                      <ng-template #emptyMechanic>
                        <span>Nenhuma</span>
                      </ng-template>
                    </button>
                    <div class="mechanic-options" *ngIf="mechanicPickerIndex() === i">
                      <button type="button" [class.active]="!pokemon.mecanica" (click)="selectMechanic(pokemon, '')">
                        <span class="mechanic-empty-dot"></span>
                        <span>Nenhuma</span>
                      </button>
                      <button
                        type="button"
                        *ngFor="let mechanic of pokemonMechanics"
                        [class.active]="pokemon.mecanica === mechanic.name"
                        (click)="selectMechanic(pokemon, mechanic.name)"
                      >
                        <img [src]="mechanicIcon(mechanic)" [alt]="mechanic.label" />
                        <span>{{ mechanic.label }}</span>
                      </button>
                    </div>
                  </div>
                </label>
                <label>
                  Nature
                  <select [(ngModel)]="pokemon.nature">
                    <option *ngFor="let nature of natures" [value]="nature">{{ displayPokemonText(nature) }}</option>
                  </select>
                </label>
                <label class="held-item-field">
                  Item segurado
                  <div class="held-item-select">
                    <button type="button" class="held-item-selected" (click)="toggleHeldItemPicker(i)">
                      <ng-container *ngIf="selectedHeldItem(pokemon) as item; else emptyHeldItem">
                        <img *ngIf="item.icon" [src]="item.icon" [alt]="item.label" loading="lazy" decoding="async" (error)="clearBrokenHeldItemIcon($event, item)" />
                        <span class="item-empty-dot" *ngIf="!item.icon"></span>
                        <span>{{ item.label }}</span>
                      </ng-container>
                      <ng-template #emptyHeldItem>
                        <span>Escolher item</span>
                      </ng-template>
                    </button>
                    <div class="held-item-options" *ngIf="heldItemPickerIndex() === i">
                      <input
                        placeholder="Buscar item"
                        [ngModel]="heldItemSearch()"
                        (ngModelChange)="heldItemSearch.set($event)"
                        (click)="$event.stopPropagation()"
                      />
                      <button type="button" [class.active]="!pokemon.holdItem" (click)="selectHeldItem(pokemon, undefined)">
                        <span class="item-empty-dot"></span>
                        <span>Nenhum</span>
                      </button>
                      <button
                        type="button"
                        *ngFor="let item of filteredHeldItems()"
                        [class.active]="pokemon.holdItem === item.name"
                        (click)="selectHeldItem(pokemon, item)"
                      >
                        <img *ngIf="item.icon" [src]="item.icon" [alt]="item.label" loading="lazy" decoding="async" (error)="clearBrokenHeldItemIcon($event, item)" />
                        <span class="item-empty-dot" *ngIf="!item.icon"></span>
                        <span>{{ item.label }}</span>
                      </button>
                    </div>
                  </div>
                </label>
                <label>Happiness<input type="number" [(ngModel)]="pokemon.happinessAtual" /></label>
              </div>
              <div class="stats-grid">
                <label>HP<input type="number" [(ngModel)]="pokemon.hp" /></label>
                <label>ATK<input type="number" [(ngModel)]="pokemon.atk" /></label>
                <label>DEF<input type="number" [(ngModel)]="pokemon.def" /></label>
                <label>SATK<input type="number" [(ngModel)]="pokemon.satk" /></label>
                <label>SDEF<input type="number" [(ngModel)]="pokemon.sdef" /></label>
                <label>Speed<input type="number" [(ngModel)]="pokemon.speed" /></label>
                <label>PWR<input type="number" [(ngModel)]="pokemon.pwr" /></label>
                <label>STM<input type="number" [(ngModel)]="pokemon.stm" /></label>
                <label>SKL<input type="number" [(ngModel)]="pokemon.skl" /></label>
                <label>JMP<input type="number" [(ngModel)]="pokemon.jmp" /></label>
                <label>Contest speed<input type="number" [(ngModel)]="pokemon.contestSpeed" /></label>
              </div>
              <label class="full-field">Combo<textarea rows="3" [(ngModel)]="pokemon.combo"></textarea></label>
              <label class="full-field">Sobre<textarea rows="4" [(ngModel)]="pokemon.sobre"></textarea></label>

              <div class="collection-head small">
                <h4>Movimentos</h4>
              </div>
              <div class="move-slot-row" *ngFor="let movimento of pokemon.movimentos; let moveIndex = index">
                <div class="move-name-field">
                  <ng-container *ngIf="!isCustomMove(pokemon, movimento); else customMoveField">
                  <select
                    class="move-select"
                    [style.--move-color]="moveTypeColor(movimento.tipo)"
                    [class.has-type]="!!movimento.tipo"
                    [ngModel]="moveSelectionValue(pokemon, movimento)"
                    (ngModelChange)="selectMoveOption(pokemon, movimento, $event)"
                  >
                    <option value="">Movimento {{ moveIndex + 1 }}</option>
                    <option *ngFor="let move of pokemonMoves(pokemon)" [value]="move.name">{{ displayPokemonText(move.name) }}</option>
                    <option value="__custom__">Movimento personalizado</option>
                  </select>
                  </ng-container>
                  <ng-template #customMoveField>
                    <input
                      class="move-select custom-move-name"
                      [style.--move-color]="moveTypeColor(movimento.tipo)"
                      [class.has-type]="!!movimento.tipo"
                      placeholder="Nome personalizado"
                      [(ngModel)]="movimento.nome"
                      (blur)="closeEmptyCustomMove(movimento)"
                    />
                  </ng-template>
                </div>
                <select [(ngModel)]="movimento.categoria">
                  <option value="" disabled>Categoria</option>
                  <option *ngFor="let category of moveCategories" [value]="category.name">{{ category.label }}</option>
                </select>
                <div class="move-type-field">
                  <span
                    *ngIf="selectedMoveType(movimento.tipo) as type"
                    class="move-type-icon"
                    role="img"
                    [attr.aria-label]="type.label"
                    [style.--move-type-icon]="'url(' + moveTypeIcon(type) + ')'"
                    [style.--move-type-color]="type.color"
                  ></span>
                  <select [(ngModel)]="movimento.tipo" [class.with-icon]="!!selectedMoveType(movimento.tipo)">
                    <option value="" disabled>Tipo</option>
                    <option *ngFor="let type of moveTypes" [value]="type.name">{{ type.label }}</option>
                  </select>
                </div>
                <select
                  class="move-style-select"
                  [class.has-style]="!!movimento.style"
                  [style.--contest-style-color]="contestStyleColor(movimento.style)"
                  [(ngModel)]="movimento.style"
                >
                  <option value="" disabled>Style</option>
                  <option *ngFor="let style of contestStyles" [value]="style">{{ style }}</option>
                </select>
                <input type="number" placeholder="Power" [(ngModel)]="movimento.poder" />
                <input type="number" placeholder="Accuracy" [(ngModel)]="movimento.accuracy" />
              </div>
            </div>
            </div>
            </ng-container>
          </section>

          <section class="tab-panel" *ngIf="tab() === 'inventario'">
            <div class="collection-head">
              <h3>Inventário</h3>
              <button type="button" class="button ghost" (click)="openInventoryPicker()">Adicionar item</button>
            </div>
            <div class="inventory-card-grid">
              <button type="button" class="inventory-card" *ngFor="let item of current.itens; let i = index" (click)="openInventoryEditor(i)">
                <span class="inventory-card-icon">
                  <img *ngIf="item.icone" [src]="item.icone" [alt]="item.nome || 'Item'" (error)="clearBrokenInventoryItemIcon($event, item)" />
                  <span *ngIf="!item.icone">?</span>
                  <small class="inventory-card-qty" *ngIf="(item.quantidade || 1) > 1">x{{ item.quantidade }}</small>
                </span>
                <span>
                  <strong>{{ item.nome || 'Item ' + (i + 1) }}</strong>
                  <small>{{ item.descricao || 'Sem descrição cadastrada.' }}</small>
                </span>
              </button>
            </div>
          </section>

          <section class="tab-panel" *ngIf="tab() === 'extras'">
            <label class="full-field">Anotações<textarea rows="8" [(ngModel)]="current.anotacoes"></textarea></label>

            <div class="collection-head">
              <h3>Habilidades</h3>
              <button type="button" class="button ghost" (click)="addHabilidade(current)">Adicionar</button>
            </div>
            <div class="repeat-card compact" *ngFor="let habilidade of current.habilidades; let i = index">
              <div class="repeat-actions">
                <strong>{{ habilidade.nome || 'Habilidade ' + (i + 1) }}</strong>
                <button type="button" class="button ghost" (click)="remove(current.habilidades, i)">Remover</button>
              </div>
              <div class="edit-panel">
                <label>Nome<input [(ngModel)]="habilidade.nome" /></label>
              </div>
              <label class="full-field">Descrição<textarea rows="3" [(ngModel)]="habilidade.descricao"></textarea></label>
            </div>

          </section>

          <section class="tab-panel" *ngIf="tab() === 'conquistas'">
            <button
              type="button"
              class="collection-head achievement-case-toggle"
              [attr.aria-expanded]="badgeCaseOpen()"
              aria-controls="badge-case-content"
              (click)="badgeCaseOpen.set(!badgeCaseOpen())"
            >
              <h3>Insígnias</h3>
              <span class="achievement-case-meta">
                Badge case
                <i class="achievement-case-chevron" [class.open]="badgeCaseOpen()" aria-hidden="true"></i>
              </span>
            </button>
            <div
              id="badge-case-content"
              class="achievement-case-collapse"
              [class.open]="badgeCaseOpen()"
            >
              <div class="achievement-case-collapse-inner">
                <div class="badge-case">
                  <button
                    type="button"
                    class="badge-slot"
                    *ngFor="let badge of badgeOptions; let slot = index"
                    [class.filled]="badgeConquista(current, slot)"
                    (click)="openBadgePicker(slot)"
                  >
                    <span class="badge-icon" [class.empty]="!badgeConquista(current, slot)">
                      <img
                        *ngIf="badgeIcon(current, slot, badge) as icon"
                        [class.badge-placeholder]="!badgeConquista(current, slot)"
                        [src]="icon"
                        [alt]="badgeConquista(current, slot)?.nome || badge.label"
                      />
                    </span>
                    <strong>{{ badge.label }}</strong>
                  </button>
                </div>
              </div>
            </div>

            <button
              type="button"
              class="collection-head achievement-case-toggle"
              [attr.aria-expanded]="ribbonCaseOpen()"
              aria-controls="ribbon-case-content"
              (click)="ribbonCaseOpen.set(!ribbonCaseOpen())"
            >
              <h3>Ribbons</h3>
              <span class="achievement-case-meta">
                Ribbon case
                <i class="achievement-case-chevron" [class.open]="ribbonCaseOpen()" aria-hidden="true"></i>
              </span>
            </button>
            <div
              id="ribbon-case-content"
              class="achievement-case-collapse"
              [class.open]="ribbonCaseOpen()"
            >
              <div class="achievement-case-collapse-inner">
                <div class="ribbon-case">
                  <button
                    type="button"
                    class="ribbon-slot"
                    *ngFor="let ribbon of ribbonOptions; let slot = index"
                    [class.filled]="ribbonConquista(current, slot)"
                    (click)="openRibbonPicker(slot)"
                  >
                    <span class="ribbon-icon" [class.empty]="!ribbonConquista(current, slot)">
                      <img
                        [class.ribbon-placeholder]="!ribbonConquista(current, slot)"
                        [src]="ribbonIcon(current, slot, ribbon)"
                        [alt]="ribbon.label"
                      />
                    </span>
                    <strong>{{ ribbon.label }}</strong>
                  </button>
                </div>
              </div>
            </div>

            <div class="collection-head">
              <h3>Premiações</h3>
              <button type="button" class="button ghost" (click)="addConquista(current, 'premiacao')">Adicionar</button>
            </div>
            <div class="achievement-card-grid">
              <button
                type="button"
                class="achievement-card"
                *ngFor="let conquista of current.conquistas; let i = index; trackBy: trackByConquista"
                [class.hidden-achievement]="conquista.tipo !== 'premiacao'"
                (click)="openConquistaEditor(i)"
              >
                <span class="achievement-card-image">
                  <img *ngIf="conquista.imagem" [src]="conquista.imagem" [alt]="conquista.nome || 'Premiação'" />
                  <span *ngIf="!conquista.imagem">?</span>
                </span>
                <strong>{{ conquista.nome || 'Premiação' }}</strong>
              </button>
            </div>
          </section>

        </div>
      </article>

      <div class="modal-backdrop" *ngIf="relacionadoDraft() as draft" (click)="closeRelacionadoEditor()">
        <div class="achievement-editor-modal relacionado-editor-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Relacionados</span>
              <h3>{{ draft.index === null ? 'Nova relação' : draft.pessoa.nome }}</h3>
            </div>
            <button type="button" class="button ghost" (click)="closeRelacionadoEditor()">Cancelar</button>
          </div>

          <div class="relacionado-editor-main">
            <div class="achievement-image-row">
              <button
                type="button"
                class="relacionado-image-button"
                aria-label="Subir imagem"
                title="Subir imagem"
                (click)="relacionadoImageInput.click()"
              >
                <img *ngIf="draft.pessoa.imagem" [src]="draft.pessoa.imagem" [alt]="draft.pessoa.nome || 'Relacionado'" />
                <span *ngIf="!draft.pessoa.imagem">?</span>
              </button>
              <input
                #relacionadoImageInput
                class="visually-hidden"
                type="file"
                accept="image/png,image/jpeg,image/webp,image/gif"
                (change)="selectRelacionadoImage($event)"
              />
              <button type="button" class="button ghost" *ngIf="draft.pessoa.imagem" (click)="clearRelacionadoImage()">
                Remover imagem
              </button>
            </div>

            <div class="edit-panel relacionado-editor-fields">
              <label>Nome<input maxlength="150" [(ngModel)]="draft.pessoa.nome" /></label>
              <label>Relação<input maxlength="120" [(ngModel)]="draft.pessoa.relacao" /></label>
            </div>
          </div>

          <label class="full-field">História<textarea rows="8" [(ngModel)]="draft.pessoa.historia"></textarea></label>

          <div class="modal-actions registro-editor-actions">
            <button
              type="button"
              class="button danger"
              *ngIf="draft.index !== null"
              (click)="removeRelacionadoDraft()"
            >Remover</button>
            <button type="button" class="button primary" [disabled]="!canSaveRelacionado()" (click)="saveRelacionado()">
              Salvar
            </button>
          </div>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="registroHistoryOpen() && ficha() as current" (click)="registroHistoryOpen.set(false)">
        <div class="achievement-editor-modal registro-history-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Histórico</span>
              <h3>Registros</h3>
            </div>
            <div class="modal-actions">
              <button type="button" class="button ghost" (click)="addRegistro(current)">Adicionar</button>
              <button type="button" class="button ghost" (click)="registroHistoryOpen.set(false)">Fechar</button>
            </div>
          </div>

          <div class="registro-grid">
            <button
              type="button"
              class="registro-card"
              *ngFor="let registro of current.registros; let i = index"
              (click)="openRegistroEditor(registro, i)"
            >
              <span class="registro-lines">
                <span
                  class="registro-line"
                  *ngFor="let line of registroLines(registro.descricao)"
                  [class.added]="registroLineClass(line) === 'added'"
                  [class.removed]="registroLineClass(line) === 'removed'"
                >
                  <span class="registro-event-meta">{{ registroMoment(registro) }} · {{ current.nome }}</span>
                  <span>{{ registroAction(line) }}{{ registroContent(line) }}</span>
                </span>
              </span>
            </button>
            <p class="empty-collection" *ngIf="!current.registros.length">Nenhum registro salvo.</p>
          </div>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="registroDraft() as draft" (click)="closeRegistroEditor()">
        <div class="achievement-editor-modal registro-editor-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Registros</span>
              <h3>{{ draft.index === null ? 'Novo registro' : 'Editar registro' }}</h3>
            </div>
            <button type="button" class="button ghost" (click)="closeRegistroEditor()">Cancelar</button>
          </div>

          <label class="full-field">
            Descrição
            <textarea
              rows="8"
              placeholder="+ Item&#10;- Item"
              [(ngModel)]="draft.registro.descricao"
            ></textarea>
          </label>
          <p class="registro-editor-hint">Use uma linha para cada alteração. Comece com + para adições e - para remoções.</p>

          <div class="modal-actions registro-editor-actions">
            <button
              type="button"
              class="button danger"
              *ngIf="draft.index !== null"
              (click)="removeRegistroDraft()"
            >Remover</button>
            <button type="button" class="button primary" [disabled]="!canSaveRegistro()" (click)="saveRegistro()">
              Salvar
            </button>
          </div>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="spritePickerFor() as selectedPokemon" (click)="closeSpritePicker()">
        <div class="sprite-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Dex</span>
              <h3>Escolher Pokémon</h3>
            </div>
            <button type="button" class="button ghost" (click)="closeSpritePicker()">Fechar</button>
          </div>

          <div class="sprite-modal-controls">
            <div class="custom-sprite-row">
              <button type="button" class="button ghost" (click)="customSpriteInput.click()">Usar sprite personalizado</button>
              <input
                #customSpriteInput
                class="visually-hidden"
                type="file"
                accept="image/png,image/jpeg,image/webp,image/gif"
                (change)="selectCustomSpriteImage($event, selectedPokemon)"
              />
            </div>

            <div class="custom-sprite-preview" *ngIf="selectedPokemon.sprite">
              <img [src]="selectedPokemon.sprite" [alt]="selectedPokemon.apelido || selectedPokemon.especie || 'Pokémon'" />
              <span>Sprite atual</span>
            </div>

            <input
              placeholder="Buscar por nome ou número, exemplo: pikachu ou 25"
              [ngModel]="spriteSearch()"
              (ngModelChange)="spriteSearch.set($event)"
            />
          </div>

          <div class="sprite-grid">
            <button
              type="button"
              class="sprite-option"
              *ngFor="let sprite of filteredSpriteChoices()"
              [class.active]="selectedPokemon.sprite === sprite.url"
              (click)="selectSprite(selectedPokemon, sprite)"
            >
              <img [src]="sprite.url" [alt]="'Pokémon #' + sprite.dex" loading="lazy" />
              <span>#{{ sprite.dex }}</span>
              <small *ngIf="sprite.name">{{ sprite.name }}</small>
            </button>
          </div>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="badgePickerSlot() !== null && ficha() as current" (click)="closeBadgePicker()">
        <div class="badge-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Insígnias</span>
              <h3>Escolher insígnia</h3>
            </div>
            <button type="button" class="button ghost" (click)="closeBadgePicker()">Fechar</button>
          </div>

          <div class="badge-picker-grid single-option" *ngIf="badgeOptionForOpenSlot() as badge">
            <button
              type="button"
              class="badge-picker-option"
              (click)="selectBadge(current, badge)"
            >
              <span class="badge-icon">
                <img *ngIf="badge.icon" [src]="badge.icon" [alt]="badge.label" />
              </span>
            </button>
          </div>

          <button type="button" class="button ghost" (click)="clearBadge(current)">Remover</button>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="ribbonPickerSlot() !== null && ficha() as current" (click)="closeRibbonPicker()">
        <div class="badge-modal ribbon-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Ribbons</span>
              <h3>Escolher ribbon</h3>
            </div>
            <button type="button" class="button ghost" (click)="closeRibbonPicker()">Fechar</button>
          </div>

          <div class="ribbon-picker-grid" *ngIf="ribbonOptionForOpenSlot() as ribbon">
            <button type="button" class="ribbon-picker-option" (click)="selectRibbon(current, ribbon)">
              <span class="ribbon-icon large">
                <img [src]="ribbon.icon" [alt]="ribbon.label" />
              </span>
            </button>
          </div>

          <button type="button" class="button ghost" (click)="clearRibbon(current)">Remover</button>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="newConquista() as conquista" (click)="closeNewConquista()">
        <div class="achievement-editor-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Conquistas</span>
              <h3>{{ conquista.tipo === 'ribbon' ? 'Novo ribbon' : 'Nova premiação' }}</h3>
            </div>
            <button type="button" class="button ghost" (click)="closeNewConquista()">Cancelar</button>
          </div>

          <div class="achievement-image-row">
            <button
              type="button"
              class="achievement-image-button"
              aria-label="Subir imagem"
              title="Subir imagem"
              (click)="newConquistaImageInput.click()"
            >
              <img *ngIf="conquista.imagem" [src]="conquista.imagem" [alt]="conquista.nome || 'Conquista'" />
              <span *ngIf="!conquista.imagem">?</span>
            </button>
            <input
              #newConquistaImageInput
              class="visually-hidden"
              type="file"
              accept="image/png,image/jpeg,image/webp,image/gif"
              (change)="selectConquistaImage($event, conquista)"
            />
            <button type="button" class="button ghost" *ngIf="conquista.imagem" (click)="clearConquistaImage(conquista)">Remover imagem</button>
          </div>

          <label>Nome<input [(ngModel)]="conquista.nome" /></label>

          <div class="modal-actions">
            <button type="button" class="button primary" [disabled]="!canConfirmNewConquista()" (click)="confirmNewConquista()">
              Adicionar
            </button>
          </div>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="selectedConquistaIndex() !== null && ficha() as current" (click)="closeConquistaEditor()">
        <div
          class="achievement-editor-modal"
          *ngIf="selectedConquista(current) as conquista"
          (click)="$event.stopPropagation()"
          (input)="scheduleAutoSave()"
          (change)="scheduleAutoSave()"
        >
          <div class="modal-head">
            <div>
              <span class="eyebrow">Conquistas</span>
              <h3>{{ conquista.tipo === 'ribbon' ? 'Ribbon' : 'Premiação' }}</h3>
            </div>
            <div class="modal-actions">
              <button type="button" class="button ghost" (click)="removeSelectedConquista(current)">Remover</button>
              <button type="button" class="button ghost" (click)="closeConquistaEditor()">Fechar</button>
            </div>
          </div>

          <div class="achievement-image-row">
            <button
              type="button"
              class="achievement-image-button"
              aria-label="Subir imagem"
              title="Subir imagem"
              (click)="conquistaImageInput.click()"
            >
              <img *ngIf="conquista.imagem" [src]="conquista.imagem" [alt]="conquista.nome || 'Conquista'" />
              <span *ngIf="!conquista.imagem">?</span>
            </button>
            <input
              #conquistaImageInput
              class="visually-hidden"
              type="file"
              accept="image/png,image/jpeg,image/webp,image/gif"
              (change)="selectConquistaImage($event, conquista)"
            />
            <button type="button" class="button ghost" *ngIf="conquista.imagem" (click)="clearConquistaImage(conquista)">Remover imagem</button>
          </div>

          <label>Nome<input [(ngModel)]="conquista.nome" /></label>
        </div>
      </div>

      <div class="modal-backdrop inventory-picker-backdrop" *ngIf="inventoryPickerOpen() && ficha() as current" (click)="closeInventoryPicker()">
        <div class="inventory-modal" (click)="$event.stopPropagation()">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Inventário</span>
              <h3>{{ inventoryPickerMode() === 'edit' ? 'Escolher item' : 'Adicionar item' }}</h3>
            </div>
            <button type="button" class="button ghost" (click)="closeInventoryPicker()">Fechar</button>
          </div>

          <input
            placeholder="Buscar item"
            [ngModel]="inventoryItemSearch()"
            (ngModelChange)="updateInventorySearch($event)"
          />

          <div class="inventory-modal-actions">
            <button type="button" class="button ghost" (click)="manualItemImageInput.click()">Subir imagem</button>
            <input
              #manualItemImageInput
              class="visually-hidden"
              type="file"
              accept="image/png,image/jpeg,image/webp,image/gif"
              (change)="addManualItemFromImage($event, current)"
            />
          </div>

          <div class="inventory-modal-grid" (scroll)="onInventoryModalScroll($event)">
            <button
              type="button"
              class="inventory-modal-option"
              *ngFor="let option of filteredInventoryItems(); trackBy: trackByInventoryItem"
              (click)="addInventoryItem(current, option)"
            >
              <img *ngIf="option.icon" [src]="option.icon" [alt]="option.label" loading="lazy" decoding="async" (error)="clearBrokenInventoryOptionIcon($event, option)" />
              <span class="item-empty-dot" *ngIf="!option.icon"></span>
              <strong>{{ option.label }}</strong>
              <small>{{ option.category }}</small>
            </button>
          </div>
        </div>
      </div>

      <ng-container *ngIf="ficha() as current">
        <div class="modal-backdrop" *ngIf="selectedInventoryItemIndex() !== null && selectedInventoryItemEntry(current) as item" (click)="closeInventoryEditor()">
          <div class="inventory-editor-modal" (click)="$event.stopPropagation()" (input)="scheduleAutoSave()" (change)="scheduleAutoSave()">
            <div class="repeat-actions">
              <strong>{{ item.nome || 'Item' }}</strong>
              <div class="collection-actions">
                <button type="button" class="button ghost" (click)="removeInventoryItem(current)">Remover</button>
                <button type="button" class="button ghost" (click)="closeInventoryEditor()">Fechar</button>
              </div>
            </div>

            <div class="inventory-image-row">
              <button type="button" class="inventory-image-button" (click)="inventoryEditorImageInput.click()">
                <img *ngIf="item.icone" [src]="item.icone" [alt]="item.nome || 'Item'" (error)="clearBrokenInventoryItemIcon($event, item)" />
                <span *ngIf="!item.icone">?</span>
              </button>
              <input
                #inventoryEditorImageInput
                class="visually-hidden"
                type="file"
                accept="image/png,image/jpeg,image/webp,image/gif"
                (change)="selectInventoryItemImage($event, item)"
              />
              <button type="button" class="button ghost" *ngIf="item.icone" (click)="clearInventoryItemImage(item)">Remover imagem</button>
            </div>

            <div class="edit-panel">
              <label class="inventory-editor-picker">
                Item
                <button type="button" class="inventory-editor-picker-button" (click)="openInventoryPickerForItem()">
                  {{ item.nome || 'Escolher item' }}
                </button>
              </label>
              <label>Nome personalizado<input [(ngModel)]="item.nome" /></label>
              <label>Categoria<input [(ngModel)]="item.categoria" /></label>
              <label>Quantidade<input type="number" [(ngModel)]="item.quantidade" /></label>
            </div>
            <label class="full-field">Descrição<textarea rows="5" [(ngModel)]="item.descricao"></textarea></label>
          </div>
        </div>
      </ng-container>
    </section>
  `,
})
export class FichaPageComponent implements OnInit {
  private readonly api = inject(FichaApiService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private autoSaveTimer?: ReturnType<typeof setTimeout>;
  private autoSaveInFlight = false;
  private autoSavePending = false;
  private pokemonListCache?: { ficha: Ficha; signature: string; lists: PokemonListView };
  private readonly pokemonIdentity = new WeakMap<FichaPokemon, number>();
  private readonly customMoveEditors = new WeakSet<FichaPokemonMovimento>();
  private nextPokemonIdentity = 1;
  private suppressPokemonClick = false;

  protected readonly ficha = signal<Ficha | null>(null);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');
  protected readonly tab = signal<FichaTab>('dados');
  protected readonly selectedPokemonIndex = signal<number | null>(null);
  protected readonly pokeballPickerIndex = signal<number | null>(null);
  protected readonly mechanicPickerIndex = signal<number | null>(null);
  protected readonly heldItemPickerIndex = signal<number | null>(null);
  protected readonly inventoryPickerOpen = signal(false);
  protected readonly inventoryPickerMode = signal<'add' | 'edit'>('add');
  protected readonly selectedInventoryItemIndex = signal<number | null>(null);
  protected readonly selectedConquistaIndex = signal<number | null>(null);
  protected readonly newConquista = signal<FichaConquista | null>(null);
  protected readonly registroDraft = signal<{ registro: FichaRegistro; index: number | null } | null>(null);
  protected readonly registroHistoryOpen = signal(false);
  protected readonly relacionadoDraft = signal<{ pessoa: FichaRelacionado; index: number | null } | null>(null);
  protected readonly badgePickerSlot = signal<number | null>(null);
  protected readonly ribbonPickerSlot = signal<number | null>(null);
  protected readonly badgeCaseOpen = signal(false);
  protected readonly ribbonCaseOpen = signal(false);
  protected readonly draggingPokemon = signal<FichaPokemon | null>(null);
  protected readonly defaultTheme = '#586a9b';
  protected readonly classes = ['Coordenador', 'Treinador', 'Criador', 'Delinquente'];
  protected readonly equipes = ['Bright', 'Reborn', 'Power'];
  protected readonly generos = ['Masculino', 'Feminino', 'Intersexo'];
  protected readonly contestStyles = ['Cool', 'Beauty', 'Cute', 'Smart', 'Tough'];
  protected readonly features = ['Normal', 'Shiny', 'Modified'];
  protected readonly moveCategories = [
    { name: 'physical', label: 'Physical' },
    { name: 'special', label: 'Special' },
    { name: 'status', label: 'Status' },
  ];
  protected readonly moveTypes: MoveTypeOption[] = [
    { name: 'normal', label: 'Normal', color: '#9fa19f', icon: 'normal.png' },
    { name: 'fire', label: 'Fire', color: '#e62829', icon: 'fire.png' },
    { name: 'water', label: 'Water', color: '#2980ef', icon: 'water.png' },
    { name: 'electric', label: 'Electric', color: '#fac000', icon: 'electric.png' },
    { name: 'grass', label: 'Grass', color: '#3fa129', icon: 'grass.png' },
    { name: 'ice', label: 'Ice', color: '#3dcef3', icon: 'ice.png' },
    { name: 'fighting', label: 'Fighting', color: '#ff8000', icon: 'fighting.png' },
    { name: 'poison', label: 'Poison', color: '#9141cb', icon: 'poison.png' },
    { name: 'ground', label: 'Ground', color: '#915121', icon: 'ground.png' },
    { name: 'flying', label: 'Flying', color: '#81b9ef', icon: 'flying.png' },
    { name: 'psychic', label: 'Psychic', color: '#ef4179', icon: 'psychic.png' },
    { name: 'bug', label: 'Bug', color: '#91a119', icon: 'bug.png' },
    { name: 'rock', label: 'Rock', color: '#afa981', icon: 'rock.png' },
    { name: 'ghost', label: 'Ghost', color: '#704170', icon: 'ghost.png' },
    { name: 'dragon', label: 'Dragon', color: '#5060e1', icon: 'dragon.png' },
    { name: 'dark', label: 'Dark', color: '#624d4e', icon: 'dark.png' },
    { name: 'steel', label: 'Steel', color: '#60a1b8', icon: 'steel.png' },
    { name: 'fairy', label: 'Fairy', color: '#ef70ef', icon: 'fairy.png' },
    { name: 'light', label: 'Light', color: '#e7cf67', icon: 'light.png' },
    { name: 'scent', label: 'Scent', color: '#43b78f', icon: 'scent.png' },
  ];
  protected readonly badgeOptions: BadgeOption[] = [
    { id: 'insignia-1', label: 'Dyna Badge', icon: '/assets/badges/dyna-badge.png' },
    { id: 'insignia-2', label: 'Clay Wing Badge', icon: '/assets/badges/clay-wing-badge.png' },
    { id: 'insignia-3', label: 'Big Wave Badge', icon: '/assets/badges/big-wave-badge.png' },
    { id: 'insignia-4', label: 'Deep Jungle Badge', icon: '/assets/badges/deep-jungle-badge.png' },
    { id: 'insignia-5', label: 'Flame Valor Badge', icon: '/assets/badges/flame-valor-badge.png' },
    { id: 'insignia-6', label: 'Sweet Everest Badge', icon: '/assets/badges/sweet-everest-badge.png' },
    { id: 'insignia-7', label: 'Dark Aura Badge', icon: '/assets/badges/dark-aura-badge.png' },
    { id: 'insignia-8', label: 'Seven Lights Badge', icon: '/assets/badges/seven-lights-badge.png' },
  ];
  protected readonly ribbonOptions: BadgeOption[] = [
    { id: 'ribbon-cool', label: 'Cool Ribbon', icon: '/assets/ribbons/cool-ribbon.png' },
    { id: 'ribbon-beauty', label: 'Beauty Ribbon', icon: '/assets/ribbons/beauty-ribbon.png' },
    { id: 'ribbon-cute', label: 'Cute Ribbon', icon: '/assets/ribbons/cute-ribbon.png' },
    { id: 'ribbon-smart', label: 'Smart Ribbon', icon: '/assets/ribbons/smart-ribbon.png' },
    { id: 'ribbon-tough', label: 'Tough Ribbon', icon: '/assets/ribbons/tough-ribbon.png' },
  ];
  protected readonly pokemonMechanics: PokemonMechanicOption[] = [
    { name: 'mega', label: 'Mega Evolução', icon: 'mega.png', color: '#42d0c6' },
    { name: 'z-move', label: 'Z-Crystal', icon: 'z-crystal.png', color: '#e3ce59' },
    { name: 'dynamax', label: 'Dynamax', icon: 'dynamax.png', color: '#e63a8b' },
    { name: 'gigantamax', label: 'Gigantamax', icon: 'gigantamax.png', color: '#b92f5b' },
    { name: 'tera-stellar', label: 'Tera Stellar', icon: 'tera-stellar.png', color: '#7bdcff' },
    { name: 'tera-light', label: 'Tera Light', icon: 'tera-light.png', color: '#dfff35' },
    { name: 'tera-dark', label: 'Tera Dark', icon: 'tera-dark.png', color: '#624d4e' },
    { name: 'tera-water', label: 'Tera Water', icon: 'tera-water.png', color: '#2980ef' },
    { name: 'tera-electric', label: 'Tera Electric', icon: 'tera-electric.png', color: '#fac000' },
    { name: 'tera-normal', label: 'Tera Normal', icon: 'tera-normal.png', color: '#9fa19f' },
    { name: 'tera-bug', label: 'Tera Bug', icon: 'tera-bug.png', color: '#91a119' },
    { name: 'tera-fighting', label: 'Tera Fighting', icon: 'tera-fighting.png', color: '#ff8000' },
    { name: 'tera-fire', label: 'Tera Fire', icon: 'tera-fire.png', color: '#e62829' },
    { name: 'tera-flying', label: 'Tera Flying', icon: 'tera-flying.png', color: '#81b9ef' },
    { name: 'tera-grass', label: 'Tera Grass', icon: 'tera-grass.png', color: '#3fa129' },
    { name: 'tera-ground', label: 'Tera Ground', icon: 'tera-ground.png', color: '#915121' },
    { name: 'tera-ice', label: 'Tera Ice', icon: 'tera-ice.png', color: '#3dcef3' },
    { name: 'tera-poison', label: 'Tera Poison', icon: 'tera-poison.png', color: '#9141cb' },
    { name: 'tera-psychic', label: 'Tera Psychic', icon: 'tera-psychic.png', color: '#ef4179' },
    { name: 'tera-fairy', label: 'Tera Fairy', icon: 'tera-fairy.png', color: '#ef70ef' },
    { name: 'tera-steel', label: 'Tera Steel', icon: 'tera-steel.png', color: '#60a1b8' },
    { name: 'tera-rock', label: 'Tera Rock', icon: 'tera-rock.png', color: '#afa981' },
    { name: 'tera-dragon', label: 'Tera Dragon', icon: 'tera-dragon.png', color: '#5060e1' },
    { name: 'tera-ghost', label: 'Tera Ghost', icon: 'tera-ghost.png', color: '#704170' },
    { name: 'tera-scent', label: 'Tera Scent', icon: 'tera-scent.png', color: '#66c7a0' },
  ];
  protected readonly pokeballs: PokeballOption[] = [
    { name: 'poke-ball', label: 'Poké Ball', icon: this.itemIcon(ITEMDEX_ICONS['poke-ball']) },
    { name: 'great-ball', label: 'Great Ball', icon: this.itemIcon(ITEMDEX_ICONS['great-ball']) },
    { name: 'ultra-ball', label: 'Ultra Ball', icon: this.itemIcon(ITEMDEX_ICONS['ultra-ball']) },
    { name: 'master-ball', label: 'Master Ball', icon: this.itemIcon(ITEMDEX_ICONS['master-ball']) },
    { name: 'premier-ball', label: 'Premier Ball', icon: this.itemIcon(ITEMDEX_ICONS['premier-ball']) },
    { name: 'luxury-ball', label: 'Luxury Ball', icon: this.itemIcon(ITEMDEX_ICONS['luxury-ball']) },
    { name: 'heal-ball', label: 'Heal Ball', icon: this.itemIcon(ITEMDEX_ICONS['heal-ball']) },
    { name: 'quick-ball', label: 'Quick Ball', icon: this.itemIcon(ITEMDEX_ICONS['quick-ball']) },
    { name: 'dusk-ball', label: 'Dusk Ball', icon: this.itemIcon(ITEMDEX_ICONS['dusk-ball']) },
    { name: 'dive-ball', label: 'Dive Ball', icon: this.itemIcon(ITEMDEX_ICONS['dive-ball']) },
    { name: 'net-ball', label: 'Net Ball', icon: this.itemIcon(ITEMDEX_ICONS['net-ball']) },
    { name: 'nest-ball', label: 'Nest Ball', icon: this.pokeApiItemIcon('nest-ball') },
    { name: 'repeat-ball', label: 'Repeat Ball', icon: this.itemIcon(ITEMDEX_ICONS['repeat-ball']) },
    { name: 'timer-ball', label: 'Timer Ball', icon: this.itemIcon(ITEMDEX_ICONS['timer-ball']) },
    { name: 'level-ball', label: 'Level Ball', icon: this.itemIcon(ITEMDEX_ICONS['level-ball']) },
    { name: 'lure-ball', label: 'Lure Ball', icon: this.itemIcon(ITEMDEX_ICONS['lure-ball']) },
    { name: 'friend-ball', label: 'Friend Ball', icon: this.itemIcon(ITEMDEX_ICONS['friend-ball']) },
    { name: 'love-ball', label: 'Love Ball', icon: this.itemIcon(ITEMDEX_ICONS['love-ball']) },
    { name: 'safari-ball', label: 'Safari Ball', icon: this.itemIcon(ITEMDEX_ICONS['safari-ball']) },
    { name: 'cherish-ball', label: 'Cherish Ball', icon: this.itemIcon(ITEMDEX_ICONS['cherish-ball']) },
    { name: 'beast-ball', label: 'Beast Ball', icon: this.itemIcon(ITEMDEX_ICONS['beast-ball']) },
    { name: 'fast-ball', label: 'Fast Ball', icon: this.itemIcon(ITEMDEX_ICONS['fast-ball']) },
    { name: 'heavy-ball', label: 'Heavy Ball', icon: this.itemIcon(ITEMDEX_ICONS['heavy-ball']) },
    { name: 'moon-ball', label: 'Moon Ball', icon: this.itemIcon(ITEMDEX_ICONS['moon-ball']) },
    { name: 'strange-ball', label: 'Strange Ball', icon: this.itemIcon(ITEMDEX_ICONS['strange-ball']) },
  ];
  protected readonly natures = [
    'hardy', 'lonely', 'brave', 'adamant', 'naughty',
    'bold', 'docile', 'relaxed', 'impish', 'lax',
    'timid', 'hasty', 'serious', 'jolly', 'naive',
    'modest', 'mild', 'quiet', 'bashful', 'rash',
    'calm', 'gentle', 'sassy', 'careful', 'quirky',
  ];
  protected readonly spriteSearch = signal('');
  protected readonly pokemonNames = signal<Record<number, string>>({});
  protected readonly pokemonDexCount = signal(1025);
  protected readonly pokemonDexData = signal<Record<string, PokemonDexDetails>>({});
  protected readonly pokemonLoading = signal<Record<string, boolean>>({});
  protected readonly spritePickerFor = signal<FichaPokemon | null>(null);
  protected readonly heldItems = signal<HeldItemOption[]>([]);
  protected readonly heldItemSearch = signal('');
  protected readonly inventoryItems = signal<InventoryItemOption[]>([]);
  protected readonly inventoryItemSearch = signal('');
  private readonly inventoryPageSize = 120;
  protected readonly inventoryVisibleLimit = signal(this.inventoryPageSize);

  protected readonly pokemonSpriteChoices = computed<PokemonSpriteChoice[]>(() => {
    const names = this.pokemonNames();
    return Array.from({ length: this.pokemonDexCount() }, (_, index) => {
      const dex = index + 1;
      return { dex, name: names[dex], url: this.pokemonSpriteUrl(dex) };
    });
  });
  protected readonly filteredSpriteChoices = computed(() => {
    const term = this.normalizeSearch(this.spriteSearch());
    const choices = this.pokemonSpriteChoices();
    const filtered = term
      ? choices.filter((sprite) => {
          const dex = String(sprite.dex);
          const name = this.normalizeSearch(sprite.name ?? '');
          return dex.includes(term.replace('#', '')) || name.includes(term);
        })
      : choices;

    return filtered;
  });

  protected readonly filteredHeldItems = computed(() => {
    const term = this.normalizeSearch(this.heldItemSearch());
    const items = this.heldItems();
    return term
      ? items.filter((item) => this.normalizeSearch(item.label).includes(term) || this.normalizeSearch(item.name).includes(term))
      : items;
  });

  protected readonly filteredInventoryMatches = computed(() => {
    const term = this.normalizeSearch(this.inventoryItemSearch());
    const items = this.inventoryItems();
    return term
      ? items.filter((item) =>
          this.normalizeSearch(item.label).includes(term)
          || this.normalizeSearch(item.name).includes(term)
          || this.normalizeSearch(item.category).includes(term)
        )
      : items;
  });

  protected readonly filteredInventoryItems = computed(() => {
    return this.filteredInventoryMatches().slice(0, this.inventoryVisibleLimit());
  });

  protected readonly inventoryHasMore = computed(() => {
    return this.filteredInventoryMatches().length > this.filteredInventoryItems().length;
  });

  ngOnInit(): void {
    this.loadPokemonNames();
    this.loadHeldItems();
    this.loadInventoryItems();

    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.get(id).subscribe({
      next: (ficha) => {
        const normalized = this.normalizeFicha(ficha);
        this.ficha.set(normalized);
        normalized.pokemons.forEach((pokemon) => this.loadPokemonDexData(pokemon));
        this.replaceAnimatedPokemonSprites(normalized);
        this.hydrateMissingMoveStyles(normalized);
        this.loading.set(false);
      },
      error: () => {
        this.router.navigate(['/ficha', id, 'visualizar']);
        this.loading.set(false);
      },
    });
  }

  @HostListener('document:click', ['$event'])
  protected closePickersOnOutsideClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;
    if (target?.closest('.pokeball-select, .mechanic-select, .held-item-select')) {
      return;
    }

    this.closeInlinePickers();
  }

  private closeInlinePickers(): void {
    this.pokeballPickerIndex.set(null);
    this.mechanicPickerIndex.set(null);
    this.heldItemPickerIndex.set(null);
  }

  protected save(): void {
    const current = this.ficha();
    if (!current) {
      return;
    }

    this.saving.set(true);
    this.success.set('');
    this.error.set('');

    this.api.update(current.id, fichaToPayload(current)).subscribe({
      next: (updated) => {
        const selectedIndex = this.selectedPokemonIndex();
        const normalized = this.normalizeFicha(updated);
        normalized.miniUpgrade = current.miniUpgrade;
        normalized.slotUpgrade = current.slotUpgrade;
        normalized.corTema = current.corTema;
        this.ficha.set(normalized);
        this.selectedPokemonIndex.set(
          selectedIndex === null || selectedIndex >= normalized.pokemons.length ? null : selectedIndex
        );
        normalized.pokemons.forEach((pokemon) => this.loadPokemonDexData(pokemon));
        this.hydrateMissingMoveStyles(normalized);
        this.success.set('');
        this.saving.set(false);
      },
      error: () => {
        this.error.set('Não foi possível salvar a ficha.');
        this.saving.set(false);
      },
    });
  }

  protected addPokemon(ficha: Ficha): void {
    if (!this.canAddBoxPokemon(ficha)) {
      this.error.set('Não há slots livres na box.');
      return;
    }

    ficha.pokemons.push({
      apelido: '',
      box: 'box',
      especie: '',
      sprite: '',
      feature: 'Normal',
      movimentos: this.createMoveSlots(),
      ordem: ficha.pokemons.length,
    });
    this.selectedPokemonIndex.set(ficha.pokemons.length - 1);
    this.scheduleAutoSave();
  }

  protected addMovimento(pokemon: FichaPokemon): void {
    pokemon.movimentos ??= [];
    pokemon.movimentos.push({ nome: '', categoria: '', ordem: pokemon.movimentos.length });
    this.scheduleAutoSave();
  }

  protected addItem(ficha: Ficha): void {
    ficha.itens.push({ categoria: 'Item', nome: `Item ${ficha.itens.length + 1}`, quantidade: 1, ordem: ficha.itens.length });
    this.scheduleAutoSave();
  }

  protected openInventoryPicker(): void {
    this.inventoryItemSearch.set('');
    this.inventoryVisibleLimit.set(this.inventoryPageSize);
    this.inventoryPickerMode.set('add');
    this.inventoryPickerOpen.set(true);
  }

  protected openInventoryPickerForItem(): void {
    this.inventoryItemSearch.set('');
    this.inventoryVisibleLimit.set(this.inventoryPageSize);
    this.inventoryPickerMode.set('edit');
    this.inventoryPickerOpen.set(true);
  }

  protected closeInventoryPicker(): void {
    this.inventoryPickerOpen.set(false);
  }

  protected openInventoryEditor(index: number): void {
    this.selectedInventoryItemIndex.set(index);
  }

  protected updateInventorySearch(value: string): void {
    this.inventoryItemSearch.set(value);
    this.inventoryVisibleLimit.set(this.inventoryPageSize);
  }

  protected onInventoryModalScroll(event: Event): void {
    if (!this.inventoryHasMore()) {
      return;
    }

    const target = event.target as HTMLElement;
    const remaining = target.scrollHeight - target.scrollTop - target.clientHeight;
    if (remaining <= 220) {
      this.showMoreInventoryItems();
    }
  }

  protected showMoreInventoryItems(): void {
    this.inventoryVisibleLimit.update((limit) => limit + this.inventoryPageSize);
  }

  protected trackByInventoryItem(_: number, item: InventoryItemOption): string {
    return item.name;
  }

  protected closeInventoryEditor(): void {
    this.selectedInventoryItemIndex.set(null);
    this.scheduleAutoSave();
  }

  protected selectedInventoryItemEntry(ficha: Ficha): FichaItem | null {
    const index = this.selectedInventoryItemIndex();
    return index === null ? null : ficha.itens[index] ?? null;
  }

  protected removeInventoryItem(ficha: Ficha): void {
    const index = this.selectedInventoryItemIndex();
    if (index === null) {
      return;
    }

    ficha.itens.splice(index, 1);
    this.closeInventoryEditor();
    this.scheduleAutoSave();
  }

  protected addManualItem(ficha: Ficha): void {
    this.addItem(ficha);
    this.closeInventoryPicker();
  }

  protected addManualItemFromImage(event: Event, ficha: Ficha): void {
    const editingItem = this.inventoryPickerMode() === 'edit'
      ? this.selectedInventoryItemEntry(ficha)
      : null;

    if (editingItem) {
      this.closeInventoryPicker();
      this.selectInventoryItemImage(event, editingItem);
      return;
    }

    const item: FichaItem = {
      categoria: 'Item personalizado',
      nome: `Item ${ficha.itens.length + 1}`,
      quantidade: 1,
      descricao: '',
      ordem: ficha.itens.length,
    };
    ficha.itens.push(item);
    this.selectedInventoryItemIndex.set(ficha.itens.length - 1);
    this.closeInventoryPicker();
    this.selectInventoryItemImage(event, item);
  }

  protected addInventoryItem(ficha: Ficha, option: InventoryItemOption): void {
    if (this.inventoryPickerMode() === 'edit') {
      const currentItem = this.selectedInventoryItemEntry(ficha);
      if (currentItem) {
        this.applyInventoryOption(currentItem, option);
        this.closeInventoryPicker();
        this.scheduleAutoSave();
        return;
      }
    }

    const item: FichaItem = {
      categoria: 'Item',
      codigo: '',
      icone: '',
      nome: 'Item',
      quantidade: 1,
      descricao: '',
      ordem: ficha.itens.length,
    };
    this.applyInventoryOption(item, option);
    ficha.itens.push(item);
    this.closeInventoryPicker();
    this.selectedInventoryItemIndex.set(ficha.itens.length - 1);
    this.scheduleAutoSave();
  }

  private applyInventoryOption(item: FichaItem, option: InventoryItemOption): void {
    item.codigo = option.name;
    item.nome = option.label;
    item.categoria = option.category || 'Item';
    item.icone = option.icon ?? '';
    item.descricao = option.description ?? 'Descrição em português não disponível.';
    this.loadInventoryItemDescription(item, option);
  }

  protected addRegistro(ficha: Ficha): void {
    this.registroHistoryOpen.set(false);
    this.registroDraft.set({
      index: null,
      registro: {
        tipoMovimento: 'Registro',
        descricao: '',
        dataRegistro: new Date().toISOString().slice(0, 10),
        ordem: ficha.registros.length,
      },
    });
  }

  protected openRegistroEditor(registro: FichaRegistro, index: number): void {
    this.registroHistoryOpen.set(false);
    this.registroDraft.set({ registro: { ...registro }, index });
  }

  protected closeRegistroEditor(): void {
    this.registroDraft.set(null);
    this.registroHistoryOpen.set(true);
  }

  protected canSaveRegistro(): boolean {
    const registro = this.registroDraft()?.registro;
    return Boolean(registro?.descricao.trim());
  }

  protected saveRegistro(): void {
    const ficha = this.ficha();
    const draft = this.registroDraft();
    if (!ficha || !draft || !this.canSaveRegistro()) {
      return;
    }

    const now = new Date();
    const user = this.auth.currentUser();
    const registro: FichaRegistro = {
      ...draft.registro,
      tipoMovimento: 'Registro',
      descricao: this.normalizeRegistroDescription(draft.registro.descricao),
      dataRegistro: this.localDateValue(now),
      registradoEm: now.toISOString(),
      registradoPor: ficha.nome.trim() || user?.nome?.trim() || user?.username || 'Personagem',
      ordem: draft.index === null ? ficha.registros.length : (draft.registro.ordem ?? draft.index),
    };

    if (draft.index === null) {
      ficha.registros.push(registro);
    } else {
      ficha.registros[draft.index] = registro;
    }

    this.closeRegistroEditor();
    this.scheduleAutoSave();
  }

  protected removeRegistroDraft(): void {
    const ficha = this.ficha();
    const index = this.registroDraft()?.index;
    if (!ficha || index === null || index === undefined) {
      return;
    }

    ficha.registros.splice(index, 1);
    ficha.registros.forEach((registro, ordem) => registro.ordem = ordem);
    this.closeRegistroEditor();
    this.scheduleAutoSave();
  }

  protected registroLines(descricao: string): string[] {
    const lines = descricao
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter(Boolean);
    return lines.length ? lines : ['Sem alterações descritas.'];
  }

  protected registroLineClass(line: string): 'added' | 'removed' | '' {
    if (line.startsWith('+')) {
      return 'added';
    }
    if (line.startsWith('-')) {
      return 'removed';
    }
    return '';
  }

  protected registroMoment(registro: FichaRegistro): string {
    if (registro.registradoEm) {
      const date = new Date(registro.registradoEm);
      return `${date.toLocaleDateString('pt-BR')} · ${date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}`;
    }
    if (registro.dataRegistro) {
      const date = new Date(`${registro.dataRegistro}T00:00:00`);
      return date.toLocaleDateString('pt-BR');
    }
    return '-';
  }

  protected registroAction(line: string): string {
    if (line.startsWith('+')) {
      return ' adicionou ';
    }
    if (line.startsWith('-')) {
      return ' removeu ';
    }
    return ' ';
  }

  protected registroContent(line: string): string {
    return /^[+-]/.test(line) ? line.slice(1).trim() : line;
  }

  private localDateValue(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private normalizeRegistroDescription(descricao: string): string {
    return descricao
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter(Boolean)
      .join('\n');
  }

  protected addHabilidade(ficha: Ficha): void {
    ficha.habilidades.push({ nome: '', descricao: '', ordem: ficha.habilidades.length });
    this.scheduleAutoSave();
  }

  protected addRelacionado(ficha: Ficha): void {
    this.relacionadoDraft.set({
      index: null,
      pessoa: {
        nome: '',
        relacao: '',
        imagem: '',
        historia: '',
        ordem: ficha.relacionados.length,
      },
    });
  }

  protected openRelacionadoEditor(pessoa: FichaRelacionado, index: number): void {
    this.relacionadoDraft.set({ pessoa: { ...pessoa }, index });
  }

  protected closeRelacionadoEditor(): void {
    this.relacionadoDraft.set(null);
  }

  protected canSaveRelacionado(): boolean {
    return Boolean(this.relacionadoDraft()?.pessoa.nome.trim());
  }

  protected saveRelacionado(): void {
    const ficha = this.ficha();
    const draft = this.relacionadoDraft();
    if (!ficha || !draft || !this.canSaveRelacionado()) {
      return;
    }

    const pessoa: FichaRelacionado = {
      ...draft.pessoa,
      nome: draft.pessoa.nome.trim(),
      relacao: draft.pessoa.relacao?.trim() ?? '',
      historia: draft.pessoa.historia?.trim() ?? '',
      imagem: draft.pessoa.imagem ?? '',
      ordem: draft.index === null ? ficha.relacionados.length : (draft.pessoa.ordem ?? draft.index),
    };

    if (draft.index === null) {
      ficha.relacionados.push(pessoa);
    } else {
      ficha.relacionados[draft.index] = pessoa;
    }

    this.closeRelacionadoEditor();
    this.scheduleAutoSave();
  }

  protected removeRelacionadoDraft(): void {
    const ficha = this.ficha();
    const index = this.relacionadoDraft()?.index;
    if (!ficha || index === null || index === undefined) {
      return;
    }

    ficha.relacionados.splice(index, 1);
    ficha.relacionados.forEach((pessoa, ordem) => pessoa.ordem = ordem);
    this.closeRelacionadoEditor();
    this.scheduleAutoSave();
  }

  protected clearRelacionadoImage(): void {
    const draft = this.relacionadoDraft();
    if (!draft) {
      return;
    }
    this.relacionadoDraft.set({ ...draft, pessoa: { ...draft.pessoa, imagem: '' } });
  }

  protected selectRelacionadoImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    const draft = this.relacionadoDraft();
    if (!file || !draft) {
      return;
    }

    const image = new Image();
    const objectUrl = URL.createObjectURL(file);
    image.onload = () => {
      const maxSize = 384;
      const ratio = Math.min(1, maxSize / Math.max(image.width, image.height));
      const width = Math.max(1, Math.round(image.width * ratio));
      const height = Math.max(1, Math.round(image.height * ratio));
      const canvas = document.createElement('canvas');
      const context = canvas.getContext('2d');
      canvas.width = width;
      canvas.height = height;
      context?.drawImage(image, 0, 0, width, height);

      const currentDraft = this.relacionadoDraft();
      if (currentDraft) {
        this.relacionadoDraft.set({
          ...currentDraft,
          pessoa: { ...currentDraft.pessoa, imagem: canvas.toDataURL('image/webp', 0.86) },
        });
      }
      input.value = '';
      URL.revokeObjectURL(objectUrl);
    };
    image.onerror = () => {
      this.error.set('Não foi possível carregar esta imagem.');
      input.value = '';
      URL.revokeObjectURL(objectUrl);
    };
    image.src = objectUrl;
  }

  protected addConquista(ficha: Ficha, tipo: 'premiacao' | 'ribbon'): void {
    this.newConquista.set({ tipo, nome: '', imagem: '', ordem: ficha.conquistas.length });
  }

  protected canConfirmNewConquista(): boolean {
    return Boolean(this.newConquista()?.nome?.trim());
  }

  protected confirmNewConquista(): void {
    const ficha = this.ficha();
    const conquista = this.newConquista();
    const nome = conquista?.nome?.trim();
    if (!ficha || !conquista || !nome) {
      return;
    }

    ficha.conquistas.push({
      ...conquista,
      nome,
      ordem: ficha.conquistas.length,
    });
    this.closeNewConquista();
    this.scheduleAutoSave();
  }

  protected closeNewConquista(): void {
    this.newConquista.set(null);
  }

  protected openConquistaEditor(index: number): void {
    this.selectedConquistaIndex.set(index);
  }

  protected closeConquistaEditor(): void {
    this.selectedConquistaIndex.set(null);
  }

  protected selectedConquista(ficha: Ficha): FichaConquista | undefined {
    const index = this.selectedConquistaIndex();
    return index === null ? undefined : ficha.conquistas[index];
  }

  protected removeSelectedConquista(ficha: Ficha): void {
    const index = this.selectedConquistaIndex();
    if (index === null) {
      return;
    }

    ficha.conquistas.splice(index, 1);
    this.closeConquistaEditor();
    this.scheduleAutoSave();
  }

  protected clearConquistaImage(conquista: FichaConquista): void {
    conquista.imagem = '';
    if (this.newConquista() === conquista) {
      this.newConquista.set({ ...conquista });
      return;
    }
    this.scheduleAutoSave();
  }

  protected selectConquistaImage(event: Event, conquista: FichaConquista): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    const image = new Image();
    const objectUrl = URL.createObjectURL(file);
    image.onload = () => {
      const maxSize = 256;
      const ratio = Math.min(1, maxSize / Math.max(image.width, image.height));
      const width = Math.max(1, Math.round(image.width * ratio));
      const height = Math.max(1, Math.round(image.height * ratio));
      const canvas = document.createElement('canvas');
      const context = canvas.getContext('2d');

      canvas.width = width;
      canvas.height = height;
      context?.clearRect(0, 0, width, height);
      context?.drawImage(image, 0, 0, width, height);
      conquista.imagem = canvas.toDataURL('image/webp', 0.86);
      input.value = '';
      URL.revokeObjectURL(objectUrl);
      if (this.newConquista() === conquista) {
        this.newConquista.set({ ...conquista });
        return;
      }
      this.scheduleAutoSave();
    };

    image.onerror = () => {
      this.error.set('Não foi possível carregar esta imagem.');
      input.value = '';
      URL.revokeObjectURL(objectUrl);
    };

    image.src = objectUrl;
  }

  protected trackByConquista(index: number, conquista: FichaConquista): string | number {
    return conquista.id ?? conquista.ordem ?? `${conquista.tipo}-${index}`;
  }

  protected badgeConquista(ficha: Ficha, slot: number): FichaConquista | undefined {
    return ficha.conquistas.find((conquista) => conquista.tipo === this.badgeType(slot));
  }

  protected selectedBadgeOption(ficha: Ficha, slot: number): BadgeOption | undefined {
    return this.badgeConquista(ficha, slot) ? this.badgeOptions[slot] : undefined;
  }

  protected badgeIcon(ficha: Ficha, slot: number, fallback: BadgeOption): string | undefined {
    return this.selectedBadgeOption(ficha, slot)?.icon ?? fallback.icon;
  }

  protected badgeOptionForOpenSlot(): BadgeOption | undefined {
    const slot = this.badgePickerSlot();
    return slot === null ? undefined : this.badgeOptions[slot];
  }

  protected openBadgePicker(slot: number): void {
    this.badgePickerSlot.set(slot);
  }

  protected closeBadgePicker(): void {
    this.badgePickerSlot.set(null);
  }

  protected selectBadge(ficha: Ficha, badge: BadgeOption): void {
    const slot = this.badgePickerSlot();
    if (slot === null) {
      return;
    }

    const tipo = this.badgeType(slot);
    const existing = ficha.conquistas.find((conquista) => conquista.tipo === tipo);
    if (existing) {
      existing.nome = badge.label;
    } else {
      ficha.conquistas.push({ tipo, nome: badge.label, ordem: slot });
    }

    this.closeBadgePicker();
    this.scheduleAutoSave();
  }

  protected clearBadge(ficha: Ficha): void {
    const slot = this.badgePickerSlot();
    if (slot === null) {
      return;
    }

    const tipo = this.badgeType(slot);
    const index = ficha.conquistas.findIndex((conquista) => conquista.tipo === tipo);
    if (index >= 0) {
      ficha.conquistas.splice(index, 1);
      this.scheduleAutoSave();
    }

    this.closeBadgePicker();
  }

  private badgeType(slot: number): string {
    return `insignia-${slot + 1}`;
  }

  protected ribbonConquista(ficha: Ficha, slot: number): FichaConquista | undefined {
    const option = this.ribbonOptions[slot];
    if (!option) {
      return undefined;
    }
    const expectedName = this.normalizeSearch(option.label);
    return ficha.conquistas.find((conquista) =>
      conquista.tipo === 'ribbon' && this.normalizeSearch(conquista.nome) === expectedName
    );
  }

  protected ribbonIcon(ficha: Ficha, slot: number, fallback: BadgeOption): string {
    return this.ribbonConquista(ficha, slot)?.imagem || fallback.icon || '';
  }

  protected ribbonOptionForOpenSlot(): BadgeOption | undefined {
    const slot = this.ribbonPickerSlot();
    return slot === null ? undefined : this.ribbonOptions[slot];
  }

  protected openRibbonPicker(slot: number): void {
    this.ribbonPickerSlot.set(slot);
  }

  protected closeRibbonPicker(): void {
    this.ribbonPickerSlot.set(null);
  }

  protected selectRibbon(ficha: Ficha, ribbon: BadgeOption): void {
    const slot = this.ribbonPickerSlot();
    if (slot === null) {
      return;
    }

    const existing = this.ribbonConquista(ficha, slot);
    if (existing) {
      existing.nome = ribbon.label;
      existing.imagem = ribbon.icon;
    } else {
      ficha.conquistas.push({
        tipo: 'ribbon',
        nome: ribbon.label,
        imagem: ribbon.icon,
        ordem: ficha.conquistas.length,
      });
    }

    this.closeRibbonPicker();
    this.scheduleAutoSave();
  }

  protected clearRibbon(ficha: Ficha): void {
    const slot = this.ribbonPickerSlot();
    if (slot === null) {
      return;
    }

    const conquista = this.ribbonConquista(ficha, slot);
    const index = conquista ? ficha.conquistas.indexOf(conquista) : -1;
    if (index >= 0) {
      ficha.conquistas.splice(index, 1);
      this.scheduleAutoSave();
    }

    this.closeRibbonPicker();
  }

  protected remove<T>(items: T[], index: number): void {
    items.splice(index, 1);
    this.scheduleAutoSave();
  }

  protected removePokemon(items: FichaPokemon[], index: number): void {
    items.splice(index, 1);
    const current = this.selectedPokemonIndex();
    if (current === null || items.length === 0) {
      this.selectedPokemonIndex.set(null);
      this.scheduleAutoSave();
      return;
    }

    this.selectedPokemonIndex.set(Math.max(0, Math.min(current, items.length - 1)));
    this.scheduleAutoSave();
  }

  protected openPokemonEditor(pokemon: FichaPokemon, index: number): void {
    this.selectedPokemonIndex.set(index);
    this.loadPokemonDexData(pokemon);
  }

  protected openPokemonCard(pokemon: FichaPokemon, index: number): void {
    if (this.suppressPokemonClick || this.draggingPokemon() !== null) {
      return;
    }

    this.openPokemonEditor(pokemon, index);
  }

  protected closePokemonEditor(): void {
    this.selectedPokemonIndex.set(null);
    this.closeInlinePickers();
  }

  protected removeMovimento(pokemon: FichaPokemon, index: number): void {
    pokemon.movimentos?.splice(index, 1);
    this.scheduleAutoSave();
  }

  protected openSpritePicker(pokemon: FichaPokemon): void {
    this.spriteSearch.set('');
    this.spritePickerFor.set(pokemon);
  }

  protected closeSpritePicker(): void {
    this.spritePickerFor.set(null);
  }

  protected hasCustomSprite(pokemon: FichaPokemon): boolean {
    return pokemon.sprite?.startsWith('data:image/') ?? false;
  }

  protected selectSprite(pokemon: FichaPokemon, sprite: PokemonSpriteChoice): void {
    const previousSpecies = this.pokemonKey(pokemon.especie);
    pokemon.sprite = sprite.url;
    if (sprite.name) {
      pokemon.especie = sprite.name;
      if (this.pokemonKey(sprite.name) !== previousSpecies) {
        this.resetPokemonDexSelections(pokemon);
      }
      this.loadPokemonDexData(pokemon);
    }
    this.spritePickerFor.set(null);
    this.applyPokemonFeatureSprite(pokemon);
  }

  protected selectPokemonFeature(pokemon: FichaPokemon, feature: string): void {
    pokemon.feature = feature;
    this.applyPokemonFeatureSprite(pokemon);
  }

  protected pokemonAbilities(pokemon: FichaPokemon): string[] {
    return this.pokemonDexData()[this.pokemonKey(pokemon.especie)]?.abilities ?? [];
  }

  protected pokemonMoves(pokemon: FichaPokemon): PokemonDexMove[] {
    return this.pokemonDexData()[this.pokemonKey(pokemon.especie)]?.moves ?? [];
  }

  protected isPokemonLoading(pokemon: FichaPokemon): boolean {
    return Boolean(this.pokemonLoading()[this.pokemonKey(pokemon.especie)]);
  }

  protected moveSelectionValue(pokemon: FichaPokemon, movimento: FichaPokemonMovimento): string {
    return this.isCustomMove(pokemon, movimento) ? '__custom__' : (movimento.nome ?? '');
  }

  protected isCustomMove(pokemon: FichaPokemon, movimento: FichaPokemonMovimento): boolean {
    if (this.customMoveEditors.has(movimento)) {
      return true;
    }
    if (!movimento.nome?.trim()) {
      return false;
    }
    return !this.pokemonMoves(pokemon).some(
      (option) => this.pokemonKey(option.name) === this.pokemonKey(movimento.nome)
    );
  }

  protected selectMoveOption(pokemon: FichaPokemon, movimento: FichaPokemonMovimento, value: string): void {
    if (value === '__custom__') {
      this.customMoveEditors.add(movimento);
      movimento.nome = '';
      movimento.categoria = '';
      movimento.tipo = '';
      movimento.style = '';
      movimento.poder = undefined;
      movimento.accuracy = undefined;
      this.scheduleAutoSave();
      return;
    }

    this.customMoveEditors.delete(movimento);
    movimento.nome = value;
    movimento.categoria = '';
    movimento.tipo = '';
    movimento.style = '';
    movimento.poder = undefined;
    movimento.accuracy = undefined;
    this.selectMove(pokemon, movimento, value);
  }

  protected closeEmptyCustomMove(movimento: FichaPokemonMovimento): void {
    setTimeout(() => {
      const hasCustomData = Boolean(
        movimento.nome?.trim()
        || movimento.categoria?.trim()
        || movimento.tipo?.trim()
        || movimento.style?.trim()
        || movimento.poder !== undefined
        || movimento.accuracy !== undefined
      );
      if (!hasCustomData) {
        this.customMoveEditors.delete(movimento);
      }
    });
  }

  protected selectMove(pokemon: FichaPokemon, movimento: FichaPokemonMovimento, moveName: string): void {
    if (!moveName.trim()) {
      movimento.categoria = '';
      movimento.tipo = '';
      movimento.style = '';
      movimento.poder = undefined;
      movimento.accuracy = undefined;
      this.scheduleAutoSave();
      return;
    }

    const move = this.pokemonMoves(pokemon).find(
      (option) => this.pokemonKey(option.name) === this.pokemonKey(moveName)
    );
    if (!move) {
      this.scheduleAutoSave();
      return;
    }

    movimento.nome = move.name;
    movimento.categoria = move.category ?? movimento.categoria ?? '';
    movimento.tipo = move.type ?? movimento.tipo ?? '';
    movimento.style = move.style ?? movimento.style ?? '';
    movimento.poder = move.power ?? movimento.poder;
    movimento.accuracy = move.accuracy ?? movimento.accuracy;
    if (!move.category || !move.type || !move.style || move.power === undefined || move.accuracy === undefined) {
      this.loadMoveDetails(pokemon, movimento, move.name);
    }
    this.scheduleAutoSave();
  }

  protected moveTypeColor(type?: string): string {
    return pokemonMoveTypeColor(type);
  }

  protected contestStyleColor(style?: string): string {
    return pokemonContestStyleColor(style);
  }

  protected selectedMoveType(type?: string): MoveTypeOption | undefined {
    const key = this.pokemonKey(type);
    return this.moveTypes.find((option) => option.name === key);
  }

  protected moveTypeIcon(type: MoveTypeOption): string {
    return `/assets/move-types/${type.icon}`;
  }

  protected displayPokemonText(value: string): string {
    return value
      .split('-')
      .filter(Boolean)
      .map((part) => part[0]?.toUpperCase() + part.slice(1))
      .join(' ');
  }

  protected displayOptionalPokemonText(value?: string): string {
    return value ? this.displayPokemonText(value) : '—';
  }

  protected displayNumber(value?: number): string {
    return value === undefined || value === null ? '—' : String(value);
  }

  protected rankingLabel(points?: number | null): string {
    if (points === undefined || points === null) {
      return '-';
    }

    const rankingTable = [
      { threshold: 3000, label: '5' },
      { threshold: 2000, label: '4' },
      { threshold: 1000, label: '3' },
      { threshold: 500, label: '2' },
      { threshold: 0, label: '1' },
    ];

    return rankingTable.find((item) => points >= item.threshold)?.label ?? '1';
  }

  protected reputationLabel(points?: number | null): string {
    if (points === undefined || points === null) {
      return '-';
    }

    if (points === 0) {
      return 'Anônimo';
    }

    const positiveTable = [
      { threshold: 30000, label: 'Astro' },
      { threshold: 20000, label: 'Notório' },
      { threshold: 15000, label: 'Celebridade' },
      { threshold: 10000, label: 'Renomado' },
      { threshold: 5000, label: 'Estrela' },
      { threshold: 3000, label: 'Influencer' },
      { threshold: 1000, label: 'Notado' },
      { threshold: 500, label: 'Conhecido' },
      { threshold: 100, label: 'Familiar' },
    ];
    const negativeTable = [
      { threshold: 30000, label: 'Abominável' },
      { threshold: 20000, label: 'Malquisto' },
      { threshold: 15000, label: 'Indesejado' },
      { threshold: 10000, label: 'Procurado' },
      { threshold: 5000, label: 'Bandido' },
      { threshold: 3000, label: 'Mal visto' },
      { threshold: 1000, label: 'Intolerado' },
    ];

    const table = points > 0 ? positiveTable : negativeTable;
    const absolutePoints = Math.abs(points);
    return table.find((item) => absolutePoints >= item.threshold)?.label ?? 'Anônimo';
  }

  protected pokemonSlotTotal(ficha: Ficha): number {
    return 6 + this.miniUpgradeBonus(ficha) + this.slotUpgradeBonus(ficha);
  }

  protected teamPokemonCount(ficha: Ficha): number {
    return ficha.pokemons.filter((pokemon) => this.pokemonLocation(pokemon) === 'equipe').length;
  }

  protected boxPokemonCount(ficha: Ficha): number {
    return ficha.pokemons.filter((pokemon) => this.pokemonLocation(pokemon) === 'box').length;
  }

  protected totalPokemonCount(ficha: Ficha): number {
    return this.teamPokemonCount(ficha) + this.boxPokemonCount(ficha);
  }

  protected totalPokemonCapacity(ficha: Ficha): number {
    return 6 + this.pokemonSlotTotal(ficha);
  }

  protected canAddTeamPokemon(ficha: Ficha): boolean {
    return this.teamPokemonCount(ficha) < 6;
  }

  protected canAddBoxPokemon(ficha: Ficha): boolean {
    return this.boxPokemonCount(ficha) < this.pokemonSlotTotal(ficha);
  }

  protected pokemonEntries(ficha: Ficha, location: 'equipe' | 'box'): PokemonEntry[] {
    return ficha.pokemons
      .map((pokemon, index) => ({ pokemon, index }))
      .filter((entry) => this.pokemonLocation(entry.pokemon) === location);
  }

  protected pokemonLists(ficha: Ficha): PokemonListView {
    const signature = ficha.pokemons
      .map((pokemon, index) => `${index}:${this.pokemonIdentityKey(pokemon)}:${pokemon.ordem ?? ''}:${this.pokemonLocation(pokemon)}`)
      .join('|');
    if (this.pokemonListCache?.ficha === ficha && this.pokemonListCache.signature === signature) {
      return this.pokemonListCache.lists;
    }

    const lists: PokemonListView = { equipe: [], box: [] };
    ficha.pokemons.forEach((pokemon, index) => {
      const entry = { pokemon, index };
      lists[this.pokemonLocation(pokemon)].push(entry);
    });
    this.pokemonListCache = { ficha, signature, lists };
    return lists;
  }

  private pokemonIdentityKey(pokemon: FichaPokemon): string {
    if (pokemon.id) {
      return `id-${pokemon.id}`;
    }

    const current = this.pokemonIdentity.get(pokemon);
    if (current) {
      return `local-${current}`;
    }

    const next = this.nextPokemonIdentity;
    this.nextPokemonIdentity += 1;
    this.pokemonIdentity.set(pokemon, next);
    return `local-${next}`;
  }

  protected trackByPokemonEntry(_index: number, entry: PokemonEntry): FichaPokemon {
    return entry.pokemon;
  }

  protected pokemonLocation(pokemon: FichaPokemon): 'equipe' | 'box' {
    const location = (pokemon.box ?? '').trim().toLowerCase();
    return location === 'box' || location === 'pc' ? 'box' : 'equipe';
  }

  protected setPokemonLocation(ficha: Ficha, pokemon: FichaPokemon, location: 'equipe' | 'box' | string, saveImmediately = false): void {
    if (location !== 'equipe' && location !== 'box') {
      return;
    }

    const currentLocation = this.pokemonLocation(pokemon);
    if (currentLocation === location) {
      return;
    }

    if (location === 'equipe' && this.teamPokemonCount(ficha) >= 6) {
      this.error.set('A equipe comporta no máximo 6 Pokémon.');
      return;
    }

    if (location === 'box' && this.boxPokemonCount(ficha) >= this.pokemonSlotTotal(ficha)) {
      this.error.set('Não há slots livres na box.');
      return;
    }

    pokemon.box = location === 'box' ? 'box' : 'equipe';
    if (saveImmediately) {
      this.saveFichaImmediately();
      return;
    }

    this.scheduleAutoSave();
  }

  protected startPokemonCdkDrag(pokemon: FichaPokemon): void {
    this.draggingPokemon.set(pokemon);
  }

  protected endPokemonCdkDrag(): void {
    if (this.draggingPokemon() !== null) {
      this.blockNextPokemonClick();
    }

    this.draggingPokemon.set(null);
  }

  protected dropPokemonCdk(event: CdkDragDrop<PokemonEntry[]>, ficha: Ficha, location: 'equipe' | 'box'): void {
    const entry = event.item.data ?? event.previousContainer.data[event.previousIndex];
    const pokemon = entry?.pokemon;
    if (!pokemon) {
      this.draggingPokemon.set(null);
      return;
    }

    const sourceLocation = this.pokemonLocation(pokemon);
    if (sourceLocation !== location && !this.canDropPokemon(ficha, location)) {
      this.error.set(location === 'equipe' ? 'A equipe comporta no máximo 6 Pokémon.' : 'Não há slots livres na box.');
      this.draggingPokemon.set(null);
      return;
    }

    this.movePokemonByIdentity(ficha, pokemon, location, event.currentIndex);
    this.pokemonListCache = undefined;
    this.draggingPokemon.set(null);
    this.blockNextPokemonClick();
    this.saveFichaImmediately();
  }

  protected canDropPokemon(ficha: Ficha, location: 'equipe' | 'box'): boolean {
    return location === 'equipe' ? this.teamPokemonCount(ficha) < 6 : this.boxPokemonCount(ficha) < this.pokemonSlotTotal(ficha);
  }

  protected readonly canEnterEquipeList = (drag: CdkDrag<PokemonEntry>): boolean =>
    this.canEnterPokemonList(drag.data?.pokemon, 'equipe');

  protected readonly canEnterBoxList = (drag: CdkDrag<PokemonEntry>): boolean =>
    this.canEnterPokemonList(drag.data?.pokemon, 'box');

  private blockNextPokemonClick(): void {
    this.suppressPokemonClick = true;
    window.setTimeout(() => {
      this.suppressPokemonClick = false;
    }, 180);
  }

  private movePokemonToLocation(
    ficha: Ficha,
    fromIndex: number,
    location: 'equipe' | 'box',
    targetPosition?: number,
    saveImmediately = true,
  ): number | null {
    const pokemon = ficha.pokemons[fromIndex];
    if (!pokemon) {
      return null;
    }

    const currentLocation = this.pokemonLocation(pokemon);
    if (currentLocation !== location && !this.canDropPokemon(ficha, location)) {
      this.error.set(location === 'equipe' ? 'A equipe comporta no máximo 6 Pokémon.' : 'Não há slots livres na box.');
      return null;
    }

    const [removed] = ficha.pokemons.splice(fromIndex, 1);
    removed.box = location === 'box' ? 'box' : 'equipe';

    const targetPokemons = ficha.pokemons.filter((entryPokemon) => this.pokemonLocation(entryPokemon) === location);
    const safePosition = Math.max(0, Math.min(targetPosition ?? targetPokemons.length, targetPokemons.length));
    targetPokemons.splice(safePosition, 0, removed);

    const otherPokemons = ficha.pokemons.filter((entryPokemon) => this.pokemonLocation(entryPokemon) !== location);
    ficha.pokemons.splice(0, ficha.pokemons.length, ...this.mergePokemonLists(otherPokemons, targetPokemons, location));
    ficha.pokemons.forEach((entryPokemon, index) => {
      entryPokemon.ordem = index;
    });

    const newIndex = ficha.pokemons.indexOf(removed);
    if (saveImmediately) {
      this.selectedPokemonIndex.set(null);
      this.draggingPokemon.set(null);
      this.saveFichaImmediately();
    }

    return newIndex >= 0 ? newIndex : null;
  }

  private mergePokemonLists(
    otherPokemons: FichaPokemon[],
    targetPokemons: FichaPokemon[],
    targetLocation: 'equipe' | 'box',
  ): FichaPokemon[] {
    return targetLocation === 'equipe'
      ? [...targetPokemons, ...otherPokemons]
      : [...otherPokemons, ...targetPokemons];
  }

  private findDropLocation(clientX: number, clientY: number): 'equipe' | 'box' | undefined {
    const zones = Array.from(document.querySelectorAll<HTMLElement>('[data-drop-location]'));
    const zone = zones.find((element) => {
      const rect = element.getBoundingClientRect();
      return clientX >= rect.left && clientX <= rect.right && clientY >= rect.top && clientY <= rect.bottom;
    });

    const location = zone?.dataset['dropLocation'];
    return location === 'equipe' || location === 'box' ? location : undefined;
  }

  private canEnterPokemonList(pokemon: FichaPokemon | undefined, location: 'equipe' | 'box'): boolean {
    const current = this.ficha();
    if (!current || !pokemon) {
      return false;
    }

    if (this.pokemonLocation(pokemon) === location) {
      return true;
    }

    return this.canDropPokemon(current, location);
  }

  private movePokemonByIdentity(ficha: Ficha, pokemon: FichaPokemon, location: 'equipe' | 'box', targetPosition: number): void {
    const currentLocation = this.pokemonLocation(pokemon);
    if (currentLocation !== location && !this.canDropPokemon(ficha, location)) {
      this.error.set(location === 'equipe' ? 'A equipe comporta no máximo 6 Pokémon.' : 'Não há slots livres na box.');
      return;
    }

    const teamPokemons = ficha.pokemons.filter((entryPokemon) => entryPokemon !== pokemon && this.pokemonLocation(entryPokemon) === 'equipe');
    const boxPokemons = ficha.pokemons.filter((entryPokemon) => entryPokemon !== pokemon && this.pokemonLocation(entryPokemon) === 'box');
    const targetPokemons = location === 'equipe' ? teamPokemons : boxPokemons;
    const safePosition = Math.max(0, Math.min(targetPosition, targetPokemons.length));

    pokemon.box = location === 'box' ? 'box' : 'equipe';
    targetPokemons.splice(safePosition, 0, pokemon);

    ficha.pokemons.splice(0, ficha.pokemons.length, ...teamPokemons, ...boxPokemons);
    ficha.pokemons.forEach((entryPokemon, index) => {
      entryPokemon.ordem = index;
    });
  }

  protected miniUpgradeBonus(ficha: Ficha): number {
    return Number(ficha.miniUpgrade ?? 0) * 2;
  }

  protected slotUpgradeBonus(ficha: Ficha): number {
    return Number(ficha.slotUpgrade ?? 0) * 5;
  }

  protected themeAccent(themeName?: string): string {
    return this.normalizeThemeColor(themeName);
  }

  protected themeDark(themeName?: string): string {
    return `color-mix(in srgb, ${this.themeAccent(themeName)} 62%, #171a24)`;
  }

  protected themeHighlight(themeName?: string): string {
    return `color-mix(in srgb, ${this.themeAccent(themeName)} 42%, #d7a13d)`;
  }

  protected selectedPokemon(ficha: Ficha): FichaPokemon | null {
    const index = this.selectedPokemonIndex();
    return index === null ? null : ficha.pokemons[index] ?? null;
  }

  protected updateFichaUpgrade(ficha: Ficha, field: 'miniUpgrade' | 'slotUpgrade', value: number | string): void {
    ficha[field] = Math.max(0, Number(value) || 0);
    this.scheduleAutoSave();
  }

  protected selectTheme(ficha: Ficha, themeName: string): void {
    ficha.corTema = themeName;
    this.scheduleAutoSave();
  }

  protected selectedPokeball(pokemon: FichaPokemon): PokeballOption | undefined {
    return this.pokeballs.find((ball) => ball.name === pokemon.pokebola);
  }

  protected selectedMechanic(pokemon: FichaPokemon): PokemonMechanicOption | undefined {
    return this.pokemonMechanics.find((mechanic) => mechanic.name === pokemon.mecanica);
  }

  protected selectedHeldItem(pokemon: FichaPokemon): HeldItemOption | undefined {
    return this.heldItems().find((item) => item.name === pokemon.holdItem);
  }

  protected mechanicIcon(mechanic: PokemonMechanicOption): string {
    return `/assets/mechanics/${mechanic.icon}`;
  }

  protected togglePokeballPicker(index: number): void {
    this.mechanicPickerIndex.set(null);
    this.heldItemPickerIndex.set(null);
    this.pokeballPickerIndex.update((current) => current === index ? null : index);
  }

  protected selectPokeball(pokemon: FichaPokemon, ball: PokeballOption): void {
    pokemon.pokebola = ball.name;
    this.pokeballPickerIndex.set(null);
    this.scheduleAutoSave();
  }

  protected toggleMechanicPicker(index: number): void {
    this.pokeballPickerIndex.set(null);
    this.heldItemPickerIndex.set(null);
    this.mechanicPickerIndex.update((current) => current === index ? null : index);
  }

  protected selectMechanic(pokemon: FichaPokemon, mechanicName: string): void {
    pokemon.mecanica = mechanicName;
    this.mechanicPickerIndex.set(null);
    this.scheduleAutoSave();
  }

  protected toggleHeldItemPicker(index: number): void {
    this.pokeballPickerIndex.set(null);
    this.mechanicPickerIndex.set(null);
    this.heldItemSearch.set('');
    this.heldItemPickerIndex.update((current) => current === index ? null : index);
  }

  protected selectHeldItem(pokemon: FichaPokemon, item?: HeldItemOption): void {
    pokemon.holdItem = item?.name ?? '';
    this.heldItemPickerIndex.set(null);
    this.scheduleAutoSave();
  }

  protected hideBrokenImage(event: Event): void {
    (event.target as HTMLImageElement).style.display = 'none';
  }

  protected clearBrokenInventoryOptionIcon(event: Event, option: InventoryItemOption): void {
    this.hideBrokenImage(event);
    option.icon = undefined;
    this.inventoryItems.set([...this.inventoryItems()]);
  }

  protected clearBrokenHeldItemIcon(event: Event, item: HeldItemOption): void {
    this.hideBrokenImage(event);
    item.icon = undefined;
    this.heldItems.set([...this.heldItems()]);
  }

  protected clearBrokenInventoryItemIcon(event: Event, item: FichaItem): void {
    this.hideBrokenImage(event);
    if (item.icone) {
      item.icone = '';
      this.scheduleAutoSave();
    }
  }

  protected clearInventoryItemImage(item: FichaItem): void {
    item.icone = '';
    this.scheduleAutoSave();
  }

  protected selectInventoryItemImage(event: Event, item: FichaItem): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    const image = new Image();
    const objectUrl = URL.createObjectURL(file);
    image.onload = () => {
      const maxSize = 160;
      const ratio = Math.min(1, maxSize / Math.max(image.width, image.height));
      const width = Math.max(1, Math.round(image.width * ratio));
      const height = Math.max(1, Math.round(image.height * ratio));
      const canvas = document.createElement('canvas');
      const context = canvas.getContext('2d');
      canvas.width = width;
      canvas.height = height;
      context?.clearRect(0, 0, width, height);
      context?.drawImage(image, 0, 0, width, height);
      item.icone = canvas.toDataURL('image/webp', 0.86);
      input.value = '';
      URL.revokeObjectURL(objectUrl);
      this.scheduleAutoSave();
    };

    image.onerror = () => {
      this.error.set('Não foi possível carregar esta imagem.');
      input.value = '';
      URL.revokeObjectURL(objectUrl);
    };

    image.src = objectUrl;
  }

  protected selectCustomSpriteImage(event: Event, pokemon: FichaPokemon): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    const image = new Image();
    const objectUrl = URL.createObjectURL(file);

    image.onload = () => {
      const maxSize = 256;
      const ratio = Math.min(1, maxSize / Math.max(image.width, image.height));
      const width = Math.max(1, Math.round(image.width * ratio));
      const height = Math.max(1, Math.round(image.height * ratio));
      const canvas = document.createElement('canvas');
      const context = canvas.getContext('2d');

      canvas.width = width;
      canvas.height = height;
      context?.clearRect(0, 0, width, height);
      context?.drawImage(image, 0, 0, width, height);

      pokemon.sprite = canvas.toDataURL('image/webp', 0.9);
      input.value = '';
      URL.revokeObjectURL(objectUrl);
      this.spritePickerFor.set(null);
      this.scheduleAutoSave();
    };

    image.onerror = () => {
      this.error.set('Não foi possível carregar este sprite.');
      input.value = '';
      URL.revokeObjectURL(objectUrl);
    };

    image.src = objectUrl;
  }

  private pokemonSpriteUrl(dex: number): string {
    return `https://resource.pokemon-home.com/battledata/img/pokei128/icon${String(dex).padStart(4, '0')}_f00_s0.png`;
  }

  private dexFromSpriteUrl(sprite?: string): number | undefined {
    const match = sprite?.match(/icon(\d{4})_f00_s[01]\.png/);
    return match ? Number(match[1]) : undefined;
  }

  private dexFromSpecies(species?: string): number | undefined {
    const normalizedSpecies = this.pokemonKey(species ?? '');
    const found = Object.entries(this.pokemonNames()).find(([, name]) => this.pokemonKey(name) === normalizedSpecies);
    return found ? Number(found[0]) : undefined;
  }

  private itemIcon(file: string): string {
    return `/assets/itemdex/${file}`;
  }

  private pokeApiItemIcon(name: string): string {
    return `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/${name}.png`;
  }

  private loadPokemonNames(): void {
    fetch('https://pokeapi.co/api/v2/pokemon-species?limit=1025')
      .then((response) => response.ok ? response.json() : Promise.reject())
      .then((data: { results?: { name: string }[] }) => {
        const results = data.results ?? [];
        const names = Object.fromEntries(results.map((pokemon, index) => [index + 1, pokemon.name]));
        this.pokemonDexCount.set(Math.max(1025, results.length));
        this.pokemonNames.set(names);
      })
      .catch(() => this.pokemonNames.set({}));
  }

  private loadHeldItems(): void {
    fetch('https://pokeapi.co/api/v2/item-category/held-items')
      .then((response) => response.ok ? response.json() : Promise.reject())
      .then((data: { items?: { name: string; url: string }[] }) => this.hydrateHeldItems(data.items ?? []))
      .catch(() => {
        fetch('https://pokeapi.co/api/v2/item?limit=2500')
          .then((response) => response.ok ? response.json() : Promise.reject())
          .then((data: { results?: { name: string; url: string }[] }) => this.hydrateHeldItems(data.results ?? []))
          .catch(() => this.heldItems.set([]));
      });
  }

  private hydrateHeldItems(items: { name: string; url: string }[]): void {
    const sorted = items
      .map((item) => ({
        name: item.name,
        label: this.displayPokemonText(item.name),
        icon: this.pokeApiItemIcon(item.name),
      }))
      .sort((first, second) => first.label.localeCompare(second.label));

    this.heldItems.set(sorted);
  }

  private loadInventoryItems(): void {
    fetch('https://pokeapi.co/api/v2/item?limit=10000')
      .then((response) => response.ok ? response.json() : Promise.reject())
      .then((data: { results?: { name: string }[] }) => {
        const apiItems = (data.results ?? [])
          .filter((item) => !this.isHiddenInventoryCatalogItem(item.name))
          .map((item) => this.toInventoryOption(item.name, true));
        const localItems = Object.keys(ITEMDEX_DETAILS).map((name) => this.toInventoryOption(name));
        const byName = new Map<string, InventoryItemOption>();
        [...apiItems, ...localItems].forEach((item) => {
          const current = byName.get(item.name);
          byName.set(item.name, { ...current, ...item, icon: item.icon ?? current?.icon });
        });
        this.inventoryItems.set([...byName.values()].sort((first, second) => first.label.localeCompare(second.label)));
      })
      .catch(() => {
        this.inventoryItems.set(
          Object.keys(ITEMDEX_DETAILS)
            .map((name) => this.toInventoryOption(name))
            .sort((first, second) => first.label.localeCompare(second.label))
        );
      });
  }

  private toInventoryOption(name: string, useApiIcon = false): InventoryItemOption {
    const details = ITEMDEX_DETAILS[name];
    const localIcon = ITEMDEX_ICONS[name];
    return {
      name,
      label: this.inventoryItemLabel(name),
      icon: this.inventoryItemIcon(name, localIcon, useApiIcon),
      category: details?.category ?? 'Item',
      description: details?.description,
    };
  }

  private isHiddenInventoryCatalogItem(name: string): boolean {
    return /^(?:tm|tr)(?:\d|-)/i.test(name)
      || /^data-card(?:-\d+)?$/i.test(name)
      || /^dynamax-crystal(?:-|$)/i.test(name);
  }

  private inventoryItemLabel(name: string): string {
    const labels: Record<string, string> = {
      tm: 'TM',
      tr: 'TR',
    };
    return labels[name] ?? this.displayPokemonText(name);
  }

  private inventoryItemIcon(name: string, localIcon?: string, useApiIcon = false): string | undefined {
    if (name === 'tm') {
      return this.pokeApiItemIcon('tm-normal');
    }

    if (name === 'tr') {
      return this.pokeApiItemIcon('tr-normal');
    }

    return localIcon ? this.itemIcon(localIcon) : useApiIcon ? this.pokeApiItemIcon(name) : undefined;
  }

  private inventoryCategoryLabel(category: string): string {
    const categories: Record<string, string> = {
      'held-items': 'Itens segurados',
      'choice': 'Itens de escolha',
      'effort-training': 'Treinamento',
      'bad-held-items': 'Itens segurados',
      'training': 'Treinamento',
      plates: 'Placas',
      'species-specific': 'Itens específicos',
      'type-enhancement': 'Aprimoramento de tipo',
      'event-items': 'Itens de evento',
      gameplay: 'Itens de jornada',
      'plot-advancement': 'Itens de história',
      loot: 'Tesouros',
      vitamin: 'Vitaminas',
      healing: 'Cura',
      'pp-recovery': 'Recuperação de PP',
      revival: 'Reviver',
      status: 'Cura de status',
      evolution: 'Evolução',
      spelunking: 'Exploração',
      'all-machines': 'Máquinas',
      'apricorn-balls': 'Pokéballs',
      'standard-balls': 'Pokéballs',
      'special-balls': 'Pokéballs',
    };
    return categories[this.pokemonKey(category)] ?? this.displayPokemonText(category);
  }

  private loadInventoryItemDescription(item: FichaItem, option: InventoryItemOption): void {
    fetch(`https://pokeapi.co/api/v2/item/${option.name}`)
      .then((response) => response.ok ? response.json() : Promise.reject())
      .then((data: {
        effect_entries?: { effect?: string; short_effect?: string; language?: { name: string } }[];
        flavor_text_entries?: { text?: string; language?: { name: string } }[];
        category?: { name: string };
        sprites?: { default?: string | null };
      }) => {
        const effect = data.effect_entries?.find((entry) => ['pt-BR', 'pt'].includes(entry.language?.name ?? ''));
        const flavor = data.flavor_text_entries?.find((entry) => ['pt-BR', 'pt'].includes(entry.language?.name ?? ''));
        item.descricao = option.description
          ?? effect?.short_effect
          ?? effect?.effect
          ?? flavor?.text?.replace(/\s+/g, ' ')
          ?? item.descricao
          ?? 'Descrição em português não disponível.';
        item.categoria = option.category || this.inventoryCategoryLabel(data.category?.name ?? 'Item');
        item.icone = data.sprites?.default ?? item.icone ?? '';
        this.scheduleAutoSave();
      })
      .catch(() => undefined);
  }

  private loadMoveDetails(pokemon: FichaPokemon, movimento: FichaPokemonMovimento, moveName: string): void {
    fetch(`https://pokeapi.co/api/v2/move/${moveName}`)
      .then((response) => response.ok ? response.json() : Promise.reject())
      .then((data: {
        accuracy?: number | null;
        damage_class?: { name: string };
        contest_type?: { name: string } | null;
        power?: number | null;
        type?: { name: string };
      }) => {
        const category = data.damage_class?.name ?? '';
        const type = data.type?.name ?? '';
        const style = data.contest_type?.name ? this.displayPokemonText(data.contest_type.name) : '';
        const power = data.power ?? undefined;
        const accuracy = data.accuracy ?? undefined;
        movimento.categoria = category;
        movimento.tipo = type;
        movimento.style = style || movimento.style;
        movimento.poder = power;
        movimento.accuracy = accuracy;
        this.scheduleAutoSave();

        const key = this.pokemonKey(pokemon.especie);
        this.pokemonDexData.update((cache) => {
          const details = cache[key];
          if (!details) {
            return cache;
          }

          return {
            ...cache,
            [key]: {
              ...details,
              moves: details.moves.map((move) => move.name === moveName ? { ...move, category, type, style, power, accuracy } : move),
            },
          };
        });
      })
      .catch(() => undefined);
  }

  protected loadPokemonDexData(pokemon: FichaPokemon): void {
    const key = this.pokemonKey(pokemon.especie);
    if (!key || this.pokemonDexData()[key] || this.pokemonLoading()[key]) {
      return;
    }

    this.pokemonLoading.update((loading) => ({ ...loading, [key]: true }));

    fetch(`https://pokeapi.co/api/v2/pokemon/${key}`)
      .then((response) => response.ok ? response.json() : Promise.reject())
      .then((data: {
        abilities?: { ability: { name: string } }[];
        moves?: { move: { name: string; url: string } }[];
        stats?: { base_stat: number; stat: { name: string } }[];
      }) => {
        const abilities = (data.abilities ?? []).map((item) => item.ability.name);
        const moves = (data.moves ?? [])
          .map((item) => ({ name: item.move.name }))
          .sort((first, second) => first.name.localeCompare(second.name));
        const stats = this.toPokemonStats(data.stats ?? []);

        pokemon.hp = stats.hp ?? pokemon.hp;
        pokemon.atk = stats.atk ?? pokemon.atk;
        pokemon.def = stats.def ?? pokemon.def;
        pokemon.satk = stats.satk ?? pokemon.satk;
        pokemon.sdef = stats.sdef ?? pokemon.sdef;
        pokemon.speed = stats.speed ?? pokemon.speed;
        this.scheduleAutoSave();

        this.pokemonDexData.update((cache) => ({ ...cache, [key]: { abilities, moves, stats } }));
        this.pokemonLoading.update((loading) => ({ ...loading, [key]: false }));
      })
      .catch(() => this.pokemonLoading.update((loading) => ({ ...loading, [key]: false })));
  }

  private resetPokemonDexSelections(pokemon: FichaPokemon): void {
    pokemon.ability = '';
    pokemon.movimentos = this.createMoveSlots();
  }

  private applyPokemonFeatureSprite(pokemon: FichaPokemon): void {
    const dex = this.dexFromSpriteUrl(pokemon.sprite) ?? this.dexFromSpecies(pokemon.especie);
    const pokemonKey = this.pokemonKey(pokemon.especie) || (dex ? String(dex) : '');
    if (!pokemonKey) {
      this.scheduleAutoSave();
      return;
    }

    fetch(`https://pokeapi.co/api/v2/pokemon/${pokemonKey}`)
      .then((response) => response.ok ? response.json() : Promise.reject())
      .then((data: { sprites?: PokemonApiSprites }) => {
        const sprite = pokemon.feature === 'Shiny'
          ? this.pickShinySprite(data.sprites)
          : this.pickDefaultSprite(data.sprites);
        if (sprite) {
          pokemon.sprite = sprite;
        }
        this.scheduleAutoSave();
      })
      .catch(() => this.scheduleAutoSave());
  }

  private replaceAnimatedPokemonSprites(ficha: Ficha): void {
    ficha.pokemons
      .filter((pokemon) => pokemon.sprite?.includes('/sprites/pokemon/other/showdown/'))
      .forEach((pokemon) => this.applyPokemonFeatureSprite(pokemon));
  }

  private pickDefaultSprite(sprites?: PokemonApiSprites): string | undefined {
    return sprites?.other?.['official-artwork']?.front_default
      ?? sprites?.other?.home?.front_default
      ?? sprites?.front_default
      ?? undefined;
  }

  private pickShinySprite(sprites?: PokemonApiSprites): string | undefined {
    return sprites?.other?.['official-artwork']?.front_shiny
      ?? sprites?.other?.home?.front_shiny
      ?? sprites?.front_shiny
      ?? undefined;
  }

  private createMoveSlots(): FichaPokemonMovimento[] {
    return Array.from({ length: 8 }, (_, index) => ({ nome: '', categoria: '', tipo: '', style: '', ordem: index }));
  }

  private toPokemonStats(stats: { base_stat: number; stat: { name: string } }[]): NonNullable<PokemonDexDetails['stats']> {
    const statMap: Record<string, keyof NonNullable<PokemonDexDetails['stats']>> = {
      hp: 'hp',
      attack: 'atk',
      defense: 'def',
      'special-attack': 'satk',
      'special-defense': 'sdef',
      speed: 'speed',
    };

    return stats.reduce<NonNullable<PokemonDexDetails['stats']>>((accumulator, stat) => {
      const key = statMap[stat.stat.name];
      if (key) {
        accumulator[key] = stat.base_stat;
      }

      return accumulator;
    }, {});
  }

  private pokemonKey(value?: string): string {
    return this.normalizeSearch(value ?? '').replace(/\s+/g, '-');
  }

  private normalizeSearch(value: string): string {
    return value
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  protected selectCharacterImage(event: Event, ficha: Ficha): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    const image = new Image();
    const objectUrl = URL.createObjectURL(file);

    image.onload = () => {
      const maxSize = 900;
      const ratio = Math.min(1, maxSize / Math.max(image.width, image.height));
      const width = Math.max(1, Math.round(image.width * ratio));
      const height = Math.max(1, Math.round(image.height * ratio));
      const canvas = document.createElement('canvas');
      const context = canvas.getContext('2d');

      canvas.width = width;
      canvas.height = height;
      context?.drawImage(image, 0, 0, width, height);

      ficha.photoplayer = canvas.toDataURL('image/jpeg', 0.82);
      input.value = '';
      URL.revokeObjectURL(objectUrl);
      this.scheduleAutoSave();
    };

    image.onerror = () => {
      this.error.set('Não foi possível carregar esta imagem.');
      input.value = '';
      URL.revokeObjectURL(objectUrl);
    };

    image.src = objectUrl;
  }

  protected displayValue(value?: string | number | null): string {
    return display(value);
  }

  protected moneyValue(value?: number): string {
    return money(value);
  }

  protected initials(name: string): string {
    return name
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join('');
  }

  private normalizeFicha(ficha: Ficha): Ficha {
    return {
      ...ficha,
      miniUpgrade: Number(ficha.miniUpgrade ?? 0),
      slotUpgrade: Number(ficha.slotUpgrade ?? 0),
      corTema: ficha.corTema || this.defaultTheme,
      relacionados: (ficha.relacionados ?? []) as FichaRelacionado[],
      habilidades: (ficha.habilidades ?? []) as FichaHabilidade[],
      conquistas: (ficha.conquistas ?? []) as FichaConquista[],
      pokemons: ((ficha.pokemons ?? []) as FichaPokemon[]).map((pokemon) => ({
        ...pokemon,
        movimentos: this.normalizeMoveSlots(pokemon.movimentos ?? []),
      })),
      itens: ((ficha.itens ?? []) as FichaItem[]).map((item, index) => this.normalizeInventoryItem(item, index)),
      registros: (ficha.registros ?? []) as FichaRegistro[],
    };
  }

  private normalizeInventoryItem(item: FichaItem, index: number): FichaItem {
    const codigo = item.codigo?.trim() || this.inventoryCodeFromName(item.nome);
    const resolvedIcon = this.inventoryIconFromCode(codigo);
    const resolvedDescription = ITEMDEX_DETAILS[codigo]?.description;
    return {
      ...item,
      categoria: item.categoria?.trim() || 'Item',
      codigo,
      icone: this.shouldRefreshInventoryIcon(item.icone, resolvedIcon) ? resolvedIcon : item.icone,
      nome: item.nome?.trim() || `Item ${index + 1}`,
      quantidade: Number(item.quantidade ?? 1),
      descricao: this.shouldRefreshInventoryDescription(codigo, item.descricao, resolvedDescription) ? resolvedDescription : item.descricao,
      ordem: item.ordem ?? index,
    };
  }

  private shouldRefreshInventoryIcon(currentIcon?: string, resolvedIcon?: string): boolean {
    if (!resolvedIcon) {
      return !currentIcon;
    }

    if (!currentIcon) {
      return true;
    }

    return currentIcon.startsWith('/assets/itemdex/') && currentIcon !== resolvedIcon;
  }

  private shouldRefreshInventoryDescription(code: string, currentDescription?: string, resolvedDescription?: string): boolean {
    if (!resolvedDescription) {
      return false;
    }

    if (!currentDescription?.trim()) {
      return true;
    }

    if (code !== 'alpha-poketch') {
      return false;
    }

    const legacyAlphaDescription = 'CONSUMÍVEL. Um Pokétch avançado. Serve como uma enciclopédia digital aprimorada que fornece informações sobre Pokémon e itens em duas ou três frases.';
    return currentDescription === ITEMDEX_DETAILS['pokeblock-kit']?.description
      || currentDescription === legacyAlphaDescription;
  }

  private inventoryCodeFromName(name?: string): string {
    const code = this.pokemonKey(name ?? '');
    return ITEMDEX_DETAILS[code] || ITEMDEX_ICONS[code] ? code : '';
  }

  private inventoryIconFromCode(code?: string): string {
    const normalizedCode = this.pokemonKey(code ?? '');
    if (!normalizedCode) {
      return '';
    }

    const localIcon = ITEMDEX_ICONS[normalizedCode];
    return this.inventoryItemIcon(normalizedCode, localIcon, true) ?? '';
  }

  private normalizeMoveSlots(movimentos: FichaPokemonMovimento[]): FichaPokemonMovimento[] {
    return Array.from({ length: 8 }, (_, index) => ({
      nome: movimentos[index]?.nome ?? '',
      categoria: movimentos[index]?.categoria ?? '',
      tipo: movimentos[index]?.tipo ?? '',
      style: movimentos[index]?.style ?? '',
      poder: movimentos[index]?.poder,
      accuracy: movimentos[index]?.accuracy,
      ordem: movimentos[index]?.ordem ?? index,
    }));
  }

  private hydrateMissingMoveStyles(ficha: Ficha): void {
    const movements = ficha.pokemons
      .flatMap((pokemon) => pokemon.movimentos ?? [])
      .filter((move) => Boolean(move.nome?.trim()) && !move.style?.trim());

    if (!movements.length) {
      return;
    }

    Promise.all(movements.map(async (move) => ({ move, style: await loadPokemonMoveStyle(move.nome) })))
      .then((results) => {
        let changed = false;
        results.forEach(({ move, style }) => {
          if (style && !move.style?.trim()) {
            move.style = style;
            changed = true;
          }
        });
        if (changed) {
          this.scheduleAutoSave();
        }
      });
  }

  protected scheduleAutoSave(): void {
    if (this.autoSaveTimer) {
      clearTimeout(this.autoSaveTimer);
    }

    this.autoSaveTimer = setTimeout(() => this.autoSaveFicha(), 650);
  }

  private saveFichaImmediately(): void {
    if (this.autoSaveTimer) {
      clearTimeout(this.autoSaveTimer);
      this.autoSaveTimer = undefined;
    }

    this.autoSaveFicha();
  }

  private autoSaveFicha(): void {
    const current = this.ficha();
    if (!current) {
      return;
    }

    if (this.autoSaveInFlight) {
      this.autoSavePending = true;
      return;
    }

    this.success.set('');
    this.error.set('');
    this.autoSaveInFlight = true;
    this.api.update(current.id, fichaToPayload(current)).subscribe({
      next: () => this.finishAutoSave(),
      error: () => {
        this.error.set('Não foi possível salvar automaticamente.');
        this.finishAutoSave();
      },
    });
  }

  private finishAutoSave(): void {
    this.autoSaveInFlight = false;
    if (!this.autoSavePending) {
      return;
    }

    this.autoSavePending = false;
    this.autoSaveFicha();
  }

  private normalizeThemeColor(themeName?: string): string {
    const legacyThemes: Record<string, string> = {
      verde: '#2f6f55',
      azul: '#2d6f9f',
      vermelho: '#9d3f3f',
      roxo: '#6f4aa8',
      cinza: '#64706c',
    };
    const value = themeName?.trim() || this.defaultTheme;
    if (value.toLowerCase() === '#2f6f55') {
      return this.defaultTheme;
    }
    return /^#[0-9a-fA-F]{6}$/.test(value) ? value : legacyThemes[value] ?? this.defaultTheme;
  }
}
