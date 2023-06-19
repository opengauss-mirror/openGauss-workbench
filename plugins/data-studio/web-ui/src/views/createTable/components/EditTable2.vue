<template>
  <el-table
    :data="props.data"
    v-loading="props.loading"
    border
    highlight-current-row
    :cell-class-name="cellClassFn"
    @click="handleTableClick"
    @cell-dblclick="handleCellDbClick"
    @current-change="handleCurrentChange"
    style="width: 100%; height: 100%"
    v-click-outside.el-select__popper.el-cascader__dropdown="clickOutsideTable"
    flexible
    :row-key="idKey"
  >
    <el-table-column type="index" width="50" align="center" />
    <template v-for="(item, index) in props.columns" :key="index">
      <el-table-column
        align="center"
        header-align="center"
        v-bind="item"
        :label="item.isI18n ? t(item.label) : item.label"
      >
        <template v-if="item.slot" v-slot="scope">
          <slot :name="item.prop" v-bind="scope"></slot>
        </template>
      </el-table-column>
    </template>
  </el-table>
</template>

<script lang="ts" setup>
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
    }>(),
    {
      data: () => [],
      columns: () => [],
      loading: false,
      globalEditing: false,
      showEditColor: false,
    },
  );
  const emit = defineEmits<{
    (e: 'currentChange', data: any): void;
    (e: 'cellDataChange', data?: any): void;
    (e: 'cellDblclick', row, column, cell, event): void;
    (e: 'update:edited', value: boolean): void;
    (e: 'update:globalEditing', value: boolean): void;
  }>();
  const editingElement = ref(null);
  const editingData = reactive({
    row: {},
    column: {} as any,
  });
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
  const handleCurrentChange = (currentRow) => {
    emit('currentChange', currentRow);
  };
</script>
<style lang="scss" scoped>
  .el-table {
    :deep(.el-table__body) {
      .el-table__cell .cell {
        min-height: 2em;
      }
    }
  }
</style>
