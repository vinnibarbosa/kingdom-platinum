import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import { useAuth } from '../context/useAuth'
import type { FichaResumo } from '../types/ficha'

export function FichaListPage() {
  const { user, logout } = useAuth()
  const [fichas, setFichas] = useState<FichaResumo[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .listFichas()
      .then((page) => setFichas(page.content))
      .catch(() => setError('Não foi possível carregar as fichas'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="list-page">
      <header className="list-header">
        <div>
          <h1>Fichas de Treinador</h1>
          <p className="muted">Olá, {user?.nome ?? user?.username}</p>
        </div>
        <button type="button" className="btn btn-ghost" onClick={() => logout()}>
          Sair
        </button>
      </header>

      {loading && (
        <div className="page-center">
          <div className="loader" />
        </div>
      )}

      {error && <p className="form-error">{error}</p>}

      {!loading && !error && fichas.length === 0 && (
        <div className="empty-state">
          <p>Nenhuma ficha cadastrada ainda.</p>
          <p className="muted">Crie uma ficha pela API ou Swagger UI.</p>
        </div>
      )}

      <div className="ficha-grid">
        {fichas.map((ficha) => (
          <Link key={ficha.id} to={`/ficha/${ficha.id}`} className="ficha-card-link">
            <article className="ficha-card-preview">
              <div className="ficha-card-avatar">{ficha.nome.charAt(0).toUpperCase()}</div>
              <div>
                <h2>{ficha.nome}</h2>
                <p className="muted">
                  {[ficha.ocupacao, ficha.classePersonagem, ficha.player]
                    .filter(Boolean)
                    .join(' · ') || 'Sem detalhes'}
                </p>
                {ficha.pontos != null && (
                  <span className="badge">{ficha.pontos} pts</span>
                )}
              </div>
            </article>
          </Link>
        ))}
      </div>
    </div>
  )
}
