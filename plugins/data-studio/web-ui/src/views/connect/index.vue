<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="dialogWidth"
    align-center
    :close-on-click-modal="false"
    @open="handleOpen"
    @close="handleClose"
  >
    <div class="dialog_body">
      <el-table
        ref="tableRef"
        :data="tableList"
        border
        :height="430"
        highlight-current-row
        @current-change="handleCurrentChange"
      >
        <el-table-column align="center" prop="name" :label="$t('connection.name')" width="160">
          <template #header>
            <FilterTableDataHeaderSlot v-model="nameFilterInput" v-model:show="showNameFilter">
              {{ $t('connection.name') }}
            </FilterTableDataHeaderSlot>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="connectInfo" :label="$t('connection.info')">
          <template #header>
            <FilterTableDataHeaderSlot v-model="infoFilterInput" v-model:show="showInfoFilter">
              {{ $t('connection.info') }}
            </FilterTableDataHeaderSlot>
          </template>
        </el-table-column>
        <el-table-column
          align="center"
          prop="edition"
          :label="$t('connection.version')"
          width="90"
        />
        <el-table-column align="center" prop="sourceName" width="105" class-name="source-column">
          <template #header>
            <el-dropdown trigger="click" popper-class="active-dropdown">
              <div style="cursor: pointer">
                {{
                  sourceFilterInput == 0
                    ? $t('connection.source')
                    : sourceFilterInput == 1
                    ? $t('connection.contentCenter')
                    : $t('connection.customConnection')
                }}
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    :class="{ active: sourceFilterInput == 0 }"
                    @click="sourceFilterInput = 0"
                  >
                    {{ $t('common.all') }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    :class="{ active: sourceFilterInput == 1 }"
                    @click="sourceFilterInput = 1"
                  >
                    {{ $t('connection.contentCenter') }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    :class="{ active: sourceFilterInput == 2 }"
                    @click="sourceFilterInput = 2"
                  >
                    {{ $t('connection.customConnection') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="auto" label-position="left">
        <el-form-item prop="type" :label="$t('connection.databaseType')">
          <el-select
            v-model="form.type"
            disabled
            :placeholder="$t('connection.databaseType_holder')"
            placement="bottom"
            style="width: 100%"
          >
            <el-option label="openGauss" value="openGauss" />
          </el-select>
        </el-form-item>
        <el-form-item prop="name" :label="$t('connection.name')">
          <el-input v-model="form.name" :disabled="props.type == 'edit'" maxlength="60" />
        </el-form-item>
        <el-form-item prop="ip" :label="$t('connection.host')">
          <el-input v-model="form.ip" />
        </el-form-item>
        <el-form-item prop="port" class="port" :label="$t('connection.port')">
          <el-input-number
            v-model="form.port"
            :min="1"
            :max="65535"
            :step="1"
            step-strictly
            controls-position="right"
            style="width: 110px"
          />
          <span>{{ $t('connection.maximum') }}: 65535</span>
        </el-form-item>
        <el-form-item prop="dataName" :label="$t('connection.database')">
          <el-input v-model="form.dataName" />
        </el-form-item>
        <el-form-item prop="userName" :label="$t('connection.username')">
          <el-input v-model="form.userName" />
        </el-form-item>
        <el-form-item prop="password" :label="$t('connection.password')">
          <el-input
            v-model="form.password"
            :type="isShowPwd ? 'text' : 'password'"
            @keyup.enter="confirmForm(ruleFormRef)"
          >
            <template #suffix>
              <el-icon class="el-input__icon" @click="isShowPwd = !isShowPwd">
                <View v-if="isShowPwd" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="isRememberPassword" :label="$t('connection.savePassword')">
          <el-radio-group v-model="form.isRememberPassword">
            <el-radio value="y">{{ $t('connection.currentSessionOnly') }}</el-radio>
            <el-radio value="n">{{ $t('connection.doNotSave') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <div class="dialog-space-footer">
        <span class="footer-left">
          <el-button
            type="danger"
            @click="handleDelete"
            :disabled="connectListInfo.listCurrentRow?.sourceType != 2"
            class="footer-left"
          >
            {{ $t('connection.deleteInfo') }}
          </el-button>
          <span v-if="testConnectionStatus == 'success'" class="connection-tips connection-success">
            <el-icon><CircleCheckFilled /></el-icon>{{ $t('message.testConnectionSuccessTips') }}
          </span>
          <span v-if="testConnectionStatus == 'fail'" class="connection-tips connection-fail">
            <el-icon><CircleCloseFilled /></el-icon>{{ $t('message.testConnectionFailTips') }}
          </span>
        </span>
        <span class="footer-right">
          <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
          <el-button @click="resetForm(ruleFormRef)">
            {{ $t('button.reset') }}
          </el-button>
          <el-button type="primary" @click="testConnection(ruleFormRef)">
            {{ $t('connection.testConnection') }}
          </el-button>
          <el-button type="primary" @click="confirmForm(ruleFormRef)">
            {{ $t('button.confirm') }}
          </el-button>
        </span>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup name="ConnectDialog">
  import {
    createConnect,
    updateConnect,
    getDatabaseAttr,
    getAllCluster,
    getDataLinkList,
    deleteDataLinkList,
    testConnectionApi,
  } from '@/api/connect';
  import { ElMessage, ElMessageBox, ElTable, FormInstance, FormRules } from 'element-plus';
  import FilterTableDataHeaderSlot from '@/components/FilterTableDataHeaderSlot.vue';
  import { ArrowDown, View, Hide } from '@element-plus/icons-vue';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { useI18n } from 'vue-i18n';
  import { useUserStore } from '@/store/modules/user';
  import Crypto from '@/utils/crypto';
  import { connectListPersist } from '@/config';
  import { sidebarForage } from '@/utils/localforage';
  import { getSystemUserProfile } from '@/api/connect';

  const { t } = useI18n();
  const UserStore = useUserStore();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: 'create' | 'edit';
      connectInfo: {
        id: string | number;
        isRememberPassword: 'y' | 'n';
        [props: string]: any;
      };
      uuid?: string;
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
  const dialogWidth = ref(1100);
  const title = ref(t('connection.new'));
  const ruleFormRef = ref<FormInstance>();
  const tableRef = ref<InstanceType<typeof ElTable>>();
  const isShowPwd = ref(false);
  const showNameFilter = ref(false);
  const showInfoFilter = ref(false);
  const nameFilterInput = ref('');
  const infoFilterInput = ref('');
  const sourceFilterInput = ref(0);
  const testConnectionStatus = ref<'none' | 'success' | 'fail'>('none');

  const form = reactive({
    type: 'openGauss', // default
    name: '',
    ip: '',
    port: 5432,
    dataName: 'postgres', // default
    userName: '',
    password: '',
    isRememberPassword: 'y',
    id: null,
    webUser: '',
  });
  const rules = reactive<FormRules>({
    name: [
      { required: true, message: t('rules.empty', [t('connection.name')]), trigger: 'blur' },
      { min: 1, max: 60, message: t('rules.charLength', 60), trigger: 'blur' },
    ],
    ip: [
      { required: true, message: t('rules.empty', [t('connection.host')]), trigger: 'blur' },
      { min: 1, max: 130, message: t('rules.charLength', 130), trigger: 'blur' },
      {
        message: t('connection.rules.host[0]'),
        trigger: 'blur',
        pattern: new RegExp('^(?:' +
            '(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|' +
            '([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|' +
            '([0-9a-fA-F]{1,4}:){1,7}:|' +
            '::([0-9a-fA-F]{1,4}:){1,6}[0-9a-fA-F]{1,4}|' +
            '([0-9a-fA-F]{1,4}:){1,6}:([0-9a-fA-F]{1,4})|' +
            '([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|' +
            '([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|' +
            '([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|' +
            '([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|' +
            '[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){1,6}|' +
            '::([0-9a-fA-F]{1,4}){1,7}|' +
            '::ffff:(\\d{1,3}\\.){3}\\d{1,3}' +
            ')$', 'g'),
      },
    ],
    port: [
      { required: true, message: t('rules.empty', [t('connection.port')]), trigger: 'blur' },
      {
        message: t('connection.rules.port[0]'),
        trigger: 'blur',
        pattern: /^[0-9]+$/,
      },
    ],
    dataName: [
      { required: true, message: t('rules.empty', [t('connection.database')]), trigger: 'blur' },
      { min: 1, max: 30, message: t('rules.charLength', 30), trigger: 'blur' },
    ],
    userName: [
      { required: true, message: t('rules.empty', [t('connection.username')]), trigger: 'blur' },
      { min: 1, max: 30, message: t('rules.charLength', 30), trigger: 'blur' },
    ],
    password: [
      { required: true, message: t('rules.empty', [t('connection.password')]), trigger: 'blur' },
      { min: 1, max: 30, message: t('rules.charLength', 30), trigger: 'blur' },
    ],
  });
  const connectListInfo = reactive({
    list: [],
    listCurrentRow: {} as any,
  });

  // type: 1 platform  2 ourself
  const doConnectList = (data, type: 1 | 2) => {
    let list = [];
    if (type == 1) {
      data.forEach((item) => {
        if (Array.isArray(item.clusterNodes)) {
          item.clusterNodes.forEach((node) => {
            node.name = node.nodeId;
            node.ip = node.publicIp;
            node.port = node.dbPort;
            node.dataName = node.dbName;
            node.userName = node.dbUser;
            node.password = '';
            node.connectInfo = `${node.publicIp}:${node.dbPort}/${node.dbName}`;
            node.sourceType = type;
            node.sourceName = t('connection.contentCenter');
            node.edition = `${item.version} ${item.versionNum}`;
          });
          list = list.concat(item.clusterNodes);
        }
      });
    } else {
      data.forEach((item) => {
        item.connectInfo = `${item.userName}@${item.ip}:${item.port}/${item.dataName}`;
        item.sourceType = type;
        item.sourceName = t('connection.customConnection');
      });
      list = data;
    }
    return list;
  };

  const getUserId = async () => {
    if (!UserStore.userId) {
      const res = await getSystemUserProfile();
      UserStore.userId = res.data.userId;
    }
  };

  const getTableList = () => {
    testConnectionStatus.value = 'none';
    Object.assign(connectListInfo, {
      list: [],
      listCurrentRow: {},
    });
    getAllCluster()
      .then((res) => {
        connectListInfo.list = connectListInfo.list.concat(doConnectList(res || [], 1));
      })
      .finally(() => {
        getDataLinkList(UserStore.userId).then((res) => {
          connectListInfo.list = connectListInfo.list.concat(doConnectList(res || [], 2));
        });
      });
  };

  const tableList = computed(() => {
    return connectListInfo.list
      .filter((item) => item.name.indexOf(nameFilterInput.value) > -1)
      .filter((item) => item.connectInfo.indexOf(infoFilterInput.value) > -1)
      .filter((item) =>
        sourceFilterInput.value == 0 ? true : item.sourceType == sourceFilterInput.value,
      );
  });

  const handleOpen = async () => {
    const formEl = ruleFormRef.value;
    formEl.resetFields();
    if (props.type === 'create') {
      title.value = t('connection.new');
      await getUserId();
      getTableList();
    } else {
      title.value = t('connection.edit');
      const infoData = await requestConnectInfo();
      Object.keys(infoData).map((key) => {
        if (form[key] !== undefined) form[key] = infoData[key];
        if (key === 'port') form[key] = Number(infoData[key]) || null;
      });
      const currentConnectInfo = props.connectInfo;
      form.isRememberPassword = currentConnectInfo.isRememberPassword;
      connectListInfo.list = [
        {
          name: currentConnectInfo.name,
          connectInfo: `${currentConnectInfo.userName}@${currentConnectInfo.ip}:${currentConnectInfo.port}/${currentConnectInfo.dataName}`,
          clusterRole: '',
          edition: currentConnectInfo.edition,
          sourceType: 2,
          sourceName: t('connection.customConnection'),
        },
      ];
    }
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    resetForm(ruleFormRef.value);
  };
  const handleDelete = async () => {
    const index = (
      ((await sidebarForage.getItem(connectListPersist.key)) as any[]) || []
    ).findIndex((list) => list.id == connectListInfo.listCurrentRow.id);
    if (index > -1) {
      ElMessage.warning(t('message.deleteConnectInfo0'));
    } else {
      ElMessageBox.confirm(t('message.deleteConnectInfo1')).then(async () => {
        await deleteDataLinkList(connectListInfo.listCurrentRow.id);
        ElMessage.success(t('message.deleteSuccess'));
        getTableList();
      });
    }
  };

  const getConnectionParams = () => {
    // common params
    const params = {
      id: form.id,
      type: form.type,
      name: form.name,
      webUser: UserStore.userId,
      connectionid: props.uuid || undefined,
    };
    // customized params
    Object.assign(params, {
      ip: form.ip,
      port: String(form.port),
      dataName: form.dataName,
      userName: form.userName,
      password: Crypto.encrypt(form.password),
      isRememberPassword: form.isRememberPassword,
    });
    return params;
  };

  const testConnection = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async (valid) => {
      if (valid) {
        const params = getConnectionParams();
        try {
          const time = await testConnectionApi(params);
          ElMessage.success(t('message.testConnectionSuccess', { time }));
          testConnectionStatus.value = 'success';
        } catch {
          testConnectionStatus.value = 'fail';
        }
      }
    });
  };
  const confirmForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async (valid) => {
      if (valid) await requestConnect();
    });
  };
  const resetForm = (formEl: FormInstance | undefined) => {
    showNameFilter.value = false;
    showInfoFilter.value = false;
    infoFilterInput.value = '';
    nameFilterInput.value = '';
    sourceFilterInput.value = 0;
    testConnectionStatus.value = 'none';
    if (!formEl) return;
    Object.keys(form).map((key) => {
      const excludeKeys = props.type === 'create' ? ['type'] : ['type', 'name'];
      if (key == 'port') {
        form[key] = null;
      } else if (key == 'isRememberPassword') {
        form[key] = 'y';
      } else if (!excludeKeys.includes(key)) {
        form[key] = '';
      }
    });
    formEl.clearValidate();
  };
  const requestConnect = async () => {
    const params = getConnectionParams();
    const data =
      props.type === 'create' ? await createConnect(params) : await updateConnect(params);
    EventBus.notify(EventTypeName.GET_CONNECTION_LIST, data);
    ElMessage({
      message:
        props.type === 'create' ? t('connection.success.create') : t('connection.success.edit'),
      type: 'success',
    });
    handleClose();
  };
  const requestConnectInfo = async () => {
    const data = await getDatabaseAttr({
      id: props.connectInfo.id,
      webUser: UserStore.userId,
    });
    return data;
  };

  const handleCurrentChange = (currentRow) => {
    if (!currentRow) return;
    if (props.type == 'edit') return;
    connectListInfo.listCurrentRow = currentRow || {};
    testConnectionStatus.value = 'none';
    Object.assign(form, {
      id: currentRow.id,
      name: currentRow.name,
      ip: currentRow.ip,
      port: currentRow.port ? Number(currentRow.port) : null,
      dataName: currentRow.dataName,
      userName: currentRow.userName,
      password: currentRow.password,
    });
  };
</script>

<style lang="scss" scoped>
  .port {
    .el-input {
      width: 180px;
    }
    span {
      margin-left: 5px;
      color: #808080;
      font-style: italic;
      font-size: 12px;
    }
  }
  .el-input__icon {
    cursor: pointer;
    font-size: 14px;
  }
  .dialog_body {
    display: flex;
  }
  .dialog-space-footer {
    display: flex;
    justify-content: space-between;
    padding-right: 20px;
  }
  .connection-tips {
    margin-left: 5px;
    vertical-align: sub;
    &.connection-success {
      color: var(--el-color-success);
    }
    &.connection-fail {
      color: var(--el-color-error);
    }
    :deep(.el-icon) {
      margin-right: 3px;
      vertical-align: sub;
      font-size: 17px;
    }
  }
  :deep(.el-table) {
    flex: 1;
    .el-table__cell {
      .cell {
        padding: 0 5px;
      }
    }
  }
  :deep(.el-form) {
    margin-left: 5px;
    width: 300px;
  }
  :deep(.source-column) {
    .cell {
      word-break: break-word;
      .el-dropdown {
        display: inline;
        color: var(--el-table-header-text-color);
      }
    }
  }
  :deep(.el-input-number) {
    .el-input-number__increase {
      height: 13px !important;
      line-height: 13px !important;
      top: 3px;
    }
  }
  :deep(.el-input-number--small) {
    .el-input-number__decrease,
    .el-input-number__increase {
      height: 14px !important;
    }
  }
</style>
