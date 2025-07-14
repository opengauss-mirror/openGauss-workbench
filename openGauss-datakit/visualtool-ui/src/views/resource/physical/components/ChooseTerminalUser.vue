<template>
  <el-dialog
    :mask-closable="false"
    v-model="visible"
    :title="$t('components.HostUserChoose.title')"
    @ok="handleOk"
    :before-close="close"
  >
    <div class="tip">{{ $t('components.HostUserChoose.tip') }}</div>
    <el-select class="selectUser" :placeholder="$t('components.HostUserChoose.userChoosePlaceholder')" v-model="username">
      <el-option v-for="( item, index ) in options" :value="item.username">{{ item.username }}</el-option>
    </el-select>
    <template #footer>
      <span class="dialog-footer">
        <el-button class="o-dlg-btn" type="primary" size="small" @click="handleOk">{{ t('components.HostUserMng.5mpi1bru2lo0') }}</el-button>
        <el-button class="o-dlg-btn" size="small" @click="close">{{ t('components.HostUserMng.5mpi1bru2s00') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
.selectUser {
  width: 100%;
  margin-top: 8px;
}
</style>
