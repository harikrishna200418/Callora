import { Link } from 'react-router-dom'
import usePageMeta from '../hooks/usePageMeta'
import './Auth.css'

export default function NotFound() {
  usePageMeta('Page Not Found', 'The page you are looking for does not exist.')

  return (
    <div className="auth-page">
      {/* Animated Liquid Blobs */}
      <div className="liquid-blob blob-1" />
      <div className="liquid-blob blob-2" />
      <div className="liquid-blob blob-3" />

      <div className="auth-container glass-card animate-fade-in" style={{ textAlign: 'center' }}>
        <div className="notfound-code">404</div>
        <h1 className="notfound-title">Page Not Found</h1>
        <p className="auth-subtitle" style={{ marginBottom: '32px' }}>
          The page you're looking for doesn't exist or has been moved.
        </p>
        <Link to="/" className="btn-primary" style={{ display: 'inline-block', textDecoration: 'none' }}>
          Go Home
        </Link>
      </div>
    </div>
  )
}
