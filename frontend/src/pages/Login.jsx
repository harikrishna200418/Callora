import { useState } from 'react'
import './Auth.css'
import { apiLogin } from '../services/api'
import { Link } from 'react-router-dom'

export default function Login({ onAuthSuccess }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await apiLogin(username, password)
      onAuthSuccess(data.accessToken, data.refreshToken, data.username)
    } catch (err) {
      setError(err.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      {/* Animated Liquid Blobs */}
      <div className="liquid-blob blob-1" />
      <div className="liquid-blob blob-2" />
      <div className="liquid-blob blob-3" />

      <div className="auth-container glass-card animate-fade-in">
        <div className="auth-header">
          <div className="logo-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="24" cy="24" r="22" stroke="url(#grad)" strokeWidth="3" fill="none"/>
              <path d="M16 20c0-4.4 3.6-8 8-8s8 3.6 8 8c0 6-8 14-8 14s-8-8-8-14z" fill="url(#grad)"/>
              <defs>
                <linearGradient id="grad" x1="0" y1="0" x2="48" y2="48">
                  <stop stopColor="#4ade80"/>
                  <stop offset="1" stopColor="#38bdf8"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h1 className="auth-title">Callora</h1>
          <p className="auth-subtitle">Battery-Aware Communication</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="input-group">
            <input
              id="login-username"
              type="text"
              className="glass-input"
              placeholder=" "
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
            <label htmlFor="login-username" className="float-label">Username</label>
          </div>

          <div className="input-group">
            <input
              id="login-password"
              type="password"
              className="glass-input"
              placeholder=" "
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <label htmlFor="login-password" className="float-label">Password</label>
          </div>

          {error && <div className="auth-error">{error}</div>}

          <button
            type="submit"
            className="btn-primary"
            disabled={loading}
          >
            {loading ? <span className="spinner" /> : 'Flow In'}
          </button>
        </form>

        <p className="auth-footer">
          Don't have an account? <Link to="/register">Create one</Link>
        </p>
      </div>
    </div>
  )
}
