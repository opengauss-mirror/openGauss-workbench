<template>
  <div class="synonym-dialog">
    <el-dialog
      v-model="visible"
      :title="$t('create.synonym')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-tabs v-model="activeName" @tab-click="handleTabClick">
          <el-tab-pane :label="$t('synonym.base')" name="Base"></el-tab-pane>
          <el-tab-pane :label="$t('synonym.preview')" name="Sql"></el-tab-pane>
        </el-tabs>
        <div v-show="activeName == 'Base'">
          <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="110px">
            <el-form-item prop="synonymName" :label="$t('synonym.name')">
              <el-input v-model="form.synonymName" />
            </el-form-item>
            <el-form-item prop="schema" :label="$t('synonym.objectOwner')">
              <el-select v-model="form.schema" teleported @change="fetchObjectNameList">
                <el-option
                  v-for="item in list.schemaList"
                  :key="item.name"
                  :label="item.name"
                  :value="item.name"
                />
              </el-select>
            </el-form-item>
            <el-form-item prop="objectType" :label="$t('synonym.objectType')">
              <el-select v-model="form.objectType" teleported @change="fetchObjectNameList">
                <el-option
                  v-for="item in objectTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
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
          </el-form>
          <el-form-item prop="replace" :label="$t('synonym.rplaceExistingSynonyms')">
            <el-switch v-model="form.replace" />
          </el-form-item>
        </div>
        <div v-show="activeName == 'Sql'">
          <AceEditor
            ref="editorPreRef"
            height="450px"
            style="margin: 4px 0; border: 1px solid #ddd"
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
          <el-button type="primary" @click="resetForm">
            {{ $t('button.reset') }}
          </el-button>
          <el-button type="primary" @click="confirmForm('Base')">
            {{ $t('button.confirm') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import type { TabsPaneContext } from 'element-plus';
  import AceEditor from '@/components/AceEditor.vue';
  import { useI18n } from 'vue-i18n';
  import { useUserStore } from '@/store/modules/user';
  import { createSynonym, createSynonymDdl } from '@/api/synonym';
  import { getSchemaList, getObjectList } from '@/api/metaData';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: string;
      connectData: any;
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
  const UserStore = useUserStore();
  const editorPreRef = ref();
  const activeName: Ref<string | number> = ref('Base');
  const connectData = computed(() => props.connectData);
  const form = reactive({
    synonymName: '',
    schema: '',
    objectType: 'ALL',
    objectName: '',
    replace: false,
    connectionName: '',
    webUser: UserStore.userId,
    uuid: '',
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

  const fetchSchemaList = async () => {
    const data = await getSchemaList({
      connectionName: form.connectionName,
      webUser: form.webUser,
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
      webUser: form.webUser,
      uuid: form.uuid,
    });
    list.objectNameList = data as unknown as any[];
  };
  const handleTabClick = (tab: TabsPaneContext) => {
    activeName.value = tab.paneName;
    if (activeName.value === 'Sql') confirmForm(activeName.value);
  };
  const handleOpen = async () => {
    form.connectionName = connectData.value.connectInfo.name;
    form.uuid = connectData.value.uuid;
    fetchSchemaList();
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    resetForm();
    activeName.value = 'Base';
  };
  const confirmForm = async (type) => {
    const api = {
      Base: createSynonym,
      Sql: createSynonymDdl,
    };
    ruleFormRef.value.validate((valid) => {
      if (valid) {
        api[type](form).then((res) => {
          if (type === 'Base') {
            ElMessage.success(`${t('create.synonym')}${t('success')}`);
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
    }, 300);
  };
</script>

<style lang="scss" scoped>
  .synonym-dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 5px;
    }
    :deep(.el-select) {
      width: 100%;
    }
  }
</style>
