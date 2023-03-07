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
        :data="connectListInfo.list"
        border
        :height="360"
        highlight-current-row
        @current-change="handleCurrentChange"
      >
        <el-table-column align="center" prop="name" :label="$t('connection.name')" width="160" />
        <el-table-column align="center" prop="connectInfo" :label="$t('connection.info')" />
        <el-table-column
          align="center"
          prop="clusterRole"
          :label="$t('connection.version')"
          width="140"
        />
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
          <el-input v-model="form.name" :disabled="props.type == 'edit'" />
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
      <span class="dialog-footer">
        <el-button @click="handleClose">{{ $t('button.cancel') }}</el-button>
        <el-button type="primary" @click="resetForm(ruleFormRef)">
          {{ $t('button.clear') }}
        </el-button>
        <el-button type="primary" @click="confirmForm(ruleFormRef)">
          {{ $t('button.confirm') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
  import { computed, ref, reactive } from 'vue';
  import {
    createConnect,
    updateConnect,
    getDatabaseAttr,
    getAllCluster,
    getDataLinkList,
  } from '@/api/connect';
  import { ElMessage, ElTable, FormInstance, FormRules } from 'element-plus';
  import { View, Hide } from '@element-plus/icons-vue';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { useI18n } from 'vue-i18n';
  import { useAppStore } from '@/store/modules/app';
  import { useUserStore } from '@/store/modules/user';
  import Crypto from '@/utils/crypto';

  const { t } = useI18n();
  const AppStore = useAppStore();
  const UserStore = useUserStore();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: 'create' | 'edit';
      connectInfo: {
        id: string | number;
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
  const dialogWidth = ref(950);
  const title = ref(t('connection.new'));
  const ruleFormRef = ref<FormInstance>();
  const tableRef = ref<InstanceType<typeof ElTable>>();
  const isShowPwd = ref(false);

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
      { min: 1, max: 30, message: t('rules.charLength', 30), trigger: 'blur' },
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
    listCurrentRow: {},
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
          });
          list = list.concat(item.clusterNodes);
        }
      });
    } else {
      data.forEach((item) => {
        item.connectInfo = `${item.userName}@${item.ip}:${item.port}/${item.dataName}`;
      });
      list = data;
    }
    return list;
  };

  const handleOpen = async () => {
    const formEl = ruleFormRef.value;
    formEl.resetFields();
    connectListInfo.list = [];
    if (props.type === 'create') {
      title.value = t('connection.new');
      getAllCluster()
        .then((res) => {
          connectListInfo.list = connectListInfo.list.concat(doConnectList(res.data || [], 1));
        })
        .finally(() => {
          getDataLinkList(UserStore.userId).then((res) => {
            connectListInfo.list = connectListInfo.list.concat(doConnectList(res || [], 2));
          });
        });
    } else {
      title.value = t('connection.edit');
      const infoData = await requestConnectInfo();
      Object.keys(infoData).map((key) => {
        if (form[key] !== undefined) form[key] = infoData[key];
        if (key === 'port') form[key] = Number(infoData[key]) || null;
      });
      const currentConnectInfo = AppStore.currentConnectInfo;
      connectListInfo.list = [
        {
          name: currentConnectInfo.name,
          connectInfo: `${currentConnectInfo.userName}@${currentConnectInfo.ip}:${currentConnectInfo.port}/${currentConnectInfo.dataName}`,
          clusterRole: '',
        },
      ];
    }
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
    resetForm(ruleFormRef.value);
  };
  const confirmForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async (valid) => {
      if (valid) await requestConnect();
    });
  };
  const resetForm = (formEl: FormInstance | undefined) => {
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
  :deep(.el-table) {
    width: 510px;
  }
  :deep(.el-input-number--small) {
    .el-input-number__decrease,
    .el-input-number__increase {
      height: 14px !important;
    }
  }
</style>
