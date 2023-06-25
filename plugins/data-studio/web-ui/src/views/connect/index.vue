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
        :height="360"
        highlight-current-row
        @current-change="handleCurrentChange"
      >
        <el-table-column align="center" prop="name" :label="$t('connection.name')" width="160">
          <template #header>
            <div v-if="showNameFilter" class="flex-header">
              <div style="word-break: keep-all; margin-right: 5px">
                {{ $t('connection.name') }}
              </div>
              <div class="flex-header">
                <el-icon @click="hideNamefilter" class="icon-pointer">
                  <Search />
                </el-icon>
                <el-input class="border-bottom-input" v-model="nameFilterInput" clearable />
              </div>
            </div>
            <div v-else class="flex-header">
              <div style="width: 12px"></div>
              <span>{{ $t('connection.name') }}</span>
              <el-icon @click="showNameFilter = true" class="icon-pointer">
                <Search />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="connectInfo" :label="$t('connection.info')">
          <template #header>
            <div v-if="showInfoFilter" class="flex-header">
              <div style="word-break: keep-all; margin-right: 5px">
                {{ $t('connection.info') }}
              </div>
              <div class="flex-header">
                <el-icon @click="hideInfofilter" class="icon-pointer">
                  <Search />
                </el-icon>
                <el-input class="border-bottom-input" v-model="infoFilterInput" clearable />
              </div>
            </div>
            <div v-else class="flex-header">
              <div style="width: 12px"></div>
              <span>{{ $t('connection.info') }}</span>
              <el-icon @click="showInfoFilter = true" class="icon-pointer">
                <Search />
              </el-icon>
            </div>
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
      <el-form :model="form" ref="ruleFormRef" :rules="rules" label-width="95px">
        <el-form-item prop="type" :label="$t('connection.databaseType')">
          <el-select
            v-model="form.type"
            disabled
            :placeholder="$t('connection.databaseType_holder')"
            style="width: 100%"
          >
            <el-option label="openGauss" value="shanghai" />
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
      </el-form>
    </div>
    <template #footer>
      <div class="dialog-space-footer">
        <el-button
          type="danger"
          @click="handleDelete"
          :disabled="connectListInfo.listCurrentRow?.sourceType != 2"
          class="footer-left"
        >
          {{ $t('connection.deleteInfo') }}
        </el-button>
        <span class="footer-right">
          <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
          <el-button type="primary" @click="resetForm(ruleFormRef)">
            {{ $t('button.reset') }}
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
  } from '@/api/connect';
  import { ElMessage, ElMessageBox, ElTable, FormInstance, FormRules } from 'element-plus';
  import { ArrowDown, View, Hide, Search } from '@element-plus/icons-vue';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { useI18n } from 'vue-i18n';
  import { useUserStore } from '@/store/modules/user';
  import Crypto from '@/utils/crypto';
  import { connectListPersist } from '@/config';

  const { t } = useI18n();
  const UserStore = useUserStore();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: 'create' | 'edit';
      connectInfo: {
        id: string | number;
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
  const dialogWidth = ref(1000);
  const title = ref(t('connection.new'));
  const ruleFormRef = ref<FormInstance>();
  const tableRef = ref<InstanceType<typeof ElTable>>();
  const isShowPwd = ref(false);
  const showNameFilter = ref(false);
  const showInfoFilter = ref(false);
  const nameFilterInput = ref('');
  const infoFilterInput = ref('');
  const sourceFilterInput = ref(0);

  const form = reactive({
    type: 'openGauss', // default
    name: '',
    ip: '',
    port: 5432,
    dataName: 'postgres', // default
    userName: '',
    password: '',
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
      { min: 1, max: 30, message: t('rules.charLength', 30), trigger: 'blur' },
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

  const getTableList = () => {
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

  const hideNamefilter = () => {
    showNameFilter.value = false;
    nameFilterInput.value = '';
  };
  const hideInfofilter = () => {
    showInfoFilter.value = false;
    infoFilterInput.value = '';
  };

  const handleOpen = async () => {
    const formEl = ruleFormRef.value;
    formEl.resetFields();
    if (props.type === 'create') {
      title.value = t('connection.new');
      getTableList();
    } else {
      title.value = t('connection.edit');
      const infoData = await requestConnectInfo();
      Object.keys(infoData).map((key) => {
        if (form[key] !== undefined) form[key] = infoData[key];
        if (key === 'port') form[key] = Number(infoData[key]) || null;
      });
      const currentConnectInfo = props.connectInfo;
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
  const handleDelete = () => {
    const index = JSON.parse(
      connectListPersist.storage.getItem(connectListPersist.key) || '[]',
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

  const confirmForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async (valid) => {
      if (valid) await requestConnect();
    });
  };
  const resetForm = (formEl: FormInstance | undefined) => {
    Object.assign(connectListInfo, {
      listCurrentRow: {},
    });
    showNameFilter.value = false;
    showInfoFilter.value = false;
    infoFilterInput.value = '';
    nameFilterInput.value = '';
    sourceFilterInput.value = 0;
    if (!formEl) return;
    Object.keys(form).map((key) => {
      const excludeKeys = props.type === 'create' ? ['type'] : ['type', 'name'];
      if (key == 'port') {
        form[key] = null;
      } else if (!excludeKeys.includes(key)) {
        form[key] = '';
      }
    });
    formEl.clearValidate();
  };
  const requestConnect = async () => {
    const params = {
      ...form,
      port: String(form.port),
      password: Crypto.encrypt(form.password),
      webUser: UserStore.userId,
      connectionid: props.uuid || undefined,
    };
    const data =
      props.type === 'create' ? await createConnect(params) : await updateConnect(params);
    EventBus.notify(EventTypeName.GET_DATABASE_LIST, data);
    ElMessage({
      message:
        props.type === 'create' ? t('connection.success.create') : t('connection.success.edit'),
      type: 'success',
    });
    handleClose();
  };
  const requestConnectInfo = async () => {
    const data = await getDatabaseAttr(props.connectInfo.id);
    return data;
  };

  const handleCurrentChange = (currentRow) => {
    if (!currentRow) return;
    if (props.type == 'edit') return;
    connectListInfo.listCurrentRow = currentRow || {};
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
      margin-left: 20px;
      color: #808080;
      font-style: italic;
    }
  }
  .el-input__icon {
    cursor: pointer;
    font-size: 14px;
  }
  :deep(.el-input) {
    height: 30px;
  }
  .dialog_body {
    display: flex;
  }
  .flex-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .icon-pointer {
    cursor: pointer;
    :hover {
      color: var(--normal-color);
    }
  }
  .border-bottom-input {
    box-shadow: none;
    height: auto;
    :deep(.el-input__wrapper) {
      box-shadow: none;
      .el-input__inner {
        box-shadow: 0 1px 0 0 var(--el-input-border-color);
      }
    }
  }
  .dialog-space-footer {
    display: flex;
    justify-content: space-between;
    padding-right: 80px;
  }
  :deep(.el-table) {
    width: 580px;
    .el-table__cell {
      .cell {
        padding: 0 8px;
      }
    }
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
