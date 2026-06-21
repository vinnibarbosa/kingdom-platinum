import { createContext } from 'react'
import type { AuthResponse } from '../types/ficha'

export interface AuthContextValue {
  user: AuthResponse['usuario'] | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (username: string, senha: string) => Promise<void>
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
