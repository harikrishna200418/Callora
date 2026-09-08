import './EmptyState.css'

/**
 * Reusable empty state component for when there's no data to display.
 * @param {{ icon: JSX.Element, title: string, subtitle: string, action?: JSX.Element }} props
 */
export default function EmptyState({ icon, title, subtitle, action }) {
  return (
    <div className="empty-state animate-fade-in">
      {icon && <div className="empty-state-icon">{icon}</div>}
      <h3 className="empty-state-title">{title}</h3>
      {subtitle && <p className="empty-state-subtitle">{subtitle}</p>}
      {action && <div className="empty-state-action">{action}</div>}
    </div>
  )
}
