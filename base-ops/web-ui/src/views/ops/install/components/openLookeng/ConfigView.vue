<template>
  <div>
    <p>您可以在下面的编辑器中编辑分片配置文件</p>
    <Codemirror
      v-model:value="code"
      :options="cmOptions"
      border
      ref="cmRef"
      height="500px"
      width="100%"
      @change="onChange"
      @input="onInput"
      @ready="onReady"
    >
    </Codemirror>
  </div>
</template>
<script lang="ts" setup>
import { mockGenerateYaml } from '@/api/ops/mock'
import { Message } from '@arco-design/web-vue'
import { ref, onMounted, onUnmounted } from 'vue'
import 'codemirror/mode/yaml/yaml.js'
import Codemirror from 'codemirror-editor-vue3'
import type { CmComponentRef } from 'codemirror-editor-vue3'
import type { Editor, EditorConfiguration } from 'codemirror'
import 'codemirror/theme/monokai.css'

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

const cmRef = ref<CmComponentRef>()
const cmOptions: EditorConfiguration = {
  mode: 'text/x-yaml',
  theme: 'default',
  lineNumbers: true,
  lineWrapping: true
}

const onChange = (val: string, cm: Editor) => {
  console.log(val)
  console.log(cm.getValue())
}

const onInput = (val: string) => {
  console.log(val)
}

const onReady = (cm: Editor) => {
  console.log(cm.focus())
}

onMounted(() => {
  setTimeout(() => {
    cmRef.value?.refresh()
  }, 1000)
  setTimeout(() => {
    cmRef.value?.cminstance.isClean()
  }, 3000)
})

onUnmounted(() => {
  cmRef.value?.destroy()
})

defineExpose({
  beforeConfirm
})
</script>
<style lang="less">
.codemirror-container {
  font-size: 16px !important;
}
</style>
