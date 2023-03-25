<template>
  <a-modal
    title="Portal安装"
    v-model:visible="visible"
    width="500px"
    modal-class="add-portal-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item field="hostUserId" label="安装用户" :rules="[{
        required: true,
        message: '安装用户不能为空'
      }]">
        <a-select v-model="form.hostUserId" placeholder="请选择安装用户">
          <a-option v-for="item in hostUserData" :key="item.hostUserId" :value="item.hostUserId">{{ item.username }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="installPath" label="安装目录" :rules="[{
        required: true,
        message: '安装目录不能为空'
      }]">
        <a-input v-model="form.installPath" placeholder="请输入安装目录" />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="cancel">取消</a-button>
        <a-button type="primary" :disabled="loading" style="margin-left: 16px;" @click="confirmSubmit">确定</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch, onMounted } from 'vue'
// import { Message } from '@arco-design/web-vue'
import { hostUsers, installPortal } from '@/api/task'

const props = defineProps({
  open: Boolean,
  hostId: String
})

const emits = defineEmits(['update:open', 'startInstall'])

const formRef = ref()
const form = reactive({
  hostUserId: undefined,
  installPath: '~'
})

const visible = ref(false)
const loading = ref(false)
const hostUserData = ref([])

watch(visible, (v) => {
  emits('update:open', v)
})

watch(() => props.open, (v) => {
  if (v) {
    hostUsers(props.hostId).then(res => {
      hostUserData.value = res.data
    })
  }
  visible.value = v
})

const cancel = () => {
  visible.value = false
}

const confirmSubmit = () => {
  formRef.value?.validate(valid => {
    if (!valid) {
      const params = {
        hostUserId: form.hostUserId,
        installPath: form.installPath
      }
      loading.value = true
      installPortal(props.hostId, params).then(() => {
        emits('startInstall')
        visible.value = false
        loading.value = false
      }).catch(() => {
        loading.value = false
      })
    }
  })
}

onMounted(() => {
  visible.value = props.open
})
</script>

<style lang="less" scoped>
.add-portal-modal {
  .modal-footer {
    text-align: center;
  }
}
</style>
