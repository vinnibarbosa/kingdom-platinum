export interface FichaRelacionado {
  id?: number
  nome: string
  relacao: string
  ordem?: number
}

export interface FichaHabilidade {
  id?: number
  nome: string
  descricao: string
  ordem?: number
}

export interface FichaConquista {
  id?: number
  tipo: string
  nome: string
  imagem?: string
  dataConquista?: string
  ordem?: number
}

export interface FichaPokemonMovimento {
  id?: number
  nome: string
  categoria: string
  tipo?: string
  style?: string
  poder?: number
  accuracy?: number
  ordem?: number
}

export interface FichaPokemon {
  id?: number
  box?: string
  pokebola?: string
  apelido: string
  especie: string
  genero?: string
  sobre?: string
  ability?: string
  feature?: string
  nature?: string
  holdItem?: string
  happinessAtual?: number
  happinessMax?: number
  combo?: string
  hp?: number
  atk?: number
  def?: number
  satk?: number
  sdef?: number
  speed?: number
  pwr?: number
  stm?: number
  skl?: number
  jmp?: number
  contestSpeed?: number
  ordem?: number
  movimentos?: FichaPokemonMovimento[]
}

export interface FichaItem {
  id?: number
  categoria: string
  codigo?: string
  nome: string
  quantidade: number
  descricao?: string
  ordem?: number
}

export interface FichaRegistro {
  id?: number
  tipoMovimento: string
  descricao: string
  dataRegistro?: string
  ordem?: number
}

export interface Ficha {
  id: number
  idOrganizacao: number
  nome: string
  frase?: string
  idade?: number
  naturalidade?: string
  classePersonagem?: string
  alturaCm?: number
  pesoKg?: number
  tipoFisico?: string
  indole?: string
  ranking?: number
  ocupacao?: string
  reputacao?: number
  dinheiro?: number
  pontosVida?: number
  equipe?: string
  pontos?: number
  photoplayer?: string
  banner?: string
  player?: string
  biografia?: string
  anotacoes?: string
  relacionados: FichaRelacionado[]
  habilidades: FichaHabilidade[]
  conquistas: FichaConquista[]
  pokemons: FichaPokemon[]
  itens: FichaItem[]
  registros: FichaRegistro[]
  createdAt?: string
  createdBy?: string
  updatedAt?: string
  updatedBy?: string
}

export type FichaPayload = Omit<
  Ficha,
  'id' | 'idOrganizacao' | 'createdAt' | 'createdBy' | 'updatedAt' | 'updatedBy'
>

export interface FichaResumo {
  id: number
  idOrganizacao: number
  nome: string
  classePersonagem?: string
  ocupacao?: string
  player?: string
  pontos?: number
  createdAt?: string
  updatedAt?: string
}

export interface Page<T> {
  content: T[]
  total: number
  pageable?: {
    page: number
    size: number
  }
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  usuario: {
    id: number
    username: string
    nome: string
    perfil: string
  }
}

export interface ThemeColors {
  primary: string
  secondary: string
}

export const ITEM_CATEGORIES = [
  'Pokeball',
  'Snack',
  'Medicine',
  'Itens de Treinador',
  'Z-Crystal',
  'Hold Item',
  'Props',
  'Fossil',
  'Megastone',
  'Treasure Item',
] as const

export const DEFAULT_THEME: ThemeColors = {
  primary: '#586a9b',
  secondary: '#c8922f',
}
