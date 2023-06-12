<template>
  <div class="partition-tab">
    <el-form
      :model="form"
      class="rule-form partition-form"
      ref="ruleFormRef"
      :rules="rules"
      label-width="125px"
      label-position="left"
      :label-suffix="$t('common.colon')"
    >
      <el-card class="box-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>{{ $t('createTable.partition.partitionType') }}</span>
          </div>
        </template>
        <el-row :gutter="50">
          <el-col :span="12">
            <el-form-item prop="partitionType" :label="$t('createTable.partition.partitionType')">
              <el-select v-model="form.partitionType" @change="changePartitionType">
                <el-option
                  v-for="item in partitionTypeList"
                  :key="item.value"
                  :label="item.name"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="valueInterval" :label="$t('createTable.partition.intervalValue')">
              <el-input
                v-model="form.valueInterval"
                :disabled="form.partitionType !== 'interval'"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>
      <el-card class="box-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>{{ $t('createTable.partition.partitionDefinition') }}</span>
          </div>
        </template>
        <el-row :gutter="50">
          <el-col :span="12">
            <el-form-item prop="partitionName" :label="$t('createTable.partition.partitionName')">
              <el-input v-model="form.partitionName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="tableSpace" :label="$t('createTable.partition.tablespace')">
              <el-select v-model="form.tableSpace">
                <el-option v-for="item in tablespaceList" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              prop="partitionColumn"
              :label="$t('createTable.partition.partitionColumn')"
            >
              <el-select
                v-model="form.partitionColumn"
                multiple
                :multiple-limit="4"
                @change="changePartitionColumn"
              >
                <el-option
                  v-for="item in props.columns"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="partitionValue" :label="$t('createTable.partition.partitionValue')">
              <el-input v-model="form.partitionValue" disabled>
                <template #append>
                  <el-button :icon="MoreFilled" @click="setPartitionValueDialog = true" />
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>
    </el-form>
    <PartitionValueDialog
      v-model="setPartitionValueDialog"
      :columns="selectColumnList"
      @success="partitionDialogSuccess"
    />
  </div>
</template>

<script lang="ts" setup>
  import { FormInstance, FormRules } from 'element-plus';
  import { MoreFilled } from '@element-plus/icons-vue';
  import PartitionValueDialog from './PartitionValueDialog.vue';
  import { useI18n } from 'vue-i18n';
  import type { AvailColumnList } from '../types';

  const props = withDefaults(
    defineProps<{
      columns: AvailColumnList[];
    }>(),
    {
      columns: () => [],
    },
  );
  const { t } = useI18n();
  const tablespaceList = inject('tablespaceList', []);
  const ruleFormRef = ref<FormInstance>();
  const form = reactive({
    partitionType: 'range',
    valueInterval: '',
    partitionName: '',
    tableSpace: 'pg_default',
    partitionColumn: [],
    partitionValue: '',
    partitionValueList: [],
  });
  const rules = reactive<FormRules>({
    partitionName: [
      {
        required: true,
        message: t('rules.empty', [t('createTable.partition.partitionName')]),
        trigger: 'blur',
      },
    ],
    partitionColumn: [
      {
        type: 'array',
        required: true,
        message: t('rules.empty', [t('createTable.partition.partitionColumn')]),
        trigger: 'change',
      },
    ],
    partitionValue: [
      {
        required: true,
        message: t('rules.empty', [t('createTable.partition.partitionValue')]),
        trigger: 'change',
      },
    ],
  });
  const partitionTypeList = ref([
    {
      name: 'BY RANGE',
      value: 'range',
    },
    {
      name: 'BY INTERVAL',
      value: 'interval',
    },
    {
      name: 'BY LIST',
      value: 'list',
    },
    {
      name: 'BY HASH',
      value: 'hash',
    },
  ]);
  const selectColumnList = ref([]);
  const setPartitionValueDialog = ref(false);

  const changePartitionType = () => {
    form.valueInterval = '';
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
          reject('PartitionTab');
        }
      });
    });
  };
  const resetFields = () => {
    ruleFormRef.value.resetFields();
    form.partitionValueList = [];
    selectColumnList.value = [];
  };
  const changePartitionColumn = (list: string[]) => {
    if (Array.isArray(list)) {
      const result = [];
      const numberTypeArr = [
        'numeric',
        'numeric[]',
        'numrange[]',
        'int16',
        'int16[]',
        'int2vector',
        'int2vector[]',
        'int2vector_extend',
        'int2vector_extend[]',
        'int4range[]',
        'int8range[]',
        'integer',
        'integer[]',
        'bigint',
        'bigint[]',
      ];
      list.forEach((item) => {
        const e = props.columns.find((c) => c.label == item);
        if (e) {
          result.push({
            name: e.label,
            value: null,
            type: numberTypeArr.includes(e.dataType) ? 'number' : 'input',
          });
        }
      });
      form.partitionValueList = [];
      form.partitionValue = '';
      selectColumnList.value = result;
    }
  };
  const partitionDialogSuccess = (list) => {
    form.partitionValue = list.map((item) => item.value).join();
    form.partitionValueList = list;
  };
  onMounted(() => {
    ruleFormRef.value.resetFields();
  });
  defineExpose({
    formValue: readonly(form),
    formRef,
    validateForm,
    resetFields,
  });
</script>

<style lang="scss" scoped>
  .card-header {
    font-weight: bold;
  }
  .box-card:first-child {
    margin-bottom: 20px;
  }
  :deep(.el-select) {
    width: 100%;
  }
  :deep(.el-input-number) {
    width: 100%;
  }
</style>
