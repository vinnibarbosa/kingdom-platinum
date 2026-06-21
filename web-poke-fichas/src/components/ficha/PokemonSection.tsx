import type { FichaPokemon } from '../../types/ficha'
import { Section } from './Section'

interface Props {
  title: string
  pokemons: FichaPokemon[]
}

export function PokemonSection({ title, pokemons }: Props) {
  return (
    <Section title={title}>
      {pokemons.length === 0 ? (
        <p className="empty-text">Nenhum Pokémon nesta seção.</p>
      ) : (
        <div className="pokemon-grid">
          {pokemons.map((p, i) => (
            <article key={p.id ?? i} className="pokemon-card">
              <div className="pokemon-card-head">
                <div className="pokemon-sprite">{p.especie.charAt(0)}</div>
                <div>
                  <h4>{p.apelido || p.especie}</h4>
                  <p className="muted">{p.especie}</p>
                </div>
                {p.pokebola && <span className="pokeball-tag">{p.pokebola}</span>}
              </div>

              <div className="pokemon-stats-mini">
                {[
                  ['HP', p.hp],
                  ['ATK', p.atk],
                  ['DEF', p.def],
                  ['SpA', p.satk],
                  ['SpD', p.sdef],
                  ['SPE', p.speed],
                ].map(([label, val]) => (
                  <div key={label as string}>
                    <span>{label}</span>
                    <strong>{val ?? '—'}</strong>
                  </div>
                ))}
              </div>

              {(p.ability || p.nature || p.holdItem) && (
                <div className="pokemon-traits">
                  {p.ability && <span>Ability: {p.ability}</span>}
                  {p.nature && <span>Nature: {p.nature}</span>}
                  {p.holdItem && <span>Item: {p.holdItem}</span>}
                </div>
              )}

              {p.movimentos && p.movimentos.length > 0 && (
                <ul className="move-list">
                  {p.movimentos.map((m, j) => (
                    <li key={m.id ?? j}>
                      {m.nome}
                      {m.categoria && <em>{m.categoria}</em>}
                    </li>
                  ))}
                </ul>
              )}
            </article>
          ))}
        </div>
      )}
    </Section>
  )
}
