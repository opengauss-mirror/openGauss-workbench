<template>
  <div class="dialog common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="props.type == 'create' ? $t('job.dialog.title[0]') : $t('job.dialog.title[1]')"
      :width="600"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="140px">
          <el-form-item prop="jobContent" :label="$t('job.jobContent')">
            <AceEditor
              ref="editorRef"
              :read-only="false"
              width="calc(100% - 20px)"
              height="150px"
            />
            <el-tooltip
              effect="light"
              :content="$t('job.dialog.contentTips')"
              placement="bottom"
              popper-class="tooltip-item"
            >
              <el-icon class="icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item prop="nextRunDate" :label="$t('job.dialog.nextRunDate')">
            <el-date-picker
              v-model="form.nextRunDate"
              type="datetime"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :popper-options="{
                modifiers: [
                  {
                    name: 'flip',
                    options: {
                      fallbackPlacements: ['right'],
                      allowedAutoPlacements: ['right'],
                    },
                  },
                ],
              }"
            />
            <el-tooltip
              effect="light"
              :content="$t('job.dialog.dateTips')"
              placement="bottom"
              popper-class="tooltip-item"
            >
              <el-icon class="icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item prop="interval" :label="$t('job.dialog.dateExpression')">
            <el-input v-model="form.interval" />
            <el-tooltip effect="light" placement="bottom" popper-class="tooltip-item">
              <template #content>
                <div class="tooltip-item">{{ $t('job.dialog.expressionTips') }}</div>
              </template>
              <el-icon class="icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
          <el-button type="primary" @click="confirmForm">
            {{ $t('button.confirm') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import AceEditor from '@/components/AceEditor.vue';
  import { useI18n } from 'vue-i18n';
  import { loadingInstance } from '@/utils';
  import { createJobApi, queryAJobApi, editJobApi } from '@/api/job';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: 'create' | 'edit';
      uuid: string;
      jobId?: string;
    }>(),
    {
      modelValue: false,
      type: 'create',
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'success'): void;
  }>();

  const loading = ref(null);
  const ruleFormRef = ref<FormInstance>();
  const editorRef = ref<InstanceType<typeof AceEditor>>(null);
  const form = reactive({
    jobContent: '',
    nextRunDate: '',
    interval: '',
  });
  const rules = reactive<FormRules>({
    jobContent: [
      {
        required: true,
        trigger: 'blur',
        validator: (rule, value, callback) => {
          editorRef.value.getValue()
            ? callback()
            : callback(new Error(t('rules.empty', [t('job.jobContent')])));
        },
      },
    ],
    nextRunDate: [
      {
        required: true,
        message: t('rules.empty', [t('job.dialog.nextRunDate')]),
        trigger: 'change',
      },
    ],
  });
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });

  const handleOpen = async () => {
    if (props.type == 'edit') {
      try {
        loading.value = loadingInstance();
        const res = await queryAJobApi({
          uuid: props.uuid,
          jobId: props.jobId,
        });
        editorRef.value.setValue(res.jobContent);
        form.nextRunDate = res.nextRunDate;
        form.interval = res.interval;
      } finally {
        loading.value.close();
      }
    }
  };
  const handleClose = () => {
    ruleFormRef.value.resetFields();
    editorRef.value.setValue('');
    myEmit('update:modelValue', false);
  };
  const confirmForm = async () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          loading.value = loadingInstance();
          if (props.type == 'create') {
            const params = {
              uuid: props.uuid,
              jobContent: editorRef.value.getValue(),
              nextRunDate: form.nextRunDate,
              interval: form.interval,
            };
            await createJobApi(params);
          } else {
            const params = {
              uuid: props.uuid,
              jobId: props.jobId,
              jobContent: editorRef.value.getValue(),
              nextRunDate: form.nextRunDate,
              interval: form.interval,
            };
            await editJobApi(params);
          }
          ElMessage.success(t('message.success'));
          myEmit('success');
          visible.value = false;
        } finally {
          loading.value.close();
        }
      }
    });
  };
</script>

<style lang="scss" scoped>
  .dialog {
    :deep(.el-input) {
      width: calc(100% - 20px);
    }
  }
  .icon {
    margin-left: 5px;
  }
</style>
