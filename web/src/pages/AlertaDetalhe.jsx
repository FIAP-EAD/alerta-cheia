import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { useToast } from '../toast'
import { useApi } from '../useApi'
import { dataHora } from '../formatos'
import { AvisoConexao, Carregando } from '../components/Estados'
import CartaoAbrigo from '../components/CartaoAbrigo'

export default function AlertaDetalhe() {
  const { id } = useParams()
  const { isAdmin } = useAuth()
  const toast = useToast()
  const alerta = useApi(`/alertas/${id}`)
  const abrigos = useApi(alerta.dados ? `/abrigos?bairroId=${alerta.dados.bairroId}&somenteComVaga=true` : null)
  const [encerrando, setEncerrando] = useState(false)

  async function encerrar() {
    if (!confirm('Encerrar este alerta? Os moradores deixarão de vê-lo como ativo.')) return
    setEncerrando(true)
    try {
      await api.put(`/alertas/${id}/encerrar`)
      toast('Alerta encerrado.')
      alerta.recarregar()
    } catch (e) {
      toast(e.message, 'erro')
    } finally {
      setEncerrando(false)
    }
  }

  if (alerta.carregando) return <Carregando />
  if (!alerta.dados) return <AvisoConexao {...alerta} onTentar={alerta.recarregar} />
  const a = alerta.dados
  const classe = a.status === 'ENCERRADO' ? 'encerrado' : a.severidade.toLowerCase()

  return (
    <section>
      <Link to="/alertas" className="link voltar">← Alertas</Link>
      <article className={`cartao borda-${classe}`}>
        <span className={`selo selo-${classe}`}>
          {a.status === 'ENCERRADO' ? 'Encerrado' : a.severidade === 'CRITICO' ? 'Crítico' : 'Atenção'}
        </span>
        <h1 className="pagina-titulo">{a.bairroNome}</h1>
        <p className="cartao-texto grande">{a.mensagem}</p>
        <p className="texto-suave">Emitido em {dataHora(a.criadoEm)}</p>
        {isAdmin && a.status === 'ATIVO' && (
          <button className="btn btn-secundario" onClick={encerrar} disabled={encerrando}>
            {encerrando ? 'Encerrando...' : 'Encerrar alerta'}
          </button>
        )}
      </article>

      <h2 className="secao-titulo">Abrigos com vaga no bairro</h2>
      {abrigos.carregando ? <Carregando />
        : abrigos.dados?.length ? abrigos.dados.map((ab) => <CartaoAbrigo key={ab.id} abrigo={ab} />)
        : <p className="texto-suave">Nenhum abrigo com vaga neste bairro. <Link to="/abrigos" className="link">Ver todos</Link></p>}
    </section>
  )
}
