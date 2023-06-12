<template>
  <div class="table-container">
    <Toolbar
      :status="barStatus"
      @addLine="handleAddLine(data)"
      @removeLine="handleRemoveLine(data)"
    />
    <EditTable2
      :data="props.data"
      :columns="tableColumns"
      :idKey="idKey"
      :rowStatusKey="rowStatusKey"
      :editingSuffix="editingSuffix"
      :editedSuffix="editedSuffix"
      v-model:globalEditing="globalEditing"
      @currentChange="handleCurrentChange"
    >
      <template v-for="item in tableColumns" :key="item.prop" v-slot:[item.prop]="scope">
        <el-input
          v-if="globalEditing && scope.row[item.prop + editingSuffix] && item.element == 'input'"
          v-model="scope.row[item.prop]"
          v-bind="item.attribute"
          @change="handleChangeValue(scope.row, scope.column)"
        />
        <el-input-number
          v-else-if="
            globalEditing && scope.row[item.prop + editingSuffix] && item.element == 'inputNumber'
          "
          v-model="scope.row[item.prop]"
          v-bind="item.attribute"
          :controls="false"
          @change="handleChangeValue(scope.row, scope.column)"
          style="width: 100%"
        />
        <el-select
          v-else-if="
            globalEditing && scope.row[item.prop + editingSuffix] && item.prop == 'dataType'
          "
          v-model="scope.row[item.prop]"
          v-bind="item.attribute"
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
            globalEditing && scope.row[item.prop + editingSuffix] && item.prop == 'columnName'
          "
          v-model="scope.row[item.prop]"
          v-bind="item.attribute"
          @change="handleChangeValue(scope.row, scope.column)"
        >
          <el-option
            v-for="op in props.columns"
            :key="op.value"
            :label="op.label"
            :value="op.value"
          />
        </el-select>
        <el-cascader
          v-else-if="
            globalEditing && scope.row[item.prop + editingSuffix] && item.element == 'cascader'
          "
          v-model="scope.row[item.prop]"
          :props="cascaderProps"
          :options="item.options"
          @change="handleChangeValue(scope.row, scope.column)"
        />
        <el-checkbox
          v-else-if="item.element == 'checkbox'"
          v-model="scope.row[item.prop]"
          @change="handleChangeValue(scope.row, scope.column)"
        />
        <span v-else>{{ formatTextCell(scope.row[item.prop], item.element) }}</span>
      </template>
    </EditTable2>
  </div>
</template>

<script lang="ts" setup>
  import type { CascaderProps } from 'element-plus';
  import EditTable2 from './EditTable2.vue';
  import Toolbar from './Toolbar.vue';
  import { getSchemaList, getSchemaObjectList } from '@/api/metaData';
  import { getTableColumn } from '@/api/table';
  import { useEditTabHooks } from './useEditTabHooks';
  import type { CommonParams, TableColumn, AvailColumnList } from '../types';
  interface FetchNode {
    name: string;
    oid: string;
  }
  const props = withDefaults(
    defineProps<{
      data: any[];
      commonParams: CommonParams;
      columns: AvailColumnList[];
    }>(),
    {
      data: () => [],
      columns: () => [],
    },
  );

  const emit = defineEmits<{
    (e: 'update:data', value: any[]): void;
  }>();

  const idKey = ref('_id');
  const rowStatusKey = ref('_rowStatus');
  const editingSuffix = ref('_isEditing');
  const editedSuffix = ref('_edited');
  const globalEditing = ref(false);
  const barStatus = reactive({
    addLine: true,
    removeLine: true,
  });

  const dataTypeList = ref([]);
  const data = computed({
    get: () => props.data,
    set: (val) => emit('update:data', val),
  });
  const tableColumns = ref<TableColumn[]>([
    {
      label: 'table.constraint.constrainName',
      prop: 'constrainName',
      slot: true,
      isI18n: true,
      element: 'input',
    },
    {
      label: 'table.constraint.columnName',
      prop: 'columnName',
      slot: true,
      isI18n: true,
      element: 'select',
      attribute: {
        multiple: true,
        collapseTags: true,
      },
      options: [],
    },
    {
      label: 'table.constraint.constrainType',
      prop: 'constrainType',
      slot: true,
      isI18n: true,
      element: 'cascader',
      options: [
        {
          value: 'c',
          label: 'CHECK',
          disabled: false,
          leaf: true,
        },
        {
          value: 'u',
          label: 'UNIQUE',
          disabled: false,
          leaf: true,
        },
        {
          value: 'p',
          label: 'PRIMARY KEY',
          disabled: false,
          leaf: true,
        },
        {
          value: 's',
          label: 'PARTIAL CLUSTER KEY',
          disabled: false,
          leaf: true,
        },
        {
          value: 'f',
          label: 'FOREIGN KEY',
          disabled: false,
          leaf: false,
        },
      ],
    },
    {
      label: 'table.constraint.expression',
      prop: 'expression',
      slot: true,
      isI18n: true,
      element: 'input',
    },
    {
      label: 'table.constraint.isDeffered',
      prop: 'isDeffered',
      slot: true,
      isI18n: true,
      element: 'checkbox',
    },
    {
      label: 'table.description',
      prop: 'description',
      slot: true,
      isI18n: true,
      element: 'input',
      attribute: {
        maxlength: 5000,
      },
    },
  ]);
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
  const { handleChangeValue, handleCurrentChange, handleAddLine, handleRemoveLine } =
    useEditTabHooks({
      idKey: idKey.value,
      rowStatusKey: rowStatusKey.value,
      editingSuffix: editingSuffix.value,
      editedSuffix: editedSuffix.value,
    });
</script>

<style lang="scss" scoped>
  .table-container {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
</style>
