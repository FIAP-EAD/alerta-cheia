import { createContext, useCallback, useContext, useState } from 'react'

const ToastContext = createContext(() => {})

/** Feedback visual das acoes do usuario (sucesso / erro), anunciado por leitores de tela. */
export function ToastProvider({ children }) {
  const [toast, setToast] = useState(null)
  const mostrar = useCallback((mensagem, tipo = 'sucesso') => {
    const id = Date.now()
    setToast({ id, mensagem, tipo })
    setTimeout(() => setToast((t) => (t?.id === id ? null : t)), 3500)
  }, [])
  return (
    <ToastContext.Provider value={mostrar}>
      {children}
      <div className="toast-area" role="status" aria-live="polite">
        {toast && <div className={`toast toast-${toast.tipo}`}>{toast.mensagem}</div>}
      </div>
    </ToastContext.Provider>
  )
}

export const useToast = () => useContext(ToastContext)
