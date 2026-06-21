import { DEFAULT_THEME, type ThemeColors } from '../../types/ficha'

interface Props {
  open: boolean
  theme: ThemeColors
  onChange: (theme: ThemeColors) => void
  onApply: () => void
  onClose: () => void
  onReset: () => void
}

export function ColorModal({ open, theme, onChange, onApply, onClose, onReset }: Props) {
  if (!open) return null

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <h2>🎨 Personalizar Cores</h2>
        <p className="muted">Cabeçalho, dados e títulos de seção</p>

        <label className="color-field">
          Cor primária
          <div className="color-input-row">
            <input
              type="color"
              value={theme.primary}
              onChange={(e) => onChange({ ...theme, primary: e.target.value })}
            />
            <input
              type="text"
              value={theme.primary}
              onChange={(e) => onChange({ ...theme, primary: e.target.value })}
            />
          </div>
        </label>

        <label className="color-field">
          Cor secundária
          <div className="color-input-row">
            <input
              type="color"
              value={theme.secondary}
              onChange={(e) => onChange({ ...theme, secondary: e.target.value })}
            />
            <input
              type="text"
              value={theme.secondary}
              onChange={(e) => onChange({ ...theme, secondary: e.target.value })}
            />
          </div>
        </label>

        <div className="modal-actions">
          <button type="button" className="btn btn-ghost" onClick={onReset}>
            ↺ Padrão ({DEFAULT_THEME.primary})
          </button>
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Cancelar
          </button>
          <button type="button" className="btn btn-primary" onClick={onApply}>
            Aplicar
          </button>
        </div>
      </div>
    </div>
  )
}
