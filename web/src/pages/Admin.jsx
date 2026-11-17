import { useState } from 'react'
import { api } from '../api'
import { useToast } from '../toast'
import { useApi } from '../useApi'
import { AvisoConexao, Carregando, Vazio } from '../components/Estados'
import CartaoAlerta from '../components/CartaoAlerta'
import { CartaoRelato } from './Relatos'

function AlertasAtivos() {
  const toast = useToast()
  const r = useApi('/alertas?status=ATIVO', { intervaloMs: 10000 })
  async function encerrar(id) {
    try {
      await api.put(`/alertas/${id}/encerrar`)
      toast('Alerta encerrado.')
      r.recarregar()
    } catch (e) { toast(e.message, 'erro') }
  }
  if (r.carregando) return <Carregando />
  return (
    <>
      <AvisoConexao {...r} onTentar={r.recarregar} />
      {r.dados?.length === 0 ? <Vazio>Nenhum alerta ativo.</Vazio> : r.dados?.map((a) => (
        <div key={a.id}>
          <CartaoAlerta alerta={a} />
          <button className="btn btn-secundario btn-pequeno sob-cartao" onClick={() => encerrar(a.id)}>Encerrar</button>
        </div>
      ))}
    </>
  )
}

function Moderacao() {
  const toast = useToast()
  const r = useApi('/relatos')
  async function mudar(id, status) {
    try {
      await api.put(`/relatos/${id}/status`, { status })
      toast(status === 'VERIFICADO' ? 'Relato verificado.' : 'Relato descartado.')
      r.recarregar()
    } catch (e) { toast(e.message, 'erro') }
  }
  if (r.carregando) return <Carregando />
  const abertos = r.dados?.filter((x) => x.status === 'ABERTO') ?? []
  return (
    <>
      <AvisoConexao {...r} onTentar={r.recarregar} />
      {abertos.length === 0 ? <Vazio>Nenhum relato aguardando análise.</Vazio> : abertos.map((rel) => (
        <CartaoRelato key={rel.id} relato={rel}>
          <div className="grade-acoes">
            <button className="btn btn-primario btn-pequeno" onClick={() => mudar(rel.id, 'VERIFICADO')}>Verificar</button>
            <button className="btn btn-secundario btn-pequeno" onClick={() => mudar(rel.id, 'DESCARTADO')}>Descartar</button>
          </div>
        </CartaoRelato>
      ))}
    </>
  )
}

function Sensores() {
  const toast = useToast()
  const r = useApi('/sensores', { intervaloMs: 15000 })
  async function alternar(s) {
    try {
      await api.put(`/sensores/${s.id}`, {
        codigo: s.codigo, tipo: s.tipo, bairroId: s.bairroId, limiteAlerta: s.limiteAlerta,
        limiteCritico: s.limiteCritico, ativo: !s.ativo,
      })
      toast(`Sensor ${s.codigo} ${s.ativo ? 'desativado' : 'ativado'}.`)
      r.recarregar()
    } catch (e) { toast(e.message, 'erro') }
  }
  if (r.carregando) return <Carregando />
  return (
    <>
      <AvisoConexao {...r} onTentar={r.recarregar} />
      {r.dados?.map((s) => (
        <article key={s.id} className="cartao">
          <div className="cartao-linha">
            <h3 className="cartao-titulo">{s.codigo}</h3>
            <span className={`selo ${s.ativo ? 'selo-normal' : 'selo-encerrado'}`}>{s.ativo ? 'Ativo' : 'Inativo'}</span>
          </div>
          <p className="texto-suave">
            {s.tipo === 'NIVEL_RIO' ? 'Nível do rio' : 'Pluviômetro'} · {s.bairroNome}
          </p>
          <p className="texto-suave">Atenção ≥ {s.limiteAlerta} {s.unidade} · Crítico ≥ {s.limiteCritico} {s.unidade}</p>
          <button className="btn-texto" onClick={() => alternar(s)}>{s.ativo ? 'Desativar' : 'Ativar'}</button>
        </article>
      ))}
    </>
  )
}

const ABAS = { alertas: ['Alertas ativos', AlertasAtivos], relatos: ['Relatos', Moderacao], sensores: ['Sensores', Sensores] }

export default function Admin() {
  const [aba, setAba] = useState('alertas')
  const Componente = ABAS[aba][1]
  return (
    <section>
      <h1 className="pagina-titulo">Gestão · Defesa Civil</h1>
      <div className="filtros" role="tablist">
        {Object.entries(ABAS).map(([chave, [rotulo]]) => (
          <button key={chave} role="tab" aria-selected={aba === chave}
                  className={`chip ${aba === chave ? 'chip-ativo' : ''}`} onClick={() => setAba(chave)}>{rotulo}</button>
        ))}
      </div>
      <Componente />
    </section>
  )
}
