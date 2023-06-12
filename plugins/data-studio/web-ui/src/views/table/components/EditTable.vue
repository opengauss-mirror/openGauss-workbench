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
    flexible
    :row-key="idKey"
  >
    <el-table-column type="index" width="50" align="center" />
    <el-table-column
      v-for="(item, index) in props.columns || []"
      :key="index"
      :prop="item.name"
      :label="item.isI18nLabel ? t(item.label) : item.label"
      :max-width="200"
      align="center"
      header-align="center"
      :min-width="item.type == 'select' ? 120 : tableColumnWidth[item.name]"
    >
      <template #default="scope">
        <el-input
          v-if="
            props.canEdit &&
            globalEditing &&
            scope.row[item.name + props.editingSuffix] &&
            item.type == 'input'
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
            props.canEdit &&
            globalEditing &&
            scope.row[item.name + props.editingSuffix] &&
            item.type == 'select' &&
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
            props.tabName == 'ColumnTab' &&
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
            item.type == 'select'
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
        <el-cascader
          v-else-if="
            props.canEdit &&
            globalEditing &&
            scope.row[item.name + props.editingSuffix] &&
            item.type == 'cascader' &&
            item.name == 'constrainType'
          "
          v-model="scope.row[item.name]"
          :props="cascaderProps"
          :options="item.options"
          @change="handleChangeValue(scope.row, scope.column)"
        />
        <el-checkbox
          v-else-if="item.type == 'checkbox'"
          v-model="scope.row[item.name]"
          :disabled="!props.canEdit"
          @change="handleChangeValue(scope.row, scope.column)"
        />
        <span v-else>{{ formatTextCell(scope.row[item.name], item.type) }}</span>
      </template>
    </el-table-column>
  </el-table>
</template>

<script lang="ts" setup>
  import vClickOutside from '@/directives/clickOutside';
  import type { CascaderProps } from 'element-plus';
  import { findParentElement, getFlexColumnWidth } from '@/utils';
  import { getSchemaList, getSchemaObjectList } from '@/api/metaData';
  import { getTableColumn } from '@/api/table';
  import { useI18n } from 'vue-i18n';

  interface Column {
    label: string;
    name: string;
    isI18nLabel: boolean;
    type: 'input' | 'inputNumber' | 'select' | 'checkbox' | 'cascader';
    attributes?: Record<string, any>;
    options?: any[];
  }
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
      tabName: string; //'ColumnTab' |'ConstraintTab' | 'IndexesTab' | 'DataTab';
      data: any[];
      allData: any;
      columnNameList: string[];
      commonParams: CommonParams;
      columns: Column[];
      idKey?: string;
      rowStatusKey?: string; // like "id_rowStatus = '' | 'add' | 'delete' | 'edit'"
      editingSuffix: string;
      editedSuffix: string;
      edited: boolean;
      loading?: boolean;
      dataTypeList: string[];
      barStatus: any;
    }>(),
    {
      canEdit: true,
      data: () => [],
      columnNameList: () => [],
      edited: false,
      loading: false,
      dataTypeList: () => [],
      barStatus: () => ({}),
    },
  );

  const emit = defineEmits<{
    (e: 'currentChange', data: any): void;
    (e: 'cellDataChange', data?: any): void;
    (e: 'cellDblclick', row, column, cell, event): void;
    (e: 'update:edited', value: boolean): void;
  }>();

  const cascaderProps: CascaderProps = {
    lazy: true,
    lazyLoad: async (node, resolve) => {
      if (node.level == 1) {
        resolve(await fetchSchemaList());
      } else if (node.level == 2) {
        resolve(await fetchSchemaContentList(node.value));
      } else if (node.level == 3) {
        resolve(await fetchColumnList(node));
      } else {
        resolve([]);
      }
    },
  };
  const { t } = useI18n();
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
  const handleCurrentChange = (currentRow) => {
    emit('currentChange', currentRow);
  };

  const fetchSchemaList = async () => {
    const data = (await getSchemaList({
      uuid: props.commonParams.uuid,
    })) as unknown as FetchNode[];
    return data.map((item) => ({ oid: item.oid, value: item.name, label: item.name, leaf: false }));
  };
  const fetchSchemaContentList = async (schema) => {
    const data = (await getSchemaObjectList({
      schema,
      uuid: props.commonParams.uuid,
    })) as unknown as any[];
    return data[0]?.table.map((item: FetchNode) => ({
      oid: item.oid,
      value: item.name,
      label: item.name,
      leaf: false,
    }));
  };
  const fetchColumnList = async (node) => {
    const res = (await getTableColumn({
      ...props.commonParams,
      schema: node.pathValues[1],
      oid: node.data.oid,
      tableName: node.data.value,
    })) as unknown as any;
    return res.result.map((item) => ({ value: item[0], label: item[0], leaf: true }));
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
