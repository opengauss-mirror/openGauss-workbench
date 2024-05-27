<template>
  <ThreeSectionTabsPage>
    <template #tabs>
      <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleTabClick">
        <el-tab-pane label="DDL" name="DDL" />
        <el-tab-pane :label="$t('foreignTable.foreign.title')" name="ForeignInfoTab" />
        <el-tab-pane :label="$t('foreignTable.columns.title')" name="ColumnsTab" />
        <el-tab-pane :label="$t('foreignTable.data.title')" name="DataTab" />
      </el-tabs>
    </template>
    <template #center-container>
      <DDL v-show="currentTabName == 'DDL'" :data="dataMap.DDL.data" />
      <ForeignInfoTab
        v-show="currentTabName == 'ForeignInfoTab'"
        :data="dataMap.ForeignInfoTab.data"
      />
      <div class="table-container" v-show="currentTabName == 'ColumnsTab'">
        <EditTable
          tabName="ColumnsTab"
          :canEdit="dataMap.ColumnsTab.canEdit"
          :data="dataMap.ColumnsTab.data"
          :columnNameList="columnNameList"
          :commonParams="commonParams"
          :columns="dataMap.ColumnsTab.columns"
          :idKey="dataMap.ColumnsTab.idKey"
          :rowStatusKey="dataMap.ColumnsTab.rowStatusKey"
          :editingSuffix="editingSuffix"
          :editedSuffix="editedSuffix"
          :loading="dataMap.ColumnsTab.loading"
          v-model:edited="dataMap.ColumnsTab.edited"
          :dataTypeList="dataTypeList"
          :barStatus="dataMap.ColumnsTab.barStatus"
          :menuList="['advancedCopy']"
          @selectionChange="handleSelectionChange"
          @cellDataChange="handleCellDataChange"
        />
        <Toolbar
          :status="dataMap.ColumnsTab.barStatus"
          type="column"
          :canEdit="dataMap.ColumnsTab.canEdit"
          @save="handleSave('ColumnsTab')"
          @cancel="handleCancel('ColumnsTab')"
          @addLine="handleAddLine"
          @copyLine="handleCopyLine"
          @removeLine="handleRemoveLine"
        />
      </div>
      <div class="table-container" v-show="currentTabName == 'DataTab'">
        <div class="data-containrer">
          <EditTable
            ref="dataTabEditTable"
            tabName="DataTab"
            :canEdit="dataMap.DataTab.canEdit"
            :data="dataMap.DataTab.data"
            :columnNameList="columnNameList"
            :commonParams="commonParams"
            :columns="dataMap.DataTab.columns"
            :idKey="dataMap.DataTab.idKey"
            :rowStatusKey="dataMap.DataTab.rowStatusKey"
            :editingSuffix="editingSuffix"
            :editedSuffix="editedSuffix"
            :loading="dataMap.DataTab.loading"
            v-model:edited="dataMap.DataTab.edited"
            :dataTypeList="dataTypeList"
            :barStatus="dataMap.DataTab.barStatus"
            sortable="custom"
            :menuList="['copy', 'advancedCopy']"
            @selectionChange="handleSelectionChange"
            @cellDataChange="handleCellDataChange"
            @sortChange="handleSortChange"
          />
          <SetSortData
            v-if="hasLoadSort"
            v-show="visibleSort"
            ref="setSortRef"
            v-model="visibleSort"
            :columns="dataMap.DataTab.columns"
            @confirm="handleConfirmSort"
          />
          <SetFilterData
            v-if="hasLoadFilter"
            v-show="visibleFilter"
            v-model="visibleFilter"
            :columns="dataMap.DataTab.columns"
            @confirm="handleConfirmFilter"
          />
        </div>
        <Toolbar
          :status="dataMap.DataTab.barStatus"
          type="data"
          :canEdit="dataMap.DataTab.canEdit"
          v-model:pageNum="page.pageNum"
          v-model:pageSize="page.pageSize"
          v-model:pageTotal="page.pageTotal"
          @save="handleSave('DataTab')"
          @cancel="handleCancel('DataTab')"
          @addLine="handleAddLine"
          @copyLine="handleCopyLine"
          @removeLine="handleRemoveLine"
          @firstPage="handleFirstPage"
          @lastPage="handleLastPage"
          @previousPage="handlePreviousPage"
          @nextPage="handleNextPage"
          @changePageNum="handlePage"
          @update:pageSize="changePageSize"
        />
      </div>
      <SetUniqueKeyDialog
        v-model="showUniqueDialog"
        :commonParams="commonParams"
        :tableName="commonParams.tableName"
        :schema="commonParams.schema"
        :uuid="commonParams.uuid"
        @success="dialogUniqueKeySuccess"
      />
    </template>
  </ThreeSectionTabsPage>
</template>

<script lang="ts" setup>
  import { Ref } from 'vue';
  import { ElMessage, ElMessageBox } from 'element-plus';
  import type { TabsPaneContext } from 'element-plus';
  import { useRoute } from 'vue-router';
  import { useAppStore } from '@/store/modules/app';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import DDL from './components/DDL.vue';
  import ForeignInfoTab from './components/ForeignInfoTab.vue';
  import EditTable from './components/EditTable.vue';
  import Toolbar from './components/Toolbar.vue';
  import SetUniqueKeyDialog from './components/SetUniqueKeyDialog.vue';
  import SetSortData from './components/SetSortData.vue';
  import SetFilterData from './components/SetFilterData.vue';
  import { eventQueue } from '@/hooks/saveData';
  import { useI18n } from 'vue-i18n';
  import { useTableDataHooks } from './useTableDataHooks';
  import { getDataTypeList } from '@/api/metaData';
  import {
    getTableColumn,
    getTableData,
    updateTableColumn,
    updateTableData,
    closeTableDatas,
  } from '@/api/table';
  import {
    getForeignTableDdlApi,
    getForeignTableAttributeApi,
    updateForeignTableApi,
  } from '@/api/foreignTable';
  import { debounce, formatEditTableData, loadingInstance } from '@/utils';
  import { getColumnsTabColumns } from './columnsData';
  import type {
    EditDataResponse,
    EditTableColumn,
    DataTabColumn,
    DataTabSortColumn,
  } from './types';

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
  const editTabs = ref(['ColumnsTab', 'DataTab']);
  const dataTabEditTable = ref<Array<InstanceType<typeof EditTable>>>([null]);
  const setSortRef = ref(null);
  const showUniqueDialog = ref(false);
  const commonParams = reactive({
    tableName: '',
    schema: '',
    uuid: '',
    oid: '',
    tableType: '',
  });
  const dataTabExpansion = reactive({
    filtration: [],
    order: [] as DataTabSortColumn[],
  });
  let dataTabHideColumns = []; // Will not disappear with requesting data
  const dataTypeList = ref([]);
  const dataMap = reactive({
    DDL: {
      data: '',
      edited: false,
      hasLoad: false,
      loading: false,
    },
    ForeignInfoTab: {
      data: [],
      hasLoad: false,
      loading: false,
    },
    ColumnsTab: {
      originData: [],
      data: [],
      canEdit: true,
      columns: getColumnsTabColumns() as EditTableColumn[],
      idKey: '_id',
      rowStatusKey: '_rowStatus',
      selectionRows: [],
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
      originData: [],
      data: [],
      canEdit: true,
      columns: [],
      querySql: '',
      idKey: '',
      rowStatusKey: '',
      selectionRows: [],
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

  const visibleSort = ref(false);
  const visibleFilter = ref(false);
  const hasLoadSort = ref(false);
  const hasLoadFilter = ref(false);

  const globalIsEdit = computed(() => {
    return dataMap.ColumnsTab.edited || dataMap.DataTab.edited;
  });
  const columnNameList: Ref<string[]> = computed(() => {
    return [...new Set(dataMap.ColumnsTab.originData.map((item) => item.columnName))];
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
      if (dataMap.ForeignInfoTab.hasLoad) {
        getData('ForeignInfoTab');
      }
    },
  );

  watch(visibleSort, (value) => {
    if (value) hasLoadSort.value = true;
  });
  watch(visibleFilter, (value) => {
    if (value) hasLoadFilter.value = true;
  });

  const handleTabClick = (tab: TabsPaneContext) => {
    if (dataMap[tab.paneName]?.hasLoad) return;
    getData(tab.paneName);
  };

  const fetchDataTypeList = async () => {
    dataTypeList.value = (await getDataTypeList({
      uuid: commonParams.uuid,
    })) as unknown as string[];
  };

  const getData = async (type) => {
    const api = {
      DDL: getForeignTableDdlApi,
      ForeignInfoTab: getForeignTableAttributeApi,
      ColumnsTab: getTableColumn,
      DataTab: getTableData,
    };
    if (!Object.keys(api).includes(type)) return;
    dataMap[type].loading = true;
    let params: any = { ...commonParams };
    if (type == 'DDL' || type == 'ForeignInfoTab') {
      params = {
        uuid: commonParams.uuid,
        schema: commonParams.schema,
        foreignTable: commonParams.tableName,
      };
    }
    if (type == 'DataTab') {
      params = {
        ...params,
        pageNum: page.value.pageNum,
        pageSize: page.value.pageSize,
        winId: page.value.winId,
        expansion: {
          filtration: dataTabExpansion.filtration,
          order: dataTabExpansion.order
            .filter((item) => item.multipleOrder)
            .map((item) => `${item.name} ${item.multipleOrder}`),
        },
      };
    }
    api[type](params)
      .then((res) => {
        if (type == 'DDL') {
          dataMap.DDL.data = res.result;
        } else if (type == 'ForeignInfoTab') {
          dataMap.ForeignInfoTab.data = res.map((itemObj) => {
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
          if (type == 'ColumnsTab') {
            idKey = idSuffix.value;
            rowStatusKey = rowStatusSuffix.value;
            columns = getColumnsTabColumns();
          }
          if (type == 'DataTab') {
            idKey = (column[0] || '') + idSuffix.value;
            rowStatusKey = (column[0] || '') + rowStatusSuffix.value;
            columns = column.map((item, index) => {
              const order = dataTabExpansion.order.find((sc) => sc.name == item)?.multipleOrder;
              return {
                label: item,
                name: item,
                prop: item,
                element: 'input',
                show: !dataTabHideColumns.includes(item),
                multipleOrder: order || null,
                systemTypeNum: res.data.typeNum[index],
                systemTypeName: res.data.typeName[index],
              };
            }) as DataTabColumn[];
            Object.assign(page.value, {
              pageNum: res.pageNum || 0,
              pageSize: res.pageSize || 0,
              pageTotal: res.pageTotal || 0,
            });
            dataMap.DataTab.querySql = res.sql;
          }
          const data = formatEditTableData({ columns, data: result, idKey, rowStatusKey });
          Object.assign(dataMap[type], {
            data,
            originData: JSON.parse(JSON.stringify(data)),
            columns,
            idKey,
            rowStatusKey,
            selectionRows: [],
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

  const handleSelectionChange = (rows) => {
    dataMap[currentTabName.value].selectionRows = rows;
  };
  const handleCellDataChange = () => {
    if (!dataMap[currentTabName.value].edited) dataMap[currentTabName.value].edited = true;
  };

  const handleSortChange = async () => {
    dataTabExpansion.order = dataMap.DataTab.columns.map((col) => ({
      name: col.name,
      multipleOrder: col.multipleOrder || null,
    }));
    page.value.pageNum = 1;
    await getData('DataTab');
    setSortRef.value?.forEach((item) => {
      item.initSort();
    });
  };

  const getRowInfo = () => {
    const data = dataMap[currentTabName.value].data;
    const selectionRows = dataMap[currentTabName.value].selectionRows;
    const idKey = dataMap[currentTabName.value].idKey;
    const rowStatusKey = dataMap[currentTabName.value].rowStatusKey;
    return { data, idKey, rowStatusKey, selectionRows };
  };

  const waitSetUniqueKey = () => {
    return new Promise((resolve) => {
      confirmUniqueKeyResolve = resolve;
    });
  };

  const handleSetUniqueKeyGuide = () => {
    return new Promise<void>((resolve, reject) => {
      ElMessageBox.confirm(t('foreignTable.confirmSave'), t('common.warning'), {
        type: 'warning',
        dangerouslyUseHTMLString: true,
      })
        .then(() => {
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
      if (tabName == 'ColumnsTab') {
        const columnParam = {
          ...commonParams,
          data: currentTabInfo.data
            .filter((item) => item[currentTabInfo.rowStatusKey])
            .map((item) => {
              const rowStatus = item[rowStatusKey];
              const getValue = (row, key) => {
                if (rowStatus == 'edit')
                  return row[key + editedSuffix.value] ? row[key] : undefined;
                return row[key];
              };
              const equalOldDataRow = originData.find((e) => e[idKey] == item[idKey]);
              return {
                operationType: saveType[item[rowStatusKey]],
                newColumnName: getValue(item, 'columnName'),
                columnName: equalOldDataRow?.columnName, // old columnName
                type: item.dataType,
                oldType: equalOldDataRow?.dataType,
                isEmpty: getValue(item, 'canBeNull'),
                defaultValue: getValue(item, 'defaultValue'),
                oldDefaultValue: equalOldDataRow?.defaultValue,
                isOnly: getValue(item, 'isUnique'),
                precision: item.precisionSize,
                oldPrecision: equalOldDataRow?.precisionSize,
                scope: item.range,
                oldScope: equalOldDataRow?.range,
                comment: getValue(item, 'description'),
              };
            }),
        };
        await updateTableColumn(columnParam);
      }
      if (tabName == 'DataTab') {
        enum saveDataType {
          'add' = 'INSERT',
          'delete' = 'DELETE',
          'edit' = 'UPDATE',
        }
        const param = {
          winId: page.value.winId,
          uuid: commonParams.uuid,
          schema: commonParams.schema,
          tableName: commonParams.tableName,
          data: currentTabInfo.data
            .filter((row) => row[currentTabInfo.rowStatusKey])
            .map((row) => {
              const rowStatus = row[rowStatusKey];
              const operationType = saveDataType[rowStatus];
              const equalOldDataRow = originData.find((e) => e[idKey] == row[idKey]);
              const dataLine = [];
              columns.forEach((col) => {
                dataLine.push({
                  columnData: row[col.name],
                  oldColumnData: equalOldDataRow?.[col.name],
                  columnName: col.name,
                  typeName: col.systemTypeName,
                  typeNum: col.systemTypeNum,
                });
              });
              return {
                operationType,
                line: dataLine,
              };
            }),
        };
        const res = (await updateTableData(param)) as unknown as EditDataResponse;
        if (res?.PKCreate) {
          loading.value.close();
          await handleSetUniqueKeyGuide();
          await updateForeignTableApi(param);
          // return;
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

  function generateRandomNumber() {
    return Math.floor(Math.random() * (9999 - 1000 + 1)) + 1000;
  }

  const getTableRowIndex = (tableData, targetRow, targetIdKey) => {
    return tableData.findIndex((item) => targetRow && item[targetIdKey] == targetRow[targetIdKey]);
  };

  const handleAddLine = () => {
    const { data, idKey, rowStatusKey, selectionRows } = getRowInfo();
    const getNewRow = () => {
      let addRowData: Record<string, any> = {
        [idKey]: Date.now() * 1000 + generateRandomNumber(),
        [rowStatusKey]: 'add',
      };
      if (currentTabName.value == 'ColumnsTab') {
        addRowData = {
          ...addRowData,
          dataType: dataTypeList.value[0],
        };
      }
      return addRowData;
    };
    if (selectionRows.length) {
      selectionRows.forEach((item) => {
        const addRowData = getNewRow();
        const rowIndex = getTableRowIndex(data, item, idKey);
        data.splice(rowIndex + 1, 0, addRowData);
      });
    } else {
      const addRowData = getNewRow();
      data.splice(0, 0, addRowData);
    }
    handleCellDataChange();
  };

  const handleRemoveLine = () => {
    const { data, idKey, rowStatusKey, selectionRows } = getRowInfo();
    if (selectionRows?.length) {
      selectionRows.forEach((item) => {
        const rowIndex = getTableRowIndex(data, item, idKey);
        item[rowStatusKey] == 'add' ? data.splice(rowIndex, 1) : (item[rowStatusKey] = 'delete');
      });
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
    changePageSize,
  } = useTableDataHooks(
    {
      idKey: dataMap.DataTab.idKey,
      rowStatusKey: dataMap.DataTab.rowStatusKey,
      editingSuffix: editingSuffix.value,
      editedSuffix: editedSuffix.value,
      barStatus: dataMap.DataTab.barStatus,
    },
    getRowInfo,
    getTableRowIndex,
    handleCellDataChange,
    getData,
  );

  const handleConfirmSort = (data) => {
    dataTabExpansion.order = data.map((item) => ({
      name: item.name,
      multipleOrder: item.multipleOrder,
    }));
    dataMap.DataTab.columns.forEach((col) => {
      col.multipleOrder = data.find((item) => item.name == col.name)?.multipleOrder || null;
    });
    page.value.pageNum = 1;
    getData('DataTab');
  };
  const handleConfirmFilter = ({ hideColumns, filtration }) => {
    dataTabExpansion.filtration = filtration;
    dataTabHideColumns = hideColumns;
    page.value.pageNum = 1;
    getData('DataTab');
    nextTick(() => {
      dataTabEditTable.value.forEach((item) => item?.doLayout());
    });
  };

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
    page.value.winId = time as string;
    closeTableDatas(page.value.winId);
    getData(currentTabName.value);
  });
  onBeforeUnmount(() => {
    closeTableDatas(page.value.winId);
  });
</script>

<style lang="scss" scoped>
  .table-container {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  .data-containrer {
    display: flex;
    flex: 1;
    overflow: hidden;
  }
  .sql-exhibition {
    padding: 5px 7px;
    background: var(--el-bg-color-bar);
    min-height: 30px;
    border-top: 1px solid var(--el-border-color-light);
    white-space: nowrap;
    overflow: auto;
  }
</style>
