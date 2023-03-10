<template>
  <a-modal
    v-if="visible"
    :title="$t('components.EditSysSettings.5mpivn7fwjc1')"
    v-model:visible="visible"
    width="500px"
    title-align="start"
    modal-class="user-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item field="uploadPath"
                   :label="$t('components.EditSysSettings.5mpivn7fwjc2')"
                   :rules="[{required: true, message: $t('components.EditSysSettings.5mpivn7fwjc6')}, { validator: pathValidator }]">
        <a-input v-model.trim="form.uploadPath" :placeholder="$t('components.EditSysSettings.5mpivn7fwjc6')"/>
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="visible = false">{{ $t('components.EditCode.5m6nfosuskg0') }}</a-button>
        <a-button type="primary" :loading="loading" style="margin-left: 16px;" @click="confirmSubmit">
          {{ $t('components.EditCode.5m6nfosuspc0') }}
        </a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import {watch, ref, reactive, onMounted} from 'vue'
import {Message} from '@arco-design/web-vue'
import {FormInstance} from '@arco-design/web-vue/es/form'
import {listSysSetting, updateSysSetting} from '@/api/sysSetting'
import {SysSetting} from '@/types/sysSetting'
import {useI18n} from 'vue-i18n'

const {t} = useI18n()

const props = withDefaults(defineProps<{
  open: boolean,
  options?: any
}>(), {
  open: false,
  options: {}
})

const emits = defineEmits(['update:open'])

const formRef = ref<FormInstance>()
let form = reactive<SysSetting>({
  id: '',
  uploadPath: ''
})
const visible = ref<boolean>(false)
const loading = ref<boolean>(false)

watch(visible, (v) => {
  emits('update:open', v)
})

watch(() => props.open, (v) => {
  if (v) {
    formRef.value?.resetFields()
    querySystemSetting()
  }
  visible.value = v
})

const pathValidator = (value: any, cb: any) => {
  return new Promise(resolve => {
    const reg = /^\/([^/\0?]+(\/[^/\0?]+)*(\/[^/\0?]+\?)?)?$/
    const re = new RegExp(reg)
    if (re.test(value)) {
      resolve(true)
    } else {
      cb(t('components.EditSysSettings.5mpivn7fwjc9'))
      resolve(false)
    }
  })
}

const confirmSubmit = async () => {
  loading.value = true
  formRef.value?.validate(valid => {
    if (!valid) {
      updateSysSetting(form).then(() => {
        Message.success('system setting update success')
        visible.value = false
      }).finally(() => {
        loading.value = false
      })
    }
  })
}

const querySystemSetting = () => {
  listSysSetting().then(res => {
    form.id = res.data?.id
    form.uploadPath = res.data?.uploadPath
  })
}

onMounted(() => {
  visible.value = props.open
})
</script>

<style lang="less" scoped>
.user-modal {
  .modal-footer {
    text-align: center;
  }
}
</style>
