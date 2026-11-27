import { useState, type FormEvent } from 'react'
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  MenuItem,
  Paper,
  TextField,
  Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import Sidebar from '../components/Sidebar'
import Topbar from '../components/Topbar'
import { useAuth } from '../hooks/useAuth'
import {
  createDeposit,
  createPurchase,
  createTransfer,
  createWithdrawal,
} from '../services/transactionsService'
import type { TransactionResponse, TransactionType } from '../types/transaction'
import '../App.css'

const options: { value: TransactionType; label: string }[] = [
  { value: 'DEPOSIT', label: 'Depósito' },
  { value: 'WITHDRAWAL', label: 'Saque' },
  { value: 'PURCHASE', label: 'Compra' },
  { value: 'TRANSFER', label: 'Transferência' },
]

function NewTransactionPage() {
  const { credentials } = useAuth()
  const navigate = useNavigate()
  const [type, setType] = useState<TransactionType>('DEPOSIT')
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('')
  const [destinationUserId, setDestinationUserId] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState<TransactionResponse | null>(null)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setResult(null)

    const numericAmount = Number(amount)

    if (!Number.isFinite(numericAmount) || numericAmount <= 0) {
      setError('Informe um valor maior que zero.')
      return
    }

    if (!description.trim()) {
      setError('Informe uma descrição para a transação.')
      return
    }

    if (type === 'PURCHASE' && !category.trim()) {
      setError('Informe a categoria da compra.')
      return
    }

    if (type === 'TRANSFER' && !destinationUserId.trim()) {
      setError('Informe o ID do usuário destinatário.')
      return
    }

    if (!credentials) {
      setError('Sua sessão expirou. Entre novamente.')
      return
    }

    setLoading(true)

    try {
      const request = {
        amount: numericAmount,
        description: description.trim(),
      }

      let response: TransactionResponse

      if (type === 'DEPOSIT') {
        response = await createDeposit(credentials, request)
      } else if (type === 'WITHDRAWAL') {
        response = await createWithdrawal(credentials, request)
      } else if (type === 'PURCHASE') {
        response = await createPurchase(credentials, {
          ...request,
          category: category.trim(),
        })
      } else {
        response = await createTransfer(credentials, {
          ...request,
          destinationUserId: destinationUserId.trim(),
        })
      }

      setResult(response)

      if (response.status === 'APPROVED') {
        setAmount('')
        setDescription('')
        setCategory('')
        setDestinationUserId('')
      }
    } catch {
      setError(
        'Não foi possível registrar a transação. Confira os dados e tente novamente.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box className="app-container">
      <Sidebar />

      <main className="main-content">
        <Topbar />

        <section className="transaction-page">
          <div className="transaction-page-heading">
            <Typography variant="h5" sx={{ fontWeight: 700 }}>
              Nova transação
            </Typography>
            <Typography color="text.secondary">
              Registre depósitos, saques, compras ou transferências.
            </Typography>
          </div>

          <Paper className="transaction-form-card" elevation={0}>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 3 }}>
              Dados da transação
            </Typography>

            <Box
              component="form"
              className="transaction-form"
              onSubmit={handleSubmit}
            >
              <TextField
                select
                label="Tipo de transação"
                value={type}
                onChange={(event) => {
                  setType(event.target.value as TransactionType)
                  setError('')
                  setResult(null)
                }}
                fullWidth
              >
                {options.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </TextField>

              {type === 'TRANSFER' && (
                <TextField
                  label="ID do usuário destinatário"
                  value={destinationUserId}
                  onChange={(event) => setDestinationUserId(event.target.value)}
                  fullWidth
                  required
                  helperText="Informe o UUID da conta que receberá a transferência."
                />
              )}

              <TextField
                label="Valor (R$)"
                type="number"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                slotProps={{ htmlInput: { min: '0.01', step: '0.01' } }}
                fullWidth
                required
              />

              <TextField
                label="Descrição"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                slotProps={{ htmlInput: { maxLength: 500 } }}
                fullWidth
                required
                multiline
                minRows={2}
              />

              {type === 'PURCHASE' && (
                <TextField
                  label="Categoria"
                  value={category}
                  onChange={(event) => setCategory(event.target.value)}
                  slotProps={{ htmlInput: { maxLength: 100 } }}
                  fullWidth
                  required
                  placeholder="Ex.: Alimentação, Transporte, Saúde"
                />
              )}

              {error && <Alert severity="error">{error}</Alert>}

              {result && (
                <Alert severity={result.status === 'APPROVED' ? 'success' : 'warning'}>
                  <strong>
                    {result.status === 'APPROVED'
                      ? 'Transação aprovada!'
                      : 'Transação não aprovada.'}
                  </strong>

                  {result.rejectionReason && (
                    <Typography variant="body2" sx={{ mt: 0.5 }}>
                      Motivo: {result.rejectionReason}
                    </Typography>
                  )}

                  {result.status === 'APPROVED' && result.balanceAfter !== null && (
                    <Typography variant="body2" sx={{ mt: 0.5 }}>
                      Saldo após a operação:{' '}
                      {result.balanceAfter.toLocaleString('pt-BR', {
                        style: 'currency',
                        currency: 'BRL',
                      })}
                    </Typography>
                  )}
                </Alert>
              )}

              <Box className="transaction-form-actions">
                <Button
                  type="button"
                  variant="outlined"
                  onClick={() => navigate('/')}
                  disabled={loading}
                >
                  Voltar ao painel
                </Button>

                <Button
                  type="submit"
                  variant="contained"
                  disabled={loading}
                  startIcon={
                    loading
                      ? <CircularProgress size={18} color="inherit" />
                      : undefined
                  }
                >
                  {loading ? 'Registrando...' : 'Registrar transação'}
                </Button>
              </Box>
            </Box>
          </Paper>
        </section>

        <footer className="page-footer">
          <Typography variant="caption" color="text.secondary">
            Meu Financeiro • Projeto de Gestão Financeira
          </Typography>
        </footer>
      </main>
    </Box>
  )
}

export default NewTransactionPage
