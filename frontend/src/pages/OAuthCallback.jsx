import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'

export default function OAuthCallback({ onAuthSuccess }) {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()

  useEffect(() => {
    const accessToken = searchParams.get('accessToken')
    const refreshToken = searchParams.get('refreshToken')
    const username = searchParams.get('username')

    if (accessToken && refreshToken && username) {
      onAuthSuccess(accessToken, refreshToken, username)
      navigate('/dashboard')
    } else {
      navigate('/login')
    }
  }, [searchParams, onAuthSuccess, navigate])

  return (
    <div className="auth-page">
      <div className="liquid-blob blob-1" />
      <div className="liquid-blob blob-2" />
      <div className="liquid-blob blob-3" />
      <div className="auth-container glass-card animate-fade-in" style={{ textAlign: 'center' }}>
        <span className="spinner" />
        <p style={{ color: '#94a3b8', marginTop: '1rem' }}>Signing you in...</p>
      </div>
    </div>
  )
}
