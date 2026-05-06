import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  private client: Client | null = null;
  private connected = false;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 3;
  private reconnectDelay = 5000;

  constructor() {}

  connect(userId: number, onNotification: (notification: any) => void, onUnreadCount: (count: number) => void) {
    if (this.connected) {
      return;
    }

    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.warn('WebSocket: 已达最大重连次数，停止连接');
      return;
    }

    try {
      const wsUrl = import.meta.env.VITE_WS_URL || '/ws/notification';
      const socket = new SockJS(wsUrl, null, { timeout: 8000 });

      this.client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: this.reconnectDelay,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          console.log('WebSocket 已连接');
          this.connected = true;
          this.reconnectAttempts = 0;

          try {
            this.client?.subscribe(`/user/${userId}/queue/notifications`, (message) => {
              const notification = JSON.parse(message.body);
              onNotification(notification);
            });
            this.client?.subscribe(`/user/${userId}/queue/unread-count`, (message) => {
              const count = parseInt(message.body);
              onUnreadCount(count);
            });
            this.client?.subscribe('/topic/notifications', (message) => {
              const notification = JSON.parse(message.body);
              onNotification(notification);
            });
          } catch (e) {
            console.warn('WebSocket 订阅失败:', e);
          }
        },
        onDisconnect: () => {
          this.connected = false;
        },
        onStompError: () => {
          this.connected = false;
        },
        onWebSocketClose: () => {
          this.connected = false;
          this.reconnectAttempts++;
          if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('WebSocket: 多次重连失败，已停止');
            this.client?.deactivate();
          }
        }
      });

      this.client.activate();
    } catch (e) {
      console.warn('WebSocket 初始化失败（后端可能未启动）:', e);
    }
  }

  disconnect() {
    try {
      if (this.client) {
        this.client.deactivate();
        this.connected = false;
      }
    } catch { /* 静默 */ }
  }

  isConnected() {
    return this.connected;
  }
}

export default new WebSocketService();
