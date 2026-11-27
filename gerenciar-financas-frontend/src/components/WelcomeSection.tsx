import { AddRounded } from '@mui/icons-material'
import { Button, Typography } from '@mui/material'
import { useNavigate } from 'react-router-dom'

interface WelcomeSectionProps {
  userName: string
}

function WelcomeSection({ userName }: WelcomeSectionProps) {
  const navigate = useNavigate()
  return (
    <section className="welcome-section">
      <div>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Olá, {userName}! 👋
        </Typography>
        <Typography color="text.secondary">
          Veja como estão suas finanças hoje.
        </Typography>
      </div>

      <Button
        variant="contained"
        startIcon={<AddRounded />}
        className="new-transaction-button"
        onClick={() => navigate('/transacoes/nova')}
      >
        Nova transação
      </Button>
    </section>
  )
}

export default WelcomeSection
