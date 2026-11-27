import axios from 'axios'
import type {
  AuthenticateUserRequest,
  CreateUserRequest,
  User,
} from '../types/user'

const usersApi = axios.create({
  baseURL: '/api/users',
  headers: { 'Content-Type': 'application/json' },
})

export async function createUser(data: CreateUserRequest): Promise<User> {
  const response = await usersApi.post<User>('', data)
  return response.data
}

export async function authenticateUser(
  data: AuthenticateUserRequest,
): Promise<User> {
  const response = await usersApi.post<User>('/authenticate', data)
  return response.data
}
