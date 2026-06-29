export interface FichaRelacionado {
  id?: number;
  nome: string;
  relacao: string;
  imagem?: string;
  historia?: string;
  ordem?: number;
}

export interface FichaHabilidade {
  id?: number;
  nome: string;
  descricao: string;
  ordem?: number;
}

export interface FichaConquista {
  id?: number;
  tipo: string;
  nome: string;
  imagem?: string;
  dataConquista?: string;
  ordem?: number;
}

export interface FichaPokemonMovimento {
  id?: number;
  nome: string;
  categoria: string;
  tipo?: string;
  style?: string;
  poder?: number;
  accuracy?: number;
  ordem?: number;
}

export interface FichaPokemon {
  id?: number;
  box?: string;
  pokebola?: string;
  apelido: string;
  especie: string;
  sprite?: string;
  genero?: string;
  sobre?: string;
  ability?: string;
  feature?: string;
  mecanica?: string;
  nature?: string;
  holdItem?: string;
  holdItemIcon?: string;
  happinessAtual?: number;
  happinessMax?: number;
  combo?: string;
  miniUpgrade?: number;
  slotUpgrade?: number;
  hp?: number;
  atk?: number;
  def?: number;
  satk?: number;
  sdef?: number;
  speed?: number;
  pwr?: number;
  stm?: number;
  skl?: number;
  jmp?: number;
  ordem?: number;
  movimentos?: FichaPokemonMovimento[];
}

export interface FichaItem {
  id?: number;
  categoria: string;
  codigo?: string;
  icone?: string;
  nome: string;
  quantidade: number;
  descricao?: string;
  ordem?: number;
}

export interface FichaRegistro {
  id?: number;
  tipoMovimento: string;
  descricao: string;
  dataRegistro?: string;
  registradoEm?: string;
  registradoPor?: string;
  ordem?: number;
}

export interface FichaHistorico {
  id: number;
  lote: string;
  acao: 'ADICIONADO' | 'REMOVIDO' | 'ALTERADO';
  campo: string;
  valorAnterior?: string;
  valorNovo?: string;
  createdAt: string;
  createdBy?: string;
}

export interface FichaPokemonResumo {
  apelido: string;
  especie: string;
  sprite?: string;
  mecanica?: string;
  ordem?: number;
}

export interface Ficha {
  id: number;
  idOrganizacao: number;
  nome: string;
  frase?: string;
  idade?: number;
  naturalidade?: string;
  classePersonagem?: string;
  alturaCm?: number;
  pesoKg?: number;
  tipoFisico?: string;
  indole?: string;
  ranking?: number;
  ocupacao?: string;
  reputacao?: number;
  dinheiro?: number;
  pontosVida?: number;
  equipe?: string;
  pontos?: number;
  miniUpgrade?: number;
  slotUpgrade?: number;
  corTema?: string;
  photoplayer?: string;
  banner?: string;
  avatar?: string;
  player?: string;
  biografia?: string;
  anotacoes?: string;
  relacionados: FichaRelacionado[];
  habilidades: FichaHabilidade[];
  conquistas: FichaConquista[];
  pokemons: FichaPokemon[];
  itens: FichaItem[];
  registros: FichaRegistro[];
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export type FichaPayload = Omit<Ficha, 'id' | 'idOrganizacao' | 'createdAt' | 'createdBy' | 'updatedAt' | 'updatedBy'>;

export interface FichaResumo {
  id: number;
  idOrganizacao: number;
  nome: string;
  classePersonagem?: string;
  ocupacao?: string;
  player?: string;
  photoplayer?: string;
  avatar?: string;
  pokemonsEquipe: FichaPokemonResumo[];
  createdAt?: string;
  updatedAt?: string;
}

export interface Page<T> {
  content: T[];
  total?: number;
  totalElements?: number;
}

export interface Usuario {
  id: number;
  idEntidade?: number;
  idOrganizacao?: number;
  username: string;
  nome: string;
  perfil: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  usuario: Usuario;
}

export interface RegisterRequest {
  username: string;
  senha: string;
}

export interface RedefinirSenhaRequest {
  username: string;
}
