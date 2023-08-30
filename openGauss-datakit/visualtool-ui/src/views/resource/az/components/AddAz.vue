<template>
  <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title"
    :ok-loading="data.loading" :modal-style="{ width: '650px' }" @cancel="close">
    <template #footer>
      <div class="flex1">

        <div v-show="data.showButton === 'show'">
          <a-button :loading="data.loading" class="mr" @click="close">{{
            $t('components.AddAz.5mpib0ipo0g0')
          }}</a-button>
          <a-button :loading="data.loading" type="primary" @click="submit">{{
            $t('components.AddAz.5mpib0ipoo40')
          }}</a-button>
        </div>
      </div>

    </template>
    <a-form :model="data.formData" ref="formRef" :label-col="{ style: { width: '90px' } }" :rules="formRules"
      :disabled="data.disabled">
      <a-form-item field="name" :label="$t('components.AddAz.azName')" validate-trigger="blur">
        <a-input v-model.trim="data.formData.name" :placeholder="$t('components.AddAz.5mpib0ipov80')"></a-input>
      </a-form-item>
      <a-form-item :label="$t('components.AddAz.5mpib0ipozw0')" field="address" validate-trigger="blur">
        <a-input v-model.trim="data.formData.address" :placeholder="$t('components.AddAz.5mpib0ipp3k0')"></a-input>
      </a-form-item>
      <a-form-item :label="$t('components.AddAz.5mpib0ippfc0')">
        <a-textarea v-model.trim="data.formData.remark" :placeholder="$t('components.AddAz.5mpib0ippjg0')"></a-textarea>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { nextTick, reactive, ref, computed } from 'vue'
import { addAz, editAz, hasNameAZ } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
enum hostStatusEnum {
  unTest = -1,
  success = 1,
  fail = 0
}

const data = reactive({
  show: false,
  title: t('components.AddAz.5mpib0ippoc0'),
  showButton: 'show',
  loading: false,
  status: hostStatusEnum.unTest,
  oldname: '',
  formData: {
    azId: '',
    name: '',
    address: '',
    remark: ''
  },
  disabled: false
})

const formRules = computed(() => {
  return {
    name: [{ required: true, 'validate-trigger': 'blur', message: t('components.AddAz.5mpib0ipov80') },
    {
      validator: (value: any, cb: any) => {
        return new Promise(resolve => {
          if (value !== data.oldname) {
            const param = {
              name: value
            }
            hasNameAZ(param).then((res: KeyValue) => {
              if (Number(res.code) === 200) {
                if (res.data) {
                  cb(t('components.AddAz.else1'))
                  resolve(false)
                } else {
                  resolve(true)
                }
              } else {
                cb(t('components.AddAz.else2'))
                resolve(false)
              }
            })
          } else {
            resolve(true)
          }
        })
      }
    }
    ],
    address: [{ required: true, 'validate-trigger': 'blur', message: t('components.AddAz.5mpib0ipp3k0') }]
  }
})

const emits = defineEmits([`finish`])
const formRef = ref<null | FormInstance>(null)
const submit = () => {
  formRef.value?.validate().then(result => {
    if (!result) {
      data.loading = true
      if (data.formData.azId) {
        editAz(data.formData.azId, data.formData).then((res: KeyValue) => {
          if (Number(res.code) === 200) {
            Message.success({ content: `Modified success` })
            emits(`finish`)
          }
          close()
        }).finally(() => {
          data.loading = false
        })
      } else {
        addAz(data.formData).then((res: KeyValue) => {
          if (Number(res.code) === 200) {
            Message.success({ content: `Created success` })
            emits(`finish`)
          }
          close()
        }).finally(() => {
          data.loading = false
        })
      }
    }
  }).catch()
}
const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
}

const open = (type: string, editData?: KeyValue) => {
  data.show = true
  data.disabled = false
  data.status = hostStatusEnum.unTest
  data.loading = false
  if (type === 'update' && data) {
    data.oldname = editData?.name
    data.title = t('components.AddAz.5mpib0ipprs0')
    Object.assign(data.formData, editData)
  } else {
    data.oldname = ''
    data.title = t('components.AddAz.5mpib0ippoc0')
    Object.assign(data.formData, {
      privateIp: '',
      publicIp: '',
      password: '',
      azId: '',
      remark: ''
    })
  }
}

defineExpose({
  open
})

</script>
<style>
.flex1 {
  display: flex;
  justify-content: center;
}
</style>
