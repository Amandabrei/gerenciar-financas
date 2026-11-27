export type TransactionType =
  | 'DEPOSIT'
  | 'WITHDRAWAL'
  | 'TRANSFER'
  | 'PURCHASE'

export type TransactionStatus = 'APPROVED' | 'REJECTED'

export interface AccountBalanceResponse {
  userId: string
  balance: number
}

export interface TransactionResponse {
  id: string
  ownerId: string
  destinationUserId: string | null
  type: TransactionType
  amount: number
  createdAt: string
  description: string | null
  category: string | null
  status: TransactionStatus
  rejectionReason: string | null
  usdExchangeRate: number | null
  usdQuoteDate: string | null
  usdQuoteAvailable: boolean
  eurExchangeRate: number | null
  eurQuoteDate: string | null
  eurQuoteAvailable: boolean
  balanceBefore: number | null
  balanceAfter: number | null
}

export interface DailyExpense {
  date: string
  total: number
}
