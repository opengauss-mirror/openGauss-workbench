<template>
  <el-form
    :model="form"
    class="rule-form"
    ref="ruleFormRef"
    label-width="130px"
    label-position="left"
    :label-suffix="$t('common.colon')"
  >
    <el-row :gutter="50">
      <el-col :span="12">
        <el-form-item prop="role" :label="$t('userRole.roleGroup')">
          <el-select v-model="form.role" clearable>
            <el-option
              v-for="item in userRoleList"
              :key="item.oid"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="administrator" :label="$t('userRole.adminsGroup')">
          <el-select v-model="form.administrator" clearable multiple collapse-tags>
            <el-option
              v-for="item in userRoleList"
              :key="item.oid"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
<script lang="ts" setup>
  import { FormInstance } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { Memberform } from '../type';
  import { getUserRoleList } from '@/api/userRole';

  const props = withDefaults(
    defineProps<{
      type: 'create' | 'edit';
      uuid: string;
      data: Memberform;
    }>(),
    {
      type: 'create',
      uuid: '',
    },
  );
  const emit = defineEmits<{
    (event: 'update:data', data: Memberform): void;
  }>();
  const { t } = useI18n();
  const userRoleList = ref<{ name: string; oid: string }[]>([]);
  const ruleFormRef = ref<FormInstance>();
  const form = computed({
    get() {
      return props.data;
    },
    set(val) {
      emit('update:data', val);
    },
  });
  const formRef = () => {
    return ruleFormRef.value;
  };

  const fetchUserRoleList = async () => {
    const res = await getUserRoleList(props.uuid);
    userRoleList.value = [].concat(res.role || [], res.user || []);
  };

  const validateForm = () => {
    return new Promise<void>((resolve, reject) => {
      ruleFormRef.value.validate((valid) => {
        if (valid) {
          resolve();
        } else {
          reject('MemberTab');
        }
      });
    });
  };
  const resetFields = () => {
    ruleFormRef.value.resetFields();
  };
  onMounted(() => {
    fetchUserRoleList();
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
