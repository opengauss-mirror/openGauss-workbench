<template>
  <el-form
    class="rule-form"
    ref="ruleFormRef"
    label-width="auto"
    label-position="left"
    :label-suffix="$t('common.colon')"
  >
    <el-row :gutter="35">
      <el-col :span="12">
        <el-form-item :label-width="0" style="height: calc(100% - 5px); margin-bottom: 0">
          {{ $t('privilege.grantOrRevoke2') + $t('common.colon') }}
          <el-radio-group v-model="grantOrRevoke" @change="changePrivilegeType">
            <el-radio value="GRANT">GRANT</el-radio>
            <el-radio value="REVOKE">REVOKE</el-radio>
          </el-radio-group>
          <div class="table-wrapper">
            <el-table :data="tableList" ref="multipleTableRef" border>
              <el-table-column width="130" align="center" :resizable="false">
                <template #header>
                  <div style="padding-left: 10px; text-align: left">
                    <el-checkbox
                      v-model="isFunctionAll"
                      :indeterminate="isIndeterminateFunction"
                    ></el-checkbox>
                    <span class="table-checkbox-label">
                      {{ $t('privilege.name') }}
                    </span>
                  </div>
                </template>
                <template #default="{ row }">
                  <div style="padding-left: 10px; text-align: left">
                    <el-checkbox
                      v-model="row.checkPrivilege"
                      @change="(value) => changeFunctionCheck(value, row)"
                    ></el-checkbox>
                    <span class="table-checkbox-label">
                      {{ row.privilege }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column align="center" :resizable="false">
                <template #header>
                  <el-checkbox
                    v-model="isGrantAll"
                    :indeterminate="isIndeterminateGrant"
                    :disabled="!isFunctionAll"
                  ></el-checkbox>
                  <span
                    style="display: inline-block; margin-left: 12px; vertical-align: text-bottom"
                  >
                    {{ grantOrRevoke == 'GRANT' ? 'with grant option' : 'only grant privilege' }}
                  </span>
                </template>
                <template #default="{ row }">
                  <el-checkbox
                    v-model="row.checkOption"
                    :disabled="!row.checkPrivilege"
                  ></el-checkbox>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
<script lang="ts" setup>
  import { ElTable, FormInstance } from 'element-plus';
  import { OperateType, GrantOrRevoke } from '../types';

  const props = withDefaults(
    defineProps<{
      type: OperateType;
      grantOrRevoke: GrantOrRevoke;
    }>(),
    {
      grantOrRevoke: 'GRANT',
    },
  );
  const emit = defineEmits<{
    (event: 'update:grantOrRevoke', data: any): void;
  }>();
  const ruleFormRef = ref<FormInstance>();
  const getTableList = (type) => {
    if (type == 'schema') {
      return [
        {
          privilege: 'usage',
          checkPrivilege: false,
          checkOption: false,
        },
        {
          privilege: 'create',
          checkPrivilege: false,
          checkOption: false,
        },
      ];
    } else if (['table', 'view'].includes(type)) {
      return [
        {
          privilege: 'insert',
          checkPrivilege: false,
          checkOption: false,
        },
        {
          privilege: 'select',
          checkPrivilege: false,
          checkOption: false,
        },
        {
          privilege: 'update',
          checkPrivilege: false,
          checkOption: false,
        },
        {
          privilege: 'delete',
          checkPrivilege: false,
          checkOption: false,
        },
        {
          privilege: 'truncate',
          checkPrivilege: false,
          checkOption: false,
        },
        {
          privilege: 'references',
          checkPrivilege: false,
          checkOption: false,
        },
      ];
    } else if (type == 'function') {
      return [
        {
          privilege: 'execute',
          checkPrivilege: false,
          checkOption: false,
        },
      ];
    } else if (type == 'sequence') {
      return [
        {
          privilege: 'usage',
          checkPrivilege: false,
          checkOption: false,
        },
        {
          privilege: 'select',
          checkPrivilege: false,
          checkOption: false,
        },
        {
          privilege: 'update',
          checkPrivilege: false,
          checkOption: false,
        },
      ];
    } else {
      return [];
    }
  };
  const tableList = ref([]);
  watch(
    () => props.type,
    (type) => {
      tableList.value = getTableList(type);
    },
    {
      immediate: true,
    },
  );
  const multipleTableRef = ref<InstanceType<typeof ElTable>>();

  const grantOrRevoke = computed({
    get: () => props.grantOrRevoke,
    set(value) {
      emit('update:grantOrRevoke', value);
    },
  });
  const isFunctionAll = computed({
    get: () => tableList.value.every((item) => item.checkPrivilege),
    set: (value) => {
      tableList.value.forEach((item) => (item.checkPrivilege = value));
      if (!value) isGrantAll.value = false;
    },
  });
  const isIndeterminateFunction = computed(
    () =>
      tableList.value.some((item) => item.checkPrivilege) &&
      tableList.value.some((item) => !item.checkPrivilege),
  );
  const isGrantAll = computed({
    get: () => tableList.value.every((item) => item.checkOption),
    set: (value) => {
      tableList.value.forEach((item) => (item.checkOption = value));
    },
  });
  const isIndeterminateGrant = computed(
    () =>
      tableList.value.some((item) => item.checkOption) &&
      tableList.value.some((item) => !item.checkOption),
  );
  const changeFunctionCheck = (value, row) => {
    if (!value) row.checkOption = false;
  };
  const changePrivilegeType = () => {
    isFunctionAll.value = false;
    isGrantAll.value = false;
  };

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
    grantOrRevoke.value = 'GRANT';
    tableList.value = getTableList(props.type);
  };

  const setBackfill = async ({ privilegeOption, grantOrRevoke: grant }) => {
    grantOrRevoke.value = grant;
    setTimeout(() => {
      privilegeOption?.forEach((item) => {
        const target = tableList.value.find((row) => row.privilege == item.privilege);
        if (target) {
          target.checkPrivilege = !!item.checkPrivilege;
          target.checkOption = !!item.checkOption;
        }
      });
    }, 500);
  };
  defineExpose({
    formRef,
    validateForm,
    resetFields,
    getPrivilegeList: () => tableList.value.filter((x) => x.checkPrivilege || x.checkOption),
    setBackfill,
  });
</script>

<style lang="scss" scoped>
  .rule-form,
  :deep(.el-row),
  :deep(.el-col) {
    height: 100%;
    overflow: hidden;
  }
  .alone-form-item {
    :deep(.el-form-item__content) {
      align-items: flex-start;
    }
  }
  .table-wrapper {
    width: 100%;
    height: calc(100% - 40px);
  }
  .table-checkbox-label {
    display: inline-block;
    margin-left: 12px;
    vertical-align: text-bottom;
  }
</style>
