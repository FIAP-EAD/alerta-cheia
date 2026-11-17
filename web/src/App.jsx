import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth'
import Layout from './components/Layout'
import Login from './pages/Login'
import Cadastro from './pages/Cadastro'
import Inicio from './pages/Inicio'
import Alertas from './pages/Alertas'
import AlertaDetalhe from './pages/AlertaDetalhe'
import Abrigos from './pages/Abrigos'
import Relatos from './pages/Relatos'
import NovoRelato from './pages/NovoRelato'
import Admin from './pages/Admin'

function Protegida({ children, somenteAdmin = false }) {
  const { usuario, isAdmin } = useAuth()
  if (!usuario) return <Navigate to="/login" replace />
  if (somenteAdmin && !isAdmin) return <Navigate to="/" replace />
  return children
}

export default function App() {
  const { usuario } = useAuth()
  return (
    <Routes>
      <Route path="/login" element={usuario ? <Navigate to="/" replace /> : <Login />} />
      <Route path="/cadastro" element={usuario ? <Navigate to="/" replace /> : <Cadastro />} />
      <Route element={<Protegida><Layout /></Protegida>}>
        <Route index element={<Inicio />} />
        <Route path="alertas" element={<Alertas />} />
        <Route path="alertas/:id" element={<AlertaDetalhe />} />
        <Route path="abrigos" element={<Abrigos />} />
        <Route path="relatos" element={<Relatos />} />
        <Route path="relatos/novo" element={<NovoRelato />} />
        <Route path="admin" element={<Protegida somenteAdmin><Admin /></Protegida>} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
