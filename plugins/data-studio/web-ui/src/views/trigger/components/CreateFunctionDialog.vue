<template>
  <div class="dialog common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="$t('trigger.createTriggerFunction.title')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-form
          :model="form"
          ref="ruleFormRef"
          label-position="top"
          :label-suffix="$t('common.colon')"
        >
          <el-form-item prop="sql" :label="$t('trigger.createTriggerFunction.function')" required>
            <AceEditor ref="editorPreRef" type="form" width="100%" height="400px" :readOnly="false" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
        <el-button type="primary" @click="confirmForm">
          {{ $t('button.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance } from 'element-plus';
  import AceEditor from '@/components/AceEditor.vue';
  import { useI18n } from 'vue-i18n';
  import { loadingInstance } from '@/utils';
  import { createFunctionTriggerApi } from '@/api/trigger';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      uuid: string;
    }>(),
    {
      modelValue: false,
    },
  );
  const emit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'success'): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val),
  });

  const loading = ref(null);
  const ruleFormRef = ref<FormInstance>();
  const editorPreRef = ref();
  const form = reactive({
    sql: '',
  });

  const handleClose = () => {
    editorPreRef.value.setValue('');
    emit('update:modelValue', false);
  };
  const confirmForm = async () => {
    const sql = editorPreRef.value.getValue();
    if (!sql) {
      return ElMessage.error(t('rules.empty', [t('trigger.createTriggerFunction.function')]));
    }
    try {
      loading.value = loadingInstance();
      await createFunctionTriggerApi({
        uuid: props.uuid,
        sql,
      });
      ElMessage.success(t('message.success'));
      emit('success');
      visible.value = false;
    } finally {
      loading.value.close();
    }
  };
</script>
