import { ref } from 'vue'


const useConfirm = () => {
  const nextConfirmVisible = ref(false)
  const confirmMessage = ref('')

  let confirmPromiseResolve: ((value: boolean) => void) | null = null
  const waitConfirm = async () => {
    const confirmPromise = new Promise<boolean>(resolve => {
      confirmPromiseResolve = resolve
    })
    try {
      const confirmed = await confirmPromise
      return confirmed
    } catch (error) {
      return false
    }
  }

  const resolveAction = (confirmed: boolean) => {
    nextConfirmVisible.value = confirmed
    if (confirmPromiseResolve) {
      confirmPromiseResolve(confirmed)
    }
  }

  return {
    nextConfirmVisible,
    confirmMessage,
    waitConfirm,
    resolveAction
  }
}

export default useConfirm
