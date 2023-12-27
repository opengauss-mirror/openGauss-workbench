<template>
  <!-- This is a multiple choice table, not a single choice table -->
  <div class="advanced-table" :style="props.style">
    <el-table
      ref="tableRef"
      v-bind="$attrs"
      border
      :data="props.data"
      :row-key="props.rowKey"
      :row-class-name="rowClass"
      v-loading="props.loading"
      @cell-click="cellClick"
      @selection-change="selectionChange"
      @row-contextmenu="handleRowContextMenu"
    >
      <el-table-column type="index" width="60" align="center" class-name="el-table-row-index" />
      <slot></slot>
    </el-table>
    <ContextMenu
      v-if="showContextMenu"
      v-model:show="contextMenuVisible"
      :offset="contextMenuOffset"
      :list="finalMenuList"
    >
    </ContextMenu>
  </div>
</template>
<script lang="ts" name="AdvancedTable">
  export default {
    inheritAttrs: false,
  };
</script>
<script lang="ts" setup>
  import { ElMessage, ElTable } from 'element-plus';
  import ContextMenu from '../ContextMenu/index.vue';
  import { useI18n } from 'vue-i18n';
  import { copyToClickBoard } from '@/utils';
  import { AdvancedTableColumn } from './types';

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      data: any[];
      rowKey?: string;
      columns: AdvancedTableColumn[];
      loading?: boolean;
      showContextMenu?: boolean;
      menuList?: Menu[];
      style?: any;
      elTableStyle?: any;
    }>(),
    {
      data: () => [],
      rowKey: '_id',
      columns: () => [],
      loading: false,
      showContextMenu: true,
      menuList: () => [],
    },
  );

  const tableRef = ref<InstanceType<typeof ElTable>>();
  type ScrollToOptions = Parameters<typeof tableRef.value.scrollTo>[0];

  const contextMenuVisible = ref(false);
  const contextMenuOffset = ref({
    left: 0,
    top: 0,
  });

  const hideContextMenu = () => {
    contextMenuVisible.value = false;
  };

  const clearSelection = () => tableRef.value!.clearSelection();
  const getSelectionRows = () => {
    const selectionRows = tableRef.value!.getSelectionRows();
    return props.data.filter(
      (row) => selectionRows.findIndex((r) => r[props.rowKey] === row[props.rowKey]) > -1,
    );
  };
  const toggleRowSelection = (row: any, selected: boolean) =>
    tableRef.value!.toggleRowSelection(row, selected);
  const toggleAllSelection = () => tableRef.value!.toggleAllSelection();
  const toggleRowExpansion = (row: any, expanded?: boolean | undefined) =>
    tableRef.value!.toggleRowExpansion(row, expanded);
  const setCurrentRow = (row: any) => tableRef.value!.setCurrentRow(row);
  const clearSort = () => tableRef.value!.clearSort();
  const clearFilter = (columnKeys?: string[]) => tableRef.value!.clearFilter(columnKeys);
  const doLayout = () => tableRef.value!.doLayout();
  const sort = (prop: string, order: string) => tableRef.value!.sort(prop, order);
  const scrollTo = (options: ScrollToOptions | number, yCoord?: number) =>
    tableRef.value!.scrollTo(options, yCoord);
  const setScrollTop = (top?: number) => tableRef.value!.setScrollTop(top);
  const setScrollLeft = (left?: number) => tableRef.value!.setScrollLeft(left);

  const multipleSelection = ref([]);
  const rowClass = ({ row }) => {
    if (multipleSelection.value.some((item) => item[props.rowKey] === row[props.rowKey])) {
      return 'el-table-selection-row';
    }
    return '';
  };
  const cellClick = (row, column) => {
    if (column.type === 'index' && props.showContextMenu) {
      tableRef.value!.toggleRowSelection(row, undefined);
    }
  };
  const selectionChange = (value) => {
    multipleSelection.value = value;
  };

  const handleRowContextMenu = (row, column, event) => {
    if (props.showContextMenu) {
      const isIn =
        multipleSelection.value.some((item) => item[props.rowKey] == row[props.rowKey]) &&
        column.type === 'index';
      event.preventDefault();
      if (isIn) {
        contextMenuVisible.value = true;
        contextMenuOffset.value = {
          left: event.pageX,
          top: event.pageY,
        };
      }
    }
  };

  const handleCopy = (hasHeader?: boolean) => {
    const data = getSelectionRows();
    const columns = props.columns;
    let headerStr = '';
    if (hasHeader) {
      headerStr +=
        columns.reduce((preStr, curCol, curColIndex) => {
          const label = curCol.isI18nLabel ? t(curCol.label) : curCol.label;
          const currentColStr = curColIndex === 0 ? label : `\t${label}`;
          return preStr + currentColStr;
        }, '') + '\n';
    }
    const tableStr = data.reduce((prev, curRow, curRowIndex) => {
      const rowStr = columns.reduce((preStr, curCol, curColIndex) => {
        let cellStr = curRow[curCol.prop] ?? '';
        if (typeof cellStr == 'string') {
          cellStr = cellStr.replaceAll('\n', '\\n');
          cellStr = cellStr.replaceAll('\r', '\\r');
          cellStr = cellStr.replaceAll('\t', '\\t');
        }
        const currentColStr = curColIndex === 0 ? cellStr : `\t${cellStr}`;
        return preStr + currentColStr;
      }, '');
      const finalRowStr = curRowIndex === 0 ? rowStr : `\n${rowStr}`;
      return prev + finalRowStr;
    }, '');
    const str = headerStr + tableStr;
    copyToClickBoard(str);
    ElMessage.success(t('message.copySuccess'));
  };

  const menuMap = reactive({
    copy: {
      label: computed(() => t('button.copy')),
      click: () => handleCopy(),
    },
    advancedCopy: {
      label: computed(() => t('button.advancedCopy')),
      click: () => handleCopy(true),
    },
  });

  type Menu = keyof typeof menuMap;

  const finalMenuList = computed(() => {
    return props.showContextMenu && props.menuList.length === 0
      ? Object.values(menuMap)
      : props.menuList.map((item) => menuMap[item]);
  });

  onMounted(() => {
    tableRef.value!.scrollBarRef.wrapRef.addEventListener('scroll', hideContextMenu);
  });
  onBeforeUnmount(() => {
    tableRef.value!.scrollBarRef.wrapRef.removeEventListener('scroll', hideContextMenu);
  });

  defineExpose({
    clearSelection,
    getSelectionRows,
    toggleRowSelection,
    toggleAllSelection,
    toggleRowExpansion,
    setCurrentRow,
    clearSort,
    clearFilter,
    doLayout,
    sort,
    scrollTo,
    setScrollTop,
    setScrollLeft,
  });
</script>

<style scoped lang="scss">
  :deep(.el-table-row-index) {
    cursor: pointer;
  }
  .advanced-table {
    width: 100%;
    height: 100%;
    overflow: hidden;
    :deep(.el-table) {
      height: 100%;
    }
  }
</style>
