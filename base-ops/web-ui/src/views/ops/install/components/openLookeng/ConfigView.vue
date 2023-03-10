<template>
  <div>
    <p>您可以在下面的编辑器中编辑分片配置文件</p>
    <a-textarea allow-clear v-model:value="code.value" :auto-size="{minRows:25, maxRows:30}" />
  </div>
</template>
<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { mockGenerateYaml } from '@/api/ops/mock'
import { Message } from '@arco-design/web-vue'

const beforeConfirm = async (): Promise<boolean> => {
  return true
}

const code = ref('')

onMounted(() => {
  generateYaml()
})

const generateYaml = () => {
  mockGenerateYaml().then(res => {
    if (Number(res.code) === 200) {
      code.value = res.data
      console.log(code.value)
    } else {
      Message.error('Failed to obtain the tar list data')
    }
  })
}

defineExpose({
  beforeConfirm
})
</script>
