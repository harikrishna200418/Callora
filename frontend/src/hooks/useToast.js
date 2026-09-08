import { useState, useCallback } from 'react'

let toastId = 0

/**
 * Hook for managing toast notifications.
 * @returns {{ toasts, showSuccess, showError, dismissToast }}
 */
export default function useToast() {
  const [toasts, setToasts] = useState([])

  const dismissToast = useCallback((id) => {
    setToasts(prev => prev.map(t => t.id === id ? { ...t, exiting: true } : t))
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id))
    }, 300)
  }, [])

  const addToast = useCallback((message, type) => {
    const id = ++toastId
    setToasts(prev => [...prev, { id, message, type, exiting: false }])
    setTimeout(() => dismissToast(id), 4000)
    return id
  }, [dismissToast])

  const showSuccess = useCallback((message) => addToast(message, 'success'), [addToast])
  const showError = useCallback((message) => addToast(message, 'error'), [addToast])

  return { toasts, showSuccess, showError, dismissToast }
}
