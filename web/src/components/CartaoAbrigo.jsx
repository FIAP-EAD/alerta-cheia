export default function CartaoAbrigo({ abrigo, children }) {
  const pct = Math.round((abrigo.vagasOcupadas / abrigo.capacidade) * 100)
  const lotado = abrigo.vagasLivres <= 0
  const mapa = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${abrigo.endereco}, ${abrigo.bairroNome}`)}`
  return (
    <article className="cartao">
      <div className="cartao-linha">
        <h3 className="cartao-titulo">{abrigo.nome}</h3>
        <span className={`selo ${lotado ? 'selo-critico' : 'selo-normal'}`}>
          {lotado ? 'Lotado' : `${abrigo.vagasLivres} vagas`}
        </span>
      </div>
      <p className="texto-suave">{abrigo.endereco} · {abrigo.bairroNome}</p>
      <div className="barra" role="progressbar" aria-valuenow={pct} aria-valuemin={0} aria-valuemax={100}
           aria-label={`Ocupação de ${pct}%`}>
        <div className={`barra-preenchida ${pct >= 90 ? 'barra-cheia' : ''}`} style={{ width: `${Math.min(pct, 100)}%` }} />
      </div>
      <div className="cartao-linha">
        <small className="texto-suave">{abrigo.vagasOcupadas}/{abrigo.capacidade} ocupadas ({pct}%)</small>
        <a href={mapa} target="_blank" rel="noopener noreferrer" className="link">Como chegar</a>
      </div>
      {children}
    </article>
  )
}
