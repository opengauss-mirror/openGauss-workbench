<template>
  <el-form
    :model="form"
    class="rule-form general-form"
    ref="ruleFormRef"
    :rules="rules"
    label-width="130px"
    label-position="left"
    :label-suffix="$t('common.colon')"
  >
    <el-row :gutter="50">
      <el-col :span="12">
        <el-form-item prop="tableName" :label="$t('createTable.general.name')">
          <el-input v-model="form.tableName" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="ifNotExists" :label="$t('createTable.general.ifNotExists')">
          <el-checkbox v-model="form.ifNotExists" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="tableType" :label="$t('createTable.general.tableType')">
          <el-select v-model="form.tableType">
            <el-option :label="$t('createTable.normal')" value="NORMAL" />
            <el-option label="UNLOGGED" value="UNLOGGED" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="oids" :label="$t('createTable.general.withOIDS')">
          <el-checkbox v-model="form.oids" :disabled="form.storage == 'COLUMN'" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="tableSpace" :label="$t('createTable.general.tablespace')">
          <el-select v-model="form.tableSpace">
            <el-option v-for="item in tablespaceList" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="fillingFactor" :label="$t('createTable.general.fillFactor')">
          <el-input-number
            v-model="form.fillingFactor"
            :min="10"
            :max="100"
            :precision="0"
            controls-position="right"
            :disabled="form.storage == 'COLUMN'"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="storage" :label="$t('createTable.general.storage')">
          <el-select v-model="form.storage" @change="changeOrientation">
            <el-option
              v-for="item in tableOrientationList"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="$t('createTable.isPartition')">
          <el-checkbox v-model="isPartition" />
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item prop="comment" :label="$t('createTable.general.description')">
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

  const props = withDefaults(
    defineProps<{
      isPartition: boolean;
    }>(),
    {
      isPartition: false,
    },
  );
  const emit = defineEmits<{
    (e: 'update:isPartition', value: boolean): void;
  }>();
  const isPartition = computed({
    get: () => props.isPartition,
    set: (val) => emit('update:isPartition', val),
  });
  const { t } = useI18n();
  const tablespaceList = inject('tablespaceList', []);
  const ruleFormRef = ref<FormInstance>();
  const tableOrientationList = ref(['ROW', 'COLUMN']);
  const form = reactive({
    tableName: '',
    ifNotExists: false,
    tableType: 'NORMAL',
    oids: false,
    tableSpace: 'pg_default',
    fillingFactor: 100,
    storage: 'ROW',
    comment: '',
  });
  const rules = reactive<FormRules>({
    tableName: [
      {
        required: true,
        message: t('rules.empty', [t('createTable.general.name')]),
        trigger: 'blur',
      },
    ],
  });
  const changeOrientation = (value) => {
    if (value == 'COLUMN') {
      form.oids = false;
    }
  };
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
  const resetFields = () => {
    ruleFormRef.value.resetFields();
  };
  defineExpose({
    formValue: readonly(form),
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
  :deep(.el-input-number) {
    width: 100%;
  }
  :deep(.el-input-number .el-input__inner) {
    text-align: left;
  }
</style>
