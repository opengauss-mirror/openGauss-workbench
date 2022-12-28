<template>
  <a-modal
    :title="$t('components.PluginConfig.5m6nj19s0700')"
    v-model:visible="visible"
    width="500px"
    title-align="start"
    modal-class="plugin-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <template v-for="(item, index) in configAttrs" :key="index">
        <a-form-item :field="item.attrCode" :label="item.attrLabel" :rules="[{
          required: true,
          message: $t('components.PluginConfig.else1', {attrLabel: item.attrLabel})
        }]">
          <a-input v-model="form[item.attrCode]" :placeholder="$t('components.PluginConfig.else2', {attrLabel: item.attrLabel})" />
        </a-form-item>
      </template>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="cancel">{{$t('components.PluginConfig.5m6nj19s1lw0')}}</a-button>
        <a-button type="primary" :disabled="loading" style="margin-left: 16px;" @click="confirmSubmit">{{$t('components.PluginConfig.5m6nj19s1rw0')}}</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { reactive, ref, watch, toRaw, onMounted } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { FormInstance } from '@arco-design/web-vue/es/form'
  import { pluginConfigData } from '@/api/plugin'

  const props = withDefaults(defineProps<{
    open: boolean,
    pluginId?: string | number,
    configAttrs?: any[],
    configData?: any
  }>(), {
    open: false,
    configAttrs: () => [],
    configData: {}
  })

  const emits = defineEmits(['update:open'])

  const formRef = ref<FormInstance>()
  const form = reactive<any>({})

  const visible = ref<boolean>(false)
  const loading = ref<boolean>(false)

  watch(visible, (v) => {
    emits('update:open', v)
  })

  watch(() => props.open, (v) => {
    if (v) {
      const configData = toRaw(props.configData)
      for (const key in configData) {
        form[key] = configData[key]
      }
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
          pluginId: props.pluginId,
          configData: JSON.stringify(form)
        }

        pluginConfigData(params).then((res: any) => {
          console.log(res)
          Message.success('Configured success')
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
.plugin-modal {
  .modal-footer {
    text-align: center;
  }
}
</style>
