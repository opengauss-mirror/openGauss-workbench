<template>
  <ThreeSectionTabsPage>
    <template #tabs>
      <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleTabClick">
        <el-tab-pane :label="$t('view.base')" name="Base"></el-tab-pane>
        <el-tab-pane :label="$t('view.preview')" name="Sql"></el-tab-pane>
      </el-tabs>
    </template>
    <template #center-container>
      <div v-show="currentTabName == 'Base'">
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="120px">
          <el-row :gutter="50">
            <el-col :span="12">
              <el-form-item prop="viewName" :label="$t('view.name')">
                <el-input v-model="form.viewName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="viewType" :label="$t('view.type')">
                <el-select v-model="form.viewType">
                  <el-option :label="$t('view.title')" value="VIEW" />
                  <el-option :label="$t('view.materializedView')" value="MATERIALIZED" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item prop="schema" :label="$t('view.objectMode')">
                <el-select v-model="form.schema" disabled>
                  <el-option v-for="item in schemaList" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item prop="sql" :label="$t('view.code')">
                <AceEditor ref="editorRef" :readOnly="false" width="100%" height="400px" />
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
  import { useI18n } from 'vue-i18n';
  import AceEditor from '@/components/AceEditor.vue';
  import { loadingInstance } from '@/utils';
  import { createView, createViewDdl } from '@/api/view';
  import EventBus, { EventTypeName } from '@/utils/event-bus';

  const route = useRoute();
  const router = useRouter();
  const TagsViewStore = useTagsViewStore();
  const { t } = useI18n();
  const loading = ref(null);

  const currentTabName = ref('Base');
  const ruleFormRef = ref<FormInstance>();
  const editorRef = ref();
  const editorPreRef = ref();
  const refreshParams = reactive({
    rootId: route.query.rootId as string,
    schemaContentCollectId: route.query.schemaContentCollectId as string,
  });

  const form = reactive({
    viewName: '',
    viewType: 'VIEW',
    schema: '',
    sql: '',
    connectionName: route.query.connectInfoName,
    uuid: route.query.uuid,
  });

  const rules = reactive<FormRules>({
    viewName: [{ required: true, message: t('rules.empty', [t('view.name')]), trigger: 'blur' }],
    viewType: [{ required: true, message: t('rules.empty', [t('view.type')]), trigger: 'change' }],
    schema: [
      { required: true, message: t('rules.empty', [t('view.objectMode')]), trigger: 'change' },
    ],
    sql: [{ required: true, message: t('rules.empty', [t('view.code')]), trigger: 'blur' }],
  });
  const schemaList = ref([]);

  const handleTabClick = (tab: TabsPaneContext) => {
    if (tab.paneName === 'Sql') handleSave(tab.paneName);
  };

  const handleSave = async (type: 'Base' | 'Sql') => {
    const api = {
      Base: createView,
      Sql: createViewDdl,
    };
    form.sql = editorRef.value.getValue();
    ruleFormRef.value.validate((valid) => {
      if (valid) {
        loading.value = loadingInstance();
        try {
          api[type](form).then((res) => {
            if (type === 'Base') {
              ElMessage.success(`${t('message.success')}`);
              EventBus.notify(EventTypeName.REFRESH_ASIDER, 'viewCollect', refreshParams);
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
      viewName: '',
      viewType: 'VIEW',
      sql: '',
    });
    editorRef.value.setValue('');
    editorPreRef.value.setValue('');
    ruleFormRef.value.clearValidate();
    currentTabName.value = 'Base';
  };

  const handleClose = () => {
    TagsViewStore.closeCurrentTabToLatest(router, route);
  };

  onMounted(() => {
    form.schema = route.query.schema as string;
    schemaList.value = [form.schema];
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
