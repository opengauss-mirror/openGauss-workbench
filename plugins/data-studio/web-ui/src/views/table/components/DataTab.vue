<template>
  <el-table
    :data="props.data || []"
    v-loading="props.loading"
    border
    highlight-current-row
    :cell-class-name="cellClassFn"
    @cell-dblclick="handleCellDbClick"
    @current-change="handleCurrentChange"
    style="width: 100%; height: 100%"
  >
    <el-table-column
      v-for="(item, index) in props.columns || []"
      :key="index"
      :prop="item"
      :label="item"
      :min-width="90"
    >
      <template #default="scope">
        <el-input
          v-if="scope.row[item + editingSuffix]"
          v-model="scope.row[item]"
          @change="handleChangeInput(scope.row, scope.column)"
          @blur="handleBlurInput(scope.row, scope.column)"
        />
        <span v-else>{{ scope.row[item] }}</span>
      </template>
    </el-table-column>
  </el-table>
</template>

<script lang="ts" setup>
  const props = withDefaults(
    defineProps<{
      data: any;
      columns: any;
      idKey?: string;
      rowStatusKey?: string; // like "id_rowStatus = '' | 'add' | 'delete'"
      loading?: boolean;
    }>(),
    {
      data: [],
      loading: false,
    },
  );

  const emit = defineEmits<{
    (e: 'currentChange', data: any): void;
    (e: 'cellDataChange', data?: any): void;
  }>();

  const editingSuffix = ref('_isEditing');
  const editedSuffix = ref('_edited');

  const cellClassFn = ({ row, column }) => {
    if (props.rowStatusKey && row[props.rowStatusKey] == 'add') return 'add';
    if (props.rowStatusKey && row[props.rowStatusKey] == 'delete') return 'delete';
    if (row[column.label + editedSuffix.value]) return 'edited';
  };
  const handleCellDbClick = (row, column, cell) => {
    row[column.label + editingSuffix.value] = true;
    nextTick(() => {
      cell.querySelector('.el-input__inner')?.focus();
    });
  };
  const handleBlurInput = (row, column) => {
    row[column.label + editingSuffix.value] = false;
  };
  const handleChangeInput = (row, column) => {
    row[column.label + editedSuffix.value] = true;
    emit('cellDataChange', row);
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
