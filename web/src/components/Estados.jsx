import { haQuantoTempo } from '../formatos'

export function Carregando({ texto = 'Carregando...' }) {
  return (
    <div className="estado" role="status" aria-live="polite">
      <span className="spinner" aria-hidden="true" /> {texto}
    </div>
  )
}

export function Vazio({ children }) {
  return <div className="estado estado-vazio">{children}</div>
}

/** Mostra erro de rede/servidor. Se ha dados em cache, avisa que sao os ultimos conhecidos. */
export function AvisoConexao({ erro, offline, salvoEm, temDados, onTentar }) {
  if (!erro) return null
  if (offline) {
    return (
      <div className="aviso aviso-offline" role="alert">
        <strong>Sem conexão.</strong>{' '}
        {temDados ? `Mostrando os últimos dados (${haQuantoTempo(salvoEm)}).` : 'Não foi possível carregar.'}
        {onTentar && <button className="btn-texto" onClick={onTentar}>Tentar de novo</button>}
      </div>
    )
  }
  return (
    <div className="aviso aviso-erro" role="alert">
      {erro.message}
      {onTentar && <button className="btn-texto" onClick={onTentar}>Tentar de novo</button>}
    </div>
  )
}

export function Campo({ id, rotulo, erro, children, dica }) {
  return (
    <div className={`campo ${erro ? 'campo-erro' : ''}`}>
      <label htmlFor={id}>{rotulo}</label>
      {children}
      {dica && !erro && <small id={`${id}-dica`} className="campo-dica">{dica}</small>}
      {erro && <small id={`${id}-erro`} className="campo-msg" role="alert">{erro}</small>}
    </div>
  )
}
