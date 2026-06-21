import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../context/useAuth'

export function LoginPage() {
  const { login } = useAuth()
  const [username, setUsername] = useState('')
  const [senha, setSenha] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username, senha)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Falha ao entrar')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-brand">
          <span className="login-icon">⚡</span>
          <h1>Poke Fichas</h1>
          <p>Entre para visualizar e editar fichas de treinador</p>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          <label>
            Usuário
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="seu.usuario"
              autoComplete="username"
              required
            />
          </label>
          <label>
            Senha
            <input
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              placeholder="••••••••"
              autoComplete="current-password"
              required
            />
          </label>

          {error && <p className="form-error">{error}</p>}

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Entrando…' : 'Entrar'}
          </button>
        </form>

        <p className="login-hint">
          Primeira vez? Execute o bootstrap na API e use as credenciais criadas.
        </p>
        <Link to="/" className="login-back">
          ← Voltar
        </Link>
      </div>
    </div>
  )
}
