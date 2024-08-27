<template>
  <el-form
    :model="form"
    class="rule-form"
    ref="ruleFormRef"
    label-width="auto"
    label-position="left"
    :label-suffix="$t('common.colon')"
  >
    <el-row :gutter="35">
      <el-col :span="8">
        <el-form-item :label="$t('common.type')">
          <el-select
            v-model="form.type"
            :disabled="selectedObjectList.length > 0"
            @change="handleChangeType"
          >
            <el-option
              v-for="item in typeList"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.type != 'schema'" :label="$t('mode.schema')">
          <el-select v-model="form.schema" @change="handleChangeSchema">
            <el-option
              v-for="item in schemaList"
              :key="item.name"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          :label-width="0"
          :style="{ height: `calc(100% - ${form.type == 'schema' ? 55 : 105}px)`, marginBottom: 0 }"
        >
          {{ $t('privilege.objects') }}
          <el-icon class="refresh" @click="handleRefresh('objects')"><Refresh /></el-icon>
          {{ $t('common.colon') }}
          <div class="table-wrapper">
            <el-table
              :data="showPrepareObjectList"
              ref="multipleTableObjectRef"
              border
              height="100%"
              @select="handleSelectObject"
              @select-all="handleSelectObject"
            >
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column
                property="name"
                :label="$t('common.name')"
                align="center"
                :resizable="false"
              >
                <template #header>
                  <FilterTableDataHeaderSlot
                    v-model="prepareFilterInput"
                    v-model:show="showPrepareFilter"
                    ref="prepareFilterRef"
                  >
                    {{ $t('common.name') }}
                  </FilterTableDataHeaderSlot>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item
          :label-width="0"
          class="alone-form-item"
          style="height: calc(100% - 3px); margin-bottom: 0"
        >
          {{ $t('privilege.selectedObjects') + $t('common.colon') }}
          <div class="table-wrapper">
            <el-table
              :data="showSelectedObjectList"
              ref="multipleTableSelectedRef"
              height="100%"
              border
              highlight-current-row
              @current-change="(val) => (table2Selected = val)"
            >
              <el-table-column
                property="showName"
                :label="$t('common.name')"
                align="center"
                :resizable="false"
              >
                <template #header>
                  <FilterTableDataHeaderSlot
                    v-model="selectedFilterInput"
                    v-model:show="showSelectedFilter"
                    ref="selectedFilterRef"
                  >
                    {{ $t('common.name') }}
                  </FilterTableDataHeaderSlot>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item :label-width="0" style="height: calc(100% - 5px); margin-bottom: 0">
          {{ $t('privilege.authorizedUsersRoles')}}
          <el-icon class="refresh" @click="handleRefresh('user')"><Refresh /></el-icon>
          {{ $t('common.colon') }}
          <div class="table-wrapper">
            <el-table
              :data="showUserRoleList"
              ref="multipleTableUserRef"
              height="100%"
              border
              @select="handleSelectUser"
              @select-all="handleSelectUser"
            >
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column
                property="name"
                :label="$t('common.name')"
                align="center"
                :resizable="false"
              >
                <template #header>
                  <FilterTableDataHeaderSlot
                    v-model="userRoleFilterInput"
                    v-model:show="showUserRoleFilter"
                    ref="userRoleFilterRef"
                  >
                    {{ $t('common.name') }}
                  </FilterTableDataHeaderSlot>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-col>
      <span class="to">TO</span>
      <div class="select-function">
        <el-button text @click="toRight">
          <el-icon><ArrowRightBold /></el-icon>
        </el-button>
        <el-button text @click="toLeft">
          <el-icon><ArrowLeftBold /></el-icon>
        </el-button>
        <el-button text @click="toAllRight">
          <el-icon><DArrowLeft /></el-icon>
        </el-button>
      </div>
    </el-row>
  </el-form>
</template>
<script lang="ts" setup>
  import { ElMessage, ElTable, FormInstance } from 'element-plus';
  import FilterTableDataHeaderSlot from '@/components/FilterTableDataHeaderSlot.vue';
  import { useI18n } from 'vue-i18n';
  import { watchDebounced } from '@vueuse/core';
  import { getSchemaList, getSchemaObjects } from '@/api/metaData';
  import { getUserRoleList } from '@/api/userRole';
  import { loadingInstance } from '@/utils';
  import { FetchNode } from '@/layout/Sidebar/types';
  import { SelectedObjectList } from '../types';
  const props = withDefaults(
    defineProps<{
      data: any;
      uuid: string;
      dbname: string;
      selectedObjectList: SelectedObjectList;
      selectedUserRoleRows: Array<{ name: string; oid?: string }>;
    }>(),
    {
      data: {},
    },
  );
  const emit = defineEmits<{
    (event: 'update:data', data: any): void;
    (event: 'update:selectedObjectList', data: any): void;
    (event: 'update:selectedUserRoleRows', data: any): void;
  }>();
  const { t } = useI18n();
  const loading = ref(null);
  const ruleFormRef = ref<FormInstance>();
  const typeList = computed(() => [
    { name: t('mode.schema'), value: 'schema' },
    { name: t('database.regular_table'), value: 'table' },
    { name: t('database.function_process'), value: 'function' },
    { name: t('database.sequence'), value: 'sequence' },
    { name: t('database.view'), value: 'view' },
  ]);
  const schemaList = ref([]);
  const prepareObjectList = ref([]);
  const selectedObjectList = computed({
    get: () => props.selectedObjectList,
    set: (value) => emit('update:selectedObjectList', value),
  });
  const userRoleList = ref([]);

  const multipleTableObjectRef = ref<InstanceType<typeof ElTable>>();
  const multipleTableSelectedRef = ref<InstanceType<typeof ElTable>>();
  const multipleTableUserRef = ref<InstanceType<typeof ElTable>>();
  const prepareFilterInput = ref('');
  const selectedFilterInput = ref('');
  const userRoleFilterInput = ref('');
  const prepareFilterRef = ref<InstanceType<typeof FilterTableDataHeaderSlot>>(null);
  const selectedFilterRef = ref<InstanceType<typeof FilterTableDataHeaderSlot>>(null);
  const userRoleFilterRef = ref<InstanceType<typeof FilterTableDataHeaderSlot>>(null);
  const showPrepareFilter = ref(false);
  const showSelectedFilter = ref(false);
  const showUserRoleFilter = ref(false);
  const table2Selected = ref();

  const showPrepareObjectList = computed(() => {
    return prepareObjectList.value.filter(
      (item) => item.name.indexOf(prepareFilterInput.value) > -1,
    );
  });
  const showSelectedObjectList = computed(() => {
    return selectedObjectList.value.filter(
      (item) => item.showName.indexOf(selectedFilterInput.value) > -1,
    );
  });
  const showUserRoleList = computed(() => {
    return userRoleList.value.filter((item) => item.name.indexOf(userRoleFilterInput.value) > -1);
  });

  const selectionObjectRows = ref([]);
  const selectionUserRoleRows = computed({
    get: () => props.selectedUserRoleRows,
    set: (value) => emit('update:selectedUserRoleRows', value),
  });

  const form = computed({
    get() {
      return props.data;
    },
    set(val) {
      emit('update:data', val);
    },
  });

  const fetchSchemaList = async (isExcutePublic = false) => {
    let data = [];
    try {
      loading.value = loadingInstance();
      data = (await getSchemaList({ uuid: props.uuid })) as unknown as FetchNode[];
      data.forEach((item) => delete item.children);
      isExcutePublic && (data = data.filter((item) => item.name != 'public'));
      return data;
    } finally {
      loading.value.close();
    }
  };
  const fetchPrepareObjectList = async () => {
    const type = form.value.type;
    let api, params;
    if (type == 'schema') {
      prepareObjectList.value = await fetchSchemaList(false);
    } else {
      api = getSchemaObjects;
      params = {
        schema: form.value.schema,
        uuid: props.uuid,
        type,
      };
      try {
        loading.value = loadingInstance();
        let data = (await api(params)) as unknown as FetchNode[];
        data.forEach((item) => delete item.children);
        data = data.filter((item) => item.name != 'public');
        prepareObjectList.value = data;
      } finally {
        loading.value.close();
      }
    }
  };
  const fetchUserRoleList = async () => {
    const res = await getUserRoleList({ uuid: props.uuid });
    userRoleList.value = [].concat(res.user, res.role);
  };

  const formRef = () => {
    return ruleFormRef.value;
  };

  watchDebounced(
    prepareFilterInput,
    () => {
      selectionObjectRows.value.forEach((row) => {
        multipleTableObjectRef.value.toggleRowSelection(row, true);
      });
    },
    { debounce: 400, maxWait: 1000 },
  );
  watchDebounced(
    userRoleFilterInput,
    () => {
      selectionUserRoleRows.value.forEach((row) => {
        multipleTableUserRef.value.toggleRowSelection(row, true);
      });
    },
    { debounce: 400, maxWait: 1000 },
  );

  const handleChangeType = async (value) => {
    resetFilter();
    form.value.schema = '';
    prepareObjectList.value = [];
    if (value == 'schema') {
      fetchPrepareObjectList();
    } else {
      schemaList.value.length == 0 && (schemaList.value = await fetchSchemaList(true));
    }
  };
  const handleChangeSchema = () => {
    resetFilter();
    fetchPrepareObjectList();
  };

  const handleSelectObject = (selection) => {
    selectionObjectRows.value = selection;
  };
  const handleSelectUser = (selection) => {
    selectionUserRoleRows.value = selection;
  };

  const toRight = () => {
    const selections = multipleTableObjectRef.value.getSelectionRows()?.map((item) => {
      return {
        ...item,
        schema: form.value.schema,
        showName:
          form.value.type == 'schema' ? `${item.name}` : `${form.value.schema}.${item.name}`,
      };
    });
    const diff = selections.filter(
      (x) => !selectedObjectList.value.find((y) => y.showName == x.showName),
    );
    selectedObjectList.value.push(...diff);
  };
  const toLeft = () => {
    const selectIndex = selectedObjectList.value.indexOf(table2Selected.value);
    if (selectIndex > -1) {
      selectedObjectList.value.splice(selectedObjectList.value.indexOf(table2Selected.value), 1);
      const leftRow = prepareObjectList.value.find(
        (item) => item.name == table2Selected.value.name && item.oid == table2Selected.value.oid,
      );
      if (leftRow) {
        multipleTableObjectRef.value.toggleRowSelection(leftRow, false);
      }
      const selectedRowIndex = selectionObjectRows.value.findIndex(
        (item) => item.name == table2Selected.value.name && item.oid == table2Selected.value.oid,
      );
      if (selectedRowIndex > -1) selectionObjectRows.value.splice(selectedRowIndex, 1);
    }
    multipleTableSelectedRef.value.clearSelection();
  };
  const toAllRight = () => {
    selectedObjectList.value = [];
  };

  const validateForm = () => {
    return new Promise<void>((resolve, reject) => {
      ruleFormRef.value.validate((valid) => {
        if (valid) {
          resolve();
        } else {
          reject('GeneralTab');
        }
      });
    });
  };

  const resetFilter = () => {
    prepareFilterInput.value = '';
    selectedFilterInput.value = '';
    userRoleFilterInput.value = '';
    showPrepareFilter.value = false;
    showSelectedFilter.value = false;
    showUserRoleFilter.value = false;
  };
  const resetFields = () => {
    resetFilter();
    if (form.value.type != 'schema') {
      form.value.type = 'schema';
      fetchPrepareObjectList();
    }
    form.value.schema = '';
    multipleTableObjectRef.value.clearSelection();
    selectedObjectList.value = [];
    table2Selected.value = null;
    multipleTableUserRef.value.clearSelection();
    selectionUserRoleRows.value = [];
  };

  const handleRefresh = async (type: 'objects' | 'user') => {
    try {
      loading.value = loadingInstance();
      if (type == 'objects') await fetchPrepareObjectList();
      if (type == 'user') await fetchUserRoleList();
      ElMessage.success(t('message.refreshSuccess'));
    } catch (error) {
      ElMessage.error(t('message.refreshFail'));
    } finally {
      loading.value.close();
    }
  };

  const setBackfill = async ({ type, obj, user }) => {
    resetFilter();
    multipleTableObjectRef.value.clearSelection();
    selectedObjectList.value = [];
    table2Selected.value = null;
    multipleTableUserRef.value.clearSelection();
    selectionUserRoleRows.value = [];

    form.value.type = type;
    if (type == 'schema') {
      selectedObjectList.value = obj.map((item) => {
        return {
          showName: item.name,
          name: item.name,
        };
      });
    } else {
      schemaList.value = await fetchSchemaList(true);
      selectedObjectList.value = obj.map((item) => {
        return {
          showName: `${item.schema}.${item.name}`,
          name: item.name,
          schema: item.schema,
        };
      });
    }
    user?.forEach((item) => {
      const row = userRoleList.value.find((u) => u.name == item);
      multipleTableUserRef.value.toggleRowSelection(row, true);
      selectionUserRoleRows.value.push(row);
    });
  };

  onMounted(() => {
    fetchPrepareObjectList();
    fetchUserRoleList();
  });
  defineExpose({
    formRef,
    validateForm,
    resetFields,
    getSelectedObjectList: () => selectedObjectList.value,
    getUserRoleList: () => selectionUserRoleRows.value,
    setBackfill,
  });
</script>

<style lang="scss" scoped>
  .rule-form,
  :deep(.el-row),
  :deep(.el-col) {
    height: 100%;
    overflow: hidden;
  }
  .alone-form-item {
    :deep(.el-form-item__content) {
      align-items: flex-start;
    }
  }
  :deep(.el-select) {
    width: 100%;
  }
  .table-wrapper {
    width: 100%;
    height: calc(100% - 40px);
  }
  .to {
    position: absolute;
    top: 50%;
    right: calc(33.33% - 8px);
    transform: translateY(-50%);
  }
  .select-function {
    position: absolute;
    top: 50%;
    left: calc(33.33% - 13px);
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    :deep(.el-button) {
      margin: 0;
    }
  }
  .refresh {
    cursor: pointer;
    &:hover {
      color: var(--hover-color);
    }
  }
</style>
