<template>
  <div class="common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="$t('userRole.changePassword.title')"
      :width="500"
      align-center
      :close-on-click-modal="false"
    >
      <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="120px">
        <el-form-item prop="oldPassword" :label="$t('userRole.changePassword.oldPassword')">
          <el-input v-model="form.oldPassword" type="password" />
        </el-form-item>
        <el-form-item prop="newPassword" :label="$t('userRole.changePassword.newPassword')">
          <el-input v-model="form.newPassword" type="password" />
        </el-form-item>
        <el-form-item prop="confirmPassword" :label="$t('userRole.changePassword.confirmPassword')">
          <el-input v-model="form.confirmPassword" type="password" />
        </el-form-item>
        <el-form-item
          prop="loginUserPassword"
          :label="$t('userRole.changePassword.loginUserPassword')"
        >
          <el-input v-model="form.loginUserPassword" type="password" />
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
  import { updateUserPassword } from '@/api/userRole';
  import { useUserStore } from '@/store/modules/user';
  import Crypto from '@/utils/crypto';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      uuid: string;
      userName: string;
      type: 'user' | 'role';
    }>(),
    {
      modelValue: false,
      uuid: '',
      userName: '',
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
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    loginUserPassword: '',
  });

  const validatePassword2 = (rule: any, value: any, callback: any) => {
    if (value === '') {
      callback(new Error(t('rules.empty', [t('userRole.changePassword.confirmPassword')])));
    } else if (value !== form.newPassword) {
      callback(new Error(t('message.confirmPasswordNotMatch')));
    } else {
      callback();
    }
  };
  const rules = reactive<FormRules>({
    oldPassword: [
      {
        required: true,
        message: t('rules.empty', [t('userRole.changePassword.oldPassword')]),
        trigger: 'blur',
      },
    ],
    newPassword: [
      {
        required: true,
        message: t('rules.empty', [t('userRole.changePassword.newPassword')]),
        trigger: 'blur',
      },
    ],
    confirmPassword: [
      {
        required: true,
        validator: validatePassword2,
        trigger: 'blur',
      },
    ],
    loginUserPassword: [
      {
        required: true,
        message: t('rules.empty', [t('userRole.changePassword.loginUserPassword')]),
        trigger: 'blur',
      },
    ],
  });
  const handleConfirm = () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        updateUserPassword({
          oldPassword: Crypto.encrypt(form.oldPassword),
          newPassword: Crypto.encrypt(form.newPassword),
          loginUserPassword: Crypto.encrypt(form.loginUserPassword),
          userName: props.userName,
          uuid: props.uuid,
          webUser: UserStore.userId,
          type: props.type,
        }).then(() => {
          ElMessage.success(`${t('message.editSuccess')}`);
          handleClose();
        });
      }
    });
  };
  const handleClose = () => {
    emit('update:modelValue', false);
  };
</script>
