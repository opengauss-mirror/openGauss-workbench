export type Callback = (e: Event) => void
export type MessageCallback<RT> = (e: RT) => void

interface Ioptions<RT> {
  url: string | null // The address of the linked channel
  heartTime?: number // heartbeat interval
  heartMsg?: string // Heartbeat information, the default is 'ping'
  isReconnect?: boolean // Whether to automatically reconnect
  isRestory?: boolean // whether to destroy
  reconnectTime?: number // reconnection interval
  reconnectCount?: number // The number of reconnections -1 is unlimited
  openCb?: Callback // callback for successful connection
  closeCb?: Callback // Close callback
  messageCb?: MessageCallback<RT> // message callback
  errorCb?: Callback // error callback
}

/**
 * heartbeat base class
 */

export class Heart {
  heartTimeOut!: number // heartbeat timer
  ServerHeartTimeOut!: number // heartbeat timer
  timeout = 5000
  // reset
  reset (): void {
    clearTimeout(this.heartTimeOut)
    clearTimeout(this.ServerHeartTimeOut)
  }

  /**
   * start heartbeat
   * @param {Function} cb
   */
  start (cb: Callback): void {
    this.heartTimeOut = setTimeout((e: Event) => {
      cb(e)
      this.ServerHeartTimeOut = setTimeout((e: Event) => {
        cb(e)
        // restart detection
        this.reset()
        this.start(cb)
      }, this.timeout)
    }, this.timeout)
  }
}

export default class Socket<T, RT> extends Heart {
  ws!: WebSocket

  reconnectTimer = 0 // reconnect timer
  reconnectCount = 10 // Variables are saved to prevent loss

  options: Ioptions<RT> = {
    url: null, // The address of the linked channel
    heartTime: 5000, // heartbeat interval
    heartMsg: 'ping', // Heartbeat information, the default is 'ping'
    isReconnect: true, // Whether to automatically reconnect
    isRestory: false, // whether to destroy
    reconnectTime: 5000, // reconnection interval
    reconnectCount: 5, // The number of reconnections -1 is unlimited
    openCb: (e: Event) => {
      console.log('callback for successful connection::::', e)
    }, // callback for successful connection
    closeCb: (e: Event) => {
      console.log('Close callback::::', e)
    }, // Close callback
    messageCb: (e: RT) => {
      console.log('message callback::::', e)
    }, // message callback
    errorCb: (e: Event) => {
      console.log('error callback::::', e)
    } // error callback
  }

  constructor (ops: Ioptions<RT>) {
    super()
    Object.assign(this.options, ops)
    this.create()
  }

  /**
   * establish connection
   */
  create (): void {
    if (!('WebSocket' in window)) {
      throw new Error('The current browser does not support it and cannot be used')
    }
    if (!this.options.url) {
      throw new Error('The address does not exist, and the channel cannot be established')
    }
    // get location host
    console.log('get locaion host: ', window.location.host)
    let wsUrl
    if (process.env.NODE_ENV === 'development') {
      wsUrl = `wss://${process.env.VUE_APP_WS_BASE_URL}/websocket/${this.options.url}`
    } else {
      const wsPrefix = window.location.protocol.includes('https') ? 'wss' : 'ws'
      wsUrl = `${wsPrefix}://${window.location.host}/websocket/${this.options.url}`
    }
    this.ws = new WebSocket(wsUrl)
    this.onopen(this.options.openCb as Callback)
    this.onclose(this.options.closeCb as Callback)
    this.onmessage(this.options.messageCb as MessageCallback<RT>)
  }

  /**
   * Custom connection success event
   * If the callback exists, call the callback, if there is no callback in OPTIONS
   * @param {Function} callback
   */
  onopen (callback: Callback): void {
    this.ws.onopen = event => {
      if (typeof callback === 'function') {
        callback(event)
      } else {
        typeof this.options.openCb === 'function' && this.options.openCb(event)
      }
    }
  }

  /**
   * custom close event
   * If the callback exists, call the callback, if there is no callback in OPTIONS
   * @param {Function} callback
   */
  onclose (callback: Callback): void {
    this.ws.onclose = event => {
      if (typeof callback === 'function') {
        callback(event)
      } else {
        typeof this.options.closeCb === 'function' && this.options.closeCb(event)
      }
    }
  }

  /**
   * custom error event
   * If the callback exists, call the callback, if there is no callback in OPTIONS
   * @param {Function} callback
   */
  onerror (callback: Callback): void {
    this.ws.onerror = event => {
      if (typeof callback === 'function') {
        callback(event)
      } else {
        typeof this.options.errorCb === 'function' && this.options.errorCb(event)
      }
    }
  }

  /**
   * Custom message monitoring event
   * If the callback exists, call the callback, if there is no callback in OPTIONS
   * @param {Function} callback
   */
  onmessage (callback: MessageCallback<any>): void {
    this.ws.onmessage = (event: MessageEvent<string>) => {
      const strMessage = event.data
      if (typeof callback === 'function') {
        callback(strMessage)
      }
    }
  }

  /**
   * Custom send message event
   * @param {String} data text sent
   */
  send (data: T | string): void {
    console.log('doNothing', data)
  }

  /**
   * destroy
   */
  destroy (): void {
    super.reset()
    clearTimeout(this.reconnectTimer) // clear reconnection timer
    this.options.isRestory = true
    this.ws.close()
  }
}
