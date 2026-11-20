// Cliente HTTP da API Alerta Cheia: adiciona o token JWT, aplica timeout e padroniza os erros.
const BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')
const TOKEN_KEY = 'alertacheia.token'
const TIMEOUT_MS = 8000

export class ApiError extends Error {
  constructor(status, message, campos) {
    super(message)
    this.status = status // 0 = sem conexao
    this.campos = campos || {}
  }
}

export function getToken() {
  try { return localStorage.getItem(TOKEN_KEY) } catch { return null }
}
export function setToken(token) {
  try { token ? localStorage.setItem(TOKEN_KEY, token) : localStorage.removeItem(TOKEN_KEY) } catch { /* sem storage */ }
}

let aoExpirarSessao = () => {}
export function onSessaoExpirada(fn) { aoExpirarSessao = fn }

async function request(method, path, body) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS)
  let resp
  try {
    resp = await fetch(BASE_URL + path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    })
  } catch {
    throw new ApiError(0, 'Sem conexao com o servidor. Verifique sua internet.')
  } finally {
    clearTimeout(timer)
  }

  if (resp.status === 204) return null
  const dados = await resp.json().catch(() => null)
  if (!resp.ok) {
    if (resp.status === 401 && token) aoExpirarSessao()
    throw new ApiError(resp.status, dados?.mensagem || `Erro ${resp.status}`, dados?.campos)
  }
  return dados
}

export const api = {
  get: (path) => request('GET', path),
  post: (path, body) => request('POST', path, body ?? {}),
  put: (path, body) => request('PUT', path, body ?? {}),
  patch: (path, body) => request('PATCH', path, body ?? {}),
  del: (path) => request('DELETE', path),
}

// O servidor guarda texto livre com HTML escapado (protecao XSS). Aqui so desfazemos as entidades
// para exibir; o React renderiza como texto, entao nao ha risco de executar HTML.
export function textoSeguro(s) {
  if (!s) return ''
  return s.replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'").replace(/&amp;/g, '&')
}
