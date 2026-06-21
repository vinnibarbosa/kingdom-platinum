import type { FichaRelacionado } from '../../types/ficha'
import { Section } from './Section'

interface Props {
  relacionados: FichaRelacionado[]
}

export function RelacionadosSection({ relacionados }: Props) {
  return (
    <Section title="Relacionados">
      {relacionados.length === 0 ? (
        <p className="empty-text">Nenhum relacionado registrado.</p>
      ) : (
        <div className="relacionados-grid">
          {relacionados.map((r, i) => (
            <div key={r.id ?? i} className="relacionado-chip">
              <strong>{r.nome}</strong>
              <span>{r.relacao}</span>
            </div>
          ))}
        </div>
      )}
    </Section>
  )
}
