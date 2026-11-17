import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { useToast } from '../toast'
import { useApi } from '../useApi'
import { Campo } from '../components/Estados'

const MAX = 500

export default function NovoRelato() {
  const { usuario } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const bairros = useApi('/bairros')
  const [bairroId, setBairroId] = useState(String(usuario.bairroId))
  const [descricao, setDescricao] = useState('')
  const [erro, setErro] = useState(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(e) {
    e.preventDefault()
    if (descricao.trim().length < 5) return setErro('Descreva com pelo menos 5 caracteres.')
    setEnviando(true)
    setErro(null)
    try {
      await api.post('/relatos', { bairroId: Number(bairroId), descricao: descricao.trim() })
      toast('Relato enviado. Obrigado por ajudar!')
      navigate('/relatos')
    } catch (err) {
      setErro(err.status === 0 ? 'Sem conexão. Seu texto continua aqui, tente de novo.' : err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section>
      <Link to="/relatos" className="link voltar">← Relatos</Link>
      <form className="cartao formulario" onSubmit={enviar} noValidate>
        <h1 className="pagina-titulo">Novo relato</h1>
        {erro && <div className="aviso aviso-erro" role="alert">{erro}</div>}
        <Campo id="bairro" rotulo="Bairro">
          <select id="bairro" value={bairroId} onChange={(e) => setBairroId(e.target.value)}>
            {bairros.dados
              ? bairros.dados.map((b) => <option key={b.id} value={b.id}>{b.nome}</option>)
              : <option value={usuario.bairroId}>{usuario.bairroNome}</option>}
          </select>
        </Campo>
        <Campo id="descricao" rotulo="O que está acontecendo?" dica={`${descricao.length}/${MAX}`}>
          <textarea id="descricao" rows={5} maxLength={MAX} value={descricao}
                    placeholder="Ex.: a água já passou da calçada na Rua A"
                    onChange={(e) => setDescricao(e.target.value)} />
        </Campo>
        <button className="btn btn-primario" disabled={enviando}>{enviando ? 'Enviando...' : 'Enviar relato'}</button>
      </form>
    </section>
  )
}
