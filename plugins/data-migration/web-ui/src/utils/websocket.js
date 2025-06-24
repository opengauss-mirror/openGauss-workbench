class Heart {
  heartTimeOut
  ServerHeartTimeOut
  timeout = 5000
  reset () {
    clearTimeout(this.heartTimeOut)
    clearTimeout(this.ServerHeartTimeOut)
  }
  start (cb) {
    this.heartTimeOut = setTimeout(() => {
      cb()
      this.ServerHeartTimeOut = setTimeout(() => {
        cb()
        this.reset()
        this.start(cb)
      }, this.timeout)
    }, this.timeout)
  }
}

export default class SocketTool extends Heart {
  constructor (ops) {
    super()
    Object.assign(this.options, ops)
    this.create()
  }

  ws
  reconnectTimer = 0
  reconnectCount = 10
  options = {
    url: null,
    heartTime: 5000,
    heartMsg: 'ping',
    isReconnect: true,
    isRestory: false,
    reconnectTime: 5000,
    reconnectCount: 5,
    openCb: (e) => {
      console.log('connect success callback::::', e)
    },
    closeCb: (e) => {
      console.log('close callback::::', e)
    },
    messageCb: (e) => {
      console.log('message callback::::', e)
    },
    errorCb: (e) => {
      console.log('error callback::::', e)
    }
  }

  create () {
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
      wsUrl = `wss://${process.env.VUE_APP_WS_BASE_URL}/ws/${this.options.url}`
    } else {
      const wsPrefix = window.location.protocol.includes('https') ? 'wss' : 'ws'
      wsUrl = `${wsPrefix}://${window.location.host}/ws/${this.options.url}`
    }
    this.ws = new WebSocket(wsUrl)
    this.onopen(this.options.openCb)
    this.onclose(this.options.closeCb)
    this.onmessage(this.options.messageCb)
  }

  onopen (callback) {
    this.ws.onopen = event => {
      if (typeof callback === 'function') {
        callback(event)
      } else {
        typeof this.options.openCb === 'function' && this.options.openCb(event)
      }
    }
  }

  onclose (callback) {
    this.ws.onclose = event => {
      if (typeof callback === 'function') {
        callback(event)
      } else {
        typeof this.options.closeCb === 'function' && this.options.closeCb(event)
      }
    }
  }

  onerror (callback) {
    this.ws.onerror = event => {
      if (typeof callback === 'function') {
        callback(event)
      } else {
        typeof this.options.errorCb === 'function' && this.options.errorCb(event)
      }
    }
  }

  onmessage (callback) {
    this.ws.onmessage = (event) => {
      const strMessage = event.data
      if (typeof callback === 'function') {
        callback(strMessage)
      }
    }
  }

  send (data) {
    console.log('doNothing', data)
  }

  destroy () {
    super.reset()
    clearTimeout(this.reconnectTimer)
    this.options.isRestory = true
    this.ws.close()
  }
}
