import type { Ficha } from '../../types/ficha'
import { Section } from './Section'

interface Props {
  anotacoes?: string
  editing: boolean
  onChange: (patch: Partial<Ficha>) => void
}

export function AnotacoesSection({ anotacoes, editing, onChange }: Props) {
  return (
    <Section title="Anotações">
      {editing ? (
        <textarea
          className="ficha-textarea"
          value={anotacoes ?? ''}
          onChange={(e) => onChange({ anotacoes: e.target.value })}
          rows={5}
          placeholder="Notas da campanha…"
        />
      ) : anotacoes ? (
        <p className="biografia-text">{anotacoes}</p>
      ) : (
        <p className="empty-text">Nenhuma anotação registrada.</p>
      )}
    </Section>
  )
}
