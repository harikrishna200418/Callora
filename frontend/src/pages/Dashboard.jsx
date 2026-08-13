import { useState, useRef, useEffect } from 'react'
import './Dashboard.css'

const mockContacts = [
  { id: '1', name: 'Sarah Chen', status: 'online', lastMsg: 'Hey! Are you free for a call?', time: '2m', unread: 2 },
  { id: '2', name: 'Alex Kumar', status: 'offline', lastMsg: 'The project looks great 🎉', time: '15m', unread: 0 },
  { id: '3', name: 'Jordan Lee', status: 'online', lastMsg: 'Check this out!', time: '1h', unread: 1 },
  { id: '4', name: 'Priya Patel', status: 'away', lastMsg: 'I\'ll call you tomorrow', time: '3h', unread: 0 },
  { id: '5', name: 'Marcus Wright', status: 'online', lastMsg: 'Video call at 5?', time: '5h', unread: 0 },
]

export default function Dashboard({ onLogout }) {
  const [activeTab, setActiveTab] = useState('chats')
  const [selectedChat, setSelectedChat] = useState(null)
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const msgEndRef = useRef(null)
  const username = localStorage.getItem('username') || 'User'

  useEffect(() => {
    msgEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const sendMsg = (e) => {
    e.preventDefault()
    if (!input.trim()) return
    setMessages(prev => [...prev, { id: Date.now(), text: input, mine: true, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }])
    setInput('')
    // Simulate reply
    setTimeout(() => {
      setMessages(prev => [...prev, { id: Date.now() + 1, text: 'Got it! 👍', mine: false, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }])
    }, 1200)
  }

  return (
    <div className="dashboard">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="user-avatar">
            <span className="avatar-letter">{username[0]?.toUpperCase()}</span>
            <span className="status-dot online" />
          </div>
          <div className="sidebar-title">
            <h2>Callora</h2>
            <p className="text-muted">{username}</p>
          </div>
          <button className="btn-icon" onClick={onLogout} title="Logout">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/></svg>
          </button>
        </div>

        {/* Tab Navigation */}
        <nav className="sidebar-tabs">
          {['chats', 'calls', 'settings'].map(tab => (
            <button
              key={tab}
              className={`tab-btn ${activeTab === tab ? 'active' : ''}`}
              onClick={() => setActiveTab(tab)}
            >
              {tab === 'chats' && <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>}
              {tab === 'calls' && <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6A19.79 19.79 0 012.12 4.18 2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>}
              {tab === 'settings' && <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/></svg>}
              <span>{tab.charAt(0).toUpperCase() + tab.slice(1)}</span>
            </button>
          ))}
        </nav>

        {/* Contact List */}
        {activeTab === 'chats' && (
          <div className="contact-list">
            {mockContacts.map(c => (
              <div
                key={c.id}
                className={`contact-item ${selectedChat?.id === c.id ? 'active' : ''}`}
                onClick={() => { setSelectedChat(c); setMessages([]); }}
              >
                <div className="contact-avatar">
                  <span>{c.name[0]}</span>
                  <span className={`status-dot ${c.status}`} />
                </div>
                <div className="contact-info">
                  <div className="contact-row">
                    <span className="contact-name">{c.name}</span>
                    <span className="contact-time">{c.time}</span>
                  </div>
                  <div className="contact-row">
                    <span className="contact-last-msg">{c.lastMsg}</span>
                    {c.unread > 0 && <span className="unread-badge">{c.unread}</span>}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {activeTab === 'calls' && (
          <div className="tab-placeholder">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" strokeWidth="1.5"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6A19.79 19.79 0 012.12 4.18 2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
            <p>Call History</p>
            <span className="text-muted">Your recent calls will appear here</span>
          </div>
        )}

        {activeTab === 'settings' && (
          <div className="settings-panel">
            <h3>Battery Protection</h3>
            <div className="setting-row">
              <span>Auto-end calls at critical battery</span>
              <label className="toggle">
                <input type="checkbox" defaultChecked />
                <span className="toggle-slider" />
              </label>
            </div>
            <div className="setting-row">
              <span>Warning threshold</span>
              <span className="setting-value">10%</span>
            </div>
            <div className="setting-row">
              <span>Critical threshold</span>
              <span className="setting-value">7%</span>
            </div>
          </div>
        )}
      </aside>

      {/* Main Chat Area */}
      <main className="chat-area">
        {selectedChat ? (
          <>
            <header className="chat-header glass-card">
              <div className="chat-header-left">
                <div className="contact-avatar sm">
                  <span>{selectedChat.name[0]}</span>
                  <span className={`status-dot ${selectedChat.status}`} />
                </div>
                <div>
                  <h3>{selectedChat.name}</h3>
                  <span className="text-muted">{selectedChat.status}</span>
                </div>
              </div>
              <div className="chat-header-actions">
                <button className="btn-icon" title="Voice Call">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6A19.79 19.79 0 012.12 4.18 2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
                </button>
                <button className="btn-icon" title="Video Call">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>
                </button>
              </div>
            </header>

            <div className="messages-area">
              {messages.length === 0 && (
                <div className="empty-chat">
                  <p className="text-muted">Send a message to start the conversation 💬</p>
                </div>
              )}
              {messages.map(m => (
                <div key={m.id} className={`message ${m.mine ? 'mine' : 'theirs'}`}>
                  <div className="message-bubble">
                    <p>{m.text}</p>
                    <span className="message-time">{m.time}</span>
                  </div>
                </div>
              ))}
              <div ref={msgEndRef} />
            </div>

            <form className="chat-input-bar glass-card" onSubmit={sendMsg}>
              <input
                type="text"
                className="chat-input"
                placeholder="Type a message..."
                value={input}
                onChange={(e) => setInput(e.target.value)}
              />
              <button type="submit" className="btn-send" disabled={!input.trim()}>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
              </button>
            </form>
          </>
        ) : (
          <div className="no-chat-selected">
            <div className="no-chat-icon">
              <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" strokeWidth="1"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
            </div>
            <h2>Welcome to <span className="text-gradient">Callora</span></h2>
            <p className="text-muted">Select a conversation to start chatting</p>
          </div>
        )}
      </main>
    </div>
  )
}
