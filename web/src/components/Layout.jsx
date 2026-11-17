import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth'

const ICONES = {
  inicio: <path d="M3 11l9-7 9 7v9a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z" />,
  alertas: <path d="M12 3a6 6 0 0 0-6 6v4l-2 3h16l-2-3V9a6 6 0 0 0-6-6zm-2 15a2 2 0 0 0 4 0" />,
  abrigos: <path d="M4 20V9l8-5 8 5v11M9 20v-6h6v6" />,
  relatos: <path d="M4 5h16v11H8l-4 4zM8 9h8M8 12h5" />,
  admin: <path d="M12 3l8 3v6c0 4.5-3.4 8.3-8 9-4.6-.7-8-4.5-8-9V6zM9 12l2 2 4-4" />,
}

function Icone({ nome }) {
  return (
    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" strokeWidth="2"
         strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{ICONES[nome]}</svg>
  )
}

export default function Layout() {
  const { usuario, sair, isAdmin } = useAuth()
  const abas = [
    { to: '/', nome: 'inicio', rotulo: 'Início', end: true },
    { to: '/alertas', nome: 'alertas', rotulo: 'Alertas' },
    { to: '/abrigos', nome: 'abrigos', rotulo: 'Abrigos' },
    { to: '/relatos', nome: 'relatos', rotulo: 'Relatos' },
    ...(isAdmin ? [{ to: '/admin', nome: 'admin', rotulo: 'Gestão' }] : []),
  ]
  return (
    <div className="app">
      <header className="topo">
        <div className="marca">
          <img src="/icone.svg" alt="" width="28" height="28" />
          <span>Alerta Cheia</span>
        </div>
        <div className="topo-usuario">
          <span className="topo-nome">{isAdmin ? 'Defesa Civil' : usuario.nome.split(' ')[0]}</span>
          <button className="btn-texto" onClick={sair}>Sair</button>
        </div>
      </header>
      <main className="conteudo">
        <Outlet />
      </main>
      <nav className="abas" aria-label="Navegação principal">
        {abas.map((a) => (
          <NavLink key={a.to} to={a.to} end={a.end} className="aba">
            <Icone nome={a.nome} />
            <span>{a.rotulo}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  )
}
