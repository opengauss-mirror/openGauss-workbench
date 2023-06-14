<template>
  <a-modal
    :unmount-on-close="true"
    title="批量安装portal"
    v-model:visible="visible"
    width="80vw"
    modal-class="add-portal-modal"
    :mask-closable="false"
    :esc-to-close="false"
  >
    <a-form :model="globalData" auto-label-width>
      <a-row :gutter="10">
        <a-col :span="5">
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
        </a-col>
        <a-col :span="5">
          <a-form-item label="安装路径">
            <a-input v-model="globalData.installPath" />
          </a-form-item>
        </a-col>
        <a-col :span="5">
          <a-form-item label="jar名称">
            <a-input v-model="globalData.jarName" />
          </a-form-item>
        </a-col>
        <a-col :span="5">
          <a-form-item label="包名称">
            <a-input v-model="globalData.pkgName" />
          </a-form-item>
        </a-col>
        <a-col :span="3">
          <a-form-item>
            <a-button type="primary" @click="handleBatchSet">统一设置</a-button>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
    <a-table :columns="columns" :data="tableData">
      <template #hostUser="{ rowIndex }">
        <a-select v-model="tableData[rowIndex].hostUserId">
          <a-option
            v-for="item in tableData[rowIndex].hostUserList"
            :label="item.username"
            :value="item.hostUserId"
            :key="item.hostUserId"
          >
          </a-option>
        </a-select>
      </template>
      <template #installPath="{ rowIndex }">
        <a-input v-model="tableData[rowIndex].installPath"></a-input>
      </template>
      <template #jarName="{ rowIndex }">
        <a-input v-model="tableData[rowIndex].jarName"></a-input>
      </template>
      <template #pkgName="{ rowIndex }">
        <a-input v-model="tableData[rowIndex].pkgName"></a-input>
      </template>
    </a-table>
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
import { INSTALL_TYPE, PORTAL_INSTALL_STATUS, PORTAL_PARAM_TYPE } from '@/utils/constants'
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
const tableData = ref([])
const globalData = reactive({
  hostUserName: '',
  interHostUserList: [],
  jarName: '',
  pkgName: '',
  installPath: '~'
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
      tableData.value = data
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
  const requestList = []
  tableData.value.map(item => {
    const reqParams = buildReqData(item)
    requestList.push(installPortalFromDatakit(item.hostId, reqParams))
  })
  loading.value = true
  Promise.all(requestList).then(res => {
    loading.value = false
    visible.value = false
  })
}

const handleBatchSet = () => {
  tableData.value.map((item) => {
    item.jarName = globalData.jarName
    item.pkgName = globalData.pkgName
    const result = item.hostUserList.find(
      (each) => each.username === globalData.hostUserName
    )
    if (result) {
      item.hostUserId = result.hostUserId
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
  const result = interFunc(allUserDoubleArray)
  if (result.length > 0) {
    globalData.interHostUserList = result
  }
}

const interFunc = (arr) => {
  return arr.reduce((data, item) => {
    return data.filter((i) => {
      return item.some((j) => {
        return i === j
      })
    })
  })
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
</style>
  