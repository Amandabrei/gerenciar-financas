import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { Card, CardContent, Chip, Typography } from '@mui/material'

export interface MovementDay {
  dia: string
  receitas: number
  despesas: number
}

interface MovementChartProps {
  data: MovementDay[]
}

const formatCurrency = (value: number) =>
  value.toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  })

function MovementChart({ data }: MovementChartProps) {
  return (
    <Card className="chart-card">
      <CardContent>
        <div className="section-heading">
          <div>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Movimentações financeiras
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Receitas e despesas dos últimos sete dias
            </Typography>
          </div>
          <Chip label="Últimos 7 dias" variant="outlined" />
        </div>

        {data.length === 0 ? (
          <Typography color="text.secondary" variant="body2">
            Não há movimentações para exibir no período.
          </Typography>
        ) : (
          <div className="chart-container">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={data}>
                <defs>
                  <linearGradient id="receitas" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#2563eb" stopOpacity={0.2} />
                    <stop offset="95%" stopColor="#2563eb" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="despesas" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.2} />
                    <stop offset="95%" stopColor="#f59e0b" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="dia" axisLine={false} tickLine={false} />
                <YAxis
                  axisLine={false}
                  tickLine={false}
                  tickFormatter={(value) => `R$${value}`}
                />
                <Tooltip formatter={(value) => formatCurrency(Number(value))} />
                <Area
                  type="monotone"
                  dataKey="receitas"
                  name="Receitas"
                  stroke="#2563eb"
                  fill="url(#receitas)"
                  strokeWidth={2}
                />
                <Area
                  type="monotone"
                  dataKey="despesas"
                  name="Despesas"
                  stroke="#f59e0b"
                  fill="url(#despesas)"
                  strokeWidth={2}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

export default MovementChart
