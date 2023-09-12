<template>
  <div class="common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="$t('historyTable.dialog.title')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @closed="resetForm"
    >
      <div class="dialog_body">
        <AceEditor
          ref="editorPreRef"
          height="400px"
          style="margin: 4px 0; border: 1px solid #ddd"
        />
      </div>
      <template #footer>
        <el-button type="primary" @click="handleLoadToTerminal">
          {{ $t('button.loadToTerminal') }}
        </el-button>
        <el-button type="primary" @click="handleCopy">
          {{ $t('button.copy') }}
        </el-button>
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage } from 'element-plus';
  import AceEditor from '@/components/AceEditor.vue';
  import { useI18n } from 'vue-i18n';
  import { copyToClickBoard } from '@/utils';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      sql: string;
    }>(),
    {
      modelValue: false,
      sql: '',
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });
  const { t } = useI18n();
  const editorPreRef = ref();

  const loadTerminalFunc: (txt: string) => void = inject('loadTerminalFunc');
  const handleOpen = () => {
    editorPreRef.value.setValue(props.sql);
  };
  const handleLoadToTerminal = () => {
    loadTerminalFunc(props.sql);
    handleClose();
  };
  const handleCopy = () => {
    copyToClickBoard(editorPreRef.value.getValue());
    ElMessage.success(t('message.copySuccess'));
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
  };
  const resetForm = () => {
    editorPreRef.value.setValue('');
  };
</script>
