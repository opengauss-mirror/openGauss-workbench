<template>
  <div class="coverage-rate-dialog">
    <el-dialog
      v-model="visible"
      :title="title"
      :width="1100"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @closed="handleClose"
    >
      <div class="dialog_body" v-loading="loading">
        <div class="buttons">
          <el-button type="primary" @click="handleExport">
            {{ $t('button.export') }}
          </el-button>
          <el-button plain @click="handleDelete">{{ $t('button.delete') }}</el-button>
        </div>
        <el-table
          ref="tableRef"
          :data="tableData"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          max-height="300"
          border
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column
            property="serialNumber"
            :label="t('coverageDialog.serialNumber')"
            width="100"
            align="center"
          />
          <el-table-column
            property="totalRows"
            :label="t('coverageDialog.totalRows')"
            width="100"
            align="center"
          />
          <el-table-column
            property="executionRows"
            :label="t('coverageDialog.executionRows')"
            width="100"
            align="center"
          />
          <el-table-column
            property="totalCoverage"
            :label="t('coverageDialog.totalCoverage')"
            width="100"
            align="center"
          />
          <el-table-column
            property="allLineNumber"
            :label="t('coverageDialog.allLineNumber')"
            width="170"
            align="center"
          />
          <el-table-column
            property="executionLineNumber"
            :label="t('coverageDialog.executionLineNumber')"
            width="170"
            align="center"
          />
          <el-table-column
            property="executionCoverage"
            :label="t('coverageDialog.executionCoverage')"
            width="120"
            align="center"
          />
          <el-table-column
            property="inputParams"
            :label="t('coverageDialog.inputParams')"
            width="120"
            align="center"
          />
          <el-table-column
            property="updateTime"
            :label="t('coverageDialog.updateTime')"
            width="170"
            align="center"
          />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
  import { ElMessage, ElMessageBox, ElTable } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { toSpacePascalCase, downLoadMyBlobType } from '@/utils';
  import { getCoverageRateList, deleteCoverageRate, exportCoverageRate } from '@/api/functionSP';
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      connectionName: string;
      uuid: string;
      oid: string;
      fileName: string;
    }>(),
    {
      modelValue: false,
      connectionName: '',
      uuid: '',
      oid: '',
      fileName: '',
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
  }>();
  interface CoverageList {
    cid: number;
    serialNumber: number;
    totalRows: number;
    executionRows: number;
    totalCoverage: string;
    allLineNumber: string;
    executionLineNumber: string;
    executionCoverage: string;
    inputParams: string;
    updateTime: string;
  }
  const { t } = useI18n();
  const loading = ref(false);
  const tableData = ref<CoverageList[]>([]);
  const tableRef = ref<InstanceType<typeof ElTable>>();
  const multipleSelection = ref<CoverageList[]>([]);
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });
  const title = computed(() => {
    return `${toSpacePascalCase(t('functionBar.coverageRate'))} - ${props.fileName}@${
      props.connectionName
    }`;
  });
  const handleSelectionChange = (val) => {
    multipleSelection.value = val;
  };
  const getList = async () => {
    loading.value = true;
    try {
      const res = (await getCoverageRateList({
        uuid: props.uuid,
        oid: props.oid,
      })) as unknown as CoverageList[];
      tableData.value = res;
    } finally {
      loading.value = false;
    }
  };
  const handleOpen = async () => {
    getList();
  };
  const handleClose = () => {
    visible.value = false;
    resetForm();
  };
  const resetForm = () => {
    tableData.value = [];
    tableRef.value!.clearSelection();
  };
  const noChooseData = () => {
    ElMessage.warning(t('message.selectedData'));
  };
  const handleExport = async () => {
    if (!multipleSelection.value.length) return noChooseData();
    const res = await exportCoverageRate({
      uuid: props.uuid,
      oid: props.oid,
      cidList: multipleSelection.value.map((item) => item.cid),
    });
    downLoadMyBlobType(res.name, res.data);
  };
  const handleDelete = async () => {
    if (!multipleSelection.value.length) return noChooseData();
    ElMessageBox.confirm(t('coverageDialog.deleteMsg')).then(async () => {
      await deleteCoverageRate({
        uuid: props.uuid,
        cidList: multipleSelection.value.map((item) => item.cid),
      });
      ElMessage.success(t('message.deleteSuccess'));
      getList();
    });
  };
</script>

<style lang="scss" scoped>
  .coverage-rate-dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 25px;
    }
  }
  .buttons {
    text-align: right;
    margin-bottom: 10px;
  }
</style>
