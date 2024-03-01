<template>
  <div class="common-dialog-wrapper">
    <el-dialog
      v-model="visible"
      :title="props.type == 'create' ? $t('database.create') : $t('database.edit')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="125px">
          <el-form-item prop="name" :label="$t('database.name')">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item prop="databaseCode" :label="$t('database.encoder')">
            <el-select v-model="form.databaseCode" :disabled="props.type == 'edit'">
              <el-option label="UTF-8" value="UTF-8" />
              <el-option label="GBK" value="GBK" />
              <el-option label="LATIN1" value="LATIN1" />
              <el-option label="SQL_ASCII" value="SQL_ASCII" />
            </el-select>
          </el-form-item>
          <el-form-item prop="compatibleType" :label="$t('database.compatibleType')">
            <el-select v-model="form.compatibleType" :disabled="props.type == 'edit'">
              <el-option label="A" value="A" />
              <el-option label="B" value="B" />
              <el-option label="C" value="C" />
              <el-option label="PG" value="PG" />
            </el-select>
          </el-form-item>
          <el-form-item prop="collation" :label="$t('database.collation')">
            <el-select
              v-model="form.collation"
              :disabled="props.type == 'edit'"
              clearable
              placeholder=" "
            >
              <el-option label="C" value="C" />
              <el-option label="POSIX" value="POSIX" />
              <el-option label="en_US.UTF-8" value="en_US.UTF-8" />
            </el-select>
          </el-form-item>
          <el-form-item prop="characterType" :label="$t('database.characterType')">
            <el-select
              v-model="form.characterType"
              :disabled="props.type == 'edit'"
              clearable
              placeholder=" "
            >
              <el-option label="C" value="C" />
              <el-option label="POSIX" value="POSIX" />
              <el-option label="en_US.UTF-8" value="en_US.UTF-8" />
            </el-select>
          </el-form-item>
          <el-form-item prop="conRestrictions" :label="$t('database.conRestrictions')">
            <el-input-number
              v-model="form.conRestrictions"
              :min="-1"
              :step="1"
              step-strictly
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item prop="isConnect" :label="$t('database.connect')">
            <el-switch v-model="form.isConnect" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
        <el-button type="primary" @click="confirmForm">
          {{ $t('button.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { useUserStore } from '@/store/modules/user';
  import { createDatabase, renameDatabase, updateDatabase } from '@/api/database';
  import { formatTableData } from '@/utils';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: 'create' | 'edit';
      uuid: string;
      data?: any;
    }>(),
    {
      modelValue: false,
      type: 'create',
      uuid: '',
      data: () => ({}),
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
  const UserStore = useUserStore();
  const form = reactive({
    name: '',
    oldName: '',
    databaseCode: 'UTF-8',
    compatibleType: 'A',
    collation: '',
    characterType: '',
    conRestrictions: -1,
    isConnect: false,
    webUser: UserStore.userId,
  });
  const rules = reactive<FormRules>({
    name: [{ required: true, message: t('rules.empty', [t('database.name')]), trigger: 'blur' }],
    databaseCode: [
      { required: true, message: t('rules.empty', [t('database.encoder')]), trigger: 'change' },
    ],
    compatibleType: [
      {
        required: true,
        message: t('rules.empty', [t('database.compatibleType')]),
        trigger: 'change',
      },
    ],
    conRestrictions: [
      {
        required: true,
        message: t('rules.empty', [t('database.conRestrictions')]),
        trigger: 'blur',
        type: 'number',
      },
    ],
  });

  const handleOpen = async () => {
    if (props.type == 'edit') {
      form.name = form.oldName = props.data.name;
      const { column, result } = (await updateDatabase({
        uuid: props.uuid,
        databaseName: props.data.name,
      })) as any;
      const data: any = formatTableData(column, result)[0];
      Object.assign(form, {
        databaseCode: data.databasecode,
        compatibleType: data.compatibletype,
        collation: data.collation,
        characterType: data.charactertype,
        conRestrictions: ['string', 'number'].includes(typeof data.conrestrictions)
          ? Number(data.conrestrictions)
          : null,
      });
    }
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    ruleFormRef.value.resetFields();
  };
  const confirmForm = async () => {
    ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        if (props.type == 'create') {
          await createDatabase({
            uuid: props.uuid,
            databaseName: form.name,
            databaseCode: form.databaseCode,
            compatibleType: form.compatibleType,
            collation: form.collation,
            characterType: form.characterType,
            conRestrictions: form.conRestrictions,
          });
          ElMessage.success(t('message.createSuccess'));
        } else {
          await renameDatabase({
            uuid: props.uuid,
            databaseName: form.name,
            oldDatabaseName: form.oldName,
            conRestrictions: String(form.conRestrictions),
          });
          ElMessage.success(t('message.editSuccess'));
        }
        myEmit('success', {
          name: form.name,
          isConnect: form.isConnect,
        });
      }
    });
  };
</script>

<style lang="scss" scoped>
  :deep(.el-select) {
    width: 100%;
  }
  :deep(.el-input-number .el-input__inner) {
    text-align: left;
  }
</style>
