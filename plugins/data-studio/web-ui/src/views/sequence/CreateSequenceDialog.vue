<template>
  <div class="sequence-dialog">
    <el-dialog
      v-model="visible"
      :title="$t('create.sequence')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-tabs v-model="activeName" @tab-click="handleTabClick">
          <el-tab-pane :label="$t('sequence.base')" name="Base"></el-tab-pane>
          <el-tab-pane :label="$t('sequence.preview')" name="Sql"></el-tab-pane>
        </el-tabs>
        <div v-show="activeName == 'Base'">
          <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="70px">
            <el-form-item prop="sequenceName" :label="$t('sequence.name')">
              <el-input v-model="form.sequenceName" />
            </el-form-item>
            <el-form-item prop="increment" :label="$t('sequence.increment')">
              <el-input v-model.number="form.increment" />
            </el-form-item>
            <el-form-item prop="start" :label="$t('sequence.startValue')">
              <el-input v-model.number="form.start" />
            </el-form-item>
            <el-form-item prop="minValue" :label="$t('sequence.minValue')">
              <el-input v-model.number="form.minValue" />
            </el-form-item>
            <el-form-item prop="maxValue" :label="$t('sequence.maxValue')">
              <el-input v-model.number="form.maxValue" />
            </el-form-item>
            <el-form-item prop="cache" :label="$t('sequence.cacheValue')">
              <el-input v-model.number="form.cache" />
            </el-form-item>
            <el-form-item prop="cycle" :label="$t('sequence.cycle')">
              <el-switch v-model="form.cycle" />
            </el-form-item>
            <el-form-item :label="$t('sequence.belongObject')">
              <hr style="width: 100%; border: none; height: 1px; background-color: #d9dbe1" />
            </el-form-item>
            <el-form-item prop="tableSchema" :label="$t('sequence.mode')">
              <el-select v-model="form.tableSchema" disabled>
                <el-option label="scott" value="1" />
              </el-select>
            </el-form-item>
            <el-form-item prop="tableName" :label="$t('sequence.table')">
              <el-select v-model="form.tableName" @change="changTableName">
                <el-option v-for="item in list.tableList" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
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
  import { computed, ref, reactive, Ref } from 'vue';
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import type { TabsPaneContext } from 'element-plus';
  import AceEditor from '@/components/AceEditor.vue';
  import { useI18n } from 'vue-i18n';
  import { getColumnList, createSequence, createSequenceDdl } from '@/api/sequence';
  import { getObjectList } from '@/api/metaData';
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
    sequenceName: '',
    increment: '',
    start: '',
    minValue: '',
    maxValue: '',
    cache: '',
    cycle: false,
    tableSchema: '',
    tableName: '',
    schema: '',
    tableColumn: '',
    connectionName: '',
    webUser: UserStore.userId,
    uuid: '',
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
    activeName.value = tab.paneName;
    if (activeName.value === 'Sql') confirmForm(activeName.value);
  };
  const handleOpen = async () => {
    form.schema = connectData.value.schemaName;
    form.tableSchema = connectData.value.schemaName;
    form.connectionName = connectData.value.connectInfo.name;
    form.uuid = connectData.value.uuid;
    fetchTableName();
  };
  const fetchTableName = async () => {
    const data = await getObjectList({
      connectionName: form.connectionName,
      objectType: 'r',
      schema: form.schema,
      webUser: form.webUser,
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
      webUser: form.webUser,
      uuid: form.uuid,
    });
    list.columnList = data as unknown as any[];
  };
  const changTableName = () => {
    form.tableColumn = '';
    fetchColumnList();
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    resetForm();
    activeName.value = 'Base';
  };
  const confirmForm = async (type) => {
    const api = {
      Base: createSequence,
      Sql: createSequenceDdl,
    };
    ruleFormRef.value.validate((valid) => {
      if (valid) {
        api[type](form).then((res) => {
          if (type === 'Base') {
            ElMessage.success(`${t('create.sequence')}${t('success')}`);
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
      sequenceName: '',
      increment: '',
      start: '',
      minValue: '',
      maxValue: '',
      cache: '',
      cycle: false,
      tableName: '',
      tableColumn: '',
    });
    editorPreRef.value.setValue('');
    ruleFormRef.value.clearValidate();
  };
</script>

<style lang="scss" scoped>
  .sequence-dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 5px;
    }
    :deep(.el-select) {
      width: 100%;
    }
  }
</style>
