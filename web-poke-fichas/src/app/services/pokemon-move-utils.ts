const contestStyleCache = new Map<string, Promise<string>>();

const contestStyleLabels: Record<string, string> = {
  cool: 'Cool',
  beauty: 'Beauty',
  cute: 'Cute',
  smart: 'Smart',
  tough: 'Tough',
};

const moveTypeColors: Record<string, string> = {
  normal: '#9fa19f',
  fire: '#e62829',
  water: '#2980ef',
  electric: '#d8a900',
  grass: '#3fa129',
  ice: '#3dcef3',
  fighting: '#e66f00',
  poison: '#9141cb',
  ground: '#915121',
  flying: '#669dd3',
  psychic: '#ef4179',
  bug: '#829316',
  rock: '#958f68',
  ghost: '#704170',
  dragon: '#5060e1',
  dark: '#624d4e',
  steel: '#4f899e',
  fairy: '#d75fd7',
  light: '#c7a82d',
  scent: '#32956f',
};

const contestStyleColors: Record<string, string> = {
  cool: '#d63c45',
  beauty: '#397fc5',
  cute: '#dc679f',
  smart: '#438f52',
  tough: '#c49a22',
};

export function pokemonMoveTypeColor(type?: string): string {
  const key = type?.trim().toLowerCase() ?? '';
  return moveTypeColors[key] ?? '#8b91a0';
}

export function pokemonContestStyleColor(style?: string): string {
  const key = style?.trim().toLowerCase() ?? '';
  return contestStyleColors[key] ?? '#8b91a0';
}

export function loadPokemonMoveStyle(moveName: string): Promise<string> {
  const key = moveName.trim().toLowerCase().replace(/\s+/g, '-');
  if (!key) {
    return Promise.resolve('');
  }

  const cached = contestStyleCache.get(key);
  if (cached) {
    return cached;
  }

  const request = fetch(`https://pokeapi.co/api/v2/move/${encodeURIComponent(key)}`)
    .then((response) => response.ok ? response.json() : Promise.reject())
    .then((data: { contest_type?: { name?: string } | null }) => {
      const style = data.contest_type?.name?.toLowerCase() ?? '';
      return contestStyleLabels[style] ?? '';
    })
    .catch(() => '');

  contestStyleCache.set(key, request);
  return request;
}
