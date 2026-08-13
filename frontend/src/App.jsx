import { Routes, Route, Navigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Dashboard from './pages/Dashboard.jsx'
import './App.css'

function App() {
  const [token, setToken] = useState(localStorage.getItem('accessToken'))

  const handleAuthSuccess = (accessToken, refreshToken, username) => {
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('username', username)
    setToken(accessToken)
  }

  const handleLogout = () => {
    localStorage.clear()
    setToken(null)
  }

  return (
    <div className="app-container">
      <Routes>
        <Route
          path="/login"
          element={token ? <Navigate to="/dashboard" /> : <Login onAuthSuccess={handleAuthSuccess} />}
        />
        <Route
          path="/register"
          element={token ? <Navigate to="/dashboard" /> : <Register onAuthSuccess={handleAuthSuccess} />}
        />
        <Route
          path="/dashboard/*"
          element={token ? <Dashboard onLogout={handleLogout} /> : <Navigate to="/login" />}
        />
        <Route path="*" element={<Navigate to={token ? "/dashboard" : "/login"} />} />
      </Routes>
    </div>
  )
}

export default App
