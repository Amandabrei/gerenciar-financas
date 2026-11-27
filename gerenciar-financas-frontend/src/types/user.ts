export interface User {
  id: number | string
  name: string
  email: string
}

export interface CreateUserRequest {
  name: string
  email: string
  password: string
}

export interface AuthenticateUserRequest {
  email: string
  password: string
}

export interface AuthCredentials {
  email: string
  password: string
}
