import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, ApiError } from '../api/client'
import { AnotacoesSection } from '../components/ficha/AnotacoesSection'
import { BiografiaSection } from '../components/ficha/BiografiaSection'
import { ColorModal } from '../components/ficha/ColorModal'
import { ConquistasSection, HabilidadesSection } from '../components/ficha/ExtrasSection'
import { FichaHero } from '../components/ficha/FichaHero'
import { InventorySection } from '../components/ficha/InventorySection'
import { PokemonSection } from '../components/ficha/PokemonSection'
import { RelacionadosSection } from '../components/ficha/RelacionadosSection'
import { DEFAULT_THEME, type Ficha, type ThemeColors } from '../types/ficha'
import {
  fichaToPayload,
  isTeamPokemon,
  loadTheme,
  saveTheme,
} from '../utils/ficha'

export function FichaPage() {
  const { id } = useParams<{ id: string }>()
  const fichaId = Number(id)

  const [ficha, setFicha] = useState<Ficha | null>(null)
  const [draft, setDraft] = useState<Ficha | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(false)
  const [saving, setSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState('')
  const [theme, setTheme] = useState<ThemeColors>(DEFAULT_THEME)
  const [draftTheme, setDraftTheme] = useState<ThemeColors>(DEFAULT_THEME)
  const [colorOpen, setColorOpen] = useState(false)

  useEffect(() => {
    let cancelled = false

    async function loadFicha() {
      try {
        const data = await api.getFicha(fichaId)
        if (cancelled) {
          return
        }

        const savedTheme = loadTheme(fichaId, DEFAULT_THEME)
        setFicha(data)
        setDraft(data)
        setTheme(savedTheme)
        setDraftTheme(savedTheme)
      } catch (err) {
        if (cancelled) {
          return
        }
        setError(err instanceof ApiError ? err.message : 'Ficha não encontrada')
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    loadFicha()

    return () => {
      cancelled = true
    }
  }, [fichaId])

  useEffect(() => {
    document.documentElement.style.setProperty('--color-primary', theme.primary)
    document.documentElement.style.setProperty('--color-secondary', theme.secondary)
  }, [theme])

  const team = useMemo(
    () => (draft?.pokemons ?? []).filter((p) => isTeamPokemon(p.box)),
    [draft?.pokemons],
  )
  const box = useMemo(
    () => (draft?.pokemons ?? []).filter((p) => !isTeamPokemon(p.box)),
    [draft?.pokemons],
  )

  function handleChange(patch: Partial<Ficha>) {
    setDraft((prev) => (prev ? { ...prev, ...patch } : prev))
  }

  function startEdit() {
    setDraft(ficha)
    setEditing(true)
    setSaveMessage('')
  }

  function cancelEdit() {
    setDraft(ficha)
    setEditing(false)
    setSaveMessage('')
  }

  async function save() {
    if (!draft || !fichaId) return
    setSaving(true)
    setSaveMessage('')
    try {
      const updated = await api.updateFicha(fichaId, fichaToPayload(draft))
      setFicha(updated)
      setDraft(updated)
      setEditing(false)
      setSaveMessage('Ficha salva com sucesso!')
    } catch (err) {
      setSaveMessage(err instanceof ApiError ? err.message : 'Erro ao salvar')
    } finally {
      setSaving(false)
    }
  }

  function applyTheme() {
    setTheme(draftTheme)
    saveTheme(fichaId, draftTheme)
    setColorOpen(false)
  }

  function resetTheme() {
    setDraftTheme(DEFAULT_THEME)
  }

  if (loading) {
    return (
      <div className="page-center">
        <div className="loader" />
      </div>
    )
  }

  if (error || !draft) {
    return (
      <div className="page-center">
        <p className="form-error">{error || 'Ficha indisponível'}</p>
        <Link to="/" className="btn btn-primary">
          Voltar à lista
        </Link>
      </div>
    )
  }

  return (
    <div className="ficha-page">
      <nav className="ficha-toolbar">
        <Link to="/" className="btn btn-ghost">
          ← Fichas
        </Link>
        <div className="toolbar-actions">
          {!editing ? (
            <>
              <button type="button" className="btn btn-ghost" onClick={() => setColorOpen(true)}>
                🎨 Cores
              </button>
              <button type="button" className="btn btn-primary" onClick={startEdit}>
                ✏️ Editar
              </button>
            </>
          ) : (
            <>
              <button type="button" className="btn btn-ghost" onClick={cancelEdit}>
                Cancelar
              </button>
              <button type="button" className="btn btn-primary" onClick={save} disabled={saving}>
                {saving ? 'Salvando…' : '💾 Salvar'}
              </button>
            </>
          )}
        </div>
      </nav>

      {saveMessage && (
        <p className={saveMessage.includes('sucesso') ? 'save-ok' : 'form-error'}>{saveMessage}</p>
      )}

      <main className="ficha-sheet">
        <FichaHero ficha={draft} editing={editing} onChange={handleChange} />

        <div className="ficha-body-grid">
          <div className="ficha-column">
            <RelacionadosSection relacionados={draft.relacionados ?? []} />
            <BiografiaSection
              biografia={draft.biografia}
              editing={editing}
              onChange={handleChange}
            />
            <HabilidadesSection habilidades={draft.habilidades ?? []} />
          </div>

          <div className="ficha-column">
            <PokemonSection title="Time Principal" pokemons={team} />
            <InventorySection itens={draft.itens ?? []} />
            <PokemonSection title="Box" pokemons={box} />
          </div>
        </div>

        <div className="ficha-bottom-grid">
          <AnotacoesSection
            anotacoes={draft.anotacoes}
            editing={editing}
            onChange={handleChange}
          />
          <ConquistasSection conquistas={draft.conquistas ?? []} />
        </div>
      </main>

      <ColorModal
        open={colorOpen}
        theme={draftTheme}
        onChange={setDraftTheme}
        onApply={applyTheme}
        onClose={() => setColorOpen(false)}
        onReset={resetTheme}
      />
    </div>
  )
}
