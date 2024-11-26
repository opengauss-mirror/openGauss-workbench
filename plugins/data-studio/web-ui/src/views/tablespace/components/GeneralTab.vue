<template>
  <el-form
    :model="form"
    class="rule-form"
    ref="ruleFormRef"
    :rules="rules"
    label-width="150px"
    label-position="left"
    :label-suffix="$t('common.colon')"
  >
    <el-row :gutter="30">
      <el-col :span="11">
        <el-form-item prop="tablespaceName" :label="$t('tablespace.tablespaceName')">
          <el-input v-model="form.tablespaceName" />
        </el-form-item>
      </el-col>
      <el-col :span="13">
        <el-form-item prop="owner" :label="$t('tablespace.tablespaceOwner')" label-width="120px">
          <el-select v-model="form.owner" style="width: 100%">
            <el-option
              v-for="item in ownerList"
              :key="item.oid"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="11">
        <el-form-item prop="sequentialOverhead" :label="$t('tablespace.sequentialPageCost')">
          <el-input v-model="form.sequentialOverhead" />
        </el-form-item>
      </el-col>
      <el-col :span="13">
        <el-form-item prop="path" :label="$t('tablespace.path')" label-width="120px">
          <el-input
            v-model="form.path"
            :disabled="props.type == 'edit'"
            style="width: calc(100% - 115px); margin-right: 5px"
          />
          <el-checkbox
            v-model="form.relativePath"
            :disabled="props.type == 'edit'"
            :label="$t('tablespace.relativePath')"
          />
        </el-form-item>
      </el-col>
      <el-col :span="11">
        <el-form-item prop="nonSequentialOverhead" :label="$t('tablespace.randomPageCost')">
          <el-input v-model="form.nonSequentialOverhead" />
        </el-form-item>
      </el-col>
      <el-col :span="13">
        <el-form-item prop="maxStorage" :label="$t('tablespace.maxStorage')" label-width="120px">
          <el-input-number
            v-model="form.maxStorage"
            :min="1"
            :step="1"
            step-strictly
            :precision="0"
            controls-position="right"
            style="width: calc(100% - 70px)"
          />
          <el-select v-model="form.maxSizeUnit" style="width: 65px; margin-left: 5px">
            <el-option label="KB" value="K" />
            <el-option label="MB" value="M" />
            <el-option label="GB" value="G" />
            <el-option label="TB" value="T" />
            <el-option label="PB" value="P" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="11">
        <el-form-item prop="comment" :label="$t('common.description')">
          <el-input
            v-model="form.comment"
            type="textarea"
            :maxlength="5000"
            :autosize="{ minRows: 1, maxRows: 3 }"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
<script lang="ts" setup>
  import { FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { Generateform } from '../type';
  import { getSchemaList, getUserMemberList } from '@/api/metaData';

  const props = withDefaults(
    defineProps<{
      type: 'create' | 'edit';
      uuid: string;
      data: Generateform;
    }>(),
    {
      type: 'create',
      uuid: '',
    },
  );
  const emit = defineEmits<{
    (event: 'update:data', data: Generateform): void;
  }>();
  const { t } = useI18n();
  const ruleFormRef = ref<FormInstance>();
  const ownerList = ref([]);
  const form = computed({
    get() {
      return props.data;
    },
    set(val) {
      emit('update:data', val);
    },
  });

  const fetchSchemaList = async () => {
    const res = (await getSchemaList({ uuid: props.uuid })) as unknown as string[];
    ownerList.value = res;
  };

  const fetchUserMemberList = async () => {
    const res = (await getUserMemberList({ uuid: props.uuid })) as unknown as string[];
    ownerList.value = res;
  }

  const rules = reactive<FormRules>({
    tablespaceName: [
      {
        required: true,
        message: t('rules.empty', [t('tablespace.tablespaceName')]),
        trigger: 'blur',
      },
    ],
    path: [
      {
        required: true,
        message: t('rules.empty', [t('tablespace.path')]),
        trigger: 'blur',
      },
    ],
  });
  const formRef = () => {
    return ruleFormRef.value;
  };
  const validateForm = () => {
    return new Promise<void>((resolve, reject) => {
      ruleFormRef.value.validate((valid) => {
        if (valid) {
          resolve();
        } else {
          reject('GeneralTab');
        }
      });
    });
  };

  const resetFields = () => {
    ruleFormRef.value.resetFields();
  };
  onMounted(() => {
    // nextTick(fetchSchemaList);
    nextTick(fetchUserMemberList);
  });
  defineExpose({
    formRef,
    validateForm,
    resetFields,
  });
</script>

<style lang="scss" scoped>
  .rule-form {
    overflow-x: hidden;
  }
  :deep(.el-input-number .el-input__inner) {
    text-align: left;
  }
  :deep(.el-form-item__label) {
    padding-right: 0;
  }
</style>
