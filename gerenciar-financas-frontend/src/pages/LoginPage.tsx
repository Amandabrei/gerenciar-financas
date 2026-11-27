import { useState, type FormEvent } from 'react'
import {
  Alert,
  Box,
  Button,
  IconButton,
  InputAdornment,
  Paper,
  TextField,
  Typography,
} from '@mui/material'
import {
  VisibilityRounded,
  VisibilityOffRounded,
  AccountBalanceWalletRounded,
} from '@mui/icons-material'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import axios from 'axios'
import { authenticateUser } from '../services/usersService'
import { useAuth } from '../hooks/useAuth'
import './LoginPage.css'

function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()

  const successMessage = (location.state as { message?: string } | null)?.message

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    try {
      setLoading(true)

      const credentials = {
        email: email.trim().toLowerCase(),
        password,
      }

      const user = await authenticateUser(credentials)

      login(user, credentials)
      navigate('/', { replace: true })
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const data = err.response?.data
        const message = typeof data === 'string' ? data : data?.message

        setError(
          message || 'E-mail ou senha inválidos. Confira seus dados e tente novamente.',
        )
      } else {
        setError('Não foi possível realizar o login. Tente novamente.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box className="login-page">
      <Paper className="login-card" elevation={0}>
        <Box className="login-brand">
          <Box className="login-brand-icon">
            <AccountBalanceWalletRounded />
          </Box>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>
            Meu Financeiro
          </Typography>
        </Box>

        <Typography variant="h4" sx={{ fontWeight: 700 }} className="login-title">
          Bem-vindo de volta!
        </Typography>

        <Typography color="text.secondary" className="login-subtitle">
          Acesse sua conta para gerenciar suas finanças.
        </Typography>

        {successMessage && <Alert severity="success">{successMessage}</Alert>}
        {error && <Alert severity="error">{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit} className="login-form">
          <Typography className="login-label">E-mail</Typography>
          <TextField
            fullWidth
            type="email"
            placeholder="seu@email.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />

          <Typography className="login-label">Senha</Typography>
          <TextField
            fullWidth
            type={showPassword ? 'text' : 'password'}
            placeholder="Digite sua senha"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            slotProps={{
              input: {
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={() => setShowPassword(!showPassword)}
                      edge="end"
                      aria-label="Mostrar ou ocultar senha"
                    >
                      {showPassword ? <VisibilityOffRounded /> : <VisibilityRounded />}
                    </IconButton>
                  </InputAdornment>
                ),
              },
            }}
          />

          <Button
            type="submit"
            variant="contained"
            fullWidth
            className="login-button"
            disabled={loading}
          >
            {loading ? 'Entrando...' : 'Entrar'}
          </Button>
        </Box>

        <Typography className="login-register">
          Ainda não tem uma conta?{' '}
          <Link to="/cadastro">Criar conta</Link>
        </Typography>
      </Paper>
    </Box>
  )
}

export default LoginPage
