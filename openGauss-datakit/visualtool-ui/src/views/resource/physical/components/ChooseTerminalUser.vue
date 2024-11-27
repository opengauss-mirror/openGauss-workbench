<template>
  <a-modal
    :mask-closable="false"
    :visible="visible"
    :title="$t('components.HostUserChoose.title')"
    :unmount-on-close="true"
    @ok="handleOk"
    @cancel="close"
  >
    <div  class="tip">{{ $t('components.HostUserChoose.tip') }}</div>
        <a-select :placeholder="$t('components.HostUserChoose.userChoosePlaceholder')" v-model="username">
          <a-option v-for="( item, index ) in options" :value="item.username">{{ item.username }}</a-option>
        </a-select>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const username = ref('')
const options = ref([])

const visible = ref(false)
const openChooseUser = (rows) => {
  visible.value = true
  username.value = rows[0]?.username
  options.value = rows
}

const close = () => {
  visible.value = false
}

const emits = defineEmits(['getUser'])
const handleOk = () => {
  emits('getUser', username.value)
  visible.value = false
}

defineExpose({ openChooseUser })
</script>

<style lang="less" scoped>
.tip{
  padding-bottom: 4px;
}

</style>
