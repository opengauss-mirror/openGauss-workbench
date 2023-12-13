<template>
  <el-form
    :model="form"
    class="rule-form"
    ref="ruleFormRef"
    :rules="rules"
    label-width="140px"
    label-position="left"
    :label-suffix="$t('common.colon')"
  >
    <el-row :gutter="50">
      <el-col :span="12">
        <el-form-item prop="foreignTable" :label="$t('foreignTable.foreign.foreignName')">
          <el-input v-model="form.foreignTable" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="exists" :label="$t('foreignTable.foreign.ifNotExists')">
          <el-checkbox v-model="form.exists" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item
          prop="datasourceType"
          :label="$t('foreignTable.foreign.foreignDataSourceType')"
        >
          <el-select v-model="form.datasourceType" @change="handleChangeDatasourceType">
            <el-option label="openGauss" value="openGauss" />
            <!-- <el-option label="Oracle" value="Oracle" /> -->
            <!-- <el-option label="MySQL" value="MySQL" /> -->
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="foreignServer" :label="$t('foreignTable.foreign.foreignServer')">
          <el-select v-model="form.foreignServer" ref="foreignServerRef">
            <el-option v-for="item in foreignServerShowList" :key="item.name" :value="item.name">
              <div class="diy-option">
                <span class="left">{{ item.name }}</span>
                <el-icon class="right" @click.stop="handleDeleteForeignServer(item.name)">
                  <Delete />
                </el-icon>
              </div>
            </el-option>
            <el-option value="add">
              <div class="add-option" @click.stop="handleCreateForeignServer">
                <el-icon><Plus /></el-icon>
                {{ $t('button.newBuild') }}
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="foreignDatabase" :label="$t('foreignTable.foreign.foreignDatabase')">
          <el-input v-model="form.foreignDatabase" disabled />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="schema" :label="$t('foreignTable.foreign.foreignSchema')">
          <el-input v-model="form.schema" disabled />
        </el-form-item>
      </el-col>
      <el-col :span="12" v-if="['Oracle', 'openGauss'].includes(form.datasourceType)">
        <el-form-item prop="remoteSchema" :label="$t('foreignTable.foreign.remoteSchema')">
          <el-input v-model="form.remoteSchema" />
        </el-form-item>
      </el-col>
      <el-col :span="12" v-if="form.datasourceType == 'MySQL'">
        <el-form-item prop="remoteDatabase" :label="$t('foreignTable.foreign.remoteDatabase')">
          <el-input v-model="form.remoteDatabase" />
        </el-form-item>
      </el-col>
      <el-col :span="12" v-if="['Oracle', 'MySQL', 'openGauss'].includes(form.datasourceType)">
        <el-form-item prop="remoteTable" :label="$t('foreignTable.foreign.remoteTable')">
          <el-input v-model="form.remoteTable" />
        </el-form-item>
      </el-col>
      <template v-if="form.datasourceType == 'File'">
        <el-col :span="12">
          <el-form-item prop="remoteFilePath" :label="$t('foreignTable.foreign.remoteFilePath')">
            <el-input v-model="form.remoteFilePath" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="fileType" :label="$t('foreignTable.foreign.remoteFileFormat')">
            <el-select v-model="form.fileType" @change="handleChangeFormat">
              <el-option v-for="item in formatList" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="quote" :label="$t('table.export.quotes')">
            <el-input v-model="form.quote" :disabled="form.fileType != 'Text'" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="escape" :label="$t('table.export.escape')">
            <el-input v-model="form.escape" :disabled="form.fileType != 'Csv'" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="replaceNull" :label="$t('table.export.replaceNullWith')">
            <el-input v-model="form.replaceNull" :disabled="form.fileType != 'Text'" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="encoding" :label="$t('table.export.encoding')">
            <el-select v-model="form.encoding" placement="top">
              <el-option v-for="item in encodingList" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="includeHeader" :label="$t('table.export.includeHeader')">
            <el-checkbox
              v-model="form.includeHeader"
              :disabled="!['Csv', 'Fixed'].includes(form.fileType)"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="delimiter" :label="$t('table.export.delimiter')">
            <el-radio-group
              v-model="form.delimiter"
              :disabled="['Fixed', 'Binary'].includes(form.fileType)"
              @change="form.delimiterOther = ''"
            >
              <el-radio label="comma">{{ t('table.export.comma') }}(,)</el-radio>
              <el-radio label="tab">
                {{ t('table.export.tab') }}({{ t('table.export.fourSpaces') }})
              </el-radio>
              <el-radio label="pipe">{{ t('table.export.pipe') }}(|)</el-radio>
              <el-radio label="semicolon">{{ t('table.export.semicolon') }}(;)</el-radio>
              <el-radio label="other">
                {{ t('table.export.other') }}{{ t('common.colon') }}
              </el-radio>
              <el-input
                v-model="form.delimiterOther"
                :disabled="form.delimiter != 'other'"
                maxlength="10"
              />
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            prop="unmatchedEmptyString"
            :label="$t('foreignTable.foreign.unmatchedEmptyString')"
          >
            <el-checkbox v-model="form.unmatchedEmptyString" :disabled="form.fileType != 'Csv'" />
          </el-form-item>
        </el-col>
      </template>
      <el-col :span="12">
        <el-form-item prop="description" :label="$t('common.description')">
          <el-input v-model="form.description" />
        </el-form-item>
      </el-col>
    </el-row>
    <CreateServerDialog
      v-model="visibleCreateDialog"
      :uuid="$props.uuid"
      :datasourceType="form.datasourceType"
      :role="$props.role"
      @success="createServerSuccess"
    />
  </el-form>
</template>
<script lang="ts" setup>
  import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { Generateform } from '../types';
  import CreateServerDialog from './CreateServerDialog.vue';
  import { getServerListApi, deleteForeignServerApi } from '@/api/foreignTable';

  const props = withDefaults(
    defineProps<{
      data: Generateform;
      role: string;
      uuid: string;
      databaseName: string;
      schema: string;
    }>(),
    {
      role: '',
      uuid: '',
      databaseName: '',
      schema: '',
    },
  );
  const emit = defineEmits<{
    (event: 'update:data', data: Generateform): void;
  }>();
  const { t } = useI18n();
  const ruleFormRef = ref<FormInstance>();
  const foreignServerRef = ref();
  const foreignServerList = ref([]);
  const formatList = ['Csv', 'Text', 'Fixed', 'Binary'] as const;
  const encodingList = reactive(['UTF-8', 'GBK', 'LATIN1']);
  const form = computed({
    get() {
      return props.data;
    },
    set(val) {
      emit('update:data', val);
    },
  });
  const foreignServerShowList = computed(() =>
    foreignServerList.value.filter((item) => item.type == form.value.datasourceType),
  );
  const visibleCreateDialog = ref(false);

  const handleChangeFormat = (val: typeof formatList[number]) => {
    const delimiterValueMap = {
      Text: 'tab',
      Csv: 'comma',
    };
    Object.assign(form.value, {
      quote: '',
      escape: '',
      replaceNull: '',
      includeHeader: false,
      delimiter: delimiterValueMap[val],
      delimiterOther: '',
      unmatchedEmptyString: false,
    });
  };

  const rules = reactive<FormRules>({
    foreignTable: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.foreign.foreignName')]),
        trigger: 'blur',
      },
    ],
    datasourceType: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.foreign.foreignDataSourceType')]),
        trigger: 'change',
      },
    ],
    foreignServer: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.foreign.foreignServer')]),
        trigger: 'change',
      },
    ],
    foreignDatabase: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.foreign.foreignDatabase')]),
        trigger: 'change',
      },
    ],
    schema: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.foreign.foreignSchema')]),
        trigger: 'change',
      },
    ],
    remoteFilePath: [
      {
        required: true,
        message: t('rules.empty', [t('foreignTable.foreign.remoteFilePath')]),
        trigger: 'blur',
      },
    ],
  });
  const formRef = () => {
    return ruleFormRef.value;
  };
  const validateForm = () => {
    return new Promise<void>((resolve, reject) => {
      if (form.value.delimiter == 'other' && !form.value.delimiterOther) {
        ElMessage.error(t('rules.enter', [t('table.export.delimiter')]));
        return reject('GeneralTab');
      }
      ruleFormRef.value.validate((valid) => {
        if (valid) {
          resolve();
        } else {
          reject('GeneralTab');
        }
      });
    });
  };

  const getServerList = async () => {
    const res = (await getServerListApi({
      uuid: props.uuid,
      schema: props.schema,
    })) as unknown as { name: string; type: string }[];
    foreignServerList.value = res;
  };

  const handleDeleteForeignServer = (name) => {
    ElMessageBox.confirm(t('foreignTable.deleteForeignServerTips', { name })).then(async () => {
      await deleteForeignServerApi({
        uuid: props.uuid,
        role: props.role,
        foreignServer: name,
      });
      ElMessage.success(t('message.deleteSuccess'));
      getServerList();
      if (form.value.foreignServer == name) {
        form.value.foreignServer = '';
      }
    });
  };

  const handleCreateForeignServer = () => {
    if (!form.value.datasourceType) {
      return ElMessage.warning(
        t('rules.chooseFirst', [t('foreignTable.foreign.foreignDataSourceType')]),
      );
    }
    visibleCreateDialog.value = true;
    foreignServerRef.value.blur();
  };

  const handleChangeDatasourceType = () => {
    form.value.foreignServer = '';
  };

  const createServerSuccess = (name) => {
    getServerList();
    form.value.foreignServer = name;
  };

  const resetFields = () => {
    ruleFormRef.value.resetFields();
  };

  onMounted(() => {
    getServerList();
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
  :deep(.el-select) {
    width: 100%;
  }
  :deep(.el-date-editor.el-input) {
    width: 100%;
  }
  :deep(.el-input-number) {
    width: 100%;
  }
  :deep(.el-input-number .el-input__inner) {
    text-align: left;
  }
  .diy-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .right {
      margin-left: 10px;
      display: none;
    }
    &:hover .right {
      display: inline-flex;
      color: red;
    }
  }
  .add-option {
    color: red;
  }
</style>
