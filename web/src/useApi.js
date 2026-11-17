import { useCallback, useEffect, useRef, useState } from 'react'
import { api } from './api'
import { lerCache, salvarCache } from './cache'

/**
 * Busca um recurso da API com loading, erro, cache offline e atualizacao periodica opcional.
 * Retorna { dados, carregando, erro, offline, salvoEm, recarregar }.
 */
export function useApi(path, { cacheKey, intervaloMs } = {}) {
  const cache = cacheKey ? lerCache(cacheKey) : null
  const [estado, setEstado] = useState({
    dados: cache?.dados ?? null,
    carregando: true,
    erro: null,
    offline: false,
    salvoEm: cache?.salvoEm ?? null,
  })
  const ativo = useRef(true)

  const recarregar = useCallback(async () => {
    if (!path) return
    try {
      const dados = await api.get(path)
      if (cacheKey) salvarCache(cacheKey, dados)
      if (ativo.current) setEstado({ dados, carregando: false, erro: null, offline: false, salvoEm: Date.now() })
    } catch (e) {
      if (!ativo.current) return
      setEstado((s) => ({ ...s, carregando: false, erro: e, offline: e.status === 0 }))
    }
  }, [path, cacheKey])

  useEffect(() => {
    ativo.current = true
    recarregar()
    if (!intervaloMs) return () => { ativo.current = false }
    const id = setInterval(recarregar, intervaloMs)
    return () => { ativo.current = false; clearInterval(id) }
  }, [recarregar, intervaloMs])

  return { ...estado, recarregar }
}
