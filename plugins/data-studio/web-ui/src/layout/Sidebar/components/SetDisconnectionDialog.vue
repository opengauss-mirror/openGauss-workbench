<template>
  <div class="common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="$t('siderbar.setDisconnectionTime.dialogTitle')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
    >
      <el-form :model="form" ref="ruleFormRef" :rules="rules" inline>
        {{ $t('siderbar.setDisconnectionTime.settingLabel') }}
        <el-form-item prop="timeLength" label=" ">
          <el-input-number
            v-model="form.timeLength"
            controls-position="right"
            :min="2"
            :max="24"
            :precision="0"
          />
        </el-form-item>
        hour, <span class="tips">{{ $t('siderbar.setDisconnectionTime.settingTips') }}2 ~ 24</span>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleConfirm">
          {{ $t('button.confirm') }}
        </el-button>
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { getConnectionTime, reconnection } from '@/api/connect';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      uuid: string;
    }>(),
    {
      modelValue: false,
      uuid: '',
    },
  );
  const emit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val),
  });
  const { t } = useI18n();

  const ruleFormRef = ref<FormInstance>();
  const form = reactive({
    timeLength: 2,
  });

  const rules = reactive<FormRules>({
    timeLength: [
      {
        required: true,
        type: 'number',
        message: t('rules.empty', [t('siderbar.setDisconnectionTime.settingLabel')]),
        trigger: 'blur',
      },
    ],
  });
  const handleConfirm = () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        reconnection({
          uuid: props.uuid,
          timeLength: form.timeLength,
        }).then(() => {
          ElMessage.success(`${t('message.editSuccess')}`);
          handleClose();
        });
      }
    });
  };

  const handleOpen = async () => {
    const time = (await getConnectionTime({ uuid: props.uuid })) as unknown as number;
    form.timeLength = time;
  };
  const handleClose = () => {
    emit('update:modelValue', false);
  };
</script>

<style lang="scss" scoped>
  .common-dialog-wrapper {
    :deep(.el-dialog__body) {
      padding-bottom: 20px;
    }
  }
  .tips {
    font-style: italic;
  }
  :deep(.el-form-item) {
    margin: 0 !important;
  }
  :deep(.el-form-item__label) {
    padding: 0;
  }
</style>
