<template>
  <AdvancedTable
    ref="editTableRef"
    :data="props.data || []"
    :loading="props.loading"
    :row-key="idKey"
    :columns="columns"
    :menuList="props.menuList"
    :cell-class-name="cellClassFn"
    @click="handleTableClick"
    @cell-dblclick="handleCellDbClick"
    @selection-change="handleSelectionChange"
    v-click-outside.el-select__popper.el-cascader__dropdown="clickOutsideTable"
    flexible
  >
    <template v-for="(item, index) in props.columns" :key="index">
      <el-table-column
        align="center"
        header-align="center"
        v-bind="filterTableColumnProps(item)"
        :label="item.isI18nLabel ? t(item.label) : item.label"
      >
        <template v-if="item.slot" v-slot="scope">
          <slot :name="item.prop" v-bind="scope"></slot>
        </template>
      </el-table-column>
    </template>
  </AdvancedTable>
</template>

<script lang="ts" setup>
  import { ElTable, ElTableColumn } from 'element-plus';
  import vClickOutside from '@/directives/clickOutside';
  import { findParentElement } from '@/utils';
  import { useI18n } from 'vue-i18n';
  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      data: any[];
      columns: any[];
      loading?: boolean;
      idKey?: string;
      rowStatusKey?: string; // such as "id_rowStatus", value in [ '', 'add', 'delete', 'edit']
      editingSuffix: string; // such as "id_isEditing"
      editedSuffix: string; // such as "id_edited"
      globalEditing: boolean;
      showEditColor?: boolean;
      menuList: string[];
    }>(),
    {
      data: () => [],
      columns: () => [],
      loading: false,
      globalEditing: false,
      showEditColor: false,
      menuList: () => [],
    },
  );
  const emit = defineEmits<{
    (e: 'selectionChange', data: any): void;
    (e: 'cellDataChange', data?: any): void;
    (e: 'cellDblclick', row, column, cell, event): void;
    (e: 'update:edited', value: boolean): void;
    (e: 'update:globalEditing', value: boolean): void;
  }>();
  const editTableRef = ref<InstanceType<typeof ElTable>>(null);
  const editingElement = ref(null);
  const editingData = reactive({
    row: {},
    column: {} as any,
  });

  const filterTableColumnProps = (targetObj) => {
    const result = {};
    for (const key in targetObj) {
      if (Object.prototype.hasOwnProperty.call(ElTableColumn.props, key)) {
        result[key] = targetObj[key];
      }
    }
    return result;
  };
  const clearSelection = () => {
    nextTick(() => {
      editTableRef.value.clearSelection();
    });
  };
  const cellClassFn = ({ row, column }) => {
    if (!props.showEditColor) return '';
    if (props.rowStatusKey && row[props.rowStatusKey] == 'add') return 'add';
    if (props.rowStatusKey && row[props.rowStatusKey] == 'delete') return 'delete';
    if (row[column.property + props.editedSuffix]) return 'edited';
  };
  const clickOutsideTable = () => {
    if (props.globalEditing) {
      emit('update:globalEditing', false);
      editingElement.value = null;
      editingData.row[editingData.column.property + props.editingSuffix] = false;
    }
  };
  const handleTableClick = (event) => {
    if (
      props.globalEditing &&
      editingElement.value &&
      !editingElement.value.contains(event.target)
    ) {
      clickOutsideTable();
    }
  };
  const handleCellDbClick = (row, column, cell, event) => {
    emit('cellDblclick', row, column, cell, event);
    editingElement.value = findParentElement(event.target, 'el-table__cell');
    emit('update:globalEditing', true);
    Object.assign(editingData, {
      row,
      column,
    });
    row[column.property + props.editingSuffix] = true;
    nextTick(() => {
      cell.querySelector('.el-input__inner')?.focus();
    });
  };
  const handleSelectionChange = (value) => {
    emit('selectionChange', value);
  };

  defineExpose({
    clearSelection,
  });
</script>
<style lang="scss" scoped>
  :deep(.el-table__body) {
    .el-table__cell .cell {
      min-height: 2em;
    }
  }
</style>
