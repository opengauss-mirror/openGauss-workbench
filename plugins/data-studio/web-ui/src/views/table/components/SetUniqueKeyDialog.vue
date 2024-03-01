<template>
  <div class="common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="$t('table.customUniqueKey')"
      :width="500"
      align-center
      append-to-body
      :close-on-click-modal="false"
      @opened="handleOpen"
      @closed="handleClose"
    >
      <div class="dialog_body">
        <el-form
          :model="form"
          ref="ruleFormRef"
          :rules="rules"
          label-width="70px"
          :label-suffix="$t('common.colon')"
        >
          <el-form-item :label="$t('table.title')">{{ tableName }}</el-form-item>
          <el-form-item prop="uniqueColumn" :label="$t('table.column.title')">
            <el-table
              ref="multipleTableRef"
              :data="tableData"
              style="width: 100%"
              @selection-change="handleSelectionChange"
              max-height="300"
              border
            >
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column
                property="columnName"
                :label="t('table.column.columnName')"
                align="center"
              />
              <el-table-column
                property="dataType"
                :label="t('table.column.dataType')"
                width="120"
                align="center"
              />
            </el-table>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="visible = false">{{ $t('button.cancel') }}</el-button>
        <el-button type="primary" @click="confirmForm">
          {{ $t('button.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
  import { ElMessage, ElTable, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { getTableColumn, setConstraintPk } from '@/api/table';
  interface CommonParams {
    tableName: string;
    schema: string;
    uuid: string;
    oid: string;
  }
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      commonParams: CommonParams;
      tableName: string;
      schema: string;
      uuid: string;
    }>(),
    {
      modelValue: false,
      commonParams: () => ({
        tableName: '',
        schema: '',
        uuid: '',
        oid: '',
      }),
      tableName: '',
      schema: '',
      uuid: '',
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'success'): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });
  const { t } = useI18n();
  const ruleFormRef = ref<FormInstance>();
  const form = reactive({
    uniqueColumn: [],
    uuid: '',
  });
  const rules = reactive<FormRules>({
    uniqueColumn: [
      {
        required: true,
        type: 'array',
        message: t('rules.empty', [t('table.column.title')]),
        trigger: 'change',
      },
    ],
  });
  const tableData = ref([]);
  const multipleTableRef = ref<InstanceType<typeof ElTable>>();
  const handleSelectionChange = (val) => {
    form.uniqueColumn = val;
  };
  const handleOpen = async () => {
    const res = await getTableColumn(props.commonParams);
    tableData.value = res.result.map((row) => {
      return {
        columnName: row[0],
        dataType: row[1],
      };
    });
  };
  const handleClose = () => {
    resetForm();
    visible.value = false;
  };
  const confirmForm = async () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        await setConstraintPk({
          column: form.uniqueColumn.map((item) => item.columnName).join(),
          schema: props.schema,
          tableName: props.tableName,
          uuid: props.uuid,
        });
        myEmit('success');
        ElMessage.success(`${t('message.setSuccess')}`);
        handleClose();
      }
    });
  };
  const resetForm = () => {
    multipleTableRef.value!.clearSelection();
    tableData.value = [];
  };
</script>

<style lang="scss" scoped>
  :deep(.el-select) {
    width: 100%;
  }
</style>
