<template>
  <ThreeSectionTabsPage>
    <template #tabs>
      <el-tabs v-model="currentTabName" class="tabs" @tab-click="handleTabClick">
        <el-tab-pane :label="$t('synonym.base')" name="Base"></el-tab-pane>
        <el-tab-pane :label="$t('synonym.preview')" name="Sql"></el-tab-pane>
      </el-tabs>
    </template>
    <template #tabs-container>
      <div v-show="currentTabName == 'Base'">
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="120px">
          <el-row :gutter="50">
            <el-col :span="12">
              <el-form-item prop="synonymName" :label="$t('synonym.name')">
                <el-input v-model="form.synonymName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="schema" :label="$t('synonym.objectOwner')">
                <el-select
                  v-model="form.schema"
                  @change="fetchObjectNameList"
                  placement="bottom-end"
                >
                  <el-option
                    v-for="item in list.schemaList"
                    :key="item.name"
                    :label="item.name"
                    :value="item.name"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="objectType" :label="$t('synonym.objectType')">
                <el-select v-model="form.objectType" @change="fetchObjectNameList">
                  <el-option
                    v-for="item in objectTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="objectName" :label="$t('synonym.objectName')">
                <el-select v-model="form.objectName" teleported>
                  <el-option
                    v-for="item in list.objectNameList"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                prop="replace"
                :label="$t('synonym.rplaceExistingSynonyms')"
                label-width="130px"
              >
                <el-switch v-model="form.replace" />
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
  import { createSynonym, createSynonymDdl } from '@/api/synonym';
  import { getSchemaList, getObjectList } from '@/api/metaData';
  import EventBus, { EventTypeName } from '@/utils/event-bus';

  const route = useRoute();
  const router = useRouter();
  const TagsViewStore = useTagsViewStore();
  const { t } = useI18n();
  const loading = ref(null);

  const currentTabName = ref('Base');
  const ruleFormRef = ref<FormInstance>();
  const editorPreRef = ref();
  const refreshParams = reactive({
    rootId: route.query.rootId as string,
    schemaContentCollectId: route.query.schemaContentCollectId as string,
  });

  const form = reactive({
    synonymName: '',
    schema: '',
    objectType: 'ALL',
    objectName: '',
    replace: false,
    connectionName: route.query.connectInfoName,
    uuid: route.query.uuid,
  });

  const rules = reactive<FormRules>({
    synonymName: [
      { required: true, message: t('rules.empty', [t('synonym.name')]), trigger: 'blur' },
    ],
    schema: [
      { required: true, message: t('rules.empty', [t('synonym.objectOwner')]), trigger: 'change' },
    ],
    objectType: [
      { required: true, message: t('rules.empty', [t('synonym.objectType')]), trigger: 'change' },
    ],
    objectName: [
      { required: true, message: t('rules.empty', [t('synonym.objectName')]), trigger: 'change' },
    ],
  });
  const objectTypeOptions = [
    { label: 'all', value: 'ALL' },
    { label: t('database.function_process'), value: 'FUN_PRO' },
    { label: t('database.view'), value: 'VIEW' },
    { label: t('database.regular_table'), value: 'r' },
  ];
  const list = reactive({
    schemaList: [],
    objectNameList: [],
  });

  const handleTabClick = (tab: TabsPaneContext) => {
    if (tab.paneName === 'Sql') handleSave(tab.paneName);
  };

  const fetchSchemaList = async () => {
    const data = await getSchemaList({
      connectionName: form.connectionName,
      uuid: form.uuid,
    });
    list.schemaList = data as unknown as any[];
  };

  const fetchObjectNameList = async () => {
    form.objectName = '';
    if (!form.schema) return;
    if (!form.objectType) return;
    const data = await getObjectList({
      connectionName: form.connectionName,
      objectType: form.objectType,
      schema: form.schema,
      uuid: form.uuid,
    });
    list.objectNameList = data as unknown as any[];
  };

  const handleSave = async (type: 'Base' | 'Sql') => {
    const api = {
      Base: createSynonym,
      Sql: createSynonymDdl,
    };
    ruleFormRef.value.validate((valid) => {
      if (valid) {
        loading.value = loadingInstance();
        try {
          api[type](form).then((res) => {
            if (type === 'Base') {
              ElMessage.success(`${t('create.synonym')}${t('message.success')}`);
              EventBus.notify(EventTypeName.REFRESH_ASIDER, 'synonymCollect', refreshParams);
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
      synonymName: '',
      schema: '',
      objectType: 'ALL',
      objectName: '',
      replace: false,
    });
    editorPreRef.value.setValue('');
    setTimeout(() => {
      ruleFormRef.value.clearValidate();
    }, 50);
    currentTabName.value = 'Base';
  };

  const handleClose = () => {
    TagsViewStore.closeCurrentTabToLatest(router, route);
  };

  onMounted(() => {
    fetchSchemaList();
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
