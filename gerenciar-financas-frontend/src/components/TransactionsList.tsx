import {
  ArrowDownwardRounded,
  ArrowUpwardRounded,
  SwapHorizRounded,
} from '@mui/icons-material'
import {
  Card,
  CardContent,
  Chip,
  Typography,
} from '@mui/material'
import type { TransactionResponse } from '../types/transaction'

interface TransactionsListProps {
  transactions: TransactionResponse[]
}

const formatCurrency = (value: number) =>
  value.toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  })

const formatDate = (value: string) =>
  new Date(value).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })

const transactionLabels: Record<string, string> = {
  DEPOSIT: 'Depósito',
  WITHDRAWAL: 'Saque',
  TRANSFER: 'Transferência enviada',
  PURCHASE: 'Compra',
}

function TransactionsList({ transactions }: TransactionsListProps) {
  const latestTransactions = [...transactions]
    .sort(
      (a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    )
    .slice(0, 5)

  return (
    <Card className="transactions-card">
      <CardContent>
        <div className="section-heading">
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            Últimas transações
          </Typography>
          <Chip label={`${transactions.length} registros`} size="small" />
        </div>

        {latestTransactions.length === 0 ? (
          <Typography color="text.secondary" variant="body2">
            Nenhuma transação encontrada.
          </Typography>
        ) : (
          latestTransactions.map((transaction) => {
            const isIncome = transaction.type === 'DEPOSIT'
            const isApproved = transaction.status === 'APPROVED'

            return (
              <div className="transaction-item" key={transaction.id}>
                <div className={`transaction-icon ${isIncome ? 'income-icon' : 'expense-icon'}`}>
                  {isIncome ? <ArrowDownwardRounded /> : <ArrowUpwardRounded />}
                </div>

                <div className="transaction-info">
                  <strong>
                    {transaction.description?.trim() ||
                      transactionLabels[transaction.type] ||
                      transaction.type}
                  </strong>
                  <span>
                    {transaction.category || transactionLabels[transaction.type]}
                    {' • '}
                    {formatDate(transaction.createdAt)}
                  </span>
                  {!isApproved && (
                    <span>
                      {transaction.status === 'REJECTED' ? 'Rejeitada' : transaction.status}
                    </span>
                  )}
                </div>

                <strong className={isIncome ? 'income-value' : 'expense-value'}>
                  {isIncome ? '+' : '-'} {formatCurrency(transaction.amount)}
                </strong>
              </div>
            )
          })
        )}

        <div style={{ display: 'flex', justifyContent: 'center', marginTop: 16 }}>
          <SwapHorizRounded color="action" />
        </div>
      </CardContent>
    </Card>
  )
}

export default TransactionsList
