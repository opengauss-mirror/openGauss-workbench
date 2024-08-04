<template>
  <div v-if="showMain">
    <div class="page-header">
      <div class="icon"></div>
      <div class="title">{{ t('AlertClusterNodeConf.title') }}</div>
      <div class="seperator"></div>
      <el-breadcrumb separator="/" style="flex-grow: 1">
        <el-breadcrumb-item>
          {{ t('AlertClusterNodeConf.title') }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-tabs v-model="activeName" @tab-change="changeTab">
      <el-tab-pane :label="$t('app.instance')" name="instance">
        <div class="search-form">
          <div class="filter">
            <el-button type="primary" @click="addClusterNodeConf">{{ t('AlertClusterNodeConf.confBtn') }}</el-button>
            <el-button type="primary" @click="unbindClusterNodeConfs">{{ t('AlertClusterNodeConf.unbindConfBtn') }}</el-button>
          </div>
          <div class="seperator"></div>
          <div class="filter">
            <el-input v-model="formData.nodeName" style="width: 200px" @keyup.enter="search"
              :placeholder="t('AlertClusterNodeConf.nodeNamePlaceholder')">
              <template #suffix>
                <el-button :icon="Search" @click="search" />
              </template>
            </el-input>
          </div>
        </div>
        <div>
          <el-table size="small" :data="tableDatas" ref="table" style="width: 100%" header-cell-class-name="grid-header"
            border>
            <el-table-column type="selection" width="40" />
            <el-table-column prop="nodeName" :label="t('AlertClusterNodeConf.table[0]')" />
            <el-table-column prop="templateName" :label="t('AlertClusterNodeConf.table[3]')" />
            <el-table-column :label="t('AlertClusterNodeConf.table[4]')">
              <template #default="scope">
                <el-link :underline="false" type="primary" style="margin-right: 10px" @click="editClusterNodeConf(scope.row)">{{
                  t('AlertClusterNodeConf.confBtn') }}</el-link>
                  <el-link :underline="false" type="primary" @click="unbindClusterNodeConf(scope.row)">{{
                  t('AlertClusterNodeConf.unbindConfBtn') }}</el-link>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane :label="$t('app.noninstance')" name="noninstance">
        <el-table size="small" :data="pluginDatas" ref="pluginTable" style="width: 100%; margin-top: 10px" header-cell-class-name="grid-header"
            border>
            <el-table-column type="selection" width="40" />
            <el-table-column prop="nodeName" :label="t('AlertClusterNodeConf.table[0]')" />
            <el-table-column prop="templateName" :label="t('AlertClusterNodeConf.table[3]')" />
            <el-table-column :label="t('AlertClusterNodeConf.table[4]')">
              <template #default="scope">
                <el-link :underline="false" type="primary" style="margin-right: 10px" @click="editClusterNodeConf(scope.row)">{{
                  t('AlertClusterNodeConf.confBtn') }}</el-link>
                  <el-link :underline="false" type="primary" @click="unbindClusterNodeConf(scope.row)">{{
                  t('AlertClusterNodeConf.unbindConfBtn') }}</el-link>
              </template>
            </el-table-column>
          </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
  <div v-else>
    <Detail :clusterNodeList="selectedDatas" @updateConfigSuccess="updateConfigSuccess" @cancelConfig="cancelConfig" :type="activeName" />
  </div>
</template>

<script setup lang='ts'>
import { Search } from "@element-plus/icons-vue";
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessage, ElMessageBox } from "element-plus";
import { useI18n } from "vue-i18n";
import { ref, onMounted } from 'vue'
import Detail from "@/views/alert/AlertClusterNodeConf/Detail.vue"
import { cloneDeep } from "lodash";
const { t } = useI18n();

const showMain = ref<boolean>(true)
const activeName = ref<string>("instance")
const formData = ref<any>({
  nodeName: ''
})
const table = ref();
const srcTableDatas = ref<any[]>([])
const tableDatas = ref<any[]>([])
const pluginDatas = ref<any[]>([])
const selectedDatas = ref<any[]>([])

const addClusterNodeConf = () => {
  setShowInstance()
  const rows = table.value.getSelectionRows()
  if (rows.length === 0) {
    ElMessage({
      message: t('AlertClusterNodeConf.tableDataSelectTip'),
      type: 'warning',
    })
  } else {
    selectedDatas.value = rows;
    showMain.value = false
  }
}
const editClusterNodeConf = (row: any) => {
  selectedDatas.value = []
  selectedDatas.value.push(row)
  showMain.value = false
}
const unbindClusterNodeConf = (row: any) => {
  if (!row.templateId) {
    ElMessage({
      message: t('AlertClusterNodeConf.tableDataTip'),
      type: 'warning',
    })
    return;
  }
  ElMessageBox.confirm(
    t('AlertClusterNodeConf.unbindTip'),
    t('app.tip'),
    {
      confirmButtonText: t('app.confirm'),
      cancelButtonText: t('app.cancel'),
      type: 'warning',
    }
  ).then(() => {
    let clusterNodeIds = row.clusterNodeId
    request.delete(`/api/v1/alertClusterNodeConf/unbind?clusterNodeIds=${clusterNodeIds}`, { type: activeName.value }).then((res: any) => {
      if (res && res.code === 200) {
        ElMessage({
          type: 'success',
          message: t('AlertClusterNodeConf.unbindSuccess'),
        })
        requestData(activeName.value)
      } else {
        ElMessage({
          type: 'error',
          message: t('AlertClusterNodeConf.unbindFail') + (res && res.msg ? "," + res.msg : ""),
        })
      }
    })
  }).catch(() => {})
}

const unbindClusterNodeConfs = () => {
  let rows = table.value.getSelectionRows() || []
  rows = rows.filter(item => item.templateId)
  if (rows.length === 0) {
    ElMessage({
      message: t('AlertClusterNodeConf.tableDataSelectTip2'),
      type: 'warning',
    })
    return;
  } 
  ElMessageBox.confirm(
    t('AlertClusterNodeConf.unbindTip'),
    t('app.tip'),
    {
      confirmButtonText: t('app.confirm'),
      cancelButtonText: t('app.cancel'),
      type: 'warning',
    }
  ).then(() => {
    let clusterNodeIds = rows.map(item => item.clusterNodeId).join(",")
    request.delete(`/api/v1/alertClusterNodeConf/unbind?clusterNodeIds=${clusterNodeIds}`, { type: activeName.value }).then((res: any) => {
      if (res && res.code === 200) {
        ElMessage({
          type: 'success',
          message: t('AlertClusterNodeConf.unbindSuccess'),
        })
        requestData(activeName.value)
      } else {
        ElMessage({
          type: 'error',
          message: t('AlertClusterNodeConf.unbindFail') + (res && res.msg ? "," + res.msg : ""),
        })
      }
    })
  }).catch(() => {})
}

const updateConfigSuccess = () => {
  showMain.value = true
  requestData(activeName.value)
}
const cancelConfig = () => {
  showMain.value = true
}
const { data: res, run: requestData } = useRequest(
  (type: string) => {
    return request.get("/api/v1/alertClusterNodeConf", {type})
  },
  { manual: true }
)
watch(res, (res: any) => {
  if (activeName.value === 'instance') {
    if (res && res.code === 200) {
      tableDatas.value = srcTableDatas.value = res.data || []
    } else {
      srcTableDatas.value = []
      tableDatas.value = []
    }
  } else {
    if (res && res.code === 200) {
      pluginDatas.value = res.data || []
    } else {
      pluginDatas.value = []
    }
  }
})

const search = () => {
  if (!formData.value.nodeName) {
    tableDatas.value = cloneDeep(srcTableDatas.value)
  } else {
    tableDatas.value = srcTableDatas.value.filter(item => item.nodeName.indexOf(formData.value.nodeName) !== -1)
  }
}

const changeTab = () => {
  console.log("aaaaaaaaaaaa")
  requestData(activeName.value)
}

onMounted(() => {
  requestData(activeName.value)
})
</script>
<style scoped lang='scss'></style>