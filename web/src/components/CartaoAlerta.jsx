import { Link } from 'react-router-dom'
import { dataHora } from '../formatos'

export default function CartaoAlerta({ alerta }) {
  const classe = alerta.status === 'ENCERRADO' ? 'encerrado' : alerta.severidade.toLowerCase()
  return (
    <Link to={`/alertas/${alerta.id}`} className={`cartao cartao-alerta borda-${classe}`}>
      <div className="cartao-linha">
        <span className={`selo selo-${classe}`}>
          {alerta.status === 'ENCERRADO' ? 'Encerrado' : alerta.severidade === 'CRITICO' ? 'Crítico' : 'Atenção'}
        </span>
        <time className="texto-suave">{dataHora(alerta.criadoEm)}</time>
      </div>
      <p className="cartao-texto">{alerta.mensagem}</p>
      <span className="texto-suave">{alerta.bairroNome}</span>
    </Link>
  )
}
