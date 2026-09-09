import { environment } from '../../environments/environment';

export const ITEMDEX_ICONS: Readonly<Record<string, string>> = {
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

export function itemCode(value: string): string {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
    .replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
}

export function localItemIcon(value: string): string | undefined {
  const file = ITEMDEX_ICONS[itemCode(value)];
  return file ? `/assets/itemdex/${file}` : undefined;
}

export function pokeApiItemIcon(value: string): string | undefined {
  const code = itemCode(value);
  return code ? `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/${code}.png` : undefined;
}

export function supabaseItemIcon(value: string): string | undefined {
  const code = itemCode(value);
  const baseUrl = environment.supabasePokemonUrl?.replace(/\/+$/, '');
  return code && baseUrl
    ? `${baseUrl}/storage/v1/object/public/pokemons/items/${code}.png`
    : undefined;
}

export function resolveItemIcon(preferred: string | null | undefined, value: string): string {
  return preferred?.trim() || supabaseItemIcon(value) || localItemIcon(value) || pokeApiItemIcon(value) || '';
}

export function nextItemIcon(current: string | null | undefined, value: string): string | undefined {
  const candidates = [supabaseItemIcon(value), localItemIcon(value), pokeApiItemIcon(value)]
    .filter((icon): icon is string => !!icon);
  const normalizedCurrent = current?.trim() ?? '';
  const currentIndex = candidates.indexOf(normalizedCurrent);
  if (currentIndex >= 0) return candidates[currentIndex + 1];
  return candidates.find((candidate) => candidate !== normalizedCurrent);
}
