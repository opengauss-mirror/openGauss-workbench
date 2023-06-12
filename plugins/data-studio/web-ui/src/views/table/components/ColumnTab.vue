<template>
  <el-table
    :data="props.data || []"
    v-loading="props.loading"
    border
    highlight-current-row
    :cell-class-name="cellClassFn"
    @click="handleTableClick"
    @cell-dblclick="handleCellDbClick"
    @current-change="handleCurrentChange"
    style="width: 100%; height: 100%"
    v-click-outside.el-select__popper.el-cascader__dropdown="clickOutsideTable"
  >
    <el-table-column type="index" width="50" />
    <el-table-column
      v-for="(item, index) in columns || []"
      :key="index"
      :prop="item.name"
      :label="t(item.label)"
      :min-width="item.type == 'select' ? 120 : 90"
      align="center"
      header-align="center"
    >
      <template #default="scope">
        <el-input
          v-if="globalEditing && scope.row[item.name + props.editingSuffix] && item.type == 'input'"
          v-model="scope.row[item.name]"
          v-bind="item.attributes"
          @change="handleChangeValue(scope.row, scope.column)"
        />
        <el-input-number
          v-else-if="
            globalEditing &&
            scope.row[item.name + props.editingSuffix] &&
            item.type == 'inputNumber'
          "
          v-model="scope.row[item.name]"
          v-bind="item.attributes"
          :controls="false"
          @change="handleChangeValue(scope.row, scope.column)"
          style="width: 100%"
        />
        <el-select
          v-else-if="
            globalEditing && scope.row[item.name + props.editingSuffix] && item.name == 'columnName'
          "
          v-model="scope.row[item.name]"
          v-bind="item.attributes"
          @change="handleChangeValue(scope.row, scope.column)"
        >
          <el-option v-for="op in props.columnNameList" :key="op" :label="op" :value="op" />
        </el-select>
        <el-select
          v-else-if="
            globalEditing && scope.row[item.name + props.editingSuffix] && item.name == 'dataType'
          "
          v-model="scope.row[item.name]"
          v-bind="item.attributes"
          @change="handleChangeValue(scope.row, scope.column)"
        >
          <el-option
            v-for="op in dataTypeList"
            :key="op.value"
            :label="op.label"
            :value="op.value"
          />
        </el-select>
        <el-select
          v-else-if="
            globalEditing && scope.row[item.name + props.editingSuffix] && item.type == 'select'
          "
          v-model="scope.row[item.name]"
          v-bind="item.attributes"
          @change="handleChangeValue(scope.row, scope.column)"
        >
          <el-option
            v-for="op in item.options"
            :key="op.value"
            :label="op.label"
            :value="op.value"
          />
        </el-select>
        <el-checkbox
          v-else-if="item.type == 'checkbox'"
          v-model="scope.row[item.name]"
          @change="handleChangeValue(scope.row, scope.column)"
        />
        <span v-else>{{ formatTextCell(scope.row[item.name], item.type) }}</span>
      </template>
    </el-table-column>
  </el-table>
</template>

<script lang="ts" setup>
  import vClickOutside from '@/directives/clickOutside';
  import { findParentElement } from '@/utils';
  import { getDataTypeList } from '@/api/metaData';
  import { useI18n } from 'vue-i18n';

  interface Column {
    label: string;
    name: string;
    type: 'input' | 'inputNumber' | 'select' | 'checkbox' | 'cascader';
    attributes?: Record<string, any>;
    options?: any[];
  }
  interface CommonParams {
    uuid: string;
    oid: string;
    [propsName: string]: string;
  }

  const props = withDefaults(
    defineProps<{
      tabName: string; //'Column' |'Constraint' | 'Indexes' | 'Data';
      data: any[];
      allData: any;
      columnNameList: string[];
      commonParams: CommonParams;
      idKey?: string;
      rowStatusKey?: string; // like "id_rowStatus = '' | 'add' | 'delete' | 'edit'"
      editingSuffix: string;
      editedSuffix: string;
      loading?: boolean;
    }>(),
    {
      data: () => [],
      columnNameList: () => [],
      loading: false,
    },
  );

  const emit = defineEmits<{
    (e: 'currentChange', data: any): void;
    (e: 'cellDataChange', data?: any): void;
    (e: 'cellDblclick', row, column, cell, event): void;
  }>();

  const { t } = useI18n();
  const globalEditing = ref(false);
  const editingElement = ref(null);
  const editingData = reactive({
    row: {},
    column: {} as any,
  });
  const dataTypeList = ref([]);
  const columns = ref<Column[]>([
    {
      label: 'table.column.columnName',
      name: 'columnName',
      type: 'input',
    },
    {
      label: 'table.column.dataType',
      name: 'dataType',
      type: 'select',
      options: [],
    },
    {
      label: 'table.column.canBeNull',
      name: 'canBeNull',
      type: 'checkbox',
    },
    {
      label: 'table.column.defaultValue',
      name: 'defaultValue',
      type: 'input',
    },
    {
      label: 'table.column.isUnique',
      name: 'isUnique',
      type: 'checkbox',
    },
    {
      label: 'table.column.precisionSize',
      name: 'precisionSize',
      type: 'inputNumber',
      attributes: {
        min: 0,
        step: 0,
        precision: 0,
      },
    },
    {
      label: 'table.column.range',
      name: 'range',
      type: 'inputNumber',
      attributes: {
        min: 0,
        step: 0,
        precision: 0,
      },
    },
    {
      label: 'table.description',
      name: 'description',
      type: 'input',
      attributes: {
        maxlength: 5000,
      },
    },
  ]);
  const formatTextCell = (value: string[] | string, type) => {
    if (['select'].includes(type) && Array.isArray(value)) return value.join(',');
    return value;
  };

  const cellClassFn = ({ row, column }) => {
    if (props.rowStatusKey && row[props.rowStatusKey] == 'add') return 'add';
    if (props.rowStatusKey && row[props.rowStatusKey] == 'delete') return 'delete';
    if (row[column.property + props.editedSuffix]) return 'edited';
  };
  const clickOutsideTable = () => {
    if (globalEditing.value) {
      globalEditing.value = false;
      editingElement.value = null;
      editingData.row[editingData.column.property + props.editingSuffix] = false;
    }
  };
  const handleTableClick = (event) => {
    if (
      globalEditing.value &&
      editingElement.value &&
      !editingElement.value.contains(event.target)
    ) {
      clickOutsideTable();
    }
  };
  const handleCellDbClick = (row, column, cell, event) => {
    emit('cellDblclick', row, column, cell, event);
    editingElement.value = findParentElement(event.target, 'el-table__cell');
    globalEditing.value = true;
    Object.assign(editingData, {
      row,
      column,
    });
    row[column.property + props.editingSuffix] = true;
    nextTick(() => {
      cell.querySelector('.el-input__inner')?.focus();
    });
  };
  const handleChangeValue = (row, column) => {
    row[column.property + props.editedSuffix] = true;
    if (!row[props.rowStatusKey]) row[props.rowStatusKey] = 'edit';
    emit('cellDataChange', row);
  };
  const handleCurrentChange = (currentRow) => {
    emit('currentChange', currentRow);
  };
  const fetchDataTypeList = async () => {
    const res = (await getDataTypeList(props.commonParams.uuid)) as unknown as string[];
    dataTypeList.value = res.map((item) => ({ value: item, label: item }));
  };

  onMounted(() => {
    nextTick(fetchDataTypeList);
  });
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
