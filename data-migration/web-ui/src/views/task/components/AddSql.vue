<template>
  <a-modal
    title="添加数据库"
    v-model:visible="visible"
    width="500px"
    title-align="start"
    modal-class="add-sql-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item field="attrCode1" label="数据库类型" :rules="[{
        required: true,
        message: '数据库类型不能为空'
      }]">
        <a-select v-model="form.attrCode1" placeholder="请选择数据库类型">
          <a-option value="1">MySQL</a-option>
          <a-option value="2">openGauss</a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="attrCode2" label="链接" :rules="[{
        required: true,
        message: '链接不能为空'
      }]">
        <a-input v-model="form.attrCode2" placeholder="请输入链接" />
      </a-form-item>
      <a-form-item field="attrCode3" label="用户名" :rules="[{
        required: true,
        message: '用户名不能为空'
      }]">
        <a-input v-model="form.attrCode3" placeholder="请输入用户名" maxlength="30" />
      </a-form-item>
      <a-form-item field="attrCode4" label="密码" :rules="[{
        required: true,
        message: '密码不能为空'
      }]">
        <a-input-password v-model="form.attrCode4" placeholder="请输入密码" minlength="6" maxlength="20" />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="cancel">取消</a-button>
        <a-button :disabled="loading" style="margin-left: 16px;">测试连通性</a-button>
        <a-button type="primary" :disabled="loading" style="margin-left: 16px;" @click="confirmSubmit">确定</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch, onMounted } from 'vue'
// import { Message } from '@arco-design/web-vue'

const props = defineProps({
  open: Boolean
})

const emits = defineEmits(['update:open'])

const formRef = ref()
const form = reactive({})

const visible = ref(false)
const loading = ref(false)

watch(visible, (v) => {
  emits('update:open', v)
})

watch(() => props.open, (v) => {
  // if (v) {}
  visible.value = v
})

const cancel = () => {
  visible.value = false
}

const confirmSubmit = () => {
  formRef.value?.validate(valid => {
    if (!valid) {
      // const params = {}

      // pluginConfigData(params).then((res: any) => {
      //   console.log(res)
      //   Message.success('Configured success')
      //   visible.value = false
      // })
    }
  })
}

onMounted(() => {
  visible.value = props.open
})
</script>

<style lang="less" scoped>
.add-sql-modal {
  .modal-footer {
    text-align: center;
  }
}
</style>
