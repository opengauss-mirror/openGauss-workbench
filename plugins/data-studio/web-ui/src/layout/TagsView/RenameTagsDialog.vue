<template>
  <div class="dialog">
    <el-dialog
      v-model="visible"
      :title="toSpacePascalCase($t('windows.renameTerminal'))"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @close="handleClose"
    >
      <div class="dialog_body">
        <div class="tips">
          {{ $t('windows.renameTerminalTips', { name: props.tag.fileName }) }}
        </div>
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="0px">
          <el-form-item prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="confirmForm" :loading="loading">
            {{ $t('button.confirm') }}
          </el-button>
          <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { toSpacePascalCase } from '@/utils';
  import { useTagsViewStore } from '@/store/modules/tagsView';

  const TagsViewStore = useTagsViewStore();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      tag: any;
    }>(),
    {
      modelValue: false,
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
  const loading = ref(false);
  const ruleFormRef = ref<FormInstance>();
  const form = reactive({
    name: '',
  });
  const rules = reactive<FormRules>({
    name: [
      {
        required: true,
        transform: (value) => value.trim(),
        message: t('rules.empty', [t('common.name')]),
        trigger: 'blur',
      },
    ],
  });

  const handleClose = () => {
    myEmit('update:modelValue', false);
    resetForm();
  };
  const confirmForm = () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        TagsViewStore.renameTagById(props.tag.id, encodeURIComponent(form.name));
        ElMessage.success(`${t('message.editSuccess')}`);
        handleClose();
      }
    });
  };
  const resetForm = () => {
    form.name = '';
  };
</script>

<style lang="scss" scoped>
  .dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 5px;
    }
  }
  .tips {
    margin-bottom: 5px;
  }
</style>
