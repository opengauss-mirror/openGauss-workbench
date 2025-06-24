<template>
  <el-dialog
    :title="t('step3.batchAddPortal.5q097pi0r541')"
    v-model="visible"
    width="95vw"
    :style="{ minWidth: '920px' }"
    :close-on-click-modal="false"
    class="batch-modal"
    @close="handleClose"
    custom-class="batch-add-dialog"
  >
    <el-form :model="globalData" inline class="form-inline">
      <el-form-item :label="t('step3.batchAddPortal.5q097pi0r542')">
        <el-select v-model="globalData.hostUserName">
          <el-option
            v-for="item in globalData.interHostUserList"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('step3.batchAddPortal.5q097pi0r543')">
        <el-input v-model="globalData.installPath" />
      </el-form-item>

      <el-form-item :label="t('step3.batchAddPortal.5q097pi0r544')">
        <el-input v-model="globalData.jarName" />
      </el-form-item>

      <el-form-item :label="t('step3.batchAddPortal.5q097pi0r545')">
        <el-input v-model="globalData.pkgName" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleBatchSet">
          {{ t('step3.batchAddPortal.5q097pi0r546') }}
        </el-button>
      </el-form-item>
    </el-form>

    <el-form :model="form" ref="formRef" class="portal-table">
      <el-table
        :data="currentPageData"
        style="width: 100%"
        class="portal-table"
        border
      >
        <el-table-column
          v-for="col in columns"
          :key="col.dataIndex || col.slotName"
          :prop="col.dataIndex || ''"
          :label="col.title"
          :width="col.dataIndex === 'publicIp' ? '180' : ''"
        >
          <template #default="scope" v-if="col.slotName">
            <el-form-item
              v-if="col.slotName === 'hostUser'"
              :prop="`tableData.${scope.$index}.hostUserId`"
              :rules="formRules.hostUserId"
            >
              <el-select v-model="scope.row.hostUserId">
                <el-option
                  v-for="item in scope.row.hostUserList"
                  :key="item.hostUserId"
                  :label="item.username"
                  :value="item.hostUserId"
                />
              </el-select>
            </el-form-item>
            <el-form-item
              v-else-if="col.slotName === 'installPath'"
              :prop="`tableData.${scope.$index}.installPath`"
              :rules="formRules.installPath"
            >
              <el-input v-model="scope.row.installPath" />
            </el-form-item>

            <!-- Jar Name -->
            <el-form-item
              v-else-if="col.slotName === 'jarName'"
              :prop="`tableData.${scope.$index}.jarName`"
              :rules="formRules.jarName"
            >
              <el-input v-model="scope.row.jarName" />
            </el-form-item>
            <el-form-item
              v-else-if="col.slotName === 'pkgName'"
              :prop="`tableData.${scope.$index}.pkgName`"
              :rules="formRules.pkgName"
            >
              <el-input v-model="scope.row.pkgName" />
            </el-form-item>
            <el-form-item
              v-else-if="col.slotName === 'kafkaPort'"
              :prop="`tableData.${scope.$index}.kafkaPort`"
              :rules="formRules.kafkaPort"
            >
              <el-input v-model="scope.row.kafkaPort" />
            </el-form-item>
            <el-form-item
              v-else-if="col.slotName === 'zookeeperPort'"
              :prop="`tableData.${scope.$index}.zookeeperPort`"
              :rules="formRules.zookeeperPort"
            >
              <el-input v-model="scope.row.zookeeperPort" />
            </el-form-item>
            <el-form-item
              v-else-if="col.slotName === 'schemaRegistryPort'"
              :prop="`tableData.${scope.$index}.schemaRegistryPort`"
              :rules="formRules.schemaRegistryPort"
            >
              <el-input v-model="scope.row.schemaRegistryPort" />
            </el-form-item>
            <template v-else-if="col.slotName === 'op'">
              <el-icon class="del-icon" @click="handleDeleteRow(scope.row.uid)">
                <Delete />
              </el-icon>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="form.tableData.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancel" v-if="!loading">
          {{ t('components.PortalInstall.5q0aajl77ik0') }}
        </el-button>
        <el-button
          type="primary"
          :loading="loading"
          @click="confirmSubmit"
        >
          {{ t('components.PortalInstall.5q0aajl77lg0') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>


<script setup>
import {reactive, ref, watch, onMounted, computed} from 'vue'
import { listHostUserByHostIds, installPortalFromDatakit } from '@/api/task'
import { getSysSetting } from '@/api/common'
import { INSTALL_TYPE, KAFKA_CONFIG_TYPE } from '@/utils/constants'
import { useI18n } from 'vue-i18n'
import { Delete } from '@element-plus/icons-vue'
const { t } = useI18n()

const currentPage = ref(1)
const pageSize = ref(10)

const currentPageData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = currentPage.value * pageSize.value
  return form.tableData.slice(start, end)
})

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}


const props = defineProps({
  open: Boolean,
  hostList: Array
})

const emits = defineEmits(['update:open', 'startInstall'])

const visible = ref(false)
const dialogWidth = computed(() => {
  return window.innerWidth * 0.95 < 920 ? '920px' : '95vw';
})
const loading = ref(false)
const form = reactive({
  tableData: []
})
const formRef = ref()
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
    dataIndex: 'publicIp'
  },
  {
    title: 'user',
    dataIndex: 'hostUser',
    slotName: 'hostUser'
  },
  {
    title: 'path',
    dataIndex: 'installPath',
    slotName: 'installPath'
  },
  {
    title: 'jarName',
    dataIndex: 'jarName',
    slotName: 'jarName'
  },
  {
    title: 'packageName',
    dataIndex: 'pkgName',
    slotName: 'pkgName'
  },
  {
    title: 'kafka Port',
    dataIndex: 'kafkaPort',
    slotName: 'kafkaPort'
  },
  {
    title: 'Zookeeper Port',
    dataIndex: 'zookeeperPort',
    slotName: 'zookeeperPort'
  },
  {
    title: 'schema Registry Port',
    dataIndex: 'schemaRegistryPort',
    slotName: 'schemaRegistryPort'
  },
  {
    title: t('step3.batchAddPortal.5q097pi0r551'),
    slotName: 'op'
  }
]

const formRules = reactive({
  hostUserId: [
    { required: true, message: t('step3.batchAddPortal.5q097pi0r547'), trigger: 'change' }
  ],
  installPath: [
    { required: true, message: t('step3.batchAddPortal.5q097pi0r548'), trigger: 'blur' }
  ],
  jarName: [
    { required: true, message: t('step3.batchAddPortal.5q097pi0r549'), trigger: 'blur' }
  ],
  pkgName: [
    { required: true, message: t('step3.batchAddPortal.5q097pi0r550'), trigger: 'blur' }
  ],
  kafkaPort: [
    { required: true, message: 'Kafka端口不能为空', trigger: 'blur' }
  ],
  zookeeperPort: [
    { required: true, message: 'Zookeeper端口不能为空', trigger: 'blur' }
  ],
  schemaRegistryPort: [
    { required: true, message: 'Schema Registry端口不能为空', trigger: 'blur' }
  ]
})

watch(visible, (v) => {
  emits('update:open', v)
})

watch(
  () => props.open,
  async (v) => {
    if (v) {
      const data = [...props.hostList]

      await buildTableData(data)
      console.log("======"+visible.value)
      form.tableData = data
    }
    visible.value = v
    console.log("======"+visible.value)
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
    item.zookeeperPort = '2181'
    item.kafkaPort = '9092'
    item.schemaRegistryPort = '8081'
    item.kafkaInstallType = KAFKA_CONFIG_TYPE.INSTALL
    item.kafkaInstallDir = item.installPath+KAFKA_CONFIG_TYPE.INSTALL_DIR_DEFAULT
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
  params.append('jarName', data.jarName)
  params.append('pkgName', data.pkgName)

  params.append('thirdPartySoftwareConfig.thirdPartySoftwareConfigType', KAFKA_CONFIG_TYPE.INSTALL)
  params.append('thirdPartySoftwareConfig.zookeeperPort', data.zookeeperPort)
  params.append('thirdPartySoftwareConfig.kafkaPort', data.kafkaPort)
  params.append('thirdPartySoftwareConfig.installDir', data.installPath+KAFKA_CONFIG_TYPE.INSTALL_DIR_DEFAULT)
  params.append('thirdPartySoftwareConfig.schemaRegistryPort', data.schemaRegistryPort)
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

const handleDeleteRow = (uid) => {
  const index = form.tableData.findIndex(item => item.uid === uid)
  if (index !== -1) {
    form.tableData.splice(index, 1)
  }
}

const unionFunc = (arr) => {
  let result = new Set()
  arr.map((item) => {
    result = new Set([...result, ...new Set(item)])
  })
  return result
}
const handleClose = () => {
  formRef.value?.resetFields()
  currentPage.value = 1
  pageSize.value = 10
}
onMounted(() => {
  visible.value = props.open
})

</script>

<style lang="less" scoped>
:deep(.batch-add-dialog) {
  .el-dialog__body {
    flex-shrink: 0 !important;
    min-width: 920px;
  }
}

.portal-table {
  min-width: 920px;
}
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
