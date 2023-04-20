<template>
  <a-modal
    :title="$t('components.PortalInstall.5q0aajl75f00')"
    v-model:visible="visible"
    width="500px"
    modal-class="add-portal-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item field="hostUserId" :label="$t('components.PortalInstall.5q0aajl76580')" :rules="[{
        required: true,
        message: $t('components.PortalInstall.5q0aajl76io0')
      }]">
        <a-select v-model="form.hostUserId" :placeholder="$t('components.PortalInstall.5q0aajl76ug0')">
          <a-option v-for="item in hostUserData" :key="item.hostUserId" :value="item.hostUserId">{{ item.username }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="installPath" :label="$t('components.PortalInstall.5q0aajl76xw0')" :rules="[{
        required: true,
        message: $t('components.PortalInstall.5q0aajl776k0')
      }]">
        <a-input v-model="form.installPath" :placeholder="$t('components.PortalInstall.5q0aajl77f40')" />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="cancel">{{$t('components.PortalInstall.5q0aajl77ik0')}}</a-button>
        <a-button type="primary" :disabled="loading" style="margin-left: 16px;" @click="confirmSubmit">{{$t('components.PortalInstall.5q0aajl77lg0')}}</a-button>
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
