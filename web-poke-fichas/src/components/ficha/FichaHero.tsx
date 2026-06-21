import type { Ficha } from '../../types/ficha'
import { displayOrDash, formatMoney } from '../../utils/ficha'

interface Props {
  ficha: Ficha
  editing: boolean
  onChange: (patch: Partial<Ficha>) => void
}

export function FichaHero({ ficha, editing, onChange }: Props) {
  return (
    <>
      <div className="ficha-banner">
        <div className="ficha-banner-overlay" />
        <span className="ficha-banner-hint">Banner do treinador</span>
      </div>

      <div className="ficha-profile-row">
        <div className="ficha-avatar">
          <span>{ficha.nome.charAt(0).toUpperCase()}</span>
        </div>

        <div className="ficha-identity">
          {editing ? (
            <>
              <input
                className="ficha-name-input"
                value={ficha.nome}
                onChange={(e) => onChange({ nome: e.target.value })}
              />
              <input
                className="ficha-frase-input"
                value={ficha.frase ?? ''}
                onChange={(e) => onChange({ frase: e.target.value })}
                placeholder="Frase ou slogan do personagem"
              />
            </>
          ) : (
            <>
              <h1>{ficha.nome}</h1>
              {ficha.frase && <p className="ficha-frase">{ficha.frase}</p>}
            </>
          )}
        </div>
      </div>

      <div className="ficha-quick-grid">
        <StatField
          label="Idade"
          value={ficha.idade}
          editing={editing}
          onChange={(v) => onChange({ idade: v ? Number(v) : undefined })}
        />
        <StatField
          label="Ocupação"
          value={ficha.ocupacao}
          editing={editing}
          onChange={(v) => onChange({ ocupacao: v })}
        />
        <StatField
          label="Origem"
          value={ficha.naturalidade}
          editing={editing}
          onChange={(v) => onChange({ naturalidade: v })}
        />
      </div>

      <div className="ficha-stats-row">
        <div className="stat-pill">
          <span className="stat-label">C$</span>
          {editing ? (
            <input
              type="number"
              min={0}
              step="0.01"
              value={ficha.dinheiro ?? ''}
              onChange={(e) =>
                onChange({ dinheiro: e.target.value ? Number(e.target.value) : undefined })
              }
            />
          ) : (
            <strong>{formatMoney(ficha.dinheiro)}</strong>
          )}
        </div>
        <div className="stat-pill">
          <span className="stat-label">Reputação</span>
          {editing ? (
            <input
              type="number"
              min={0}
              value={ficha.reputacao ?? ''}
              onChange={(e) =>
                onChange({ reputacao: e.target.value ? Number(e.target.value) : undefined })
              }
            />
          ) : (
            <strong>{displayOrDash(ficha.reputacao)}</strong>
          )}
        </div>
        <div className="stat-pill">
          <span className="stat-label">PV</span>
          {editing ? (
            <input
              type="number"
              min={0}
              value={ficha.pontosVida ?? ''}
              onChange={(e) =>
                onChange({ pontosVida: e.target.value ? Number(e.target.value) : undefined })
              }
            />
          ) : (
            <strong>{displayOrDash(ficha.pontosVida)}</strong>
          )}
        </div>
        <div className="stat-pill">
          <span className="stat-label">Pontos</span>
          {editing ? (
            <input
              type="number"
              min={0}
              value={ficha.pontos ?? ''}
              onChange={(e) =>
                onChange({ pontos: e.target.value ? Number(e.target.value) : undefined })
              }
            />
          ) : (
            <strong>{displayOrDash(ficha.pontos)}</strong>
          )}
        </div>
      </div>

      <div className="ficha-meta-row">
        <MetaBadge
          label="Photoplayer"
          value={ficha.photoplayer}
          editing={editing}
          onChange={(v) => onChange({ photoplayer: v })}
        />
        <MetaBadge
          label="Player"
          value={ficha.player}
          editing={editing}
          onChange={(v) => onChange({ player: v })}
        />
        <MetaBadge
          label="Equipe"
          value={ficha.equipe}
          editing={editing}
          onChange={(v) => onChange({ equipe: v })}
        />
      </div>
    </>
  )
}

function StatField({
  label,
  value,
  editing,
  onChange,
}: {
  label: string
  value?: string | number | null
  editing: boolean
  onChange: (value: string) => void
}) {
  return (
    <div className="quick-stat">
      <span>{label}</span>
      {editing ? (
        <input
          value={value ?? ''}
          onChange={(e) => onChange(e.target.value)}
        />
      ) : (
        <strong>{displayOrDash(value)}</strong>
      )}
    </div>
  )
}

function MetaBadge({
  label,
  value,
  editing,
  onChange,
}: {
  label: string
  value?: string
  editing: boolean
  onChange: (value: string) => void
}) {
  return (
    <div className="meta-badge">
      <span>{label}</span>
      {editing ? (
        <input value={value ?? ''} onChange={(e) => onChange(e.target.value)} />
      ) : (
        <strong>{displayOrDash(value)}</strong>
      )}
    </div>
  )
}
