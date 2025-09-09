<template>
  <a-modal
  :title="`${props.options.roleId ? $t('components.EditRole.5m6nl1uvgfk0'): $t('components.EditRole.else1')}${$t('components.EditRole.else2')}`"
    v-model:visible="visible"
    width="500px"
    title-align="start"
    modal-class="role-modal"
  >
    <a-form ref="formRef" :model="form" auto-label-width>
      <a-form-item field="roleName" :label="$t('components.EditRole.5m6nl1uvjes0')" :rules="[
        {
          required: true,
          message: $t('components.EditRole.5m6nl1uvjls0')
        },
        {
          required: true,
          match: /^[\u4e00-\u9fa50-9A-Za-z]*$/,
          message: $t('components.EditRole.5nsfs3sdwwg0')
        }
      ]">
        <a-input v-model.trim="form.roleName" :placeholder="$t('components.EditRole.5m6nl1uvjp80')" maxlength="30" />
      </a-form-item>
      <a-form-item field="status" :label="$t('components.EditRole.5m6nl1uvk7g0')">
        <a-radio-group v-model="form.status">
          <a-radio value="0">{{$t('components.EditRole.5m6nl1uvkmc0')}}</a-radio>
          <a-radio value="1">{{$t('components.EditRole.5m6nl1uvkq80')}}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item field="status" :label="$t('components.EditRole.5m6nl1uvkw40')">
        <div class="menu-tree">
          <div class="menu-all">
            <a-checkbox v-model="isAllCheck" @change="allCheckChange">{{$t('components.EditRole.5m6nl1uvl2o0')}}</a-checkbox>
          </div>
          <div class="tree-con">
            <a-tree
              :checkable="true"
              v-model:checked-keys="checkedKeys"
              v-model:half-checked-keys="halfCheckedKeys"
              :data="treeData"
              :field-names="{
                key: 'id',
                title: 'label'
              }"
            />
          </div>
        </div>
      </a-form-item>
      <a-form-item field="remark" :label="$t('components.EditRole.5m6nl1uvl5g0')">
        <a-textarea v-model="form.remark" :placeholder="$t('components.EditRole.5m6nl1uvl8k0')" allow-clear :max-length="200" show-word-limit />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="visible = false">{{$t('components.EditRole.5m6nl1uvlbw0')}}</a-button>
        <a-button type="primary" :disabled="loading" style="margin-left: 16px;" @click="confirmSubmit">{{$t('components.EditRole.5m6nl1uvles0')}}</a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { watch, ref, reactive, onMounted } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { FormInstance } from '@arco-design/web-vue/es/form'
  import { createRole, updateRole, roleMenuTree } from '@/api/role'

  const deepFlatten: any = (arr: any[]) => {
    if (Array.isArray(arr)) {
      return arr.reduce((flatArr, el) => {
        if (el.children) {
          return [...flatArr, el, ...deepFlatten(el.children)]
        } else {
          return [...flatArr, el]
        }
      }, [])
    } else {
      return [arr]
    }
  }

  const props = withDefaults(defineProps<{
    open: boolean,
    options?: any
  }>(), {
    open: false,
    options: {}
  })

  const emits = defineEmits(['update:open', 'ok'])

  const formRef = ref<FormInstance>()
  const form = reactive<any>({})

  const visible = ref<boolean>(false)
  const loading = ref<boolean>(false)

  const treeData = ref([])
  const checkedKeys = ref<any[]>([])
  const halfCheckedKeys = ref<any[]>([])
  const isAllCheck = ref<boolean>(false)
  const allCheckedKeys: any[] = []

  watch(visible, (v) => {
    emits('update:open', v)
  })

  watch(() => props.open, (v) => {
    if (v) {
      if (props.options.roleId) {
        form['roleName'] = props.options.roleName
        form['status'] = props.options.status
        form['remark'] = props.options.remark
        getRoleMenuTree(props.options.roleId)
      } else {
        form['roleName'] = undefined
        form['status'] = '0'
        form['remark'] = undefined
        treeData.value = []
        checkedKeys.value = []
        halfCheckedKeys.value = []
        isAllCheck.value = false
        getRoleMenuTree(2)
      }
    }
    visible.value = v
  })

  const getRoleMenuTree = (roleId: string | number) => {
    roleMenuTree(roleId).then((res: any) => {
      treeData.value = res.menus
      const allMenu = deepFlatten(res.menus)
      allMenu.forEach((menu: any) => {
        allCheckedKeys.push(menu.id)
      })
      checkedKeys.value = res.checkedKeys
    })
  }

  const allCheckChange = (value: any) => {
    checkedKeys.value = value ? allCheckedKeys : []
  }

  const confirmSubmit = () => {
    formRef.value?.validate(valid => {
      if (!valid) {
        const menuIds = [...halfCheckedKeys.value, ...checkedKeys.value]

        if (props.options.roleId) {
          updateRole({
            roleId: props.options.roleId,
            menuIds,
            ...form
          }).then(() => {
            Message.success('Modified success')
            emits('ok')
            visible.value = false
          })
        } else {
          createRole({
            menuIds,
            ...form
          }).then(() => {
            Message.success('Added success')
            emits('ok')
            visible.value = false
          })
        }
      }
    })
  }

  onMounted(() => {
    visible.value = props.open
  })
</script>

<style lang="less" scoped>
.role-modal {
  .menu-tree {
    border: 1px solid var(--color-border-1);
    border-radius: 4px;
    width: 100%;
    padding: 10px;
    max-height: 350px;
    overflow-y: auto;
  }
  .modal-footer {
    text-align: center;
  }
}
</style>
