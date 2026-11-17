import { Link } from 'react-router-dom'
import { api, textoSeguro } from '../api'
import { useToast } from '../toast'
import { useApi } from '../useApi'
import { dataHora } from '../formatos'
import { AvisoConexao, Carregando, Vazio } from '../components/Estados'

const ROTULO = { ABERTO: 'Em análise', VERIFICADO: 'Verificado', DESCARTADO: 'Descartado' }
const CLASSE = { ABERTO: 'atencao', VERIFICADO: 'normal', DESCARTADO: 'encerrado' }

export function CartaoRelato({ relato, children }) {
  return (
    <article className="cartao">
      <div className="cartao-linha">
        <span className={`selo selo-${CLASSE[relato.status]}`}>{ROTULO[relato.status]}</span>
        <time className="texto-suave">{dataHora(relato.criadoEm)}</time>
      </div>
      <p className="cartao-texto">{textoSeguro(relato.descricao)}</p>
      <span className="texto-suave">{relato.bairroNome} · {relato.usuarioNome}</span>
      {children}
    </article>
  )
}

export default function Relatos() {
  const toast = useToast()
  const r = useApi('/relatos')

  async function excluir(id) {
    if (!confirm('Excluir este relato?')) return
    try {
      await api.del(`/relatos/${id}`)
      toast('Relato excluído.')
      r.recarregar()
    } catch (e) {
      toast(e.message, 'erro')
    }
  }

  return (
    <section>
      <div className="cartao-linha">
        <h1 className="pagina-titulo">Relatos</h1>
        <Link to="/relatos/novo" className="btn btn-primario btn-pequeno">+ Novo</Link>
      </div>
      <p className="texto-suave">O que você relata ajuda a Defesa Civil a confirmar o que os sensores mostram.</p>
      <AvisoConexao {...r} temDados={!!r.dados} onTentar={r.recarregar} />
      {r.carregando ? <Carregando />
        : r.dados?.length === 0 ? <Vazio>Você ainda não enviou relatos.</Vazio>
        : r.dados?.map((rel) => (
          <CartaoRelato key={rel.id} relato={rel}>
            <button className="btn-texto perigo" onClick={() => excluir(rel.id)}>Excluir</button>
          </CartaoRelato>
        ))}
    </section>
  )
}
