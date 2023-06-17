<template>
  <a-modal
    :unmount-on-close="true"
    title="批量安装portal"
    v-model:visible="visible"
    width="95vw"
    :mask-closable="false"
    :esc-to-close="false"
  >
    <a-form :model="globalData" layout="inline" auto-label-width="">
      <a-form-item label="用户名">
        <a-select v-model="globalData.hostUserName">
          <a-option
            v-for="item in globalData.interHostUserList"
            :key="item"
            :label="item"
            :value="item"
          />
        </a-select>
      </a-form-item>
      <a-form-item label="安装路径">
        <a-input v-model="globalData.installPath" />
      </a-form-item>
      <a-form-item label="jar名称">
        <a-input v-model="globalData.jarName" />
      </a-form-item>
      <a-form-item label="包名称">
        <a-input v-model="globalData.pkgName" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleBatchSet">统一设置</a-button>
      </a-form-item>
    </a-form>
    <a-form :model="form" ref="formRef" class="portal-table">
      <a-table
        :columns="columns"
        :data="form.tableData"
        :pagination="{ pageSize: 1000 }"
        class="portal-table"
      >
        <template #hostUser="{ rowIndex }">
          <a-form-item
            hide-asterisk
            :field="`tableData.${rowIndex}.hostUserId`"
            :rules="{ required: true, message: '请选择安装用户' }"
          >
            <a-select v-model="form.tableData[rowIndex].hostUserId">
              <a-option
                v-for="item in form.tableData[rowIndex].hostUserList"
                :label="item.username"
                :value="item.hostUserId"
                :key="item.hostUserId"
              >
              </a-option>
            </a-select>
          </a-form-item>
        </template>
        <template #installPath="{ rowIndex }">
          <a-form-item
            hide-asterisk
            :field="`tableData.${rowIndex}.installPath`"
            :rules="{ required: true, message: '请输入安装路径' }"
          >
            <a-input v-model="form.tableData[rowIndex].installPath"></a-input>
          </a-form-item>
        </template>
        <template #jarName="{ rowIndex }">
          <a-form-item
            hide-asterisk
            :field="`tableData.${rowIndex}.jarName`"
            :rules="{ required: true, message: '请输入jar名称' }"
          >
            <a-input v-model="form.tableData[rowIndex].jarName"></a-input>
          </a-form-item>
        </template>
        <template #pkgName="{ rowIndex }">
          <a-form-item
            hide-asterisk
            :field="`tableData.${rowIndex}.pkgName`"
            :rules="{ required: true, message: '请输入安装包名称' }"
          >
            <a-input v-model="form.tableData[rowIndex].pkgName"></a-input>
          </a-form-item>
        </template>
        <template #op="{ rowIndex }">
          <icon-delete class="del-icon" @click="handleDeleteRow(rowIndex)" />
        </template>
      </a-table>
    </a-form>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="cancel" v-if="!loading">{{
          $t('components.PortalInstall.5q0aajl77ik0')
        }}</a-button>
        <a-button
          type="primary"
          :disabled="loading"
          style="margin-left: 16px"
          @click="confirmSubmit"
          :loading="loading"
          >{{ $t('components.PortalInstall.5q0aajl77lg0') }}</a-button
        >
      </div>
    </template>
  </a-modal>
</template>
  
  <script setup>
import { reactive, ref, watch, onMounted } from 'vue'
// import { Message } from '@arco-design/web-vue'
import {
  hostUsers,
  installPortal,
  deletePortal,
  listHostUserByHostIds,
  installPortalFromDatakit,
} from '@/api/task'
import { getSysSetting } from '@/api/common'
import {
  INSTALL_TYPE,
  PORTAL_INSTALL_STATUS,
  PORTAL_PARAM_TYPE,
} from '@/utils/constants'
import { useI18n } from 'vue-i18n'
import { Message } from '@arco-design/web-vue'
import { getToken } from '@/utils/auth'

const { t } = useI18n()

const props = defineProps({
  open: Boolean,
  hostList: Array,
})

const emits = defineEmits(['update:open', 'startInstall'])

const visible = ref(false)
const loading = ref(false)
const form = reactive({
  tableData: [],
})
const formRef = ref()
const globalData = reactive({
  hostUserName: '',
  interHostUserList: [],
  jarName: '',
  pkgName: '',
  installPath: '~',
})
const columns = [
  {
    title: 'IP',
    dataIndex: 'publicIp',
  },
  {
    title: '用户名',
    dataIndex: 'hostUser',
    slotName: 'hostUser',
  },
  {
    title: '安装路径',
    dataIndex: 'installPath',
    slotName: 'installPath',
  },
  {
    title: 'jar名称',
    dataIndex: 'jarName',
    slotName: 'jarName',
  },
  {
    title: '包名称',
    dataIndex: 'pkgName',
    slotName: 'pkgName',
  },
  {
    title: '操作',
    slotName: 'op',
  },
]

watch(visible, (v) => {
  emits('update:open', v)
})

watch(
  () => props.open,
  async (v) => {
    if (v) {
      const data = [...props.hostList]
      await buildTableData(data)
      form.tableData = data
    }
    visible.value = v
  }
)

const cancel = () => {
  visible.value = false
}

const buildTableData = async (data) => {
  const hostIds = []

  const sysSetting = await getSysSetting()
  data.forEach((item) => {
    hostIds.push(item.hostId)
    item.pkgName = sysSetting.data.portalPkgName
    item.jarName = sysSetting.data.portalJarName
    item.installPath = '~'
  })
  globalData.pkgName = sysSetting.data.portalPkgName
  globalData.jarName = sysSetting.data.portalJarName

  const users = await listHostUserByHostIds(hostIds.join(','))
  if (users.data.length > 0) {
    data.forEach((item) => {
      const targetUsers = users.data.filter(
        (each) => each.hostId === item.hostId
      )
      item.hostUserList = targetUsers
      if (targetUsers.length > 0) {
        item.hostUserId = targetUsers[0].hostUserId
      }
    })
  }
  buildInterUserList(data)
}

const confirmSubmit = (e) => {
  e.stopPropagation()
  formRef.value.validate((validResult) => {
    if (validResult) {
      return
    }
    const requestList = []
    const reqHostIdList = []
    form.tableData.map((item) => {
      const reqParams = buildReqData(item)
      requestList.push(installPortalFromDatakit(item.hostId, reqParams))
      reqHostIdList.push(item.hostId)
    })
    loading.value = true
    Promise.all(requestList)
      .then((res) => {
        emits('startInstall', reqHostIdList)
        visible.value = false
      })
      .finally(() => {
        loading.value = false
      })
  })
}

const handleBatchSet = () => {
  form.tableData.map((item) => {
    item.jarName = globalData.jarName
    item.pkgName = globalData.pkgName
    item.installPath = globalData.installPath
    const result = item.hostUserList.find(
      (each) => each.username === globalData.hostUserName
    )
    if (result) {
      item.hostUserId = result.hostUserId
    } else {
      item.hostUserId = ''
    }
  })
}

const buildReqData = (data) => {
  const params = new FormData()
  params.append('runHostId', data.hostId)
  params.append('installPath', data.installPath)
  params.append('hostUserId', data.hostUserId)
  params.append('installType', INSTALL_TYPE.OFFLINE)
  // params.append('pkgDownloadUrl', form.pkgDownloadUrl)
  params.append('jarName', data.jarName)
  params.append('pkgName', data.pkgName)
  return params
}

const buildInterUserList = (data) => {
  const allUserDoubleArray = []
  data.map((item) => {
    const usernameList = []
    if (item.hostUserList.length > 0) {
      item.hostUserList.map((user) => {
        usernameList.push(user.username)
      })
    }
    if (usernameList.length > 0) {
      allUserDoubleArray.push(usernameList)
    }
  })
  const result = unionFunc(allUserDoubleArray)
  if (result.size > 0) {
    globalData.interHostUserList = [...result]
  }
}

const handleDeleteRow = (rowIndex) => {
  form.tableData.splice(rowIndex, 1)
}

const unionFunc = (arr) => {
  let result = new Set()
  arr.map((item) => {
    result = new Set([...result, ...new Set(item)])
  })
  return result
}
onMounted(() => {
  visible.value = props.open
})
</script>
  
<style lang="less" scoped>
.add-portal-modal {
  .modal-footer {
    text-align: center;
  }
}
:deep(.arco-table-td .arco-form-item) {
  margin-bottom: 0;
}
:deep(.arco-table-td .arco-form-item-label-col) {
  display: none;
}

.del-icon {
  color: rgb(var(--primary-6));
  cursor: pointer;
}
.portal-table {
  min-height: 400px;
}
</style>
  