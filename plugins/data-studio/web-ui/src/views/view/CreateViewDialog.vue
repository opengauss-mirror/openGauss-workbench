<template>
  <div class="view-dialog">
    <el-dialog
      v-model="visible"
      :title="$t('create.view')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @open="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-tabs v-model="activeName" @tab-click="handleTabClick">
          <el-tab-pane :label="$t('view.base')" name="Base"></el-tab-pane>
          <el-tab-pane :label="$t('view.preview')" name="Sql"></el-tab-pane>
        </el-tabs>
        <div v-show="activeName == 'Base'">
          <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="95px">
            <el-form-item prop="viewName" :label="$t('view.name')">
              <el-input v-model="form.viewName" />
            </el-form-item>
            <el-form-item prop="viewType" :label="$t('view.type')">
              <el-select v-model="form.viewType">
                <el-option :label="$t('view.view')" value="VIEW" />
                <el-option :label="$t('view.materializedView')" value="MATERIALIZED" />
              </el-select>
            </el-form-item>
            <el-form-item prop="schema" :label="$t('view.objectMode')">
              <el-select v-model="form.schema" disabled>
                <el-option :label="form.schema" :value="form.schema" />
              </el-select>
            </el-form-item>
            <el-form-item prop="sql" :label="$t('view.code')">
              <AceEditor
                :value="form.sql"
                ref="editorRef"
                :readOnly="false"
                height="450px"
                style="margin: 4px 0; border: 1px solid #ddd; width: 100%"
              />
            </el-form-item>
          </el-form>
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
          <el-button type="primary" @click="resetForm()">
            {{ $t('button.clear') }}
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
  import { computed, ref, reactive, Ref } from 'vue';
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import type { TabsPaneContext } from 'element-plus';
  import AceEditor from '@/components/AceEditor.vue';
  import { useI18n } from 'vue-i18n';
  import { createView, createViewDdl } from '@/api/view';
  import { useUserStore } from '@/store/modules/user';

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
  const connectData = computed(() => props.connectData);
  const activeName: Ref<string | number> = ref('Base');
  const form = reactive({
    viewName: '',
    viewType: 'VIEW',
    schema: '',
    sql: '',
    connectionName: '',
    webUser: UserStore.userId,
  });
  const rules = reactive<FormRules>({
    viewName: [{ required: true, message: t('view.rules.name[0]'), trigger: 'blur' }],
    viewType: [{ required: true, message: t('view.rules.type[0]'), trigger: 'change' }],
    schema: [{ required: true, message: t('view.rules.mode[0]'), trigger: 'change' }],
    sql: [{ required: true, message: t('view.rules.code[0]'), trigger: 'blur' }],
  });

  const handleTabClick = (tab: TabsPaneContext) => {
    activeName.value = tab.paneName;
    if (activeName.value === 'Sql') confirmForm(activeName.value);
  };

  const handleOpen = async () => {
    form.schema = connectData.value.schema_name;
    form.connectionName = connectData.value.connectInfo.name;
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    resetForm();
    activeName.value = 'Base';
  };
  const confirmForm = async (type) => {
    const api = {
      Base: createView,
      Sql: createViewDdl,
    };
    form.sql = editorRef.value.getValue();
    ruleFormRef.value.validate((valid) => {
      if (valid) {
        api[type](form).then((res) => {
          if (type === 'Base') {
            ElMessage.success(`${t('create.view')}${t('success')}`);
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
      viewName: '',
      viewType: 'VIEW',
      sql: '',
    });
    editorRef.value.setValue('');
    editorPreRef.value.setValue('');
    ruleFormRef.value.clearValidate();
  };
</script>

<style lang="scss" scoped>
  .view-dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 5px;
    }
    :deep(.el-select) {
      width: 100%;
    }
  }
</style>
