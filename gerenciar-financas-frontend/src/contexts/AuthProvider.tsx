import { useState, type ReactNode } from 'react'
import { AuthContext } from './AuthContext'
import type { AuthCredentials, User } from '../types/user'

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null)
  const [credentials, setCredentials] = useState<AuthCredentials | null>(null)

  function login(authenticatedUser: User, authCredentials: AuthCredentials) {
    setUser(authenticatedUser)
    setCredentials(authCredentials)
  }

  function logout() {
    setUser(null)
    setCredentials(null)
  }

  return (
    <AuthContext.Provider value={{ user, credentials, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
