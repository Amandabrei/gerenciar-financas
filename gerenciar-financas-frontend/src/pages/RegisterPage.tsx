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
import { Link, useNavigate } from 'react-router-dom'
import axios from 'axios'
import { createUser } from '../services/usersService'
import './LoginPage.css'

function RegisterPage() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    if (password !== confirmPassword) {
      setError('As senhas não coincidem.')
      return
    }

    if (password.length < 8) {
      setError('A senha deve ter pelo menos 8 caracteres.')
      return
    }

    try {
      setLoading(true)

      await createUser({
        name: name.trim(),
        email: email.trim().toLowerCase(),
        password,
      })

      navigate('/login', {
        state: { message: 'Conta criada com sucesso! Faça seu login.' },
      })
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const data = err.response?.data
        const message = typeof data === 'string' ? data : data?.message

        setError(
          message || 'Não foi possível criar sua conta. Verifique os dados e tente novamente.',
        )
      } else {
        setError('Ocorreu um erro inesperado ao criar sua conta.')
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
          Crie sua conta
        </Typography>

        <Typography color="text.secondary" className="login-subtitle">
          Preencha seus dados para começar a gerenciar suas finanças.
        </Typography>

        {error && <Alert severity="error">{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit} className="login-form">
          <Typography className="login-label">Nome completo</Typography>
          <TextField
            fullWidth
            placeholder="Seu nome"
            value={name}
            onChange={(event) => setName(event.target.value)}
            required
          />

          <Typography className="login-label">E-mail</Typography>
          <TextField
            fullWidth
            type="email"
            placeholder="seu@email.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
            slotProps={{ htmlInput: { maxLength: 180 } }}
          />

          <Typography className="login-label">Senha</Typography>
          <TextField
            fullWidth
            type={showPassword ? 'text' : 'password'}
            placeholder="Crie uma senha"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            slotProps={{
              htmlInput: { minLength: 8, maxLength: 100 },
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

          <Typography className="login-label">Confirmar senha</Typography>
          <TextField
            fullWidth
            type={showPassword ? 'text' : 'password'}
            placeholder="Digite a senha novamente"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            required
          />

          <Button
            type="submit"
            variant="contained"
            fullWidth
            className="login-button"
            disabled={loading}
          >
            {loading ? 'Criando conta...' : 'Criar conta'}
          </Button>
        </Box>

        <Typography className="login-register">
          Já tem uma conta? <Link to="/login">Entrar</Link>
        </Typography>
      </Paper>
    </Box>
  )
}

export default RegisterPage
