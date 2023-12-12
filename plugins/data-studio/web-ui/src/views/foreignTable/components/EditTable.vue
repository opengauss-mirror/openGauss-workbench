<template>
  <AdvancedTable
    ref="editTableRef"
    :data="props.data || []"
    :loading="props.loading"
    :row-key="idKey"
    :columns="columns"
    :menuList="props.menuList"
    :cell-class-name="cellClassFn"
    :header-cell-class-name="handleHeaderClass"
    @click="handleTableClick"
    @cell-dblclick="handleCellDbClick"
    @selection-change="handleSelectionChange"
    @sort-change="handleSortChange"
    v-click-outside.el-select__popper.el-cascader__dropdown="clickOutsideTable"
    flexible
  >
    <template v-for="(item, index) in props.columns || []">
      <el-table-column
        :key="index"
        v-if="item.show !== false"
        :prop="item.name"
        :label="item.isI18nLabel ? t(item.label) : item.label"
        :sortable="props.sortable"
        :max-width="200"
        align="center"
        header-align="center"
        :min-width="item.element == 'select' ? 120 : tableColumnWidth[item.name]"
      >
        <template #default="scope">
          <el-input
            v-if="
              props.canEdit &&
              globalEditing &&
              scope.row[item.name + props.editingSuffix] &&
              item.element == 'input'
            "
            v-model="scope.row[item.name]"
            v-bind="item.attributes"
            @change="handleChangeValue(scope.row, scope.column)"
          />
          <el-input-number
            v-else-if="
              props.canEdit &&
              globalEditing &&
              scope.row[item.name + props.editingSuffix] &&
              item.element == 'inputNumber'
            "
            v-model="scope.row[item.name]"
            v-bind="item.attributes"
            :controls="false"
            @change="handleChangeValue(scope.row, scope.column)"
            style="width: 100%"
          />
          <el-select
            v-else-if="
              props.canEdit &&
              globalEditing &&
              scope.row[item.name + props.editingSuffix] &&
              item.element == 'select' &&
              item.name == 'columnName'
            "
            v-model="scope.row[item.name]"
            v-bind="item.attributes"
            @change="handleChangeValue(scope.row, scope.column)"
          >
            <el-option v-for="op in props.columnNameList" :key="op" :label="op" :value="op" />
          </el-select>
          <el-select
            v-else-if="
              props.canEdit &&
              globalEditing &&
              scope.row[item.name + props.editingSuffix] &&
              props.tabName == 'ColumnsTab' &&
              item.name == 'dataType'
            "
            v-model="scope.row[item.name]"
            v-bind="item.attributes"
            @change="handleChangeValue(scope.row, scope.column)"
          >
            <el-option v-for="op in props.dataTypeList" :key="op" :label="op" :value="op" />
          </el-select>
          <el-select
            v-else-if="
              props.canEdit &&
              globalEditing &&
              scope.row[item.name + props.editingSuffix] &&
              item.element == 'select'
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
            v-else-if="item.element == 'checkbox'"
            v-model="scope.row[item.name]"
            :disabled="!props.canEdit"
            @change="handleChangeValue(scope.row, scope.column)"
          />
          <span v-else>{{ formatTextCell(scope.row[item.name], item.element) }}</span>
        </template>
      </el-table-column>
    </template>
  </AdvancedTable>
</template>

<script lang="ts" setup>
  import { ElTable } from 'element-plus';
  import vClickOutside from '@/directives/clickOutside';
  import type { CascaderProps } from 'element-plus';
  import { findParentElement, getFlexColumnWidth } from '@/utils';
  import { getSchemaList, getSchemaObjectList } from '@/api/metaData';
  import { getTableColumn } from '@/api/table';
  import { useI18n } from 'vue-i18n';
  import type { EditTableColumn } from '../types';

  interface FetchNode {
    name: string;
    oid: string;
  }
  interface CommonParams {
    uuid: string;
    oid: string;
    [propsName: string]: string;
  }

  const props = withDefaults(
    defineProps<{
      canEdit?: boolean;
      tabName: string; //'ColumnsTab' |'ConstraintTab' | 'IndexesTab' | 'DataTab';
      data: any[];
      columnNameList: string[];
      commonParams: CommonParams;
      columns: EditTableColumn[];
      idKey?: string;
      rowStatusKey?: string; // like "id_rowStatus = '' | 'add' | 'delete' | 'edit'"
      editingSuffix: string;
      editedSuffix: string;
      edited: boolean;
      loading?: boolean;
      dataTypeList: string[];
      barStatus: any;
      sortable?: boolean | string;
      menuList?: string[];
    }>(),
    {
      canEdit: true,
      data: () => [],
      columnNameList: () => [],
      edited: false,
      loading: false,
      dataTypeList: () => [],
      barStatus: () => ({}),
      sortable: false,
      menuList: () => [],
    },
  );

  const emit = defineEmits<{
    (e: 'selectionChange', data: any): void;
    (e: 'cellDataChange', data?: any): void;
    (e: 'cellDblclick', row, column, cell, event): void;
    (e: 'sortChange', { column, prop, order }): void;
    (e: 'update:edited', value: boolean): void;
  }>();

  const { t } = useI18n();
  const editTableRef = ref<InstanceType<typeof ElTable>>(null);
  const globalEditing = ref(false);
  const editingElement = ref(null);
  const editingData = reactive({
    row: {},
    column: {} as any,
  });
  const edited = computed({
    get() {
      return props.edited;
    },
    set(value) {
      emit('update:edited', value);
    },
  });
  const tableColumnWidth = ref({});
  const sortOrderMap = {
    ASC: 'ascending',
    DESC: 'descending',
  };

  watch(
    edited,
    (value) => {
      Object.assign(props.barStatus, {
        save: value,
        cancel: value,
      });
    },
    {
      immediate: true,
    },
  );
  watch([globalEditing, () => props.data], ([globalEditing]) => {
    if (!globalEditing) {
      const obj = {};
      props.columns.forEach((c) => {
        obj[c.name] = getFlexColumnWidth(props.data, c.name);
      });
      tableColumnWidth.value = obj;
    }
  });

  const doLayout = () => {
    nextTick(() => {
      editTableRef.value.doLayout();
    });
  };

  const clearSelection = () => {
    nextTick(() => {
      editTableRef.value.clearSelection();
    });
  };

  const formatTextCell = (value: string[] | string, type) => {
    if (type == 'cascader' && Array.isArray(value)) {
      enum ConstrainType {
        c = 'CHECK',
        u = 'UNIQUE',
        p = 'PRIMARY KEY',
        s = 'PRIMARY KEY',
        f = 'FOREIGN KEY',
      }
      return value.length > 1
        ? `${ConstrainType[value[0]]}/${value[1]}.${value[2]}.${value[3]}`
        : ConstrainType[value[0]];
    }
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

  const handleSelectionChange = (value) => {
    emit('selectionChange', value);
  };

  const handleHeaderClass = ({ column }) => {
    if (column.sortable == 'custom') {
      column.order =
        sortOrderMap[props.columns.find((item) => item.name == column.property)?.multipleOrder] ||
        null;
    }
    return '';
  };

  const handleSortChange = ({ column, prop, order }) => {
    if (column.sortable !== 'custom') {
      return;
    }
    if (!column.multipleOrder) {
      column.multipleOrder = 'DESC';
    } else if (column.multipleOrder == 'DESC') {
      column.multipleOrder = 'ASC';
    } else {
      column.multipleOrder = null;
    }
    props.columns.forEach((item) => {
      if (item.name == prop) {
        item.multipleOrder = column.multipleOrder;
      }
    });
    emit('sortChange', { column, prop, order });
  };

  const fetchSchemaList = async () => {
    const data = (await getSchemaList({
      uuid: props.commonParams.uuid,
    })) as unknown as FetchNode[];
    return data.map((item) => ({ oid: item.oid, value: item.name, label: item.name, leaf: false }));
  };

  defineExpose({
    tableRef: toRef(() => editTableRef),
    doLayout,
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
