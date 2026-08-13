import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';

const WS_BASE = 'http://localhost:8080';

let stompClient = null;

export function connectChat(onMessageReceived) {
  stompClient = new Client({
    webSocketFactory: () => new SockJS(`${WS_BASE}/ws/chat`),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
      console.log('[Callora WS] Connected to chat');
    },
    onStompError: (frame) => {
      console.error('[Callora WS] STOMP error', frame);
    },
  });

  stompClient.activate();
  return stompClient;
}

export function subscribeToConversation(conversationId, callback) {
  if (!stompClient || !stompClient.connected) {
    console.warn('[Callora WS] Not connected');
    return null;
  }
  return stompClient.subscribe(`/topic/conversations/${conversationId}`, (message) => {
    callback(JSON.parse(message.body));
  });
}

export function sendMessage(conversationId, message) {
  if (!stompClient || !stompClient.connected) {
    console.warn('[Callora WS] Not connected');
    return;
  }
  stompClient.publish({
    destination: `/app/chat/${conversationId}/send`,
    body: JSON.stringify(message),
  });
}

export function disconnectChat() {
  if (stompClient) {
    stompClient.deactivate();
  }
}
