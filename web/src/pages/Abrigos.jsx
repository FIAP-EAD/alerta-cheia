import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { useToast } from '../toast'
import { useApi } from '../useApi'
import { AvisoConexao, Carregando, Vazio } from '../components/Estados'
import CartaoAbrigo from '../components/CartaoAbrigo'

/** Controle +/- para a Defesa Civil atualizar a ocupacao do abrigo em tempo real. */
function AjusteVagas({ abrigo, onSalvo }) {
  const toast = useToast()
  const [salvando, setSalvando] = useState(false)
  async function ajustar(delta) {
    setSalvando(true)
    try {
      await api.patch(`/abrigos/${abrigo.id}/ocupacao`, { delta })
      onSalvo()
    } catch (e) {
      toast(e.message, 'erro')
    } finally {
      setSalvando(false)
    }
  }
  return (
    <div className="ajuste" aria-label="Ajustar ocupação">
      <button className="btn-redondo" onClick={() => ajustar(-1)} disabled={salvando} aria-label="Uma pessoa saiu">−</button>
      <span>Ocupação</span>
      <button className="btn-redondo" onClick={() => ajustar(1)} disabled={salvando} aria-label="Uma pessoa chegou">+</button>
    </div>
  )
}

export default function Abrigos() {
  const { usuario, isAdmin } = useAuth()
  const [params] = useSearchParams()
  const [comVaga, setComVaga] = useState(params.get('comVaga') === '1')
  const [todosBairros, setTodosBairros] = useState(isAdmin)
  const q = new URLSearchParams({ somenteComVaga: comVaga })
  if (!todosBairros) q.set('bairroId', usuario.bairroId)
  const r = useApi(`/abrigos?${q}`, { cacheKey: `abrigos-${q}` })

  return (
    <section>
      <h1 className="pagina-titulo">Abrigos</h1>
      <div className="filtros">
        <label className="chip-check">
          <input type="checkbox" checked={comVaga} onChange={(e) => setComVaga(e.target.checked)} /> Só com vaga
        </label>
        <label className="chip-check">
          <input type="checkbox" checked={todosBairros} onChange={(e) => setTodosBairros(e.target.checked)} /> Todos os bairros
        </label>
      </div>
      <AvisoConexao {...r} temDados={!!r.dados} onTentar={r.recarregar} />
      {r.carregando && !r.dados ? <Carregando />
        : r.dados?.length === 0 ? <Vazio>Nenhum abrigo encontrado.</Vazio>
        : r.dados?.map((a) => (
          <CartaoAbrigo key={a.id} abrigo={a}>
            {isAdmin && <AjusteVagas abrigo={a} onSalvo={r.recarregar} />}
          </CartaoAbrigo>
        ))}
    </section>
  )
}
