
<template>
  <a-modal
    class="share-container"
    :visible="visible"
    :title="$t('components.Share.5m7ipfzhwh40')"
    :ok-text="$t('components.Share.5m7ipfzhx540')"
    :cancel-text="$t('components.Share.5m7ipfzhx9g0')"
    @cancel="close"
    @ok="copy"
    width="700px"
  >
    <div class="share-content" @click="jumpTo">
      {{ url }}
    </div>
  </a-modal>
</template>
<script setup lang="ts">
import { Message } from '@arco-design/web-vue'
import { ref } from 'vue'
import Base64 from '../../../../../../../utils/base64'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const base64 = new Base64()
const visible = ref<boolean>(false)
const url = ref<string>('')
const open = (data: any) => {
  let domain = window.location.href.split('modeling/dataflow')[0]
  if (domain.includes('#/')) domain = window.location.href.split('#/')[0]
  url.value = `${domain}modeling/visualization/report/share/${base64.encode(data.id + '|d1a2ay1k0|')}`
  visible.value = true
}
const close = () => {
  visible.value = false
}
const copy = () => {
  const text = url.value
  const input = document.createElement('input')
  input.value = text
  document.body.appendChild(input)
  input.select()
  document.execCommand('copy')
  document.body.removeChild(input)
  Message.success({ content: t('components.Share.5m7ipfzhxd40') })
}
const jumpTo = () => {
  window.open(url.value, '_blank')
}
defineExpose({ open })
</script>
<style scoped lang="less">
  .share-container {
    .share-content {
      background-color: var(--color-neutral-2);
      padding: 5px 10px;
      cursor: pointer;
      word-break: break-all;
    }
  }
</style>
