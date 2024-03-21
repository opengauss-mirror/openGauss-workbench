<template>
  <div class="common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="title"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="getList"
    >
      <div class="info">{{ $t('database.database') + $t('common.colon') + props.database }}</div>
      <div v-if="!['schemaDDL', 'schemaDDLData'].includes(props.type)" class="info">
        {{ $t('mode.schema') + $t('common.colon') + props.schema }}
      </div>
      <el-table
        :data="tableList"
        :height="400"
        ref="multipleTableRef"
        border
        @select="handleSelect"
        @select-all="handleSelect"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column
          property="name"
          :label="$t('common.name')"
          align="center"
          :resizable="false"
        >
          <template #header>
            <div v-if="showNameFilter" class="flex-header">
              <div style="word-break: keep-all; margin-right: 5px">
                {{ $t('common.name') }}
              </div>
              <div class="flex-header">
                <el-icon @click="hideNamefilter" class="icon-pointer">
                  <Search />
                </el-icon>
                <el-input class="border-bottom-input" v-model="nameFilterInput" clearable />
              </div>
            </div>
            <div v-else class="flex-header">
              <div style="width: 12px"></div>
              <span>{{ $t('common.name') }}</span>
              <el-icon @click="showNameFilter = true" class="icon-pointer">
                <Search />
              </el-icon>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
        <el-button type="primary" @click="handleConfirm">
          {{ $t('button.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, ElTable } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { getSchemaList, getSchemaObjects } from '@/api/metaData';
  import { exportSchemaDdl } from '@/api/schema';
  import { exportTableDdl } from '@/api/table';
  import { exportFunctionDdl } from '@/api/functionSP';
  import { exportSequenceDdl } from '@/api/sequence';
  import { exportViewDdl } from '@/api/view';
  import { loadingInstance, downLoadMyBlobType } from '@/utils';
  import { FetchNode, BatchExportType } from '@/layout/Sidebar/types';
  import { watchDebounced } from '@vueuse/core';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: BatchExportType;
      uuid: string;
      database: string;
      schema: string;
      extraParams?: Record<string, unknown>;
    }>(),
    {
      modelValue: false,
      uuid: '',
      extraParams: () => ({}),
    },
  );
  const emit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val),
  });
  const title = computed(() => {
    return {
      schemaDDL: t('export.specific.batchSchemaDdl'),
      schemaDDLData: t('export.specific.batchSchemaDdlData'),
      tableDDL: t('export.specific.batchTableDdl'),
      tableDDLData: t('export.specific.batchTableDdlData'),
      functionDDL: t('export.specific.batchFunctionDdl'),
      viewDDL: t('export.specific.batchViewDdl'),
      sequenceDDL: t('export.specific.batchSequenceDdl'),
      sequenceDDLData: t('export.specific.batchSequenceDdlData'),
    }[props.type];
  });

  const { t } = useI18n();
  let loading = ref(null);
  const list = ref([]);
  const selectionRows = ref([]);
  const multipleTableRef = ref<InstanceType<typeof ElTable>>();
  const showNameFilter = ref(false);
  const nameFilterInput = ref('');

  const tableList = computed(() => {
    return list.value.filter((item) => item.name.indexOf(nameFilterInput.value) > -1);
  });

  watchDebounced(
    nameFilterInput,
    () => {
      selectionRows.value.forEach((row) => {
        multipleTableRef.value.toggleRowSelection(row, true);
      });
    },
    { debounce: 400, maxWait: 1000 },
  );

  const hideNamefilter = () => {
    showNameFilter.value = false;
    nameFilterInput.value = '';
  };
  const getList = async () => {
    let api, params;
    if (['schemaDDL', 'schemaDDLData'].includes(props.type)) {
      api = getSchemaList;
      params = {
        uuid: props.uuid,
      };
    } else {
      api = getSchemaObjects;
      const apiType = {
        tableDDL: 'table',
        tableDDLData: 'table',
        functionDDL: 'function',
        viewDDL: 'view',
        sequenceDDL: 'sequence',
        sequenceDDLData: 'sequence',
      };
      params = {
        schema: props.schema,
        uuid: props.uuid,
        type: apiType[props.type],
      };
    }
    try {
      loading.value = loadingInstance();
      let data = (await api(params)) as unknown as FetchNode[];
      data.forEach((item) => delete item.children);
      list.value = data;
    } finally {
      loading.value.close();
    }
  };

  /* 
   * if use '@selection-change', it will be executed in this order: nameFilterInput -->  computed 'tableList' --> 'toggleRowSelection()' --> '@selection-change'. Causing sequence issues after filtering is cancelled.

   * so we only can use '@select'/ '@select-all' to toggle the selection.
  
   * or you can find the order way: nameFilterInput -->  computed 'tableList' --> 'toggleRowSelection()' --> [end]
  */
  const handleSelect = (selection) => {
    selectionRows.value = selection;
  };

  const getConfirmParams = () => {
    const type = props.type;
    const selections = selectionRows.value;
    if (['schemaDDL', 'schemaDDLData'].includes(type)) {
      return {
        api: exportSchemaDdl,
        params: {
          dataFlag: type == 'schemaDDLData',
          schemaList: selections.map((item) => item.name),
        },
      };
    } else if (['tableDDL', 'tableDDLData'].includes(type)) {
      return {
        api: exportTableDdl,
        params: {
          dataFlag: type == 'tableDDLData',
          tableList: selections.map((item) => item.name),
        },
      };
    } else if (type == 'functionDDL') {
      return {
        api: exportFunctionDdl,
        params: {
          functionMap: selections.map((item) => {
            return {
              oid: item.oid,
              name: item.name,
              isPackage: item.isPackage,
            };
          }),
        },
      };
    } else if (type == 'viewDDL') {
      return {
        api: exportViewDdl,
        params: {
          viewList: selections.map((item) => item.name),
        },
      };
    } else if (['sequenceDDL', 'sequenceDDLData'].includes(type)) {
      return {
        api: exportSequenceDdl,
        params: {
          dataFlag: type == 'sequenceDDLData',
          sequenceList: selections.map((item) => item.name),
        },
      };
    } else {
      return {};
    }
  };
  const handleConfirm = async () => {
    if (!selectionRows.value.length) {
      return ElMessage.warning(t('message.selectedData'));
    }
    const { api, params } = getConfirmParams();
    const finalParams = { ...params, uuid: props.uuid, schema: props.schema || undefined };
    loading.value = loadingInstance();
    try {
      const res = await api(finalParams);
      downLoadMyBlobType(res.name, res.data);
      visible.value = false;
    } finally {
      loading.value.close();
    }
  };
  const handleClose = () => {
    emit('update:modelValue', false);
  };
</script>

<style lang="scss" scoped>
  .info {
    padding: 5px 0;
  }
  .flex-header {
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .icon-pointer {
    cursor: pointer;
    :hover {
      color: var(--normal-color);
    }
  }
  .border-bottom-input {
    flex: 1;
    box-shadow: none;
    height: auto;
    :deep(.el-input__wrapper) {
      box-shadow: none;
      .el-input__inner {
        box-shadow: 0 1px 0 0 var(--el-input-border-color);
      }
    }
  }
</style>
