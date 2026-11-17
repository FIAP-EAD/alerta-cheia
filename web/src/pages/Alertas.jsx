import { useState } from 'react'
import { useAuth } from '../auth'
import { useApi } from '../useApi'
import { AvisoConexao, Carregando, Vazio } from '../components/Estados'
import CartaoAlerta from '../components/CartaoAlerta'

export default function Alertas() {
  const { usuario, isAdmin } = useAuth()
  const [somenteAtivos, setSomenteAtivos] = useState(true)
  const [todosBairros, setTodosBairros] = useState(isAdmin)
  const params = new URLSearchParams()
  if (somenteAtivos) params.set('status', 'ATIVO')
  if (!todosBairros) params.set('bairroId', usuario.bairroId)
  const r = useApi(`/alertas?${params}`, { intervaloMs: 15000 })

  return (
    <section>
      <h1 className="pagina-titulo">Alertas</h1>
      <div className="filtros" role="group" aria-label="Filtros">
        <button className={`chip ${somenteAtivos ? 'chip-ativo' : ''}`} aria-pressed={somenteAtivos}
                onClick={() => setSomenteAtivos(true)}>Ativos</button>
        <button className={`chip ${!somenteAtivos ? 'chip-ativo' : ''}`} aria-pressed={!somenteAtivos}
                onClick={() => setSomenteAtivos(false)}>Todos</button>
        <label className="chip-check">
          <input type="checkbox" checked={todosBairros} onChange={(e) => setTodosBairros(e.target.checked)} />
          Todos os bairros
        </label>
      </div>
      <AvisoConexao {...r} temDados={!!r.dados} onTentar={r.recarregar} />
      {r.carregando && !r.dados ? <Carregando />
        : r.dados?.length === 0 ? <Vazio>Nenhum alerta encontrado.</Vazio>
        : r.dados?.map((a) => <CartaoAlerta key={a.id} alerta={a} />)}
    </section>
  )
}
