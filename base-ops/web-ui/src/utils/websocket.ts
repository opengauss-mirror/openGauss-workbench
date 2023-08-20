export type Callback = (e: Event) => void
export type MessageCallback<RT> = (e: RT) => void

interface Ioptions<RT> {
  url: string | null
  heartTime?: number
  heartMsg?: string
  isReconnect?: boolean
  isRestory?: boolean
  reconnectTime?: number
  reconnectCount?: number
  openCb?: Callback
  closeCb?: Callback
  messageCb?: MessageCallback<RT>
  errorCb?: Callback
}

export class Heart {
  heartTimeOut!: number
  ServerHeartTimeOut!: number
  timeout = 5000
  reset(): void {
    clearTimeout(this.heartTimeOut)
    clearTimeout(this.ServerHeartTimeOut)
  }

  start(cb: Callback): void {
    this.heartTimeOut = setTimeout((e: Event) => {
      cb(e)
      this.ServerHeartTimeOut = setTimeout((e: Event) => {
        cb(e)
        this.reset()
        this.start(cb)
      }, this.timeout)
    }, this.timeout)
  }
}

export default class Socket<T, RT> extends Heart {
  ws!: WebSocket

  reconnectTimer = 0
  reconnectCount = 10

  options: Ioptions<RT> = {
    url: null,
    heartTime: 5000,
    heartMsg: 'ping',
    isReconnect: true,
    isRestory: false,
    reconnectTime: 5000,
    reconnectCount: 5,
    openCb: (e: Event) => {
      console.log('connect success callback::::', e)
    },
    closeCb: (e: Event) => {
      console.log('close callback::::', e)
    },
    messageCb: (e: RT) => {
      console.log('message callback::::', e)
    },
    errorCb: (e: Event) => {
      console.log('error callback::::', e)
    }
  }

  constructor(ops: Ioptions<RT>) {
    super()
    Object.assign(this.options, ops)
    this.create()
  }

  create(): void {
    if (!('WebSocket' in window)) {
      throw new Error('The current browser does not support it and cannot be used')
    }
    if (!this.options.url) {
      throw new Error('The address does not exist, and the channel cannot be established')
    }
    // this.ws = null
    console.log('get locaion host: ', window.location.host, window.location.protocol)
    let wsUrl
    if (process.env.NODE_ENV === 'development') {
      // change by yourself
      wsUrl = `ws://${process.env.VUE_APP_WS_BASE_URL}/ws/base-ops/${this.options.url}`
    } else {
      const wsPrefix = window.location.protocol.includes('https') ? 'wss' : 'ws'
      wsUrl = `${wsPrefix}://${window.location.host}/ws/base-ops/${this.options.url}`
    }
    this.ws = new WebSocket(wsUrl)
    this.onopen(this.options.openCb as Callback)
    this.onclose(this.options.closeCb as Callback)
    this.onmessage(this.options.messageCb as MessageCallback<RT>)
  }

  onopen(callback: Callback): void {
    this.ws.onopen = event => {
      if (typeof callback === 'function') {
        callback(event)
      } else {
        typeof this.options.openCb === 'function' && this.options.openCb(event)
      }
    }
  }

  onclose(callback: Callback): void {
    this.ws.onclose = event => {
      if (typeof callback === 'function') {
        callback(event)
      } else {
        typeof this.options.closeCb === 'function' && this.options.closeCb(event)
      }
    }
  }

  onerror(callback: Callback): void {
    this.ws.onerror = event => {
      if (typeof callback === 'function') {
        callback(event)
      } else {
        typeof this.options.errorCb === 'function' && this.options.errorCb(event)
      }
    }
  }

  onmessage(callback: MessageCallback<any>): void {
    this.ws.onmessage = (event: MessageEvent<string>) => {
      const strMessage = event.data
      if (typeof callback === 'function') {
        callback(strMessage)
      }
    }
  }

  send(data: T | string): void {
    console.log('doNothing', data)
  }

  destroy(): void {
    super.reset()
    clearTimeout(this.reconnectTimer)
    this.options.isRestory = true
    this.ws.close()
  }
}
