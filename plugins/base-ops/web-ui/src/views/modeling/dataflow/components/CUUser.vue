
<template>
  <a-modal
    class="cu-user-container"
    v-model:visible="dData.show" :title="dData.title"
    :ok-text="$t('modeling.components.CUUser.5mpu44y3ic40')" :confirm-loading="dData.loading" :cancel-text="$t('modeling.components.CUUser.5mpu44y3iso0')"
    @ok="submit" @cancel="close" :modal-style="{ width: '400px' }"
  >
    <div class="d-content">
      <div class="d-row" v-for="(item, key) in dData.userList" :key="`userlist${key}`">
        <a-checkbox v-model="item.selected">{{ item.name }}</a-checkbox>
        <div class="select-role" v-show="item.selected">
          <a-select v-model="item.role">
            <a-option v-for="(item, key) in dData.roles" :key="`role${key}`" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </div>
      </div>
    </div>
  </a-modal>
</template>
<script setup lang="ts">
import { reactive } from 'vue'
import { Modal as AModal, Checkbox as ACheckbox, Select as aSelect } from '@arco-design/web-vue'
import { KeyValue } from '@/api/modeling/request'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const AOption = aSelect.Option
const userList: Array<KeyValue> = [
]
const emits = defineEmits([`success`])
const dData = reactive({
  show: false, title: t('modeling.components.CUUser.5mpu44y3ivw0'), loading: false,
  userList: [] as Array<KeyValue>,
  roles: [] as Array<KeyValue>
})
const close = () => {
  dData.show = false
}
const open = (roles: Array<KeyValue>, tuserList: Array<KeyValue>) => {
  dData.show = true
  dData.roles = roles
  dData.userList = userList.filter((item: KeyValue) => {
    return tuserList.findIndex((item2: KeyValue) => item2.name === item.name) === -1
  })
}
const submit = () => {
  emits(`success`, JSON.parse(JSON.stringify(dData.userList.filter(item => item.selected))))
  close()
}
defineExpose({ open })
</script>
<style scoped lang="less">
  .cu-user-container {
    .d-content {
      .d-row {
        display: flex;
        align-items: center;
        height: 40px;
        cursor: pointer;
        .selected {
          margin-right: 10px;
        }
        display: flex;
        .select-role {
          margin-left: auto;
          width: 200px;
        }
      }
    }
  }
</style>
