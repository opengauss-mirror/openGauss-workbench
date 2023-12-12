<template>
  <div class="data-asider">
    <div class="top">
      <div>{{ $t('table.filter.showColumns') + $t('common.colon') }}</div>
      <el-table class="compact-table" :data="tableColumnsData" ref="multipleShowColumnsTableRef">
        <el-table-column type="selection" width="40" />
        <el-table-column prop="name" :label="t('common.selectAll')" min-width="100" />
      </el-table>

      <div>{{ $t('table.filter.title') + $t('common.colon') }}</div>
      <el-table
        class="compact-table"
        :data="tableFilterData"
        :show-header="false"
        ref="multipleConditionTableRef"
      >
        <el-table-column min-width="100">
          <template #default="{ row }">
            <el-checkbox v-model="row.check" class="table-checkbox" />
            <div
              class="block"
              :style="{ paddingLeft: `${row.level * 25}px`, display: 'inline-block' }"
            >
              <div v-if="row.type == 'normal' && row.key" style="display: inline-block">
                <el-dropdown trigger="click">
                  <span class="text-link">
                    {{ row.key }}
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-for="item in props.columns"
                        :key="item.name"
                        @click="setFilterColumnName(row, item)"
                      >
                        {{ item.name }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-dropdown trigger="click" max-height="200px">
                  <span class="text-link">
                    {{ t(`conditionalSymbols.${row.connector}`) }}
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu role="navigation">
                      <el-dropdown-item
                        v-for="item in conditionalSymbols"
                        :key="item"
                        @click="setFilterTableConnector(row, item)"
                      >
                        {{ t(`conditionalSymbols.${item}`) }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <span
                  v-if="!['isNull', 'isNotNull', 'isEmpty', 'isNotEmpty'].includes(row.connector)"
                  class="text-link"
                  @click="handleOpenSetValueDialog(row, 'value', row.value)"
                >
                  {{ row.value }}
                </span>
                <span
                  v-if="['isBetween', 'isNotBetween'].includes(row.connector)"
                  class="text-link"
                  @click="handleOpenSetValueDialog(row, 'value2', row.value2)"
                >
                  {{ row.value2 }}
                </span>
              </div>
              <span v-if="row.type == 'leftBracket'">( </span>
              <span v-if="row.type == 'rightBracket'">) </span>
              <span
                v-if="row.logic"
                class="text-link"
                @click="row.logic = row.logic == 'and' ? 'or' : 'and'"
              >
                {{ row.logic }}
              </span>
              <el-button
                link
                type="primary"
                class="table-row-operation"
                @click="handleAddBrotherCondition(row)"
              >
                <el-icon class="operation-icon"><CirclePlusFilled /></el-icon>
              </el-button>
              <el-button
                link
                type="primary"
                class="table-row-operation"
                @click="handleAddChidrenCondition(row)"
                v-if="!(row.type == 'leftBracket' || (tableFilterData.length == 1 && !row.key))"
              >
                <span class="iconfont icon-line-parenthesesyuankuohao operation-icon"></span>
              </el-button>
              <el-button
                link
                type="primary"
                class="table-row-operation"
                @click="handleDeleteCondition(row)"
              >
                <span class="iconfont icon-shanchu operation-icon"></span>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="bottom">
      <el-button type="primary" @click="handleApply">
        {{ $t('button.apply') }}
      </el-button>
      <el-button @click="handleClose">{{ $t('button.hide') }}</el-button>
    </div>
    <el-dialog
      v-model="visibleSetValueDialog"
      class="common-dialog-wrapper"
      :width="350"
      :title="t('table.filter.pleaseEnter', { name: selectRow?.keySystemDataType })"
      align-center
      append-to-body
      :close-on-click-modal="false"
      @closed="handleCloseSetValueDialog"
    >
      <el-input v-model="dialogInputValue" />
      <template #footer>
        <el-button @click="visibleSetValueDialog = false">{{ $t('button.cancel') }}</el-button>
        <el-button type="primary" @click="handleConfirmSetValueDialog">
          {{ $t('button.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n';
  import conditionalSymbols from './conditionalSymbols.js';
  import translateSqlEngine from './translateSqlEngine.js';
  import { SetFilterTableDataRow1, SetFilterTableDataRow, DataTabColumn } from '../types';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      columns: DataTabColumn[];
    }>(),
    {
      modelValue: false,
      columns: () => [],
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'confirm', data: any): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });

  const { t } = useI18n();
  const defaultValue = '<?>';
  const multipleShowColumnsTableRef = ref();
  const multipleConditionTableRef = ref();

  const tableColumnsData = props.columns.map((item) => {
    return {
      name: item.name,
      show: true,
    };
  });

  const tableFilterData = ref<SetFilterTableDataRow[]>([]);

  const visibleSetValueDialog = ref(false);
  const dialogInputValue = ref('');
  const selectRow = ref<SetFilterTableDataRow1>(null);
  const selectField = ref(null);

  const initColumnsSelection = () => {
    tableColumnsData.forEach((row) => {
      multipleShowColumnsTableRef.value!.toggleRowSelection(row, row.show);
    });
  };
  const initFilter = () => {
    tableFilterData.value = [
      {
        id: Date.now(),
        level: 1,
        type: 'normal',
        key: '',
        keySystemTypeNum: '',
        keySystemDataType: null,
        connector: '=',
        value: defaultValue,
        value2: defaultValue,
        logic: '',
        check: false,
      },
    ];
  };

  const findFilterTableRowInfo = (row: SetFilterTableDataRow) => {
    const obj = {
      index: -1,
      level: row.level,
      logic: row.logic,
      isEnd: false,
      isBlockSqlEnd: false, // sql sentense, not is '()'
    };
    obj.index = tableFilterData.value.findIndex((item) => item.id == row.id);
    obj.isEnd = obj.index == tableFilterData.value.length - 1;
    obj.isBlockSqlEnd = !['normal', 'leftBracket'].includes(
      tableFilterData.value[obj.index + 1]?.type,
    );
    return obj;
  };

  const setFilterColumnName = (row: SetFilterTableDataRow1, column: DataTabColumn) => {
    row.key = column.name;
    row.keySystemDataType = column.systemTypeName;
    row.keySystemTypeNum = column.systemTypeNum;
  };
  const setFilterTableConnector = (row, value) => {
    row.connector = value;
  };

  const handleOpenSetValueDialog = (row, key, value) => {
    visibleSetValueDialog.value = true;
    selectRow.value = row;
    selectField.value = key;
    dialogInputValue.value = value == defaultValue ? '' : value;
  };
  const handleConfirmSetValueDialog = () => {
    selectRow.value[selectField.value] = dialogInputValue.value.trim()
      ? dialogInputValue.value
      : defaultValue;
    visibleSetValueDialog.value = false;
  };
  const handleCloseSetValueDialog = () => {
    selectRow.value = null;
    selectField.value = null;
    dialogInputValue.value = '';
  };

  const handleAddBrotherCondition = (row: SetFilterTableDataRow) => {
    const { index, level, logic, isBlockSqlEnd } = findFilterTableRowInfo(row);
    if (index > -1) {
      const isEmptyNormalRow = row.type == 'normal' && !row.key;
      logic || row.type == 'leftBracket' ? '' : (tableFilterData.value[index].logic = 'and');
      tableFilterData.value.splice(isEmptyNormalRow ? index : index + 1, isEmptyNormalRow ? 1 : 0, {
        id: Date.now(),
        level: row.type == 'leftBracket' ? level + 1 : level,
        type: 'normal',
        key: props.columns[0]?.name,
        keySystemTypeNum: props.columns[0]?.systemTypeNum,
        keySystemDataType: props.columns[0]?.systemTypeName,
        connector: '=',
        value: defaultValue,
        value2: defaultValue,
        logic: isBlockSqlEnd ? '' : 'and',
        check: true,
      });
    }
  };
  const handleAddChidrenCondition = (row: SetFilterTableDataRow) => {
    const { index, level, logic } = findFilterTableRowInfo(row);
    if (index > -1) {
      logic ? '' : (tableFilterData.value[index].logic = 'and');
      const timeStamp = Date.now();
      const arr: SetFilterTableDataRow[] = [
        {
          id: timeStamp,
          level,
          type: 'leftBracket',
          check: true,
        },
        {
          id: timeStamp + 1,
          level: level + 1,
          type: 'normal',
          key: props.columns[0]?.name,
          keySystemTypeNum: props.columns[0]?.systemTypeNum,
          keySystemDataType: props.columns[0]?.systemTypeName,
          connector: '=',
          value: defaultValue,
          value2: defaultValue,
          logic: '',
          check: true,
        },
        {
          id: timeStamp + 2,
          level,
          type: 'rightBracket',
          logic: tableFilterData.value[index + 1]?.level == level ? 'and' : '', // If the next line (excluding the last line already) still has the same level, then add 'and'
          check: true,
        },
      ];
      tableFilterData.value.splice(index + 1, 0, ...arr);
    }
  };
  const handleDeleteCondition = (row: SetFilterTableDataRow) => {
    const { index, level, isEnd } = findFilterTableRowInfo(row);
    if (row.type == 'normal') {
      if (index == 0 && isEnd) {
        initFilter();
      } else if (index > 0) {
        tableFilterData.value.splice(index, 1);
      } else {
        tableFilterData.value.splice(index, 1);
      }
    }
    if (row.type == 'leftBracket') {
      const anotherIndex = tableFilterData.value.findIndex((item, i) => {
        return i > index && item.level == level && item.type == 'rightBracket';
      });
      tableFilterData.value.splice(index, anotherIndex - index + 1);
    }
    if (row.type == 'rightBracket') {
      const anotherIndex = tableFilterData.value.findLastIndex((item, i) => {
        return i < index && item.level == level && item.type == 'leftBracket';
      });
      tableFilterData.value.splice(anotherIndex, index - anotherIndex + 1);
    }
    tableFilterData.value.forEach((item, index) => {
      const isBlockSqlEnd = !['normal', 'leftBracket'].includes(
        tableFilterData.value[index + 1]?.type,
      );
      if (isBlockSqlEnd) item.logic = '';
    });
  };

  const handleApply = () => {
    const selectionList = multipleShowColumnsTableRef.value!.getSelectionRows();
    const hideColumns = props.columns
      .filter((col) => {
        return !selectionList.find((s) => s.name === col.name);
      })
      .map((col) => col.name);
    const filterResult = translateSqlEngine(tableFilterData.value);
    myEmit('confirm', {
      hideColumns,
      filtration: filterResult,
    });
  };
  const handleClose = () => {
    visible.value = false;
  };

  onMounted(() => {
    initColumnsSelection();
    initFilter();
  });
</script>

<style lang="scss" scoped>
  .data-asider {
    width: 400px;
    height: 100%;
    padding: 0 10px;
    display: flex;
    flex-direction: column;
    border-right: 1px solid var(--el-border-color-lighter);
    .top {
      flex: 1;
      flex-basis: auto;
      overflow: auto;
    }
    .bottom {
      padding: 10px 0;
      text-align: center;
    }
  }
  .compact-table {
    font-size: 12px;
    :deep(.el-table__cell) {
      padding: 3px 0;
    }
    :deep(.cell) {
      padding: 0 8px;
    }
  }
  .text-link {
    cursor: pointer;
    font-size: 12px;
    margin-right: 5px;
    &:hover {
      text-decoration: underline;
    }
  }
  :deep(.el-dropdown) {
    line-height: inherit;
  }
  .table-row-operation {
    padding: 0;
    & + .table-row-operation {
      margin: 0px 3px;
    }
    .operation-icon {
      font-size: 14px;
    }
  }
</style>
