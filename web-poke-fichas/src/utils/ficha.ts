import type { Ficha, FichaPayload, ThemeColors } from '../types/ficha'

export function fichaToPayload(ficha: Ficha): FichaPayload {
  return {
    nome: ficha.nome,
    frase: ficha.frase ?? '',
    idade: ficha.idade,
    naturalidade: ficha.naturalidade ?? '',
    classePersonagem: ficha.classePersonagem ?? '',
    alturaCm: ficha.alturaCm,
    pesoKg: ficha.pesoKg,
    tipoFisico: ficha.tipoFisico ?? '',
    indole: ficha.indole ?? '',
    ranking: ficha.ranking,
    ocupacao: ficha.ocupacao ?? '',
    reputacao: ficha.reputacao,
    dinheiro: ficha.dinheiro,
    pontosVida: ficha.pontosVida,
    equipe: ficha.equipe ?? '',
    pontos: ficha.pontos,
    photoplayer: ficha.photoplayer ?? '',
    player: ficha.player ?? '',
    biografia: ficha.biografia ?? '',
    anotacoes: ficha.anotacoes ?? '',
    relacionados: ficha.relacionados ?? [],
    habilidades: ficha.habilidades ?? [],
    conquistas: ficha.conquistas ?? [],
    pokemons: ficha.pokemons ?? [],
    itens: ficha.itens ?? [],
    registros: ficha.registros ?? [],
  }
}

export function themeKey(fichaId: number) {
  return `poke-ficha-theme-${fichaId}`
}

export function loadTheme(fichaId: number, fallback: ThemeColors): ThemeColors {
  const raw = localStorage.getItem(themeKey(fichaId))
  if (!raw) return fallback
  try {
    return JSON.parse(raw) as ThemeColors
  } catch {
    return fallback
  }
}

export function saveTheme(fichaId: number, theme: ThemeColors) {
  localStorage.setItem(themeKey(fichaId), JSON.stringify(theme))
}

export function formatMoney(value?: number) {
  if (value == null) return '—'
  return `C$ ${Number(value).toLocaleString('pt-BR', { minimumFractionDigits: 0 })}`
}

export function displayOrDash(value?: string | number | null) {
  if (value == null || value === '') return '—'
  return String(value)
}

export function isTeamPokemon(box?: string) {
  if (!box) return true
  const normalized = box.toLowerCase()
  return normalized !== 'box' && normalized !== 'pc'
}
