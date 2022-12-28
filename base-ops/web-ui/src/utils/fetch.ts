// Credentials carrying login status must be include
export default function fetch (url: any, options: any) {
  return window.fetch(url, { ...options, credentials: 'omit' })
}
