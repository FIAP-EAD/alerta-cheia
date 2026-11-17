// Guarda a ultima resposta boa de cada tela: se a conexao cair, o app continua mostrando
// o ultimo status conhecido e os abrigos (util justamente durante uma enchente).
const PREFIXO = 'alertacheia.cache.'

export function salvarCache(chave, dados) {
  try { localStorage.setItem(PREFIXO + chave, JSON.stringify({ dados, salvoEm: Date.now() })) } catch { /* ignora */ }
}

export function lerCache(chave) {
  try {
    const bruto = localStorage.getItem(PREFIXO + chave)
    return bruto ? JSON.parse(bruto) : null
  } catch { return null }
}

export function limparCache() {
  try {
    Object.keys(localStorage).filter((k) => k.startsWith(PREFIXO)).forEach((k) => localStorage.removeItem(k))
  } catch { /* ignora */ }
}
