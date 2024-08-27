<template>
  <ThreeSectionTabsPage>
    <template #tabs>
      <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleTabClick">
        <el-tab-pane :label="$t('sequence.base')" name="Base"></el-tab-pane>
        <el-tab-pane :label="$t('sequence.preview')" name="Sql"></el-tab-pane>
      </el-tabs>
    </template>
    <template #center-container>
      <div v-show="currentTabName == 'Base'">
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="105px">
          <el-row :gutter="50">
            <el-col :span="12">
              <el-form-item prop="sequenceName" :label="$t('sequence.name')">
                <el-input v-model="form.sequenceName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="increment" :label="$t('sequence.increment')">
                <el-input v-model.number="form.increment" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="start" :label="$t('sequence.startValue')">
                <el-input v-model.number="form.start" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="minValue" :label="$t('sequence.minValue')">
                <el-input v-model.number="form.minValue" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="maxValue" :label="$t('sequence.maxValue')">
                <el-input v-model.number="form.maxValue" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="cache" :label="$t('sequence.cacheValue')">
                <el-input v-model.number="form.cache" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="isCycle" :label="$t('sequence.isCycle')">
                <el-switch v-model="form.isCycle" />
              </el-form-item>
            </el-col>
          </el-row>
          <hr
            style="
              width: 100%;
              border: none;
              height: 1px;
              background-color: #d9dbe1;
              margin-bottom: 18px;
            "
          />
          <el-row :gutter="50">
            <el-col :span="12">
              <el-form-item prop="tableSchema" :label="$t('sequence.mode')">
                <el-select v-model="form.tableSchema" disabled>
                  <el-option label="scott" value="1" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="tableName" :label="$t('sequence.table')">
                <el-select v-model="form.tableName" @change="changTableName">
                  <el-option
                    v-for="item in list.tableList"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="tableColumn" :label="$t('sequence.column')">
                <el-select v-model="form.tableColumn">
                  <el-option
                    v-for="item in list.columnList"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <AceEditor v-show="currentTabName == 'Sql'" ref="editorPreRef" style="width: 100%" />
    </template>
    <template #page-bottom-button>
      <el-button type="primary" @click="handleSave('Base')">
        {{ $t('button.create') }}
      </el-button>
      <el-button @click="handleReset">
        {{ $t('button.reset') }}
      </el-button>
    </template>
  </ThreeSectionTabsPage>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import type { TabsPaneContext } from 'element-plus';
  import { useRoute, useRouter } from 'vue-router';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import AceEditor from '@/components/AceEditor.vue';
  import { useI18n } from 'vue-i18n';
  import { loadingInstance } from '@/utils';
  import { getColumnList, createSequence, createSequenceDdl } from '@/api/sequence';
  import { getObjectList } from '@/api/metaData';
  import EventBus, { EventTypeName } from '@/utils/event-bus';

  const route = useRoute();
  const router = useRouter();
  const TagsViewStore = useTagsViewStore();
  const { t } = useI18n();
  const loading = ref(null);

  const platform = ref(route.query.platform as Platform);
  const currentTabName = ref('Base');
  const ruleFormRef = ref<FormInstance>();
  const editorPreRef = ref();
  const refreshParams = reactive({
    rootId: route.query.rootId as string,
  });

  const form = reactive({
    sequenceName: '',
    increment: '',
    start: '',
    minValue: '',
    maxValue: '',
    cache: '',
    isCycle: false,
    tableSchema: route.query.schema || undefined,
    tableName: '',
    schema: route.query.schema || undefined,
    tableColumn: '',
    connectionName: route.query.connectInfoName,
    uuid: route.query.uuid,
  });

  const rules = reactive<FormRules>({
    sequenceName: [
      { required: true, message: t('rules.empty', [t('sequence.name')]), trigger: 'blur' },
    ],
    increment: [
      {
        pattern: /^(0|[1-9][0-9]*|-[1-9][0-9]*)$/,
        message: t('rules.integer', [t('sequence.increment')]),
        trigger: 'blur',
      },
    ],
    start: [
      {
        pattern: /^(0|[1-9][0-9]*|-[1-9][0-9]*)$/,
        message: t('rules.integer', [t('sequence.startValue')]),
        trigger: 'blur',
      },
    ],
    minValue: [
      {
        pattern: /^(0|[1-9][0-9]*|-[1-9][0-9]*)$/,
        message: t('rules.integer', [t('sequence.minValue')]),
        trigger: 'blur',
      },
    ],
    maxValue: [
      {
        pattern: /^(0|[1-9][0-9]*|-[1-9][0-9]*)$/,
        message: t('rules.integer', [t('sequence.maxValue')]),
        trigger: 'blur',
      },
    ],
    cache: [
      {
        pattern: /^(0|[1-9][0-9]*|-[1-9][0-9]*)$/,
        message: t('rules.integer', [t('sequence.cacheValue')]),
        trigger: 'blur',
      },
    ],
  });
  const list = reactive({
    tableList: [],
    columnList: [],
  });

  const handleTabClick = (tab: TabsPaneContext) => {
    if (tab.paneName === 'Sql') handleSave(tab.paneName);
  };

  const fetchTableName = async () => {
    const data = await getObjectList({
      connectionName: form.connectionName,
      objectType: 'r',
      schema: form.schema,
      uuid: form.uuid,
    });
    list.tableList = data as unknown as any[];
  };
  const fetchColumnList = async () => {
    if (!form.tableName) return;
    const data = await getColumnList({
      connectionName: form.connectionName,
      objectName: form.tableName,
      schema: form.schema,
      uuid: form.uuid,
    });
    list.columnList = data as unknown as any[];
  };
  const changTableName = () => {
    form.tableColumn = '';
    fetchColumnList();
  };
  const handleSave = async (type: 'Base' | 'Sql') => {
    const api = {
      Base: createSequence,
      Sql: createSequenceDdl,
    };
    ruleFormRef.value.validate((valid) => {
      if (valid) {
        loading.value = loadingInstance();
        try {
          api[type](form).then((res) => {
            if (type === 'Base') {
              ElMessage.success(`${t('create.sequence')}${t('message.success')}`);
              EventBus.notify(EventTypeName.REFRESH_ASIDER, 'sequenceCollect', refreshParams);
              handleClose();
            } else if (type === 'Sql') {
              editorPreRef.value.setValue(res);
            }
          });
        } finally {
          loading.value.close();
        }
      } else {
        currentTabName.value = 'Base';
      }
    });
  };
  const handleReset = () => {
    Object.assign(form, {
      sequenceName: '',
      increment: '',
      start: '',
      minValue: '',
      maxValue: '',
      cache: '',
      isCycle: false,
      tableName: '',
      tableColumn: '',
    });
    editorPreRef.value.setValue('');
    ruleFormRef.value.clearValidate();
    currentTabName.value = 'Base';
  };

  const handleClose = () => {
    TagsViewStore.closeCurrentTabToLatest(router, route);
  };

  onMounted(() => {
    fetchTableName();
  });
</script>
<style lang="scss" scoped>
  :deep(.el-select) {
    width: 100%;
  }
  :deep(.el-row) {
    width: 100%;
  }
</style>
