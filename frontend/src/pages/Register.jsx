import { useState } from 'react'
import './Auth.css'
import { apiRegister } from '../services/api'
import { Link } from 'react-router-dom'

export default function Register({ onAuthSuccess }) {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const { accessToken, refreshToken, username: responseUser, userId } = await apiRegister(username, password, email)
      onAuthSuccess(accessToken, refreshToken, responseUser || username, userId)
    } catch (err) {
      setError(err.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="liquid-blob blob-1" />
      <div className="liquid-blob blob-2" />
      <div className="liquid-blob blob-3" />

      <div className="auth-container glass-card animate-fade-in">
        <div className="auth-header">
          <div className="logo-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="24" cy="24" r="22" stroke="url(#grad2)" strokeWidth="3" fill="none"/>
              <path d="M16 20c0-4.4 3.6-8 8-8s8 3.6 8 8c0 6-8 14-8 14s-8-8-8-14z" fill="url(#grad2)"/>
              <defs>
                <linearGradient id="grad2" x1="0" y1="0" x2="48" y2="48">
                  <stop stopColor="#a78bfa"/>
                  <stop offset="1" stopColor="#f472b6"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h1 className="auth-title">Join Callora</h1>
          <p className="auth-subtitle">Start communicating smarter</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="input-group">
            <input
              id="reg-username"
              type="text"
              className="glass-input"
              placeholder=" "
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
            <label htmlFor="reg-username" className="float-label">Username</label>
          </div>

          <div className="input-group">
            <input
              id="reg-email"
              type="email"
              className="glass-input"
              placeholder=" "
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <label htmlFor="reg-email" className="float-label">Email</label>
          </div>

          <div className="input-group">
            <input
              id="reg-password"
              type="password"
              className="glass-input"
              placeholder=" "
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <label htmlFor="reg-password" className="float-label">Password</label>
          </div>

          {error && <div className="auth-error">{error}</div>}

          <button
            type="submit"
            className="btn-primary btn-gradient-accent"
            disabled={loading}
          >
            {loading ? <span className="spinner" /> : 'Create Account'}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Flow In</Link>
        </p>
      </div>
    </div>
  )
}
