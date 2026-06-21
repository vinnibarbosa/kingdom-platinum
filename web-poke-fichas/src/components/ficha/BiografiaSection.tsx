import type { Ficha } from '../../types/ficha'
import { Section } from './Section'

interface Props {
  biografia?: string
  editing: boolean
  onChange: (patch: Partial<Ficha>) => void
}

export function BiografiaSection({ biografia, editing, onChange }: Props) {
  return (
    <Section title="Biografia">
      {editing ? (
        <textarea
          className="ficha-textarea"
          value={biografia ?? ''}
          onChange={(e) => onChange({ biografia: e.target.value })}
          rows={6}
          placeholder="História do personagem…"
        />
      ) : biografia ? (
        <p className="biografia-text">{biografia}</p>
      ) : (
        <p className="empty-text">Nenhuma biografia registrada.</p>
      )}
    </Section>
  )
}
