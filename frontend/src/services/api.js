// In dev, defaults to '' to use Vite proxy. In prod, VITE_API_URL points to the real backend.
const API_BASE = import.meta.env.VITE_API_URL || '';
export async function apiLogin(username, password) {
  const res = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) {
    let msg = 'Login failed';
    const text = await res.text();
    try {
      const data = JSON.parse(text);
      msg = data.error || data.message || Object.values(data)[0] || msg;
    } catch {
      msg = text || msg;
    }
    throw new Error(msg);
  }
  return res.json();
}

export async function apiRegister(username, password, email) {
  const res = await fetch(`${API_BASE}/api/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password, email }),
  });
  if (!res.ok) {
    let msg = 'Registration failed';
    const text = await res.text();
    try {
      const data = JSON.parse(text);
      msg = data.error || data.message || Object.values(data)[0] || msg;
    } catch {
      msg = text || msg;
    }
    throw new Error(msg);
  }
  return res.json();
}

export function authFetch(url, options = {}) {
  const token = localStorage.getItem('accessToken');
  return fetch(`${API_BASE}${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
}

export async function apiGetUsers() {
  const res = await authFetch('/api/users');
  if (!res.ok) throw new Error('Failed to fetch users');
  return res.json();
}

export async function apiGetConversations(userId) {
  const res = await authFetch(`/api/chat/conversations?userId=${userId}`);
  if (!res.ok) throw new Error('Failed to fetch conversations');
  return res.json();
}

export async function apiGetOrCreateOneToOneConversation(user1Id, user2Id) {
  const res = await authFetch(`/api/chat/conversations/one-to-one?user1Id=${user1Id}&user2Id=${user2Id}`, {
    method: 'POST'
  });
  if (!res.ok) throw new Error('Failed to create conversation');
  return res.json();
}

export async function apiGetMessages(conversationId) {
  const res = await authFetch(`/api/chat/conversations/${conversationId}/messages`);
  if (!res.ok) throw new Error('Failed to fetch messages');
  return res.json();
}

export async function apiGetSettings(userId) {
  const res = await authFetch(`/api/users/${userId}/settings`);
  if (!res.ok) throw new Error('Failed to fetch settings');
  return res.json();
}

export async function apiUpdateSettings(userId, settings) {
  const res = await authFetch(`/api/users/${userId}/settings`, {
    method: 'PUT',
    body: JSON.stringify(settings),
  });
  if (!res.ok) throw new Error('Failed to update settings');
  return res.json();
}

export async function apiGetContacts() {
  const res = await authFetch('/api/v1/contacts');
  if (!res.ok) throw new Error('Failed to fetch contacts');
  return res.json();
}

export async function apiAddContact(contactName, phoneNumber, email = '', notes = '') {
  const res = await authFetch('/api/v1/contacts', {
    method: 'POST',
    body: JSON.stringify({ contactName, phoneNumber, email, notes }),
  });
  if (!res.ok) {
    let msg = 'Failed to add contact';
    const text = await res.text();
    try {
      const data = JSON.parse(text);
      msg = data.error || data.message || Object.values(data)[0] || msg;
    } catch {
      msg = text || msg;
    }
    throw new Error(msg);
  }
  return res.json();
}

export async function apiUpdateContact(contactId, contactName, phoneNumber, email = '', notes = '') {
  const res = await authFetch(`/api/v1/contacts/${contactId}`, {
    method: 'PUT',
    body: JSON.stringify({ contactName, phoneNumber, email, notes }),
  });
  if (!res.ok) {
    let msg = 'Failed to update contact';
    const text = await res.text();
    try {
      const data = JSON.parse(text);
      msg = data.error || data.message || Object.values(data)[0] || msg;
    } catch {
      msg = text || msg;
    }
    throw new Error(msg);
  }
  return res.json();
}

export async function apiDeleteContact(contactId) {
  const res = await authFetch(`/api/v1/contacts/${contactId}`, {
    method: 'DELETE'
  });
  if (!res.ok) {
    throw new Error('Failed to delete contact');
  }
  return true;
}

export async function apiGetOrCreateSmsConversation(userId, contactId) {
  const res = await authFetch(`/api/chat/conversations/sms?userId=${userId}&contactId=${contactId}`, {
    method: 'POST'
  });
  if (!res.ok) throw new Error('Failed to create SMS conversation');
  return res.json();
}

export async function apiInitiateVoiceCall() {
  const res = await authFetch('/api/call/voice', { method: 'POST' });
  if (!res.ok) {
    const data = await res.json();
    throw new Error(data.error || 'Voice call failed');
  }
  return res.json();
}

export async function apiInitiateVideoCall() {
  const res = await authFetch('/api/call/video', { method: 'POST' });
  if (!res.ok) {
    const data = await res.json();
    throw new Error(data.error || 'Video call failed');
  }
  return res.json();
}

export async function apiSendSms() {
  const res = await authFetch('/api/call/sms', { method: 'POST' });
  if (!res.ok) {
    const data = await res.json();
    throw new Error(data.error || 'SMS failed');
  }
  return res.json();
}
