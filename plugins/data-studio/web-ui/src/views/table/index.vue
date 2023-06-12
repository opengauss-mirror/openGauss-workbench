<template>
  <div class="table-page">
    <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleTabClick">
      <el-tab-pane label="DDL" name="DDL" />
      <el-tab-pane :label="$t('table.general.title')" name="GeneralTab" />
      <el-tab-pane :label="$t('table.column.title')" name="ColumnTab" />
      <el-tab-pane :label="$t('table.constraint.title')" name="ConstraintTab" />
      <el-tab-pane :label="$t('table.indexes.title')" name="IndexesTab" />
      <el-tab-pane :label="$t('table.data.title')" name="DataTab" />
    </el-tabs>
    <el-icon
      :class="['refresh', { disabled: dataMap[currentTabName]?.edited }]"
      @click="!dataMap[currentTabName].edited && handleRefresh()"
      ><Refresh
    /></el-icon>
    <div class="tabs-container">
      <DDL v-show="currentTabName == 'DDL'" :data="dataMap.DDL.data" />
      <GeneralTab
        v-show="currentTabName == 'GeneralTab'"
        :columns="dataMap.GeneralTab.columns"
        :data="dataMap.GeneralTab.data"
      />
      <div
        v-for="item in editTabs"
        :key="item"
        v-show="currentTabName == item"
        class="table-container"
      >
        <EditTable
          :tabName="item"
          :canEdit="dataMap[item].canEdit"
          :databaseName="databaseName"
          :data="dataMap[item].data"
          :allData="dataMap"
          :columnNameList="columnNameList"
          :commonParams="commonParams"
          :columns="dataMap[item].columns"
          :idKey="dataMap[item].idKey"
          :rowStatusKey="dataMap[item].rowStatusKey"
          :editingSuffix="editingSuffix"
          :editedSuffix="editedSuffix"
          :loading="dataMap[item].loading"
          v-model:edited="dataMap[item].edited"
          :dataTypeList="dataTypeList"
          :barStatus="dataMap[item].barStatus"
          @currentChange="handleCurrentChange"
          @cellDataChange="handleCellDataChange"
          @cellDblclick="handleCellDbClick"
        />
        <Toolbar
          :status="dataMap[item].barStatus"
          :type="item == 'DataTab' ? 'table' : 'common'"
          :canEdit="dataMap[item].canEdit"
          v-model:pageNum="page.pageNum"
          v-model:pageSize="page.pageSize"
          v-model:pageTotal="page.pageTotal"
          @save="handleSave(item)"
          @cancel="handleCancel(item)"
          @addLine="handleAddLine"
          @copyLine="handleCopyLine"
          @removeLine="handleRemoveLine"
          @firstPage="handleFirstPage"
          @lastPage="handleLastPage"
          @previousPage="handlePreviousPage"
          @nextPage="handleNextPage"
          @page="handlePage"
          @update:pageSize="handlePageSize"
        />
      </div>
    </div>
    <SetUniqueKeyDialog
      v-model="showUniqueDialog"
      :commonParams="commonParams"
      :tableName="commonParams.tableName"
      :schema="commonParams.schema"
      :uuid="commonParams.uuid"
      @success="dialogUniqueKeySuccess"
    />
  </div>
</template>

<script lang="ts" setup>
  import { ElMessage, ElMessageBox } from 'element-plus';
  import type { TabsPaneContext } from 'element-plus';
  import { useRoute } from 'vue-router';
  import { useAppStore } from '@/store/modules/app';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import DDL from './components/DDL.vue';
  import GeneralTab from './components/GeneralTab.vue';
  import EditTable from './components/EditTable.vue';
  import Toolbar from './components/Toolbar.vue';
  import SetUniqueKeyDialog from './components/SetUniqueKeyDialog.vue';
  import { eventQueue } from '@/hooks/saveData';
  import { useI18n } from 'vue-i18n';
  import { useTableDataHooks } from './useTableDataHooks';
  import { getDataTypeList } from '@/api/metaData';
  import {
    getTableDdl,
    getTableColumn,
    getTableConstraint,
    getTableIndex,
    getTableData,
    updateTableColumn,
    updateTableConstraint,
    updateTableIndex,
    updateTableData,
    getTableAttribute,
    closeTableDatas,
  } from '@/api/table';
  import { debounce, formatEditTableData, loadingInstance } from '@/utils';
  import { getColumn } from './columnsData';
  import type { EditDataResponse } from './types';

  const idSuffix = ref('_id');
  const rowStatusSuffix = ref('_rowStatus');
  const editingSuffix = ref('_isEditing');
  const editedSuffix = ref('_edited');
  const route = useRoute();
  const AppStore = useAppStore();
  const TagsViewStore = useTagsViewStore();
  const tagId = TagsViewStore.getViewByRoute(route)?.id;
  const { t } = useI18n();
  const loading = ref(null);

  const currentTabName = ref('DDL');
  const editTabs = ref(['ColumnTab', 'ConstraintTab', 'IndexesTab', 'DataTab']);
  const showUniqueDialog = ref(false);
  const databaseName = ref('');
  const commonParams = reactive({
    tableName: '',
    schema: '',
    uuid: '',
    oid: '',
    tableType: '',
  });
  const dataTypeList = ref([]);
  const dataMap = reactive({
    DDL: {
      data: '',
      edited: false,
      hasLoad: false,
      loading: false,
    },
    GeneralTab: {
      data: [],
      columns: [],
      edited: false,
      hasLoad: false,
      loading: false,
    },
    ColumnTab: {
      data: [],
      originData: [],
      canEdit: true,
      columns: getColumn('ColumnTab'),
      idKey: '_id',
      rowStatusKey: '_rowStatus',
      currentRow: undefined,
      edited: false,
      hasLoad: false,
      loading: false,
      barStatus: {
        save: true,
        cancel: true,
        addLine: true,
        removeLine: true,
      },
    },
    ConstraintTab: {
      data: [],
      originData: [],
      canEdit: true,
      columns: getColumn('ConstraintTab'),
      idKey: '_id',
      rowStatusKey: '_rowStatus',
      currentRow: undefined,
      edited: false,
      hasLoad: false,
      loading: false,
      barStatus: {
        save: true,
        cancel: true,
        addLine: true,
        removeLine: true,
      },
    },
    IndexesTab: {
      data: [],
      originData: [],
      canEdit: true,
      columns: getColumn('IndexesTab'),
      idKey: '_id',
      rowStatusKey: '_rowStatus',
      currentRow: undefined,
      edited: false,
      hasLoad: false,
      loading: false,
      barStatus: {
        save: true,
        cancel: true,
        addLine: true,
        removeLine: true,
      },
    },
    DataTab: {
      data: [],
      originData: [],
      canEdit: true,
      columns: [],
      idKey: '',
      rowStatusKey: '',
      currentRow: undefined,
      edited: false,
      hasLoad: false,
      loading: false,
      barStatus: {
        save: true,
        cancel: true,
        addLine: true,
        copyLine: true,
        removeLine: true,
        firstPage: true,
        previousPage: true,
        pageNum: true,
        nextPage: true,
        lastPage: true,
        pageSize: true,
      },
    },
  });
  let confirmUniqueKeyResolve = null;

  const globalIsEdit = computed(() => {
    return (
      dataMap.ColumnTab.edited ||
      dataMap.ConstraintTab.edited ||
      dataMap.IndexesTab.edited ||
      dataMap.DataTab.edited
    );
  });
  const columnNameList = computed(() => {
    return [...new Set(dataMap.ColumnTab.originData.map((item) => item.columnName))];
  });

  watch(
    globalIsEdit,
    (value) => {
      if (value) {
        eventQueue[tagId] = () => handleGlobalSave();
      } else {
        delete eventQueue[tagId];
      }
    },
    {
      immediate: true,
    },
  );

  watch(
    () => AppStore.language,
    () => {
      if (dataMap.GeneralTab.hasLoad) {
        getData('GeneralTab');
      }
    },
  );

  const handleTabClick = (tab: TabsPaneContext) => {
    if (dataMap[tab.paneName]?.hasLoad) return;
    getData(tab.paneName);
  };

  const fetchDataTypeList = async () => {
    dataTypeList.value = (await getDataTypeList(commonParams.uuid)) as unknown as string[];
  };

  const getData = async (type) => {
    const api = {
      DDL: getTableDdl,
      GeneralTab: getTableAttribute,
      ColumnTab: getTableColumn,
      ConstraintTab: getTableConstraint,
      IndexesTab: getTableIndex,
      DataTab: getTableData,
    };
    if (!Object.keys(api).includes(type)) return;
    dataMap[type].loading = true;
    let params: any = { ...commonParams };
    if (type == 'DataTab') {
      params = {
        ...params,
        pageNum: page.value.pageNum,
        pageSize: page.value.pageSize,
        winId: page.value.winId,
      };
    }
    api[type](params)
      .then((res) => {
        if (type == 'DDL') {
          dataMap.DDL.data = res.result;
        } else if (type == 'GeneralTab') {
          dataMap.GeneralTab.data = res.map((itemObj) => {
            const attr = Object.keys(itemObj)[0];
            return {
              attr,
              value: itemObj[attr],
            };
          });
        } else {
          const { column, result } = res.data || res;
          let idKey = '';
          let rowStatusKey = '';
          let columns = [];
          if (type == 'DataTab') {
            idKey = (column[0] || '') + idSuffix.value;
            rowStatusKey = (column[0] || '') + rowStatusSuffix.value;
            columns = column.map((item) => ({ name: item, label: item, type: 'input' }));
            Object.assign(page.value, {
              pageNum: res.pageNum || 0,
              pageSize: res.pageSize || 0,
              pageTotal: res.pageTotal || 0,
            });
          } else {
            idKey = idSuffix.value;
            rowStatusKey = rowStatusSuffix.value;
            columns = getColumn(type);
          }
          const data = formatEditTableData({ columns, data: result, idKey, rowStatusKey });
          if (type == 'ConstraintTab') {
            data.forEach((item: any, index) => {
              item.columnName = item.columnName?.split(',') || [];
              item.constrainType = result[index][6]
                ? [result[index][2], result[index][6], result[index][7], result[index][8]]
                : [result[index][2]];
            });
          }
          if (type == 'IndexesTab') {
            data.forEach((item: any) => {
              item.columnName = item.columnName?.split(',') || [];
            });
          }
          Object.assign(dataMap[type], {
            data,
            originData: JSON.parse(JSON.stringify(data)),
            columns,
            idKey,
            rowStatusKey,
            currentRow: undefined,
            edited: false,
          });
        }
      })
      .finally(() => {
        dataMap[type].hasLoad = true;
        dataMap[type].loading = false;
        return;
      });
  };

  const doFreshDebounce = debounce(
    () => {
      getData(currentTabName.value);
    },
    1000,
    true,
  );
  const handleRefresh = async () => {
    if (!currentTabName.value) return;
    if (currentTabName.value == 'DataTab') {
      await closeTableDatas(page.value.winId);
    }
    doFreshDebounce();
  };

  const handleCurrentChange = (currentRow) => {
    dataMap[currentTabName.value].currentRow = currentRow;
  };
  const handleCellDataChange = () => {
    if (!dataMap[currentTabName.value].edited) dataMap[currentTabName.value].edited = true;
  };
  const handleCellDbClick = (row, column) => {
    const cellColumn = dataMap[currentTabName.value].columns.find(
      (item) => item.name == column.property,
    );
    if (
      currentTabName.value == 'ConstraintTab' &&
      cellColumn &&
      cellColumn.type == 'cascader' &&
      row.columnName
    ) {
      cellColumn.options[4].disabled = row.columnName.length > 1;
    }
  };

  const getRowInfo = () => {
    const data = dataMap[currentTabName.value].data;
    const currentRow = dataMap[currentTabName.value].currentRow;
    const idKey = dataMap[currentTabName.value].idKey;
    const rowStatusKey = dataMap[currentTabName.value].rowStatusKey;
    const rowIndex = data.findIndex(
      (item) => currentRow && item[idKey] == dataMap[currentTabName.value].currentRow[idKey],
    );
    return { data, idKey, rowStatusKey, currentRow, rowIndex };
  };

  const waitSetUniqueKey = () => {
    return new Promise((resolve) => {
      confirmUniqueKeyResolve = resolve;
    });
  };

  const handleSetUniqueKeyGuide = () => {
    return new Promise<void>((resolve, reject) => {
      ElMessageBox.confirm(
        t('message.editTableGuide', { name: `${commonParams.schema}.${commonParams.tableName}` }),
        t('table.editTableGuide'),
        {
          confirmButtonText: t('table.customUniqueKey'),
          type: 'warning',
          dangerouslyUseHTMLString: true,
        },
      )
        .then(async () => {
          showUniqueDialog.value = true;
          await waitSetUniqueKey();
          resolve();
        })
        .catch(() => {
          reject();
        });
    });
  };

  const dialogUniqueKeySuccess = async () => {
    confirmUniqueKeyResolve();
    confirmUniqueKeyResolve = null;
    await handleSave('DataTab');
  };

  const handleSave = async (tabName, needNewData = true) => {
    enum saveType {
      'add' = 1,
      'delete' = 2,
      'edit' = 3,
    }
    const { columns, idKey, rowStatusKey, originData } = dataMap[tabName];
    const currentTabInfo = dataMap[tabName];
    if (!currentTabInfo.edited) return;
    loading.value = loadingInstance();
    try {
      if (tabName == 'ColumnTab') {
        const columnParam = {
          ...commonParams,
          data: currentTabInfo.data
            .filter((item) => item[currentTabInfo.rowStatusKey])
            .map((item) => {
              const rowStatus = item[rowStatusKey];
              const getStatus = (row, key) => {
                if (rowStatus == 'edit')
                  return row[key + editedSuffix.value] ? row[key] : undefined;
                return row[key];
              };
              return {
                operationType: saveType[item[rowStatusKey]],
                newColumnName: getStatus(item, 'columnName'),
                columnName: originData.find((e) => e[idKey] == item[idKey])?.columnName, // old columnName
                type: item.dataType,
                empty: getStatus(item, 'canBeNull'),
                defaultValue: getStatus(item, 'defaultValue'),
                only: getStatus(item, 'isUnique'),
                precision: item.precisionSize,
                scope: item.range,
                comment: getStatus(item, 'description'),
              };
            }),
        };
        await updateTableColumn(columnParam);
      }
      if (tabName == 'ConstraintTab') {
        const constraintParam = {
          ...commonParams,
          constraints: dataMap.ConstraintTab.data
            .filter((item) => item[dataMap.ConstraintTab.rowStatusKey])
            .map((item) => {
              return {
                type: saveType[item[rowStatusKey]],
                conname: item.constrainName,
                oldConname: originData.find((e) => e[idKey] == item[idKey])?.constrainName,
                attname: Array.isArray(item.columnName) ? item.columnName.join(',') : '',
                contype: Array.isArray(item.constrainType) ? item.constrainType[0] : null,
                nspname:
                  item.constrainType?.[0] == 'f' && item.constrainType[1]
                    ? item.constrainType[1]
                    : null, //fKey namespace
                tbname:
                  item.constrainType?.[0] == 'f' && item.constrainType[2]
                    ? item.constrainType[2]
                    : null, //fKey tablename
                colname:
                  item.constrainType?.[0] == 'f' && item.constrainType[3]
                    ? item.constrainType[3]
                    : null, //fKey colname
                constraintdef: item.expression,
                condeferrable: item.isDeffered,
                description: item.description,
              };
            }),
        };
        await updateTableConstraint(constraintParam);
      }
      if (tabName == 'IndexesTab') {
        const indexesParam = {
          ...commonParams,
          indexs: dataMap.IndexesTab.data
            .filter((item) => item[dataMap.IndexesTab.rowStatusKey])
            .map((item) => {
              return {
                type: saveType[item[rowStatusKey]],
                indexName: item.indexName,
                oldIndexName: originData.find((e) => e[idKey] == item[idKey])?.indexName,
                unique: item.isUnique,
                amname: item.accessMethod,
                attname: Array.isArray(item.columnName) ? item.columnName.join(',') : '',
                expression: item.expression,
                description: item.description,
              };
            }),
        };
        await updateTableIndex(indexesParam);
      }
      if (tabName == 'DataTab') {
        enum saveDataType {
          'add' = 'Insert',
          'delete' = 'Delete',
          'edit' = 'Update',
        }
        const param = {
          winId: page.value.winId,
          data: currentTabInfo.data
            .filter((row) => row[currentTabInfo.rowStatusKey])
            .map((row) => {
              const rowStatus = row[rowStatusKey];
              const type = saveDataType[rowStatus];
              const columnData = {};
              if (rowStatus == 'add') {
                columns.forEach((col) => {
                  columnData[col.name] = row[col.name];
                });
              }
              if (rowStatus == 'edit') {
                columns.forEach((col) => {
                  if (row[col.name + editedSuffix.value]) {
                    columnData[col.name] = row[col.name];
                  }
                });
              }
              return {
                type,
                columnData,
                rowNum: ['add'].includes(rowStatus) ? null : row[idKey] + 1,
              };
            }),
        };
        const res = (await updateTableData(param)) as unknown as EditDataResponse;
        if (res?.pkcreate) {
          loading.value.close();
          await handleSetUniqueKeyGuide();
          return;
        }
      }
      ElMessage.success(t('message.editSuccess'));
      if (needNewData) {
        await getData(tabName);
        currentTabInfo.originData = JSON.parse(JSON.stringify(currentTabInfo.data));
        currentTabInfo.edited = false;
      }
    } finally {
      loading.value.close();
    }
  };
  const handleGlobalSave = async () => {
    const tabNames = editTabs.value.filter((item) => dataMap[item].edited);
    if (tabNames.length == 0) return;
    for (const item of tabNames) {
      await handleSave(item, false);
    }
  };
  const handleCancel = (type) => {
    if (dataMap[type].edited) {
      dataMap[type].data = JSON.parse(JSON.stringify(dataMap[type].originData));
      dataMap[type].edited = false;
    }
  };
  const handleAddLine = () => {
    const { data, idKey, rowStatusKey, rowIndex } = getRowInfo();
    let addRowData: Record<string, any> = { [idKey]: Date.now(), [rowStatusKey]: 'add' };
    if (currentTabName.value == 'ColumnTab') {
      addRowData = {
        ...addRowData,
        dataType: dataTypeList.value[0],
      };
    }
    if (currentTabName.value == 'IndexesTab') {
      addRowData = {
        ...addRowData,
        isUnique: true,
      };
    }
    data.splice(rowIndex + 1, 0, addRowData);
    handleCellDataChange();
  };
  const handleRemoveLine = () => {
    const { data, rowStatusKey, currentRow, rowIndex } = getRowInfo();
    if (currentRow) {
      currentRow[rowStatusKey] == 'add'
        ? data.splice(rowIndex, 1)
        : (currentRow[rowStatusKey] = 'delete');
      handleCellDataChange();
    } else {
      ElMessage.info(t('message.selectedData'));
    }
  };

  const {
    page,
    handleCopyLine,
    handlePreviousPage,
    handleNextPage,
    handleLastPage,
    handleFirstPage,
    handlePage,
    handlePageSize,
  } = useTableDataHooks(
    {
      idKey: dataMap.DataTab.idKey,
      rowStatusKey: dataMap.DataTab.rowStatusKey,
      editingSuffix: editingSuffix.value,
      editedSuffix: editedSuffix.value,
      barStatus: dataMap.DataTab.barStatus,
    },
    getRowInfo,
    handleCellDataChange,
    getData,
  );

  onBeforeMount(() => {
    const { fileName: tableName, schema, uuid, oid, parttype, time } = route.query;
    Object.assign(commonParams, {
      tableName,
      schema,
      uuid,
      oid,
      tableType: parttype,
    });
    fetchDataTypeList();
    dataMap.DataTab.canEdit = parttype !== 'y';
    page.value.winId = time as string;
    closeTableDatas(page.value.winId);
    databaseName.value = route.query.dbname as string;
    const loadTab = new Set([currentTabName.value, 'ColumnTab']);
    loadTab.forEach((item) => getData(item));
  });
  onBeforeUnmount(() => {
    closeTableDatas(page.value.winId);
  });
</script>
<style lang="scss" scoped>
  .table-page {
    height: 100%;
    padding: 10px 20px;
    position: relative;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  .tabs {
    position: relative;
    :deep(.el-tabs__content) {
      padding: 0;
      color: #6b778c;
      font-size: 32px;
      font-weight: normal;
    }
  }
  .refresh {
    position: absolute;
    right: 18px;
    top: 15px;
    cursor: pointer;
    &:not(.disabled):hover {
      color: var(--hover-color);
    }
    &.disabled {
      cursor: not-allowed;
      opacity: 0.4;
    }
  }
  .tabs-container {
    flex: 1;
    display: flex;
    flex-basis: auto;
    overflow: auto;
  }
  .table-container {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
</style>
