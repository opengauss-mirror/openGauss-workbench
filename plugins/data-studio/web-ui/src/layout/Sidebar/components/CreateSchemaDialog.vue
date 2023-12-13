<template>
  <div class="schema-dialog">
    <el-dialog
      v-model="visible"
      :title="props.type == 'create' ? $t('create.mode') : $t('edit.mode')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="125px">
          <el-form-item prop="schemaName" :label="$t('mode.name')">
            <el-input v-model="form.schemaName" />
          </el-form-item>
          <el-form-item :label="$t('database.database')">
            {{ form.databaseName }}
          </el-form-item>
          <el-form-item prop="owner" :label="$t('mode.owner')">
            <el-select v-model="form.owner" :disabled="props.type == 'edit'">
              <el-option v-for="item in ownerList" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item prop="description" :label="$t('common.description')">
            <el-input v-model="form.description" type="textarea" :rows="2" resize="none" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
          <el-button type="primary" @click="confirmForm">
            {{ $t('button.confirm') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { getSchemaUsers, createSchema, getSchema, updateSchema } from '@/api/schema';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: 'create' | 'edit';
      uuid: string;
      databaseName: string;
      oid: string;
      owner: string;
    }>(),
    {
      modelValue: false,
      type: 'create',
      uuid: '',
      databaseName: '',
      oid: '',
      owner: '',
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'success', data: any): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });

  const { t } = useI18n();
  const ruleFormRef = ref<FormInstance>();
  const form = reactive({
    oid: '',
    schemaName: '',
    databaseName: '',
    owner: '',
    description: '',
  });
  const rules = reactive<FormRules>({
    schemaName: [{ required: true, message: t('rules.empty', [t('mode.name')]), trigger: 'blur' }],
    owner: [{ required: true, message: t('rules.empty', [t('mode.owner')]), trigger: 'change' }],
  });

  const ownerList = ref<string[]>([]);

  const getOwnerList = async () => {
    ownerList.value = (await getSchemaUsers({ uuid: props.uuid })) as unknown as string[];
  };
  const handleOpen = async () => {
    if (props.type == 'create') {
      getOwnerList();
      form.databaseName = props.databaseName;
      form.owner = props.owner;
    } else if (props.type == 'edit') {
      const res = (await getSchema({
        uuid: props.uuid,
        oid: props.oid,
      })) as unknown as typeof form;
      Object.assign(form, {
        oid: props.oid,
        schemaName: res.schemaName,
        databaseName: props.databaseName,
        owner: res.owner,
        description: res.description,
      });
    }
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    ruleFormRef.value.resetFields();
    ownerList.value = [];
  };
  const confirmForm = async () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        const params = {
          uuid: props.uuid,
          schemaName: form.schemaName,
          owner: form.owner,
          description: form.description,
        };
        if (props.type == 'create') {
          await createSchema({ ...params });
          ElMessage.success(t('message.createSuccess'));
        } else {
          await updateSchema({
            ...params,
            oid: form.oid,
          });
          ElMessage.success(t('message.editSuccess'));
        }
        myEmit('success', { schemaName: form.schemaName });
      }
    });
  };
</script>

<style lang="scss" scoped>
  .schema-dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 5px;
    }
    :deep(.el-select) {
      width: 100%;
    }
    :deep(.el-input-number .el-input__inner) {
      text-align: left;
    }
  }
</style>
