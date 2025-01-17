import { wsHeartbeatTime } from '@/config';

export default class WebSocketClass {
  ws: WebSocket;
  name = null;
  sessionId: null;
  instance = null;
  callback = null;
  connected = false;
  setIntervalWesocketPush = null;
  static instance: any;

  static getInstance(name, sessionId) {
    if (!this.instance) {
      this.instance = new WebSocketClass(name, sessionId);
    }
    return this.instance;
  }

  constructor(name: string, sessionId, callback?) {
    this.name = name;
    this.sessionId = sessionId;
    this.instance = null;
    this.connect(name, sessionId, callback);
  }

  connect(name: string, sessionId, callback?) {
    if (!window.WebSocket) {
      return console.log('Your browser does not support WebSocket');
    }
    const baseURL = import.meta.env.DEV
      ? `${import.meta.env.VITE_WS_BASE_URL}`
      : `${location.protocol == 'http:' ? 'ws:' : 'wss:'}//${location.host}`;
    const pluginName = import.meta.env.VITE_PLUGIN_NAME;
    const url = `${baseURL}/ws/${pluginName ? pluginName + '/' : ''}${sessionId}`;
    this.ws = new WebSocket(url);

    this.ws.onopen = () => {
      this.connected = true;
      this.sendPing();
    };
    if (callback) this.callback = callback;

    this.ws.onclose = () => {
      this.connected = false;
      clearInterval(this.setIntervalWesocketPush);
    };

    this.ws.onmessage = (msg: any) => {
      const res = JSON.parse(msg.data);
      if (this.callback) {
        this.callback.call(this, res);
      }
    };

    this.ws.onerror = () => {
      if (this.ws.readyState !== 3) {
        this.connect(name, sessionId);
      }
    };
  }

  registerCallBack(callBack) {
    this.callback = callBack;
  }

  unRegisterCallBack() {
    this.callback = null;
  }

  send(data) {
    if (!!this.ws && this.ws.readyState === 3) {
      this.ws.close();
      this.connect(this.name, this.sessionId);
    } else if (this.ws.readyState === 1) {
      this.ws.send(JSON.stringify(data));
    } else if (this.ws.readyState === 0) {
      this.connecting(data);
    }
  }

  sendPing(time = wsHeartbeatTime, ping = 'ping') {
    clearInterval(this.setIntervalWesocketPush);
    this.ws.send(ping);
    this.setIntervalWesocketPush = setInterval(() => {
      this.ws.send(ping);
    }, time);
  }

  // When sending data but the connection is not established, it will be processed and wait for retransmission
  connecting(message: any) {
    setTimeout(() => {
      if (this.ws.readyState === 0) {
        this.connecting(message);
      } else if (this.ws.readyState === 3) {
        return;
      } else {
        this.ws.send(JSON.stringify(message));
      }
    }, 100);
  }

  close() {
    this.ws.close();
  }
}
