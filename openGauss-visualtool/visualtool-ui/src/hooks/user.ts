import { Message } from '@arco-design/web-vue'

import { useUserStore } from '@/store'

export default function useUser () {
  const userStore = useUserStore()
  const logout = async () => {
    await userStore.logout()
    Message.success('Logout success')
    window.location.href = '/'
  }
  return {
    logout
  }
}
