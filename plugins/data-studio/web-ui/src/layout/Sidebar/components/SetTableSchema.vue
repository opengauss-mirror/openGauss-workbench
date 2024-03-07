<template>
  <div class="common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="$t('siderbar.table.setSchema')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <div class="tips">{{
          $t('message.setTableSchema', {
            name: `${commonParams.schema}.${commonParams.tableName}`,
          })
        }}</div>
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="0px">
          <el-form-item prop="input">
            <el-select v-model="form.input">
              <el-option
                v-for="item in optionList"
                :key="item"
                :value="item"
                :label="item"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
        <el-button type="primary" @click="confirmForm" :loading="loading">
          {{ $t('button.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { setTableSchema } from '@/api/table';
  import { getSchemaList } from '@/api/metaData';
  import type { NodeData } from '../types';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      nodeData: Partial<NodeData>;
    }>(),
    {
      modelValue: false,
      nodeData: () => ({}),
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });
  const commonParams = computed(() => {
    return {
      uuid: props.nodeData?.uuid,
      schema: props.nodeData?.schemaName,
      tableName: props.nodeData?.name,
    };
  });

  const { t } = useI18n();
  const loading = ref(false);
  const optionList = ref([]);
  const ruleFormRef = ref<FormInstance>();
  const form = reactive({
    input: '',
  });
  const rules = reactive<FormRules>({
    input: [
      { required: true, message: t('rules.empty', [t('table.tablespace')]), trigger: 'change' },
    ],
  });

  interface schemaObject {
    name: string;
    oid: string;
  }
  const fetchSchemaList = async () => {
    const res = (await getSchemaList({
      uuid: commonParams.value.uuid,
    })) as unknown as schemaObject[];
    optionList.value = res.map((item) => {
      return item.name;
    });
  };

  const handleOpen = async () => {
    fetchSchemaList();
  };

  const handleClose = () => {
    myEmit('update:modelValue', false);
    resetForm();
  };
  const confirmForm = () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true;
        setTableSchema({
          generalPurpose: form.input,
          ...commonParams.value,
        })
          .then(() => {
            ElMessage.success(`${t('message.editSuccess')}`);
            EventBus.notify(EventTypeName.REFRESH_ASIDER, 'database', {
              rootId: props.nodeData.rootId,
              databaseId: props.nodeData.databaseId,
            });
            handleClose();
          })
          .finally(() => {
            loading.value = false;
          });
      }
    });
  };
  const resetForm = () => {
    form.input = '';
  };
</script>

<style lang="scss" scoped>
  :deep(.el-select) {
    width: 100%;
  }
  .tips {
    margin-bottom: 5px;
  }
</style>
