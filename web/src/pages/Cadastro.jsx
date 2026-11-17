import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { useToast } from '../toast'
import { useApi } from '../useApi'
import { Campo } from '../components/Estados'

function validar(f) {
  const e = {}
  if (!f.nome.trim()) e.nome = 'Informe seu nome'
  if (!/^\S+@\S+\.\S+$/.test(f.email)) e.email = 'Email inválido'
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,72}$/.test(f.senha)) e.senha = 'Mínimo de 8 caracteres, com letras e números'
  if (f.telefone && !/^\d{10,11}$/.test(f.telefone)) e.telefone = 'Use 10 ou 11 dígitos, só números'
  if (!f.bairroId) e.bairroId = 'Escolha seu bairro'
  return e
}

export default function Cadastro() {
  const { entrar } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const bairros = useApi('/bairros')
  const [form, setForm] = useState({ nome: '', email: '', senha: '', telefone: '', bairroId: '' })
  const [erros, setErros] = useState({})
  const [erroGeral, setErroGeral] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const mudar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  async function enviar(e) {
    e.preventDefault()
    const v = validar(form)
    setErros(v)
    if (Object.keys(v).length) return
    setEnviando(true)
    setErroGeral(null)
    try {
      await api.post('/auth/register', {
        ...form, email: form.email.trim(), telefone: form.telefone || null, bairroId: Number(form.bairroId),
      })
      await entrar(form.email.trim(), form.senha)
      toast('Conta criada! Você receberá os alertas do seu bairro.')
      navigate('/', { replace: true })
    } catch (err) {
      setErros(err.campos || {})
      setErroGeral(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="tela-entrada">
      <form className="cartao formulario" onSubmit={enviar} noValidate>
        <h2>Criar conta</h2>
        {erroGeral && <div className="aviso aviso-erro" role="alert">{erroGeral}</div>}
        <Campo id="nome" rotulo="Nome" erro={erros.nome}>
          <input id="nome" autoComplete="name" value={form.nome} onChange={mudar('nome')} aria-invalid={!!erros.nome} />
        </Campo>
        <Campo id="email" rotulo="Email" erro={erros.email}>
          <input id="email" type="email" inputMode="email" autoComplete="email" value={form.email}
                 onChange={mudar('email')} aria-invalid={!!erros.email} />
        </Campo>
        <Campo id="senha" rotulo="Senha" erro={erros.senha} dica="Mínimo de 8 caracteres, com letras e números">
          <input id="senha" type="password" autoComplete="new-password" value={form.senha}
                 onChange={mudar('senha')} aria-invalid={!!erros.senha} />
        </Campo>
        <Campo id="telefone" rotulo="Celular (opcional)" erro={erros.telefone} dica="Só números, com DDD">
          <input id="telefone" type="tel" inputMode="numeric" autoComplete="tel" value={form.telefone}
                 onChange={(e) => setForm({ ...form, telefone: e.target.value.replace(/\D/g, '') })} />
        </Campo>
        <Campo id="bairro" rotulo="Seu bairro" erro={erros.bairroId}>
          <select id="bairro" value={form.bairroId} onChange={mudar('bairroId')} aria-invalid={!!erros.bairroId}>
            <option value="">{bairros.carregando ? 'Carregando...' : 'Selecione'}</option>
            {bairros.dados?.map((b) => <option key={b.id} value={b.id}>{b.nome} · {b.cidade}</option>)}
          </select>
        </Campo>
        {bairros.erro && <div className="aviso aviso-erro">Não foi possível carregar os bairros.</div>}
        <button className="btn btn-primario" disabled={enviando}>{enviando ? 'Criando...' : 'Criar conta'}</button>
        <p className="centro">Já tem conta? <Link to="/login" className="link">Entrar</Link></p>
      </form>
    </div>
  )
}
