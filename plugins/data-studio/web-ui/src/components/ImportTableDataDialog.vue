<template>
  <div class="import-dialog common-dialog-wrapper">
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
          <el-form-item prop="file" :label="$t('table.data.importFile')">
            <el-upload
              ref="uploadRef"
              class="upload-demo"
              v-model:file-list="form.file"
              :limit="1"
              action="#"
              :data="uploadParams"
              :auto-upload="false"
              :on-exceed="handleFileExceed"
            >
              <template #trigger>
                <el-button type="primary">
                  {{ $t('table.data.chooseFile') }}
                </el-button>
              </template>
            </el-upload>
          </el-form-item>
          <el-row :gutter="10">
            <el-col :span="13">
              <el-form-item prop="fileType" :label="$t('table.export.format')">
                <el-select v-model="form.fileType" @change="handleChangeFormat">
                  <el-option v-for="item in formatList" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item prop="quote" :label="$t('table.export.quotes')">
                <el-input
                  v-model="form.quote"
                  :disabled="!['CSV', 'Text'].includes(form.fileType)"
                />
              </el-form-item>
              <el-form-item prop="escape" :label="$t('table.export.escape')">
                <el-input
                  v-model="form.escape"
                  :disabled="!['CSV', 'Text'].includes(form.fileType)"
                />
              </el-form-item>
              <el-form-item prop="replaceNull" :label="$t('table.export.replaceNullWith')">
                <el-input
                  v-model="form.replaceNull"
                  :disabled="!['CSV', 'Text'].includes(form.fileType)"
                />
              </el-form-item>
              <el-form-item prop="encoding" :label="$t('table.export.encoding')">
                <el-select v-model="form.encoding" placement="top">
                  <el-option v-for="item in encodingList" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item prop="timeFormat" :label="$t('table.export.timeFormat')">
                <el-select
                  v-model="form.timeFormat"
                  :disabled="!['Excel(xlsx)', 'Excel(xls)'].includes(form.fileType)"
                  placement="top"
                >
                  <el-option
                    v-for="item in timeFormatList"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="11">
              <el-form-item prop="includeHeader" :label="$t('table.export.includeHeader')">
                <el-checkbox v-model="form.includeHeader" :disabled="form.fileType == 'Binary'" />
              </el-form-item>
              <el-form-item prop="delimiter" :label="$t('table.export.delimiter')">
                <el-radio-group
                  v-model="form.delimiter"
                  :disabled="!['CSV', 'Text'].includes(form.fileType)"
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
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
        <el-button type="primary" @click="confirmForm">
          {{ $t('button.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, ElTable, FormInstance, FormRules, genFileId } from 'element-plus';
  import type { UploadInstance, UploadProps, UploadRawFile } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { getTableColumn } from '@/api/table';
  import { loadingInstance } from '@/utils';
  import { importTableData } from '@/api/table';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      uuid: string;
      schema: string;
      tableName: string;
      oid: string;
    }>(),
    {
      modelValue: false,
      nodeData: () => ({}),
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'success'): void;
  }>();

  const loading = ref(null);
  const ruleFormRef = ref<FormInstance>();
  const uploadRef = ref<UploadInstance>();
  const tableData = ref<{ columnName: string; dataType: string }[]>([]);
  const multipleTableRef = ref<InstanceType<typeof ElTable>>();
  const formatList = computed(() => ['Excel(xlsx)', 'Excel(xls)', 'CSV', 'Text', 'Binary']);
  const encodingList = reactive(['UTF-8', 'GBK', 'LATIN1']);
  const timeFormatList = ['yyyy-MM-dd HH:mm:ss', 'yyyy-MM-dd hh:mm:ss'];
  const form = reactive({
    columnList: [],
    file: [],
    fileType: '',
    quote: '',
    escape: '',
    replaceNull: '',
    encoding: 'UTF-8',
    timeFormat: 'yyyy-MM-dd HH:mm:ss',
    includeHeader: true,
    delimiter: '',
    delimiterOther: '',
  });
  const rules = reactive<FormRules>({
    file: [
      {
        required: true,
        type: 'array',
        len: 1,
        message: t('rules.empty', [t('table.data.importFile')]),
        trigger: 'change',
      },
    ],
    columnList: [
      {
        required: true,
        type: 'array',
        message: t('rules.empty', [t('table.export.selectColumns')]),
        trigger: 'change',
      },
    ],
  });
  const delimiterMap = {
    comma: ',',
    tab: '    ',
    pipe: '|',
    semicolon: ';',
  };
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });
  const title = computed(() => {
    return `${t('import.tableData')}${t('common.colon')} ${props.schema ? props.schema + '.' : ''}${
      props.tableName
    }`;
  });
  const uploadParams = {};

  const handleSelectionChange = (selections) => {
    form.columnList = tableData.value.filter((row) => {
      return selections.findIndex((s) => s.columnName == row.columnName) > -1;
    });
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

  const handleFileExceed: UploadProps['onExceed'] = (files) => {
    uploadRef.value!.clearFiles();
    const file = files[0] as UploadRawFile;
    file.uid = genFileId();
    uploadRef.value!.handleStart(file);
  };

  const handleFileUpload = () => {
    const params = {
      file: form.file[0]?.raw,
      uuid: props.uuid,
      schema: props.schema,
      tableName: props.tableName,
      columnString: form.columnList.map((item) => item.columnName).join(),
      fileType: form.fileType,
      titleFlag: form.includeHeader,
      quote: form.quote || undefined,
      escape: form.escape || undefined,
      replaceNull: form.replaceNull || undefined,
      delimiter: form.delimiter == 'other' ? form.delimiterOther : delimiterMap[form.delimiter],
      encoding: form.encoding,
      timeFormat: form.timeFormat,
    };
    const formData = new FormData();
    for (const [key, value] of Object.entries(params)) {
      if (['quote', 'escape', 'replaceNull', 'delimiter'].includes(key)) {
        // if value === undefined, this formdata will also retain this field, and there will be problem in the backend.
        value && formData.append(key, value);
      } else {
        formData.append(key, value);
      }
    }
    return importTableData(formData);
  };

  const handleOpen = async () => {
    Object.assign(form, {
      fileType: formatList.value[0],
    });
    try {
      loading.value = loadingInstance();
      const res = await getTableColumn({
        uuid: props.uuid,
        schema: props.schema,
        tableName: props.tableName,
        oid: props.oid,
      });
      tableData.value = res.result.map((row) => {
        return {
          columnName: row[0],
          dataType: row[1],
        };
      });
      nextTick(() => {
        multipleTableRef.value?.toggleAllSelection();
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
        try {
          loading.value = loadingInstance();
          await handleFileUpload();
          ElMessage.success(t('message.success'));
          myEmit('success');
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
  .upload-demo {
    display: flex;
    width: 100%;
    :deep(.el-upload-list) {
      margin: 0;
      flex: 1;
      .el-upload-list__item {
        margin-bottom: 0;
      }
    }
  }
</style>
