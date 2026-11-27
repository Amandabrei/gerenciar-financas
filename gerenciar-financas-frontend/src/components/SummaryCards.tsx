import {
  AccountBalanceWalletRounded,
  ArrowDownwardRounded,
  ArrowUpwardRounded,
} from '@mui/icons-material'
import { Card, CardContent, Chip, Typography } from '@mui/material'

interface SummaryCardsProps {
  balance: number
  income: number
  expenses: number
}

const formatCurrency = (value: number) =>
  value.toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  })

function SummaryCards({ balance, income, expenses }: SummaryCardsProps) {
  return (
    <section className="summary-grid">
      <Card className="summary-card balance-card">
        <CardContent>
          <div className="card-heading">
            <span>Saldo disponível</span>
            <AccountBalanceWalletRounded />
          </div>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>
            {formatCurrency(balance)}
          </Typography>
          <Chip label="Saldo atual" size="small" />
        </CardContent>
      </Card>

      <Card className="summary-card">
        <CardContent>
          <div className="card-heading">
            <span>Receitas</span>
            <div className="metric-icon income-icon">
              <ArrowDownwardRounded />
            </div>
          </div>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>
            {formatCurrency(income)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Total de entradas aprovadas
          </Typography>
        </CardContent>
      </Card>

      <Card className="summary-card">
        <CardContent>
          <div className="card-heading">
            <span>Despesas</span>
            <div className="metric-icon expense-icon">
              <ArrowUpwardRounded />
            </div>
          </div>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>
            {formatCurrency(expenses)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Total de saídas aprovadas
          </Typography>
        </CardContent>
      </Card>
    </section>
  )
}

export default SummaryCards
