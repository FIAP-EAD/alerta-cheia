import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, getToken, onSessaoExpirada, setToken } from './api'
import { limparCache } from './cache'

const USER_KEY = 'alertacheia.usuario'
const AuthContext = createContext(null)

function lerUsuario() {
  try { return JSON.parse(localStorage.getItem(USER_KEY)) } catch { return null }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => (getToken() ? lerUsuario() : null))

  const sair = useCallback(() => {
    setToken(null)
    try { localStorage.removeItem(USER_KEY) } catch { /* ignora */ }
    limparCache()
    setUsuario(null)
  }, [])

  useEffect(() => { onSessaoExpirada(sair) }, [sair])

  const entrar = useCallback(async (email, senha) => {
    const resp = await api.post('/auth/login', { email, senha })
    setToken(resp.token)
    try { localStorage.setItem(USER_KEY, JSON.stringify(resp.usuario)) } catch { /* ignora */ }
    setUsuario(resp.usuario)
    return resp.usuario
  }, [])

  const valor = useMemo(() => ({ usuario, entrar, sair, isAdmin: usuario?.perfil === 'ADMIN' }), [usuario, entrar, sair])
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
