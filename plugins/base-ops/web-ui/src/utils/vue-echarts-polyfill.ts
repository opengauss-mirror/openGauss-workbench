if (typeof document !== 'undefined') {
  try {
    const descriptor = Object.getOwnPropertyDescriptor(
      Document.prototype,
      'adoptedStyleSheets'
    )
    if (descriptor?.set) {
      const originalSetter = descriptor.set
      Object.defineProperty(Document.prototype, 'adoptedStyleSheets', {
        ...descriptor,
        set(sheets: CSSStyleSheet[]) {
          try {
            originalSetter.call(this, sheets)
          } catch {
            console.warn('[vue-echarts-polyfill] adoptedStyleSheets not supported in sandbox environment')
          }
        }
      })
    }
  } catch {
    console.warn('[vue-echarts-polyfill] Document.prototype is not accessible')
  }
}
