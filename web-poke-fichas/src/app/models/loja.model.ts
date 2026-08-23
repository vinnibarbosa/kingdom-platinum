export interface LojaItem {
  id: number;
  categoria: string;
  codigo?: string;
  icone?: string;
  nome: string;
  descricao?: string;
  preco: number;
  ativo: boolean;
  ordem?: number;
}

export interface LojaItemPayload {
  categoria: string;
  codigo?: string;
  icone?: string;
  nome: string;
  descricao?: string;
  preco: number;
  ativo?: boolean;
  ordem?: number;
}

export interface FichaCompra {
  id: number;
  nome: string;
  dinheiro?: number;
  corTema?: string;
}

export interface CompraLojaResponse {
  idFicha: number;
  dinheiroRestante: number;
  itens: string[];
  subtotal: number;
  desconto: number;
  totalPago: number;
}

export interface CompraLojaItem { idItem: number; quantidade: number; }

export interface CatalogoLojaImportacao { importados: number; ignorados: number; }

export interface LojaCupom { id: number; codigo: string; percentual: number; ativo: boolean; }
export interface LojaCupomPayload { codigo: string; percentual: number; ativo: boolean; }
