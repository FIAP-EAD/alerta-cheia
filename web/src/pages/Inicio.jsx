import { useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth'
import { useApi } from '../useApi'
import { SITUACAO, haQuantoTempo } from '../formatos'
import { AvisoConexao, Carregando } from '../components/Estados'
import CartaoAlerta from '../components/CartaoAlerta'

export default function Inicio() {
  const { usuario } = useAuth()
  // Atualiza a cada 10 s: e assim que o alerta gerado pelo sensor aparece no app
  const r = useApi(`/bairros/${usuario.bairroId}/situacao`, { cacheKey: 'situacao', intervaloMs: 10000 })
  const situacao = SITUACAO[r.dados?.situacao] || SITUACAO.NORMAL
  const anterior = useRef(null)

  useEffect(() => {
    const atual = r.dados?.situacao
    if (anterior.current && atual && atual !== anterior.current && atual !== 'NORMAL') {
      navigator.vibrate?.([200, 100, 200]) // celular vibra quando a situacao piora
    }
    anterior.current = atual
  }, [r.dados?.situacao])

  if (r.carregando && !r.dados) return <Carregando texto="Verificando seu bairro..." />

  return (
    <section>
      <AvisoConexao {...r} temDados={!!r.dados} onTentar={r.recarregar} />
      {r.dados && (
        <>
          <div className={`status status-${situacao.classe}`} aria-live="assertive">
            <span className="status-rotulo">Situação em {r.dados.bairro.nome}</span>
            <span className="status-valor"><span aria-hidden="true">{situacao.icone} </span>{situacao.rotulo}</span>
            <span className="status-dica">{situacao.dica}</span>
            <span className="status-hora">Atualizado {haQuantoTempo(r.salvoEm)}</span>
          </div>

          {r.dados.situacao !== 'NORMAL' && (
            <Link to="/abrigos?comVaga=1" className="btn btn-perigo">Ver abrigos com vaga</Link>
          )}

          <h2 className="secao-titulo">Alertas ativos</h2>
          {r.dados.alertasAtivos.length === 0
            ? <p className="texto-suave">Nenhum alerta ativo no seu bairro.</p>
            : r.dados.alertasAtivos.map((a) => <CartaoAlerta key={a.id} alerta={a} />)}

          <div className="grade-acoes">
            <Link to="/relatos/novo" className="btn btn-secundario">Relatar algo no bairro</Link>
            <Link to="/alertas" className="btn btn-secundario">Histórico de alertas</Link>
          </div>
        </>
      )}
    </section>
  )
}
