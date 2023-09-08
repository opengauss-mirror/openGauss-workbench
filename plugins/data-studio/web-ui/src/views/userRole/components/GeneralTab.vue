<template>
  <el-form
    :model="form"
    class="rule-form"
    ref="ruleFormRef"
    :rules="rules"
    label-width="130px"
    label-position="left"
    :label-suffix="$t('common.colon')"
  >
    <el-row :gutter="50">
      <el-col :span="12">
        <el-form-item prop="name" :label="$t('common.name')">
          <el-input v-model="form.name" :disabled="props.type == 'edit'" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="type" :label="$t('common.type')">
          <el-radio-group v-model="form.type" :disabled="props.type == 'edit'" @change="roleChange">
            <el-radio label="user">{{ $t('userRole.user') }}</el-radio>
            <el-radio label="role">{{ $t('userRole.role') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
      <el-col :span="12" v-if="props.type == 'create'">
        <el-form-item prop="password" :label="$t('common.password')">
          <el-input v-model="form.password" show-password autocomplete="new-password" />
        </el-form-item>
      </el-col>
      <el-col :span="12" v-if="props.type == 'create'">
        <el-form-item prop="confirmPassword" :label="$t('common.confirmPassword')">
          <el-input v-model="form.confirmPassword" show-password />
        </el-form-item>
      </el-col>
      <el-col :span="12" v-if="props.type == 'edit'">
        <el-form-item label="OID">
          {{ form.oid }}
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="beginDate" :label="$t('common.startDate')">
          <el-date-picker
            v-model="form.beginDate"
            type="datetime"
            :placeholder="$t('common.selectDate')"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="endDate" :label="$t('common.endDate')">
          <el-date-picker
            v-model="form.endDate"
            type="datetime"
            :placeholder="$t('common.selectDate')"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="connectionLimit" :label="$t('database.conRestrictions')">
          <el-input-number
            v-model="form.connectionLimit"
            :min="-1"
            :max="100"
            :step="1"
            step-strictly
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="resourcePool" :label="$t('userRole.resourcePool')">
          <el-select v-model="form.resourcePool">
            <el-option v-for="item in resourcePoolList" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="power" :label="$t('userRole.privileges')">
          <el-checkbox
            v-model="checkAll"
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
          >
            {{ $t('common.all2') }}
          </el-checkbox>
          <el-checkbox-group v-model="form.power" @change="handleCheckedCitiesChange">
            <el-checkbox label="LOGIN" :disabled="form.type === 'role'">
              {{ $t('userRole.privilegeItem.login') }}
            </el-checkbox>
            <el-checkbox label="INHERIT">{{ $t('userRole.privilegeItem.inherit') }}</el-checkbox>
            <el-checkbox label="REPLICATION">
              {{ $t('userRole.privilegeItem.replication') }}
            </el-checkbox>
            <el-checkbox label="CREATEROLE">
              {{ $t('userRole.privilegeItem.createRole') }}
            </el-checkbox>
            <el-checkbox label="CREATEDB">
              {{ $t('userRole.privilegeItem.createDatabase') }}
            </el-checkbox>
            <el-checkbox label="AUDITADMIN">
              {{ $t('userRole.privilegeItem.auditAdministrator') }}
            </el-checkbox>
            <el-checkbox label="SYSADMIN">
              {{ $t('userRole.privilegeItem.systemAdministrator') }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="comment" :label="$t('common.description')">
          <el-input
            v-model="form.comment"
            type="textarea"
            :maxlength="5000"
            :autosize="{ minRows: 1, maxRows: 3 }"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
<script lang="ts" setup>
  import { FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { Generateform } from '../type';
  import { getResource } from '@/api/userRole';

  const props = withDefaults(
    defineProps<{
      type: 'create' | 'edit';
      uuid: string;
      data: Generateform;
    }>(),
    {
      type: 'create',
      uuid: '',
    },
  );
  const emit = defineEmits<{
    (event: 'update:data', data: Generateform): void;
  }>();
  const { t } = useI18n();
  const ruleFormRef = ref<FormInstance>();
  const resourcePoolList = ref([]);
  const checkAll = ref(false);
  const isIndeterminate = ref(false);
  const allPowerList = [
    'LOGIN',
    'INHERIT',
    'REPLICATION',
    'CREATEROLE',
    'CREATEDB',
    'AUDITADMIN',
    'SYSADMIN',
  ];
  const powerList = computed(() => {
    return allPowerList.filter((item) => (form.value.type === 'role' ? item !== 'LOGIN' : item));
  });
  const form = computed({
    get() {
      return props.data;
    },
    set(val) {
      emit('update:data', val);
    },
  });

  const fetchTablespaceList = async () => {
    const res = (await getResource(props.uuid)) as unknown as string[];
    resourcePoolList.value = res;
  };

  const handleCheckAllChange = (val: boolean) => {
    form.value.power = val ? powerList.value.map((item) => item) : [];
    isIndeterminate.value = false;
  };
  const handleCheckedCitiesChange = (value: string[]) => {
    const checkedCount = value.length;
    checkAll.value = checkedCount === powerList.value.length;
    isIndeterminate.value = checkedCount > 0 && checkedCount < powerList.value.length;
  };
  const validatePassword2 = (rule: any, value: any, callback: any) => {
    if (value === '') {
      callback(new Error(t('rules.empty', [t('common.password')])));
    } else if (value !== form.value.password) {
      callback(new Error(t('message.confirmPasswordNotMatch')));
    } else {
      callback();
    }
  };
  const rules = reactive<FormRules>({
    name: [
      {
        required: true,
        message: t('rules.empty', [t('common.name')]),
        trigger: 'blur',
      },
    ],
    password: [
      {
        required: true,
        message: t('rules.empty', [t('common.password')]),
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
  });
  const formRef = () => {
    return ruleFormRef.value;
  };
  const validateForm = () => {
    return new Promise<void>((resolve, reject) => {
      ruleFormRef.value.validate((valid) => {
        if (valid) {
          resolve();
        } else {
          reject('GeneralTab');
        }
      });
    });
  };
  const roleChange = () => {
    form.value.power = form.value.power.filter((item) => item != 'LOGIN');
    isIndeterminate.value =
      form.value.power.length > 0 && form.value.power.length < powerList.value.length;
  };
  const resetFields = () => {
    ruleFormRef.value.resetFields();
  };
  onMounted(() => {
    nextTick(fetchTablespaceList);
  });
  defineExpose({
    formRef,
    validateForm,
    resetFields,
  });
</script>

<style lang="scss" scoped>
  .rule-form {
    overflow-x: hidden;
  }
  :deep(.el-select) {
    width: 100%;
  }
  :deep(.el-date-editor.el-input) {
    width: 100%;
  }
  :deep(.el-input-number) {
    width: 100%;
  }
  :deep(.el-input-number .el-input__inner) {
    text-align: left;
  }
</style>
