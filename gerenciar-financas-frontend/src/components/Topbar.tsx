import { NotificationsNoneRounded } from '@mui/icons-material'
import { IconButton, Typography } from '@mui/material'

function Topbar() {
  return (
    <header className="topbar">
      <div>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Visão geral
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Acompanhe sua vida financeira.
        </Typography>
      </div>

      <div className="topbar-actions">
        <IconButton aria-label="Notificações">
          <NotificationsNoneRounded />
        </IconButton>
        <div className="user-avatar">AB</div>
      </div>
    </header>
  )
}

export default Topbar
