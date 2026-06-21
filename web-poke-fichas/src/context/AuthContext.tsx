import {
  useCallback,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { api } from '../api/client'
import type { AuthResponse } from '../types/ficha'
import { AuthContext } from './auth-context'

function getStoredUser() {
  const stored = localStorage.getItem('user')
  if (!stored) {
    return null
  }

  try {
    return JSON.parse(stored) as AuthResponse['usuario']
  } catch {
    localStorage.removeItem('user')
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthResponse['usuario'] | null>(() => getStoredUser())
  const [isLoading] = useState(false)

  const login = useCallback(async (username: string, senha: string) => {
    const response = await api.login(username, senha)
    localStorage.setItem('accessToken', response.accessToken)
    localStorage.setItem('user', JSON.stringify(response.usuario))
    setUser(response.usuario)
  }, [])

  const logout = useCallback(async () => {
    try {
      await api.logout()
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      setUser(null)
    }
  }, [])

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: !!user,
      isLoading,
      login,
      logout,
    }),
    [user, isLoading, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
