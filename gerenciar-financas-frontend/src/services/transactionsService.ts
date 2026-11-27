import axios from 'axios'
import type { AuthCredentials } from '../types/user'
import type {
  AccountBalanceResponse,
  DailyExpense,
  TransactionResponse,
  TransactionType,
} from '../types/transaction'

interface TransactionFilters {
  startDate?: string
  endDate?: string
  type?: TransactionType
}

function createApi(credentials: AuthCredentials) {
  return axios.create({
    baseURL: '/api',
    auth: {
      username: credentials.email,
      password: credentials.password,
    },
    headers: {
      'Content-Type': 'application/json',
    },
  })
}

export async function getAccountBalance(
  userId: string | number,
  credentials: AuthCredentials,
): Promise<AccountBalanceResponse> {
  const api = createApi(credentials)
  const response = await api.get<AccountBalanceResponse>(
    `/accounts/${userId}/balance`,
  )
  return response.data
}

export async function getTransactions(
  credentials: AuthCredentials,
  filters: TransactionFilters = {},
): Promise<TransactionResponse[]> {
  const api = createApi(credentials)
  const response = await api.get<TransactionResponse[]>('/transactions', {
    params: filters,
  })
  return response.data
}

export async function getDailyExpenses(
  credentials: AuthCredentials,
  startDate: string,
  endDate: string,
): Promise<DailyExpense[]> {
  const api = createApi(credentials)
  const response = await api.get<DailyExpense[]>(
    '/transactions/analytics/day',
    {
      params: { startDate, endDate },
    },
  )
  return response.data
}

interface CreateTransactionRequest {
  amount: number
  description: string
}

interface CreatePurchaseRequest extends CreateTransactionRequest {
  category: string
}

interface CreateTransferRequest extends CreateTransactionRequest {
  destinationUserId: string
}

export async function createDeposit(
  credentials: AuthCredentials,
  request: CreateTransactionRequest,
): Promise<TransactionResponse> {
  const api = createApi(credentials)
  const response = await api.post<TransactionResponse>(
    '/transactions/deposits',
    request,
  )
  return response.data
}

export async function createWithdrawal(
  credentials: AuthCredentials,
  request: CreateTransactionRequest,
): Promise<TransactionResponse> {
  const api = createApi(credentials)
  const response = await api.post<TransactionResponse>(
    '/transactions/withdrawal',
    request,
  )
  return response.data
}

export async function createPurchase(
  credentials: AuthCredentials,
  request: CreatePurchaseRequest,
): Promise<TransactionResponse> {
  const api = createApi(credentials)
  const response = await api.post<TransactionResponse>(
    '/transactions/purchase',
    request,
  )
  return response.data
}

export async function createTransfer(
  credentials: AuthCredentials,
  request: CreateTransferRequest,
): Promise<TransactionResponse> {
  const api = createApi(credentials)
  const response = await api.post<TransactionResponse>(
    '/transactions/transfers',
    request,
  )
  return response.data
}
