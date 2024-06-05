<template>
  <el-form
    :model="form"
    class="rule-form"
    ref="ruleFormRef"
    :rules="rules"
    label-width="120px"
    label-position="left"
    :label-suffix="$t('common.colon')"
  >
    <el-row :gutter="30" justify="start" align="top">
      <el-col :span="12">
        <el-form-item prop="name" :label="$t('trigger.general.name')">
          <el-input v-model="form.name" :disabled="$props.type == 'edit'" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="status" :label="$t('trigger.general.status')">
          <el-radio-group
            v-model="form.status"
            :disabled="form.type !== 'table' || $props.type == 'edit'"
          >
            <el-radio :value="true">{{ $t('button.enabled') }}</el-radio>
            <el-radio :value="false">{{ $t('button.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="type" :label="$t('trigger.general.type')">
          <el-select v-model="form.type" @change="changeType" :disabled="$props.type == 'edit'">
            <el-option :label="$t('table.title')" value="table" />
            <el-option :label="$t('view.title')" value="view" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item
          prop="tableName"
          :label="
            form.type == 'view' ? $t('trigger.general.tableName2') : $t('trigger.general.tableName')
          "
        >
          <el-select
            v-model="form.tableName"
            @change="fetchUpdateFieldList"
            :disabled="$props.type == 'edit'"
          >
            <el-option
              v-for="item in tableList"
              :key="item.oid"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="time" :label="$t('trigger.general.time')">
          <el-select v-model="form.time" @change="changeTime" :disabled="$props.type == 'edit'">
            <el-option label="BEFORE" value="BEFORE" />
            <el-option label="AFTER" value="AFTER" />
            <el-option v-if="form.type == 'view'" label="INSTEAD OF" value="INSTEAD OF" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item
          prop="frequency"
          :label="$t('trigger.general.frequency')"
          :required="form.time != 'INSTEAD OF'"
        >
          <el-select
            v-model="form.frequency"
            @change="changeFrequency"
            :disabled="
              form.time == 'INSTEAD OF' ||
              (form.type == 'view' && ['BEFORE', 'AFTER'].includes(form.time)) ||
              $props.type == 'edit'
            "
          >
            <el-option label="ROW" value="ROW" />
            <el-option label="STATEMENT" value="STATEMENT" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="function" :label="$t('trigger.general.function')">
          <el-select v-model="form.function" ref="diySelectRef" :disabled="$props.type == 'edit'">
            <el-option value="add">
              <div class="add-option" @click.stop="handleCreateFunction">
                <el-icon><Plus /></el-icon>
                {{ $t('button.newBuild') }}
              </div>
            </el-option>
            <el-option v-for="item in functionList" :key="item" :value="item" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="event" :label="$t('trigger.general.event')">
          <el-checkbox-group
            v-model="form.event"
            @change="fetchUpdateFieldList"
            :disabled="$props.type == 'edit'"
          >
            <el-checkbox label="INSERT" />
            <el-checkbox label="DELETE" />
            <el-checkbox
              label="TRUNCATE"
              :disabled="
                form.type == 'view' || form.time == 'INSTEAD OF' || form.frequency != 'STATEMENT'
              "
            />
            <el-checkbox label="UPDATE" />
          </el-checkbox-group>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-row>
          <el-form-item
            prop="condition"
            :label="$t('trigger.general.condition')"
            style="width: 100%"
          >
            <el-input
              v-model="form.condition"
              type="textarea"
              :maxlength="5000"
              :autosize="{ minRows: 1 }"
              :disabled="form.time == 'INSTEAD OF' || $props.type == 'edit'"
            />
          </el-form-item>
        </el-row>
        <el-row>
          <el-form-item prop="description" :label="$t('common.description')" style="width: 100%">
            <el-input
              v-model="form.description"
              type="textarea"
              :maxlength="5000"
              :autosize="{ minRows: 1 }"
              :disabled="$props.type == 'edit'"
            />
          </el-form-item>
        </el-row>
      </el-col>
      <el-col :span="12" v-show="form.type == 'table' && form.event.includes('UPDATE')">
        <el-form-item prop="columnList" :label="$t('trigger.general.field')">
          <el-table
            ref="fieldTableRef"
            :data="columnsList"
            border
            max-height="290"
            row-key="columnName"
            @selection-change="handleSelectionChange"
          >
            <el-table-column
              type="selection"
              align="center"
              width="35"
              :selectable="() => $props.type != 'edit'"
            />
            <el-table-column
              prop="columnName"
              align="center"
              :label="$t('table.column.columnName')"
              min-width="100"
            />
            <el-table-column
              prop="dataType"
              align="center"
              :label="$t('table.column.dataType')"
              min-width="60"
            />
          </el-table>
        </el-form-item>
      </el-col>
    </el-row>

    <CreateFunctionDialog
      v-model="visibleCreateDialog"
      :uuid="props.uuid"
      @success="fetchFunctionList"
    />
  </el-form>
</template>
<script lang="ts" setup>
  import { FormInstance, FormRules } from 'element-plus';
  import { useI18n } from 'vue-i18n';
  import { Generateform } from '../type';
  import { getSchemaObjects } from '@/api/metaData';
  import { getTableColumn } from '@/api/table';
  import { queryTriggerFunctionApi } from '@/api/trigger';
  import CreateFunctionDialog from './CreateFunctionDialog.vue';

  const props = withDefaults(
    defineProps<{
      type: 'create' | 'edit';
      userName: string;
      uuid: string;
      schema: string;
      data: Generateform;
    }>(),
    {
      type: 'create',
      uuid: '',
      schema: '',
    },
  );
  const emit = defineEmits<{
    (event: 'update:data', data: Generateform): void;
  }>();
  const { t } = useI18n();
  const ruleFormRef = ref<FormInstance>();
  const tableList = ref([]);
  const functionList = ref([]);
  const columnsList = ref([]);
  const diySelectRef = ref(null);
  const fieldTableRef = ref(null);
  const visibleCreateDialog = ref(false);
  const form = computed({
    get() {
      return props.data;
    },
    set(val) {
      emit('update:data', val);
    },
  });

  const fetchFunctionList = async () => {
    functionList.value = (await queryTriggerFunctionApi({
      uuid: props.uuid,
      userName: props.userName,
    })) as unknown as string[];
  };

  const fetchTableViewList = async () => {
    const type = form.value.type;
    tableList.value =
      ((await getSchemaObjects({
        schema: props.schema,
        uuid: props.uuid,
        type,
      })) as unknown as { name: string; oid: string; parttype?: string }[]) || [];
  };
  const fetchUpdateFieldList = async () => {
    const event = form.value.event;
    const type = form.value.type;
    if (!event.includes('UPDATE') || type !== 'table') return;
    const select = tableList.value.find((item) => item.name === form.value.tableName);
    if (!select) return;
    const res = await getTableColumn({
      tableName: select.name,
      schema: props.schema,
      uuid: props.uuid,
      oid: select.oid,
      parttype: select.parttype,
    });
    columnsList.value = res.result.map((row) => {
      return {
        columnName: row[0],
        dataType: row[1],
      };
    });
  };

  const rules = reactive<FormRules>({
    name: [
      {
        required: true,
        message: t('rules.empty', [t('trigger.general.name')]),
        trigger: 'blur',
      },
    ],
    type: [
      {
        required: true,
        message: t('rules.empty', [t('trigger.general.type')]),
        trigger: 'change',
      },
    ],
    tableName: [
      {
        required: true,
        message: t('rules.empty', [t('trigger.general.tableName')]),
        trigger: 'change',
      },
    ],
    time: [
      {
        required: true,
        message: t('rules.empty', [t('trigger.general.time')]),
        trigger: 'change',
      },
    ],
    frequency: [
      {
        required: true,
        message: t('rules.empty', [t('trigger.general.frequency')]),
        trigger: 'change',
      },
    ],
    function: [
      {
        required: true,
        message: t('rules.empty', [t('trigger.general.function')]),
        trigger: 'change',
      },
    ],
    event: [
      {
        required: true,
        message: t('rules.empty', [t('trigger.general.event')]),
        trigger: 'change',
      },
    ],
  });
  const formRef = () => {
    return ruleFormRef.value;
  };

  const handleCreateFunction = () => {
    visibleCreateDialog.value = true;
    diySelectRef.value.blur();
  };

  const changeType = async (value) => {
    if (value == 'table' && form.value.time == 'INSTEAD OF') {
      form.value.time = '';
    }
    if (value == 'view') {
      form.value.status = true;
      setValue2();
    }
    setValue1();
    form.value.tableName = '';
    columnsList.value = [];
    await fetchTableViewList();
  };

  const changeTime = (value) => {
    setValue1();
    if (value == 'INSTEAD OF') {
      form.value.frequency = 'ROW';
      form.value.condition = '';
      setValue2();
    }
  };

  const changeFrequency = (value) => {
    if (value == 'ROW') {
      setValue2();
    }
  };

  const setValue1 = () => {
    if (form.value.type == 'view' && ['BEFORE', 'AFTER'].includes(form.value.time)) {
      form.value.frequency = 'STATEMENT';
    }
  };
  const setValue2 = () => {
    let eventIndex = form.value.event.indexOf('TRUNCATE');
    eventIndex >= 0 && form.value.event.splice(eventIndex, 1);
  };

  const handleSelectionChange = (list) => {
    form.value.columnList = list.map((item) => item.columnName);
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

  const setCurrentTableSelect = async () => {
    form.value.columnList.forEach((target) => {
      fieldTableRef.value.toggleRowSelection(
        columnsList.value.find((item) => item.columnName == target),
      );
    });
  };

  const resetFields = () => {
    ruleFormRef.value.resetFields();
  };
  onMounted(async () => {
    fetchFunctionList();
  });
  defineExpose({
    formRef,
    validateForm,
    resetFields,
    fetchTableViewList,
    fetchUpdateFieldList,
    setCurrentTableSelect,
  });
</script>

<style lang="scss" scoped>
  .rule-form {
    overflow-x: hidden;
  }
  :deep(.el-select) {
    width: 100%;
  }
  :deep(.el-input-number .el-input__inner) {
    text-align: left;
  }
  :deep(.el-form-item__label) {
    padding-right: 0;
  }
  .add-option {
    color: red;
  }
  :deep(.el-table) {
    .cell {
      padding: 5px;
    }
  }
</style>
