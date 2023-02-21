<template>
  <div class="db-dialog">
    <el-dialog
      v-model="visible"
      :title="$t('database.create')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @open="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="125px">
          <el-form-item prop="name" :label="$t('database.name')">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item
            v-if="props.type == 'create'"
            prop="encoder"
            :label="$t('database.encoder')"
          >
            <el-select v-model="form.encoder">
              <el-option label="UTF-8" value="UTF-8" />
              <el-option label="GBK" value="GBK" />
              <el-option label="LATIN1" value="LATIN1" />
              <el-option label="SQL_ASCII" value="SQL_ASCII" />
            </el-select>
          </el-form-item>
          <el-form-item prop="isConnect" :label="$t('database.connect')">
            <el-switch v-model="form.isConnect" />
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
  import { computed, ref, reactive, Ref } from 'vue';
  import { ElMessage, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  // import { createViewDdl } from '@/api/view';
  import { useUserStore } from '@/store/modules/user';
  import { createDatabase } from '@/api/database';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: 'create' | 'rename';
      // connectData: any;
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
  const UserStore = useUserStore();
  // const connectData = computed(() => props.connectData);
  const form = reactive({
    name: '',
    encoder: 'UTF-8',
    isConnect: false,
    // connectionName: '',
    webUser: UserStore.userId,
  });
  const rules = reactive<FormRules>({
    name: [{ required: true, message: t('rules.empty', [t('database.name')]), trigger: 'blur' }],
    encoder: [
      { required: true, message: t('rules.empty', [t('database.encoder')]), trigger: 'change' },
    ],
  });

  const handleOpen = async () => {
    // form.isConnect = connectData.value.schemaName;
    // form.connectionName = connectData.value.connectInfo.name;
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    ruleFormRef.value.resetFields();
  };
  const confirmForm = async () => {
    ruleFormRef.value.validate((valid) => {
      if (valid) {
        console.log(123);
        
        // api[type](form).then((res) => {
        //   ElMessage.success(`${t('create.view')}${t('success')}`);
        //   handleClose();
        // });
      }
    });
  };
  // const resetForm = () => {
  //   Object.assign(form, {
  //     name: '',
  //     encoder: 'VIEW',
  //   });
  //   ruleFormRef.value.clearValidate();
  // };
</script>

<style lang="scss" scoped>
  .db-dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 5px;
    }
    :deep(.el-select) {
      width: 100%;
    }
  }
</style>
