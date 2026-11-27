import { useEffect, useState } from 'react'
import { Alert, Box, CircularProgress, Typography } from '@mui/material'
import Sidebar from '../components/Sidebar'
import SummaryCards from '../components/SummaryCards'
import MovementChart, { type MovementDay } from '../components/MovementChart'
import TransactionsList from '../components/TransactionsList'
import Topbar from '../components/Topbar'
import WelcomeSection from '../components/WelcomeSection'
import { useAuth } from '../hooks/useAuth'
import {
  getAccountBalance,
  getDailyExpenses,
  getTransactions,
} from '../services/transactionsService'
import type { TransactionResponse } from '../types/transaction'
import '../App.css'

interface DashboardData {
  balance: number
  income: number
  expenses: number
  transactions: TransactionResponse[]
  movement: MovementDay[]
}

function getLocalDateString(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function DashboardPage() {
  const { user, credentials } = useAuth()
  const [dashboard, setDashboard] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true

    async function loadDashboard() {
      if (!user || !credentials) {
        setError('Não foi possível identificar a sessão. Entre novamente.')
        setLoading(false)
        return
      }

      setLoading(true)
      setError('')

      try {
        const today = new Date()
        const endDate = getLocalDateString(today)
        const firstDay = new Date(today)
        firstDay.setDate(today.getDate() - 6)
        const startDate = getLocalDateString(firstDay)

        const [balanceResponse, transactions, dailyExpenses] = await Promise.all([
          getAccountBalance(user.id, credentials),
          getTransactions(credentials),
          getDailyExpenses(credentials, startDate, endDate),
        ])

        if (!active) return

        const approvedTransactions = transactions.filter(
          (transaction) => transaction.status === 'APPROVED',
        )

        const income = approvedTransactions
          .filter((transaction) => transaction.type === 'DEPOSIT')
          .reduce((total, transaction) => total + transaction.amount, 0)

        const expenses = approvedTransactions
          .filter((transaction) => transaction.type !== 'DEPOSIT')
          .reduce((total, transaction) => total + transaction.amount, 0)

        const dailyExpenseMap = new Map(
          dailyExpenses.map((expense) => [expense.date, expense.total]),
        )

        const dailyIncomeMap = new Map<string, number>()

        approvedTransactions
          .filter((transaction) => transaction.type === 'DEPOSIT')
          .forEach((transaction) => {
            const date = transaction.createdAt.slice(0, 10)
            if (date >= startDate && date <= endDate) {
              dailyIncomeMap.set(
                date,
                (dailyIncomeMap.get(date) ?? 0) + transaction.amount,
              )
            }
          })

        const movement: MovementDay[] = []
        const cursor = new Date(firstDay)

        for (let index = 0; index < 7; index += 1) {
          const date = getLocalDateString(cursor)
          movement.push({
            dia: cursor.toLocaleDateString('pt-BR', { weekday: 'short' }),
            receitas: dailyIncomeMap.get(date) ?? 0,
            despesas: dailyExpenseMap.get(date) ?? 0,
          })
          cursor.setDate(cursor.getDate() + 1)
        }

        setDashboard({
          balance: balanceResponse.balance,
          income,
          expenses,
          transactions,
          movement,
        })
      } catch {
        if (active) {
          setError(
            'Não foi possível carregar os dados financeiros. Verifique se as APIs estão disponíveis e tente novamente.',
          )
        }
      } finally {
        if (active) setLoading(false)
      }
    }

    void loadDashboard()

    return () => {
      active = false
    }
  }, [user, credentials])

  return (
    <Box className="app-container">
      <Sidebar />

      <main className="main-content">
        <Topbar />

        <WelcomeSection userName={user?.name ?? 'usuário'} />

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
            <CircularProgress />
          </Box>
        ) : dashboard ? (
          <>
            <SummaryCards
              balance={dashboard.balance}
              income={dashboard.income}
              expenses={dashboard.expenses}
            />

            <section className="dashboard-grid">
              <MovementChart data={dashboard.movement} />
              <TransactionsList transactions={dashboard.transactions} />
            </section>
          </>
        ) : null}

        <footer className="page-footer">
          <Typography variant="caption" color="text.secondary">
            Meu Financeiro • Projeto de Gestão Financeira
          </Typography>
        </footer>
      </main>
    </Box>
  )
}

export default DashboardPage
