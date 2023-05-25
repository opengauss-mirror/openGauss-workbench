import { computed, ref } from 'vue'

const theme = ref('')

export default function useTheme () {
  const currentTheme = computed(() => {
    return theme.value
  })
  const changeTheme = (value) => {
    theme.value = value
  }
  return {
    currentTheme,
    changeTheme
  }
}
