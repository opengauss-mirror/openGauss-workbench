<template>
  <div class="common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="$t('connection.enterPassword')"
      :width="400"
      align-center
      :close-on-click-modal="false"
    >
      <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="120px">
        <el-form-item prop="password" :label="$t('connection.password')">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item prop="isRememberPassword" :label="$t('connection.savePassword')">
          <el-radio-group v-model="form.isRememberPassword">
            <el-radio value="y">{{ $t('connection.currentSessionOnly') }}</el-radio>
            <el-radio value="n">{{ $t('connection.doNotSave') }}</el-radio>
          </el-radio-group>
        </el-form-item>
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
  import { reLogin } from '@/api/connect';
  import { useUserStore } from '@/store/modules/user';
  import Crypto from '@/utils/crypto';
  import EventBus, { EventTypeName } from '@/utils/event-bus';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      uuid: string;
      connectInfo: any;
    }>(),
    {
      modelValue: false,
      uuid: '',
      connectInfo: () => ({}),
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
  const UserStore = useUserStore();

  const ruleFormRef = ref<FormInstance>();
  const form = reactive({
    password: '',
    isRememberPassword: 'y',
  });

  const rules = reactive<FormRules>({
    password: [
      {
        required: true,
        message: t('rules.empty', [t('connection.password')]),
        trigger: 'blur',
      },
    ],
  });
  const handleConfirm = () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        reLogin({
          type: props.connectInfo.type,
          name: props.connectInfo.name,
          ip: props.connectInfo.ip,
          port: props.connectInfo.port,
          dataName: props.connectInfo.dataName,
          userName: props.connectInfo.userName,
          id: props.connectInfo.id,
          password: Crypto.encrypt(form.password),
          isRememberPassword: form.isRememberPassword,
          webUser: UserStore.userId,
          connectionid: props.uuid,
        }).then((res) => {
          EventBus.notify(EventTypeName.UPDATE_CONNECTINFO, res);
          ElMessage.success(`${t('message.connectSuccess')}`);
          handleClose();
        });
      }
    });
  };
  const handleClose = () => {
    emit('update:modelValue', false);
  };
</script>
