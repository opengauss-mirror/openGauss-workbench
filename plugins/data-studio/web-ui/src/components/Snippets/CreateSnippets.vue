<template>
  <div class="create-wrapper">
    <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="auto" class="my-form">
      <el-form-item prop="name" :label="$t('common.name')" class="form-item-line1">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item prop="code" :label="$t('snippets.name')">
        <AceEditor ref="editorRef" type="form" :readOnly="false" width="100%" height="400px" />
      </el-form-item>
      <el-form-item prop="description" :label="$t('common.description')">
        <el-input v-model="form.description" type="textarea" :rows="2" resize="none" />
      </el-form-item>
    </el-form>
    <div class="button-wrapper">
      <el-button v-if="props.type == 'create'" @click="handleCancel">
        {{ $t('button.cancel') }}
      </el-button>
      <el-button v-if="props.type == 'edit'" type="primary" @click="handleDelelte">
        {{ $t('button.delete') }}
      </el-button>
      <el-button v-if="props.type == 'edit'" type="primary" @click="handleCopy">
        {{ $t('button.copy') }}
      </el-button>
      <el-button v-if="props.type == 'edit'" type="primary" @click="handleExport">
        {{ $t('button.exportToTerminal') }}
      </el-button>
      <el-button type="primary" @click="handleSave">
        {{ $t('button.save') }}
      </el-button>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus';
  import AceEditor from '@/components/AceEditor.vue';
  import { useI18n } from 'vue-i18n';
  import { loadingInstance, copyToClickBoard } from '@/utils';
  import { createSqlCodeApi, updateSqlCodeApi, getSqlCodeApi, deleteSqlCodeApi } from '@/api/snippets';

  const { t } = useI18n();
  const loading = ref(null);
  const props = withDefaults(
    defineProps<{
      type: 'create' | 'edit';
      codeId?: number | string;
      code?: string;
    }>(),
    {
      type: 'create',
    },
  );
  const ruleFormRef = ref<FormInstance>();
  const editorRef = ref();
  const form = reactive({
    name: '',
    code: '',
    description: '',
  });
  const rules = reactive<FormRules>({
    name: [{ required: true, message: t('rules.empty', [t('common.name')]), trigger: 'blur' }],
    code: [{ required: true, message: t('rules.empty', [t('snippets.name')]), trigger: 'blur' }],
  });

  const toggleSnippetsList = inject<() => void>('toggleSnippetsList');

  const getData = async () => {
    const res = await getSqlCodeApi({
      id: props.codeId,
    });
    Object.assign(form, {
      name: res.name,
      description: res.description,
    });
    editorRef.value.setValue(res.code);
  };

  const handleCopy = () => {
    const code = editorRef.value.getValue();
    if (code) {
      copyToClickBoard(editorRef.value.getValue());
      ElMessage.success(t('message.copySuccess'));
    } else {
      ElMessage.error(t('rules.empty', [t('snippets.name')]));
    }
  };

  const handleDelelte = () => {
    ElMessageBox.confirm(t('message.deleteSnippet'), t('common.warning'), {
      type: 'warning',
    })
      .then(async () => {
        await deleteSqlCodeApi({ id: props.codeId });
        ElMessage({
          type: 'success',
          message: t('message.deleteSuccess'),
        });
        handleCancel();
      })
      .catch(() => ({}));
  };

  const handleImportFile = inject<(data: string) => void>('handleImportFile');
  const handleExport = () => {
    const code = editorRef.value.getValue();
    if (code) {
      handleImportFile(editorRef.value.getValue());
    } else {
      ElMessage.error(t('rules.empty', [t('snippets.name')]));
    }
  };

  const handleCancel = () => {
    ruleFormRef.value.resetFields();
    editorRef.value.setValue('');
    toggleSnippetsList();
  };
  const handleSave = () => {
    const api = {
      create: createSqlCodeApi,
      edit: updateSqlCodeApi,
    };
    form.code = editorRef.value.getValue();
    ruleFormRef.value.validate((valid) => {
      if (valid) {
        loading.value = loadingInstance();
        try {
          api[props.type]({
            id: props.type == 'edit' ? props.codeId : undefined,
            name: form.name,
            code: form.code,
            description: form.description,
          }).then(() => {
            if (props.type === 'create') {
              ElMessage.success(`${t('message.success')}`);
            } else if (props.type === 'edit') {
              ElMessage.success(`${t('message.editSuccess')}`);
            }
            handleCancel();
          });
        } finally {
          loading.value.close();
        }
      }
    });
  };
  onMounted(() => {
    if (props.type == 'create' && props.code) {
      editorRef.value.setValue(props.code);
    }
    if (props.type == 'edit') {
      nextTick(async () => {
        getData();
      });
    }
  });
</script>
<style lang="scss" scoped>
  .create-wrapper {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
  .my-form {
    flex: 1;
  }
  .button-wrapper {
    text-align: center;
  }
</style>
