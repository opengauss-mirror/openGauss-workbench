<template>
  <div class="snippets-list-wrapper">
    <div :class="['my-form-wrapper', { 'is-advanced-search': showAdvancedSearch }]">
      <el-form :model="form" ref="ruleFormRef" label-width="75px" class="my-form" @submit.prevent>
        <el-form-item prop="name" :label="$t('common.name')" class="form-item-line1">
          <el-input
            v-model="form.name"
            style="width: calc(100% - 130px)"
            @keyup.enter="handleQuery"
          >
            <template #append>
              <el-button :icon="Search" @click="handleQuery" />
            </template>
          </el-input>
          <el-button @click="resetQueryForm" style="margin-left: 5px">
            {{ $t('button.reset') }}
          </el-button>
          <el-button
            plain
            style="margin-left: 5px"
            @click="showAdvancedSearch = !showAdvancedSearch"
          >
            {{ $t('button.advancedSearch') }}
          </el-button>
        </el-form-item>
        <div class="advanced-search-box" v-show="showAdvancedSearch">
          <el-form-item prop="code" :label="$t('snippets.name')">
            <el-input v-model="form.code" type="textarea" :rows="2" resize="none" />
          </el-form-item>
          <el-form-item class="form-item-search" label-width="0">
            <el-button type="primary" @click="handleQuery">
              {{ $t('button.search') }}
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <el-table :data="tableData" border row-key="no" default-expand-all class="my-table">
      <el-table-column type="expand" width="1">
        <template #default="props">
          <div class="code-preview-wrapper">
            <div class="code-preview">{{ props.row.code }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('common.serialNumber')"
        prop="no"
        align="center"
        width="65"
      ></el-table-column>
      <el-table-column :label="$t('common.name')" prop="name" align="center">
        <template #default="{ row }">
          <a class="id-text" @click="handleEdit(row)">
            {{ row.name }}
          </a>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" align="center" width="100">
        <template #default="{ row }">
          <span
            class="iconfont icon-copy opetation-button"
            :title="$t('button.copy')"
            @click="handleCopy(row)"
          ></span>
          <span
            class="iconfont icon-daochu opetation-button"
            :title="$t('button.exportToTerminal')"
            @click="handleExport(row)"
          ></span>
          <span
            class="iconfont icon-shanchu opetation-button"
            :title="$t('button.delete')"
            @click="handleDelelte(row)"
          ></span>
        </template>
      </el-table-column>
    </el-table>
    <Toolbar v-model:page="page" @getData="(params) => onlyGetData(params)" />
  </div>
</template>
<script lang="ts" setup>
  import { Search } from '@element-plus/icons-vue';
  import { ElMessage, ElMessageBox, ElTable } from 'element-plus';
  import Toolbar from './Toolbar.vue';
  import { useI18n } from 'vue-i18n';
  import { copyToClickBoard } from '@/utils';
  import { getSqlCodeListApi, deleteSqlCodeApi } from '@/api/snippets';

  const { t } = useI18n();
  const form = reactive({
    name: '',
    code: '',
  });
  const showAdvancedSearch = ref(false);
  const page = reactive({
    pageNum: 1,
    pageSize: 10,
    pageTotal: 1,
    dataSize: 0,
  });
  const tableData = ref([]);
  const toggleSnippetsList = inject<({ id }: { id?: string }) => void>('toggleSnippetsList');

  const resetQueryForm = () => {
    Object.assign(form, {
      name: '',
      code: '',
    });
  };
  const resetQueryPage = () => {
    showAdvancedSearch.value = false;
    resetQueryForm();
  };
  const onlyGetData = async (pageParams = {}) => {
    const res = await getSqlCodeListApi({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      name: form.name,
      code: form.code,
      ...pageParams, // Optional parameters, if so, override
    });
    tableData.value = res.data?.map((item, index) => {
      return {
        no: index + 1,
        ...item,
      };
    });
    Object.assign(page, {
      pageNum: res.pageNum,
      pageSize: res.pageSize,
      pageTotal: res.pageTotal,
      dataSize: res.dataSize,
    });
  };
  const handleQuery = () => {
    Object.assign(page, {
      pageNum: 1,
      pageSize: page.pageSize,
    });
    onlyGetData();
  };
  const handleCopy = (row) => {
    copyToClickBoard(row.code);
    ElMessage.success(t('message.copySuccess'));
  };
  const handleImportFile =
    inject<(data: string, textType?: 'import' | 'export') => void>('handleImportFile');
  const handleExport = (row) => {
    handleImportFile(row.code, 'export');
  };
  const handleDelelte = (row) => {
    ElMessageBox.confirm(t('message.deleteSnippet'), t('common.warning'), {
      type: 'warning',
    })
      .then(async () => {
        await deleteSqlCodeApi({ id: row.id });
        ElMessage({
          type: 'success',
          message: t('message.deleteSuccess'),
        });
        handleQuery();
      })
      .catch(() => ({}));
  };
  const handleEdit = (row) => {
    toggleSnippetsList({ id: row.id });
  };
  defineExpose({
    resetQueryPage,
    handleQuery,
  });
</script>
<style lang="scss" scoped>
  .id-text {
    color: var(--el-color-primary);
    text-decoration: underline;
    cursor: pointer;
  }
  .snippets-list-wrapper {
    height: 100%;
    display: flex;
    flex-direction: column;
    position: relative;
  }
  .is-advanced-search {
    border: 1px solid var(--el-border-color-lighter);
    margin: -10px;
    margin-bottom: -10px;
    padding: 10px;
    padding-bottom: 0;
    box-shadow: 0px 12px 32px 4px rgba(0, 0, 0, 0.04), 0px 8px 20px rgba(0, 0, 0, 0.08);
  }
  .my-form-wrapper {
    position: absolute;
    top: 0;
    left: 0;
    z-index: 5;
    background: #fff;
    width: 100%;
    .my-form {
      :deep(.el-form-item) {
        margin-right: 0;
        width: 100%;
      }
      .form-item-line1 {
        :deep(.el-form-item__content) {
          flex-wrap: nowrap;
        }
      }
      .form-item-search {
        :deep(.el-form-item__content) {
          justify-content: center;
        }
      }
    }
  }
  .my-table {
    flex: 1;
    margin-top: 51px;
  }
  :deep(.el-table__expand-column) {
    visibility: hidden;
  }
  .code-preview-wrapper {
    padding: 0 8px;
    .code-preview {
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 3;
    }
  }
  .opetation-button {
    color: var(--el-color-primary);
    cursor: pointer;
    &:nth-child(n + 2) {
      margin-left: 5px;
    }
  }
</style>
