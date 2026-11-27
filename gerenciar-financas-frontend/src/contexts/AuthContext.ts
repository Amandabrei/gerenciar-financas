import { createContext } from 'react'
import type { AuthCredentials, User } from '../types/user'

export interface AuthContextValue {
  user: User | null
  credentials: AuthCredentials | null
  login: (user: User, credentials: AuthCredentials) => void
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
