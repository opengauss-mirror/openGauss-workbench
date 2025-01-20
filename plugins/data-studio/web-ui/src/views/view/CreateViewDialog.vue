<template>
  <div class="common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="props.type === 'create' ? $t('create.view') : $t('edit.view')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-tabs v-model="activeName" @tab-click="handleTabClick">
          <el-tab-pane :label="$t('view.base')" name="Base"></el-tab-pane>
          <el-tab-pane :label="$t('view.preview')" name="Sql"></el-tab-pane>
        </el-tabs>
        <div v-show="activeName == 'Base'">
          <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="120px">
            <el-form-item prop="viewName" :label="$t('view.name')">
              <el-input v-model="form.viewName" />
            </el-form-item>
            <el-form-item prop="viewType" :label="$t('view.type')">
              <el-select v-model="form.viewType" :disabled="props.type === 'edit'">
                <el-option :label="$t('view.title')" value="VIEW" />
                <el-option :label="$t('view.materializedView')" value="MATERIALIZED" />
              </el-select>
            </el-form-item>
            <el-form-item prop="schema" :label="$t('view.objectMode')">
              <el-select v-model="form.schema" :disabled="props.type === 'create'">
                <el-option v-for="item in schemaList" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item prop="sql" :label="$t('view.code')">
              <AceEditor
                ref="editorRef"
                type="form"
                :readOnly="props.type === 'edit'"
                width="100%"
                height="400px"
              />
            </el-form-item>
          </el-form>
        </div>
        <div v-show="activeName == 'Sql'">
          <AceEditor ref="editorPreRef" type="form" height="450px" />
        </div>
      </div>
      <template #footer>
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
        <el-button type="primary" @click="resetForm()">
          {{ $t('button.reset') }}
        </el-button>
        <el-button type="primary" @click="confirmForm('Base')">
          {{ $t('button.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import type { TabsPaneContext } from 'element-plus';
  import AceEditor from '@/components/AceEditor.vue';
  import { useI18n } from 'vue-i18n';
  import { getViewInfo, createView, createViewDdl, setEditView } from '@/api/view';
  import { useUserStore } from '@/store/modules/user';
  import { getSchemaList } from '@/api/metaData';

  const viewTypeMap = {
    v: 'VIEW',
    m: 'MATERIALIZED',
  };

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: 'create' | 'edit';
      nodeData: any;
    }>(),
    {
      modelValue: false,
      type: 'create',
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'success'): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });

  const { t } = useI18n();
  const ruleFormRef = ref<FormInstance>();
  const editorRef = ref();
  const editorPreRef = ref();
  const UserStore = useUserStore();
  const nodeData = computed(() => props.nodeData);
  const activeName = ref('Base');
  const platform = ref<Platform>();
  const editDefaultValue = {
    viewName: '',
    viewType: 'VIEW',
    schema: '',
    sql: '',
  };
  const form = reactive({
    viewName: '',
    viewType: 'VIEW',
    schema: '',
    sql: '',
    connectionName: '',
    webUser: UserStore.userId,
    uuid: '',
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
    activeName.value = tab.paneName as string;
    if (activeName.value === 'Sql') confirmForm(activeName.value);
  };

  const getSchemaOptionsList = async () => {
    const data = (await getSchemaList({
      uuid: form.uuid,
      connectionName: form.connectionName,
      webUser: UserStore.userId,
    })) as unknown as { name: string; oid: string }[];
    schemaList.value = data.map((item) => item.name);
  };

  const handleOpen = async () => {
    nextTick(async () => {
      form.schema = nodeData.value.schemaName;
      form.connectionName = nodeData.value.connectInfo.name;
      form.uuid = nodeData.value.uuid;
      platform.value = nodeData.value.connectInfo.type as Platform;
      if (props.type === 'create') {
        schemaList.value = [form.schema];
      } else {
        getSchemaOptionsList();
        const res = await getViewInfo({
          uuid: form.uuid,
          viewName: nodeData.value.name,
          schema: form.schema,
        });
        const obj = {
          viewName: res.name,
          viewType: viewTypeMap[res.type],
          schema: res.schema,
          sql: res.sourcecode,
        };
        Object.assign(editDefaultValue, obj);
        Object.assign(form, obj);
        editorRef.value.setValue(obj.sql);
      }
    });
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    resetForm();
    activeName.value = 'Base';
  };
  const confirmForm = async (type: 'Base' | 'Sql') => {
    const api = {
      Base: props.type === 'create' ? createView : setEditView,
      Sql: createViewDdl,
    };
    form.sql = editorRef.value.getValue();
    ruleFormRef.value.validate((valid) => {
      if (valid) {
        const params =
          props.type === 'create' || type == 'Sql'
            ? form
            : {
                uuid: form.uuid,
                viewName: editDefaultValue.viewName,
                schema: editDefaultValue.schema,
                newViewName: form.viewName,
                newSchema: form.schema,
              };
        api[type](params).then((res) => {
          if (type === 'Base') {
            ElMessage.success(`${t('message.success')}`);
            myEmit('success');
            handleClose();
          } else if (type === 'Sql') {
            editorPreRef.value.setValue(res);
          }
        });
      } else {
        activeName.value = 'Base';
      }
    });
  };
  const resetForm = () => {
    if (props.type == 'create') {
      Object.assign(form, {
        viewName: '',
        viewType: 'VIEW',
        sql: '',
      });
      editorRef.value.setValue('');
    } else {
      Object.assign(form, editDefaultValue);
      editorPreRef.value.setValue(editDefaultValue.sql);
    }
    editorPreRef.value.setValue('');
    ruleFormRef.value.clearValidate();
    activeName.value = 'Base';
  };
</script>

<style lang="scss" scoped>
  :deep(.el-select) {
    width: 100%;
  }
</style>
