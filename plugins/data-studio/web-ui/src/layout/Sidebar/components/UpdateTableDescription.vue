<template>
  <div class="dialog">
    <el-dialog
      v-model="visible"
      :title="toSpacePascalCase($t('siderbar.table.setDescription'))"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @close="handleClose"
    >
      <div class="dialog_body">
        <div class="tips">{{
          $t('message.setTableDescription', {
            name: `${commonParams.schema}.${commonParams.tableName}`,
          })
        }}</div>
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="0px">
          <el-form-item prop="description">
            <el-input v-model="form.description" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
          <el-button type="primary" @click="confirmForm" :loading="loading">
            {{ $t('button.confirm') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { setTableComment } from '@/api/table';
  import { toSpacePascalCase } from '@/utils';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import type { NodeData } from '../types';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      nodeData: Partial<NodeData>;
    }>(),
    {
      modelValue: false,
      nodeData: () => ({}),
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });
  const commonParams = computed(() => {
    return {
      uuid: props.nodeData?.uuid,
      schema: props.nodeData?.schemaName,
      tableName: props.nodeData?.name,
    };
  });

  const { t } = useI18n();
  const loading = ref(false);
  const ruleFormRef = ref<FormInstance>();
  const form = reactive({
    description: '',
  });
  const rules = reactive<FormRules>({
    description: [
      { required: true, message: t('rules.empty', [t('table.description')]), trigger: 'blur' },
    ],
  });

  const handleClose = () => {
    myEmit('update:modelValue', false);
    resetForm();
  };
  const confirmForm = () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true;
        setTableComment({
          generalPurpose: form.description,
          ...commonParams.value,
        })
          .then(() => {
            ElMessage.success(`${t('message.editSuccess')}`);
            EventBus.notify(EventTypeName.REFRESH_ASIDER, 'mode', {
              rootId: props.nodeData.rootId,
              databaseId: props.nodeData.databaseId,
              schemaId: props.nodeData.schemaId,
            });
            handleClose();
          })
          .finally(() => {
            loading.value = false;
          });
      }
    });
  };
  const resetForm = () => {
    form.description = '';
  };
</script>

<style lang="scss" scoped>
  .dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 5px;
    }
    :deep(.el-select) {
      width: 100%;
    }
  }
  .tips {
    margin-bottom: 5px;
  }
</style>
