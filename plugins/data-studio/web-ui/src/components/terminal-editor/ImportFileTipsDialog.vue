<template>
  <div class="import-file-tips-dialog">
    <el-dialog
      v-model="visible"
      :title="title"
      :width="350"
      align-center
      :close-on-click-modal="false"
      @closed="myEmit('close')"
    >
      <div class="dialog_body">
        {{ $t('terminal.importSqlText') }}
        <el-radio-group v-model="ruleForm.radio">
          <el-radio value="overwrite">{{ t('button.overwrite') }}</el-radio>
          <el-radio value="append">{{ t('button.append') }}</el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="handleClose">
          {{ $t('button.cancel') }}
        </el-button>
        <el-button type="primary" @click="handleOperation" :disabled="!ruleForm.radio">
          {{ $t('button.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n';
  type ImportSqlType = 'overwrite' | 'append';
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      textType?: 'import' | 'export'; // you can choose the text in this dialog
    }>(),
    {
      modelValue: false,
      textType: 'import',
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', value: boolean): void;
    (event: 'operation', type: ImportSqlType): void;
    (event: 'close'): void;
  }>();
  const { t } = useI18n();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });
  const title = computed(() => {
    return props.textType == 'import' ? t('terminal.importSql') : t('button.exportToTerminal');
  });
  const ruleForm = reactive({
    radio: null as ImportSqlType,
  });
  const handleOperation = () => {
    myEmit('operation', ruleForm.radio);
    visible.value = false;
  };
  const handleClose = () => {
    visible.value = false;
    ruleForm.radio = null;
  };
</script>

<style lang="scss" scoped>
  .import-file-tips-dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 25px;
    }
  }
</style>
