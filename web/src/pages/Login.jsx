import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import { Campo } from '../components/Estados'

export default function Login() {
  const { entrar } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', senha: '' })
  const [erro, setErro] = useState(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(e) {
    e.preventDefault()
    if (!form.email || !form.senha) return setErro('Preencha email e senha.')
    setEnviando(true)
    setErro(null)
    try {
      await entrar(form.email.trim(), form.senha)
      navigate('/', { replace: true })
    } catch (err) {
      setErro(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="tela-entrada">
      <div className="entrada-marca">
        <img src="/icone.svg" alt="" width="64" height="64" />
        <h1>Alerta Cheia</h1>
        <p>O alerta chega antes da água.</p>
      </div>
      <form className="cartao formulario" onSubmit={enviar} noValidate>
        <h2>Entrar</h2>
        {erro && <div className="aviso aviso-erro" role="alert">{erro}</div>}
        <Campo id="email" rotulo="Email">
          <input id="email" type="email" autoComplete="email" inputMode="email" value={form.email}
                 onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </Campo>
        <Campo id="senha" rotulo="Senha">
          <input id="senha" type="password" autoComplete="current-password" value={form.senha}
                 onChange={(e) => setForm({ ...form, senha: e.target.value })} />
        </Campo>
        <button className="btn btn-primario" disabled={enviando}>{enviando ? 'Entrando...' : 'Entrar'}</button>
        <p className="centro">Não tem conta? <Link to="/cadastro" className="link">Cadastre-se</Link></p>
      </form>
    </div>
  )
}
