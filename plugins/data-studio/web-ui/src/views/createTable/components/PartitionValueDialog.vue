<template>
  <div class="common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="$t('createTable.partition.definePartitionValue')"
      :width="750"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-form :model="form" ref="ruleFormRef" label-width="0" :show-message="false">
          <el-table ref="tableRef" :data="form.columns" max-height="300" border>
            <el-table-column property="name" :label="t('createTable.column')" />
            <el-table-column property="value" :label="t('createTable.value')" v-slot="scope">
              <el-form-item
                v-if="scope.row.type == 'input'"
                :prop="`columns.${scope.$index}.value`"
                :rules="rules.input"
              >
                <el-input v-model="scope.row.value" />
              </el-form-item>
              <el-form-item
                v-if="scope.row.type == 'number'"
                :prop="`columns.${scope.$index}.value`"
                :rules="rules.number"
              >
                <el-input-number v-model="scope.row.value" :controls="false" />
              </el-form-item>
            </el-table-column>
          </el-table>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
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

  interface Column {
    name: string;
    value: any;
    type: string;
  }

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      columns: Column[];
    }>(),
    {
      modelValue: false,
      columns: () => [],
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'success', data?: any): void;
  }>();

  const ruleFormRef = ref<FormInstance>();
  const tableRef = ref<InstanceType<typeof ElTable>>();
  const form = reactive({
    columns: [],
  });
  const rules = reactive<FormRules>({
    input: [
      {
        required: true,
        message: t('rules.empty', [t('createTable.value')]),
        trigger: 'blur',
      },
    ],
    number: [
      {
        required: true,
        type: 'number',
        message: t('rules.empty', [t('createTable.value')]),
        trigger: 'blur',
      },
    ],
  });
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });

  const handleOpen = async () => {
    form.columns = JSON.parse(JSON.stringify(props.columns));
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    ruleFormRef.value.resetFields();
  };
  const confirmForm = async () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        myEmit('success', JSON.parse(JSON.stringify(form.columns)));
        visible.value = false;
      } else {
        ElMessage.error(t('message.enterValue'));
      }
    });
  };
</script>

<style lang="scss" scoped>
  :deep(.el-input-number .el-input__inner) {
    text-align: left;
  }
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
</style>
