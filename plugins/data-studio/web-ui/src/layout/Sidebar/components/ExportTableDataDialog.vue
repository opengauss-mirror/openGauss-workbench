<template>
  <div class="export-dialog">
    <el-dialog
      v-model="visible"
      :title="title"
      :width="750"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="140px">
          <el-form-item prop="columnList" :label="$t('table.export.selectColumns')">
            <el-table
              ref="multipleTableRef"
              :data="tableData"
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
            </el-table>
          </el-form-item>
          <el-row :gutter="10">
            <el-col :span="13">
              <el-form-item prop="fileType" :label="$t('table.export.format')">
                <el-select v-model="form.fileType" @change="handleChangeFormat">
                  <el-option v-for="item in formatList" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item prop="quote" :label="$t('table.export.quotes')">
                <el-input v-model="form.quote" :disabled="form.fileType != 'Text'" />
              </el-form-item>
              <el-form-item prop="escape" :label="$t('table.export.escape')">
                <el-input v-model="form.escape" :disabled="form.fileType != 'Text'" />
              </el-form-item>
              <el-form-item prop="replaceNull" :label="$t('table.export.replaceNullWith')">
                <el-input v-model="form.replaceNull" :disabled="form.fileType != 'Text'" />
              </el-form-item>
              <el-form-item prop="encoding" :label="$t('table.export.encoding')">
                <el-select v-model="form.encoding" placement="top">
                  <el-option v-for="item in encodingList" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="11">
              <el-form-item prop="includeHeader" :label="$t('table.export.includeHeader')">
                <el-checkbox v-model="form.includeHeader" :disabled="form.fileType != 'Text'" />
              </el-form-item>
              <el-form-item prop="delimiter" :label="$t('table.export.delimiter')">
                <el-radio-group
                  v-model="form.delimiter"
                  :disabled="form.fileType != 'Text'"
                  @change="handleChangeDelimiter"
                >
                  <el-radio label="comma">{{ t('table.export.comma') }}(,)</el-radio>
                  <el-radio label="tab">
                    {{ t('table.export.tab') }}({{ t('table.export.fourSpaces') }})
                  </el-radio>
                  <el-radio label="pipe">{{ t('table.export.pipe') }}(|)</el-radio>
                  <el-radio label="semicolon">{{ t('table.export.semicolon') }}(;)</el-radio>
                  <el-radio label="other">
                    {{ t('table.export.other') }}{{ t('common.colon') }}
                  </el-radio>
                  <el-input
                    v-model="form.delimiterOther"
                    :disabled="form.delimiter != 'other'"
                    maxlength="10"
                  />
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
          <el-button type="primary" @click="confirmForm">
            {{ $t('button.confirm') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, ElTable, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import type { NodeData } from '../types';
  import { getTableColumn } from '@/api/table';
  import { loadingInstance, downLoadMyBlobType } from '@/utils';
  import { exportTableData } from '@/api/table';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      nodeData: Partial<NodeData>;
    }>(),
    {
      modelValue: false,
      nodeData: () => ({}),
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'success', data: any): void;
  }>();

  const loading = ref(null);
  const ruleFormRef = ref<FormInstance>();
  const tableData = ref([]);
  const multipleTableRef = ref<InstanceType<typeof ElTable>>();
  const formatList = reactive(['Excel(xlsx)', 'Excel(xls)', 'Text', 'Binary']);
  const encodingList = reactive(['UTF-8', 'GBK', 'LATIN1']);
  const form = reactive({
    columnList: [],
    fileType: 'Excel(xlsx)',
    quote: '',
    escape: '',
    replaceNull: '',
    encoding: 'UTF-8',
    includeHeader: true,
    delimiter: '',
    delimiterOther: '',
  });
  const rules = reactive<FormRules>({
    columnList: [
      {
        required: true,
        type: 'array',
        message: t('rules.empty', [t('table.export.selectColumns')]),
        trigger: 'change',
      },
    ],
  });
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });
  const uuid = computed(() => {
    return props.nodeData?.uuid;
  });
  const schema = computed(() => {
    return props.nodeData?.schemaName;
  });
  const tableName = computed(() => {
    return props.nodeData?.name;
  });
  const oid = computed(() => {
    return props.nodeData?.oid;
  });
  const title = computed(() => {
    return `${t('export.tableData')}${t('common.colon')} ${schema.value}.${tableName.value}`;
  });

  const handleSelectionChange = (val) => {
    form.columnList = val;
  };
  const handleChangeFormat = (val) => {
    Object.assign(form, {
      quote: '',
      escape: '',
      replaceNull: '',
      includeHeader: true,
      delimiter: val == 'Text' ? 'comma' : '',
      delimiterOther: '',
    });
  };
  const handleChangeDelimiter = () => {
    form.delimiterOther = '';
  };

  const handleOpen = async () => {
    loading.value = loadingInstance();
    try {
      const res = await getTableColumn({
        uuid: uuid.value,
        schema: schema.value,
        tableName: tableName.value,
        oid: oid.value,
      });
      tableData.value = res.result.map((row) => {
        return {
          columnName: row[0],
          dataType: row[1],
        };
      });
      nextTick(() => {
        multipleTableRef.value!.toggleAllSelection();
      });
    } finally {
      loading.value.close();
    }
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    ruleFormRef.value.resetFields();
  };
  const confirmForm = async () => {
    if (form.delimiter == 'other' && !form.delimiterOther) {
      return ElMessage.error(t('rules.enter', [t('table.export.delimiter')]));
    }
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = loadingInstance();
        const delimiterMap = {
          comma: ',',
          tab: '    ',
          pipe: '|',
          semicolon: ';',
        };
        const params = {
          columnList: form.columnList.map((item) => item.columnName),
          fileType: form.fileType,
          titleFlag: form.includeHeader,
          quote: form.quote,
          escape: form.escape,
          replaceNull: form.replaceNull,
          delimiter: form.delimiter == 'other' ? form.delimiterOther : delimiterMap[form.delimiter],
          encoding: form.encoding,
          uuid: uuid.value,
          schema: schema.value,
          tableName: tableName.value,
        };
        try {
          const res = await exportTableData(params);
          downLoadMyBlobType(res.name, res.data);
          visible.value = false;
        } finally {
          loading.value.close();
        }
      }
    });
  };
</script>

<style lang="scss" scoped>
  .export-dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 5px;
    }
    :deep(.el-select) {
      width: 100%;
    }
    :deep(.el-input-number .el-input__inner) {
      text-align: left;
    }
    :deep(.el-radio-group) {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
    }
  }
</style>
