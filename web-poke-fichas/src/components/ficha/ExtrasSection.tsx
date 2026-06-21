import type { FichaConquista, FichaHabilidade } from '../../types/ficha'
import { Section } from './Section'

export function HabilidadesSection({ habilidades }: { habilidades: FichaHabilidade[] }) {
  if (habilidades.length === 0) return null

  return (
    <Section title="Habilidades">
      <div className="habilidades-list">
        {habilidades.map((h, i) => (
          <div key={h.id ?? i} className="habilidade-item">
            <strong>{h.nome}</strong>
            {h.descricao && <p>{h.descricao}</p>}
          </div>
        ))}
      </div>
    </Section>
  )
}

export function ConquistasSection({ conquistas }: { conquistas: FichaConquista[] }) {
  return (
    <Section title="Anotações e Conquistas">
      {conquistas.length === 0 ? (
        <p className="empty-text">Nenhuma conquista registrada.</p>
      ) : (
        <div className="conquistas-list">
          {conquistas.map((c, i) => (
            <div key={c.id ?? i} className="conquista-item">
              <span className="conquista-type">{c.tipo}</span>
              <strong>{c.nome}</strong>
              {c.dataConquista && (
                <time>{new Date(c.dataConquista).toLocaleDateString('pt-BR')}</time>
              )}
            </div>
          ))}
        </div>
      )}
    </Section>
  )
}
