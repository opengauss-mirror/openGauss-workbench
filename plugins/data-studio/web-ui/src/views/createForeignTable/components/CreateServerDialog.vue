<template>
  <div class="import-dialog common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="$t('foreignTable.createForeignServerDialog.title')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="170px">
          <el-form-item
            prop="name"
            :label="$t('foreignTable.createForeignServerDialog.foreignServer')"
          >
            <el-input v-model="form.name" maxlength="30" />
          </el-form-item>
          <el-form-item
            prop="remoteHost"
            :label="$t('foreignTable.createForeignServerDialog.remoteServerHost')"
          >
            <el-input v-model="form.remoteHost" />
          </el-form-item>
          <el-form-item
            prop="remotePort"
            :label="$t('foreignTable.createForeignServerDialog.remoteServerPort')"
          >
            <el-input-number
              v-model="form.remotePort"
              :min="0"
              :max="65535"
              :step="1"
              step-strictly
              controls-position="right"
            />
          </el-form-item>
          <el-form-item
            prop="remoteDatabase"
            :label="$t('foreignTable.createForeignServerDialog.remoteDatabase')"
          >
            <el-input v-model="form.remoteDatabase" />
          </el-form-item>
          <el-form-item prop="role" :label="$t('foreignTable.foreign.userMapping')">
            <el-select v-model="form.role">
              <el-option
                v-for="item in userMappingList"
                :key="item.name"
                :label="item.name"
                :value="item.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            prop="remoteUsername"
            :label="$t('foreignTable.createForeignServerDialog.remoteUserName')"
          >
            <el-input v-model="form.remoteUsername" />
          </el-form-item>
          <el-form-item
            prop="remotePassword"
            :label="$t('foreignTable.createForeignServerDialog.remotePassword')"
          >
            <el-input v-model="form.remotePassword" show-password autocomplete="new-password" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
        <el-button type="primary" @click="testRemoteConnection">
          {{ $t('foreignTable.testRemoteConnection') }}
        </el-button>
        <el-button type="primary" @click="confirmForm">
          {{ $t('button.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { loadingInstance } from '@/utils';
  import { createServerListApi, testForeignTableApi } from '@/api/foreignTable';
  import { getUserRoleList } from '@/api/userRole';
  import Crypto from '@/utils/crypto';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      uuid: string;
      datasourceType: string;
      role: string;
    }>(),
    {
      modelValue: false,
    },
  );
  const emit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'success', name: string): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val),
  });

  const loading = ref(null);
  const userMappingList = ref([]);
  const ruleFormRef = ref<FormInstance>();
  const form = reactive({
    name: '',
    remoteHost: '',
    remotePort: null,
    remoteDatabase: '',
    role: '',
    remoteUsername: '',
    remotePassword: '',
  });
  const rules = reactive<FormRules>({
    name: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.createForeignServerDialog.foreignServer')]),
        trigger: 'blur',
      },
    ],
    remoteHost: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.createForeignServerDialog.remoteServerHost')]),
        trigger: 'blur',
      },
      { min: 1, max: 130, message: t('rules.charLength', 130), trigger: 'blur' },
      {
        message: t('connection.rules.host[0]'),
        trigger: 'blur',
        pattern: new RegExp('^(?:' +
            '(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|' +
            '([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|' +
            '([0-9a-fA-F]{1,4}:){1,7}:|' +
            '::([0-9a-fA-F]{1,4}:){1,6}[0-9a-fA-F]{1,4}|' +
            '([0-9a-fA-F]{1,4}:){1,6}:([0-9a-fA-F]{1,4})|' +
            '([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|' +
            '([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|' +
            '([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|' +
            '([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|' +
            '[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){1,6}|' +
            '::([0-9a-fA-F]{1,4}){1,7}|' +
            '::ffff:(\\d{1,3}\\.){3}\\d{1,3}' +
            ')$', 'g'),
      },
    ],
    remotePort: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.createForeignServerDialog.remoteServerPort')]),
        trigger: 'blur',
      },
      {
        message: t('connection.rules.port[0]'),
        trigger: 'blur',
        pattern: /^[0-9]+$/,
      },
    ],
    remoteDatabase: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.createForeignServerDialog.remoteDatabase')]),
        trigger: 'blur',
      },
      { min: 1, max: 30, message: t('rules.charLength', 30), trigger: 'blur' },
    ],
    role: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.foreign.userMapping')]),
        trigger: 'blur',
      },
    ],
    remoteUsername: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.createForeignServerDialog.remoteUserName')]),
        trigger: 'blur',
      },
      { min: 1, max: 30, message: t('rules.charLength', 30), trigger: 'blur' },
    ],
    remotePassword: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.createForeignServerDialog.remotePassword')]),
        trigger: 'blur',
      },
      { min: 1, max: 30, message: t('rules.charLength', 30), trigger: 'blur' },
    ],
  });

  const fetchUserList = async () => {
    const res = await getUserRoleList({ uuid: props.uuid });
    userMappingList.value = res.user;
  };

  const handleOpen = () => {
    fetchUserList();
    form.role = props.role;
  };

  const handleClose = () => {
    ruleFormRef.value.resetFields();
    emit('update:modelValue', false);
  };
  const testRemoteConnection = async () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          loading.value = loadingInstance();
          await testForeignTableApi({
            uuid: props.uuid,
            datasourceType: props.datasourceType,
            remoteHost: form.remoteHost,
            remotePort: form.remotePort,
            remoteDatabase: form.remoteDatabase,
            remoteUsername: form.remoteUsername,
            remotePassword: Crypto.encrypt(form.remotePassword),
          });
          ElMessage.success(t('message.success'));
        } finally {
          loading.value.close();
        }
      }
    });
  };
  const confirmForm = async () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          loading.value = loadingInstance();
          await createServerListApi({
            uuid: props.uuid,
            datasourceType: props.datasourceType,
            foreignServer: form.name,
            remoteHost: form.remoteHost,
            remotePort: form.remotePort,
            remoteDatabase: form.remoteDatabase,
            role: form.role,
            remoteUsername: form.remoteUsername,
            remotePassword: Crypto.encrypt(form.remotePassword),
          });
          ElMessage.success(t('message.success'));
          emit('success', form.name);
          visible.value = false;
        } finally {
          loading.value.close();
        }
      }
    });
  };
</script>

<style lang="scss" scoped>
  .import-dialog {
    :deep(.el-input-number .el-input__inner) {
      text-align: left;
    }
  }
</style>
