<template>
  <a-modal
    :title="$t('components.EditConfig.5mjace1rbj80')"
    v-model:visible="visible"
    width="500px"
    title-align="start"
    modal-class="logConfig-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item :label="$t('components.EditConfig.5mjace1rc240')" :rules="[{required: true}]">
        <a-select v-model="form.level">
           <a-option value="debug">DEBUG</a-option>
           <a-option value="info">INFO</a-option>
           <a-option value="warn">WARN</a-option>
           <a-option value="error">ERROR</a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="maxHistory" :label="$t('components.EditConfig.5mjace1rc9c0')" :rules="[
          {
            required: true,
            message: $t('components.EditConfig.5mjace1rcd40')
          },
          {
            type: 'number',
            min: 1,
            message: $t('components.EditConfig.5mjace1rch40')
          }
        ]">
        <a-input v-model="form.maxHistory" type="number"></a-input>
      </a-form-item>
      <a-form-item field="maxFileSize" :label="$t('components.EditConfig.5mjace1rckw0')" :help="$t('components.EditConfig.5mjace1rd9o0')" :rules="[
          {
            required: true,
            message: $t('components.EditConfig.5mjace1rco40')
          },
          {
            match: /^\d+(kb|mb|gb)$/,
            message: $t('components.EditConfig.5mjace1rcr80')
          }
        ]">
        <a-input v-model="form.maxFileSize"></a-input>
      </a-form-item>
      <a-form-item field="totalSizeCap" :label="$t('components.EditConfig.5mjace1rcuk0')" :help="$t('components.EditConfig.5mjace1rd9o0')" :rules="[
          {
            required: true,
            message: $t('components.EditConfig.5mjace1rcxg0')
          },
          {
            match: /^\d+(kb|mb|gb)$/,
            message: $t('components.EditConfig.5mjace1rcr80')
          }
        ]">
        <a-input v-model="form.totalSizeCap"></a-input>
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="visible = false">{{$t('components.EditConfig.5mjace1rd0c0')}}</a-button>
        <a-button type="primary" :disabled="loading" style="margin-left: 16px;" @click="confirmSubmit">{{$t('components.EditConfig.5mjace1rd3o0')}}</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { watch, ref, reactive, onMounted } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { FormInstance } from '@arco-design/web-vue/es/form'
  import { saveLogConfig } from '@/api/sysLog'

  const props = withDefaults(defineProps<{
    open: boolean,
    configs?: any
  }>(), {
    open: false,
    configs: {}
  })

  const emits = defineEmits(['update:open', 'ok'])

  const formRef = ref<FormInstance>()
  const form = reactive<any>({})

  const visible = ref<boolean>(false)
  const loading = ref<boolean>(false)

  watch(visible, (v) => {
    emits('update:open', v)
  })

  watch(() => props.open, (v) => {
    if (v) {
      if (props.configs.level) {
        form['level'] = props.configs.level
        form['maxHistory'] = props.configs.maxHistory
        form['maxFileSize'] = props.configs.maxFileSize
        form['totalSizeCap'] = props.configs.totalSizeCap
      }
    }
    visible.value = v
  })

  const confirmSubmit = () => {
    formRef.value?.validate(erros => {
      console.info(erros)
      if (!erros) {
        saveLogConfig({
          level: form.level,
          maxHistory: form.maxHistory,
          totalSizeCap: form.totalSizeCap,
          maxFileSize: form.maxFileSize
        }).then(() => {
          Message.success('Saved success')
          emits('ok')
          visible.value = false
        })
      }
    })
  }

  onMounted(() => {
    visible.value = props.open
  })
</script>

<style lang="less" scoped>
.logConfig-modal {
  .modal-footer {
    text-align: center;
  }
}
</style>
