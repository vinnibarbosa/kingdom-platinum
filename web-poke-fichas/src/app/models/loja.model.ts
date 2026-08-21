import { FichaItem } from './ficha.model';

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
  item: FichaItem;
  totalPago: number;
}
