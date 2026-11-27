import {
  AccountBalanceWalletRounded,
  BarChartRounded,
  DashboardRounded,
  LogoutRounded,
  SettingsRounded,
  SwapHorizRounded,
} from '@mui/icons-material'
import { Typography } from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

function Sidebar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  const initials = user?.name
    ?.trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase() || 'U'

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-icon">
          <AccountBalanceWalletRounded />
        </div>
        <Typography variant="h6">Meu Financeiro</Typography>
      </div>

      <Typography className="menu-label">MENU PRINCIPAL</Typography>

      <nav className="navigation">
        <button
          type="button"
          className={`nav-item ${location.pathname === '/' ? 'active' : ''}`}
          onClick={() => navigate('/')}
        >
          <DashboardRounded />
          Visão geral
        </button>
        <button
          type="button"
          className={`nav-item ${location.pathname.startsWith('/transacoes') ? 'active' : ''}`}
          onClick={() => navigate('/transacoes/nova')}
        >
          <SwapHorizRounded />
          Transações
        </button>
        <button type="button" className="nav-item" disabled>
          <BarChartRounded />
          Relatórios
        </button>
        <button type="button" className="nav-item" disabled>
          <SettingsRounded />
          Configurações
        </button>
      </nav>

      <div className="sidebar-footer">
        <div className="user-avatar">{initials}</div>
        <div className="sidebar-user-info">
          <strong>{user?.name}</strong>
          <span>{user?.email}</span>
        </div>
        <button
          type="button"
          className="logout-button"
          onClick={handleLogout}
          aria-label="Sair da conta"
          title="Sair da conta"
        >
          <LogoutRounded />
          <span>Sair</span>
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
