import { useState, useRef, useEffect } from 'react'
import { connectChat, subscribeToConversation, sendMessage as wsSendMessage, disconnectChat } from '../services/websocket.js'
import { 
  apiGetConversations, 
  apiGetOrCreateOneToOneConversation, 
  apiGetMessages, 
  apiGetSettings, 
  apiUpdateSettings,
  apiGetContacts,
  apiAddContact,
  apiInitiateVoiceCall,
  apiInitiateVideoCall,
  apiSendSms
} from '../services/api.js'
import './Dashboard.css'

export default function Dashboard({ onLogout }) {
  const [activeTab, setActiveTab] = useState('chats')
  const [selectedChat, setSelectedChat] = useState(null)
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  
  // Real data states
  const [contacts, setContacts] = useState([])
  const [conversations, setConversations] = useState([])
  const [settings, setSettings] = useState(null)
  const [stompClient, setStompClient] = useState(null)

  // New states for Contact
  const [showAddContact, setShowAddContact] = useState(false)
  const [newContactName, setNewContactName] = useState('')
  const [newContactPhone, setNewContactPhone] = useState('')
  const [searchQuery, setSearchQuery] = useState('')

  const msgEndRef = useRef(null)
  const username = localStorage.getItem('username') || 'User'
  const userId = localStorage.getItem('userId')

  // Load Initial Data
  useEffect(() => {
    async function loadData() {
      if (!userId) return;
      try {
        const [contactsData, convsData, settingsData] = await Promise.all([
          apiGetContacts(),
          apiGetConversations(userId),
          apiGetSettings(userId)
        ])
        setContacts(contactsData)
        setConversations(convsData)
        setSettings(settingsData)
      } catch (err) {
        console.error("Failed to load initial data", err)
      }
    }
    loadData()
  }, [userId])

  // Setup STOMP WebSocket
  useEffect(() => {
    if (!userId) return;
    
    const token = localStorage.getItem('accessToken');
    const client = connectChat(token, (msg) => {
      // General message handler if needed
    });
    setStompClient(client);

    return () => {
      disconnectChat();
    }
  }, [userId])

  // Scroll to bottom
  useEffect(() => {
    msgEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleAddContact = async (e) => {
    e.preventDefault()
    try {
      await apiAddContact(newContactName, newContactPhone)
      setShowAddContact(false)
      setNewContactName('')
      setNewContactPhone('')
      // refresh
      const contactsData = await apiGetContacts()
      setContacts(contactsData)
    } catch (err) {
      alert(err.message)
    }
  }

  // Handle Contact Click to start/open chat
  const handleContactClick = async (contact) => {
    if (!contact.targetUser) {
      alert('This contact is not registered on Callora yet. Invite them via SMS!')
      return;
    }
    try {
      // Find or create conversation
      const conv = await apiGetOrCreateOneToOneConversation(userId, contact.targetUser.id)
      setSelectedChat({ ...conv, targetUser: contact.targetUser, contactName: contact.contactName })
      
      // Load history
      const msgs = await apiGetMessages(conv.id)
      setMessages(msgs || [])

      // Subscribe to conversation
      if (stompClient && stompClient.connected) {
        subscribeToConversation(conv.id, (newMsg) => {
          setMessages(prev => [...prev, newMsg])
        })
      }
    } catch (err) {
      console.error("Error opening chat", err)
    }
  }

  const handleCall = async (type) => {
    try {
      if (type === 'voice') await apiInitiateVoiceCall()
      if (type === 'video') await apiInitiateVideoCall()
      if (type === 'sms') await apiSendSms()
    } catch (err) {
      alert(err.message)
    }
  }

  const sendMsg = (e) => {
    e.preventDefault()
    if (!input.trim() || !selectedChat || !stompClient || !stompClient.connected) return

    const msgPayload = {
      type: 'TEXT',
      content: input
    }
    
    wsSendMessage(selectedChat.id, msgPayload)
    
    setInput('')
  }

  const toggleSetting = async (key) => {
    if (!settings) return;
    const newSettings = { ...settings, [key]: !settings[key] }
    setSettings(newSettings)
    try {
      await apiUpdateSettings(userId, newSettings)
    } catch (err) {
      console.error("Failed to update setting", err)
      // Revert on fail
      setSettings(settings)
    }
  }

  return (
    <div className="dashboard">
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

        {activeTab === 'chats' && (
          <div className="contact-list-container">
            <div className="contact-list-actions">
              <input 
                type="text" 
                placeholder="Search contacts..." 
                value={searchQuery} 
                onChange={e => setSearchQuery(e.target.value)} 
                className="search-input" 
              />
              <button className="btn-icon add-contact-btn" onClick={() => setShowAddContact(true)} title="Add Contact">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
              </button>
            </div>
            <div className="contact-list">
              {contacts.length === 0 && <p className="text-muted" style={{padding: '1rem'}}>No contacts found.</p>}
              {contacts.filter(c => c.contactName.toLowerCase().includes(searchQuery.toLowerCase())).map(c => (
                <div
                  key={c.id}
                  className={`contact-item ${selectedChat?.targetUser?.id === c.targetUser?.id && selectedChat ? 'active' : ''}`}
                  onClick={() => handleContactClick(c)}
                >
                  <div className="contact-avatar">
                    <span>{c.contactName[0].toUpperCase()}</span>
                    {c.targetUser && <span className={`status-dot ${c.targetUser.onlineStatus === 'ONLINE' ? 'online' : 'offline'}`} />}
                  </div>
                  <div className="contact-info">
                    <div className="contact-row">
                      <span className="contact-name">{c.contactName}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'calls' && (
          <div className="tab-placeholder">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" strokeWidth="1.5"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6A19.79 19.79 0 012.12 4.18 2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
            <p>Call History</p>
            <span className="text-muted">Calls not yet implemented</span>
          </div>
        )}

        {activeTab === 'settings' && settings && (
          <div className="settings-panel">
            <h3>Battery Protection</h3>
            <div className="setting-row">
              <span>Auto-end calls at critical battery</span>
              <label className="toggle">
                <input type="checkbox" checked={settings.automaticCallEndEnabled} onChange={() => toggleSetting('automaticCallEndEnabled')} />
                <span className="toggle-slider" />
              </label>
            </div>
            <div className="setting-row">
              <span>Battery protection enabled</span>
              <label className="toggle">
                <input type="checkbox" checked={settings.batteryProtectionEnabled} onChange={() => toggleSetting('batteryProtectionEnabled')} />
                <span className="toggle-slider" />
              </label>
            </div>
            <div className="setting-row">
              <span>Warning threshold</span>
              <span className="setting-value">{settings.warningBatteryThreshold}%</span>
            </div>
            <div className="setting-row">
              <span>Critical threshold</span>
              <span className="setting-value">{settings.criticalBatteryThreshold}%</span>
            </div>
          </div>
        )}
      </aside>

      <main className="chat-area">
        {selectedChat ? (
          <>
            <header className="chat-header glass-card">
              <div className="chat-header-left">
                <div className="contact-avatar sm">
                  <span>{selectedChat.contactName?.[0]?.toUpperCase() || selectedChat.targetUser.username[0].toUpperCase()}</span>
                  <span className={`status-dot ${selectedChat.targetUser.onlineStatus === 'ONLINE' ? 'online' : 'offline'}`} />
                </div>
                <div>
                  <h3>{selectedChat.contactName || selectedChat.targetUser.fullName || selectedChat.targetUser.username}</h3>
                </div>
              </div>
              <div className="chat-header-actions">
                <button className="btn-icon" title="Voice Call" onClick={() => handleCall('voice')}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6A19.79 19.79 0 012.12 4.18 2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
                </button>
                <button className="btn-icon" title="Video Call" onClick={() => handleCall('video')}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="23 7 16 12 23 17 23 7"></polygon><rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect></svg>
                </button>
                <button className="btn-icon" title="SMS" onClick={() => handleCall('sms')}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
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
                <div key={m.id} className={`message ${m.senderId === userId ? 'mine' : 'theirs'}`}>
                  <div className="message-bubble">
                    <p>{m.content}</p>
                    <span className="message-time">{new Date(m.sentAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
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
            <p className="text-muted">Select a contact to start chatting</p>
          </div>
        )}
      </main>
      
      {showAddContact && (
        <div className="modal-overlay">
          <div className="modal-content glass-card">
            <h3>Add Contact</h3>
            <form onSubmit={handleAddContact}>
              <div className="form-group">
                <label>Name</label>
                <input 
                  type="text" 
                  value={newContactName} 
                  onChange={e => setNewContactName(e.target.value)} 
                  required 
                  className="auth-input"
                />
              </div>
              <div className="form-group">
                <label>Phone Number</label>
                <input 
                  type="text" 
                  value={newContactPhone} 
                  onChange={e => setNewContactPhone(e.target.value)} 
                  required 
                  className="auth-input"
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowAddContact(false)}>Cancel</button>
                <button type="submit" className="btn-primary">Add Contact</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
