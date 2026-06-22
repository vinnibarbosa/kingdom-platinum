import { Ficha, FichaPayload } from '../models/ficha.model';

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
    miniUpgrade: Number(ficha.miniUpgrade ?? 0),
    slotUpgrade: Number(ficha.slotUpgrade ?? 0),
    corTema: ficha.corTema ?? '',
    photoplayer: ficha.photoplayer ?? '',
    avatar: ficha.avatar ?? '',
    player: ficha.player ?? '',
    biografia: ficha.biografia ?? '',
    anotacoes: ficha.anotacoes ?? '',
    relacionados: ficha.relacionados ?? [],
    habilidades: ficha.habilidades ?? [],
    conquistas: (ficha.conquistas ?? []).map((conquista, index) => ({
      ...conquista,
      tipo: conquista.tipo?.trim() || 'premiacao',
      nome: conquista.nome?.trim() || `Conquista ${index + 1}`,
      ordem: conquista.ordem ?? index,
    })),
    pokemons: (ficha.pokemons ?? []).map(({ miniUpgrade, slotUpgrade, ...pokemon }, index) => ({
      ...pokemon,
      apelido: pokemon.apelido?.trim() || `Pokémon ${index + 1}`,
      especie: pokemon.especie?.trim() || 'Indefinido',
      movimentos: (pokemon.movimentos ?? [])
        .filter((movimento) => Boolean(movimento.nome?.trim()))
        .map((movimento) => ({
          ...movimento,
          categoria: movimento.categoria ?? '',
          tipo: movimento.tipo ?? '',
          style: movimento.style ?? '',
          accuracy: movimento.accuracy,
        })),
    })),
    itens: (ficha.itens ?? []).map((item, index) => ({
      ...item,
      categoria: (item.categoria?.trim() || 'Item').slice(0, 60),
      codigo: item.codigo?.slice(0, 40),
      icone: item.icone ?? '',
      nome: item.nome?.trim() || `Item ${index + 1}`,
      quantidade: Number(item.quantidade ?? 0),
      descricao: item.descricao ?? '',
      ordem: item.ordem ?? index,
    })),
    registros: ficha.registros ?? [],
  };
}

export function display(value?: string | number | null): string {
  if (value === undefined || value === null || value === '') {
    return '-';
  }

  return String(value);
}

export function money(value?: number): string {
  if (value === undefined || value === null) {
    return '-';
  }

  return `C$ ${Number(value).toLocaleString('pt-BR', { minimumFractionDigits: 0 })}`;
}

export function isTeam(box?: string): boolean {
  if (!box) {
    return true;
  }

  const normalized = box.toLowerCase();
  return normalized !== 'box' && normalized !== 'pc';
}
