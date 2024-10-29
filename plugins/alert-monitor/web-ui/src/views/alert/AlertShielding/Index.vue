<template>
  <div v-if="showMain" id="alertShield">
    <div class="page-header">
      <div class="icon"></div>
      <div class="title">{{ $t(`alertShielding.title`) }}</div>
      <div class="seperator"></div>
      <el-breadcrumb separator="/" style="flex-grow: 1">
        <el-breadcrumb-item> {{ $t(`alertShielding.title`) }} </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="search-form">
      <div class="filter">
        <el-button type="primary" @click="add">{{ $t(`alertShielding.addShield`) }}</el-button>
        <el-button  @click="batchDelete">{{ $t(`app.delete`) }}</el-button>
        <el-button  @click="enable">{{ $t(`app.enable`) }}</el-button>
        <el-button  @click="stop">{{ $t(`app.disable`) }}</el-button>
      </div>
      <div class="seperator"></div>
      <div class="filter">
        <span>{{ $t(`alertRecord.cluster`) }}：</span>
        <el-cascader style="margin-right: 10px;"
          v-model="formData.nodeId"
          :options="clusterList"
          @change="changeClusterNode" clearable />
        <el-input
          v-model="formData.ruleName"
          style="width: 200px"
          @keyup.enter="search"
          :placeholder="$t(`alertShielding.inputRuleNameTip`)"
        >
          <template #suffix>
            <el-button :icon="Search" @click="search" />
          </template>
        </el-input>
      </div>
    </div>
    <div>
      <el-table
        size="small"
        :data="tableDatas"
        ref="table"
        style="width: 100%"
        header-cell-class-name="grid-header"
        border
      >
        <el-table-column type="selection" width="40" />
        <el-table-column prop="ruleName" :label="$t(`alertShielding.ruleName`)" />
        <el-table-column prop="ruleDetail" :label="$t(`alertShielding.ruleDetail`)" />
        <el-table-column header-align="center" :label="$t(`alertShielding.shieldInstance`)">
          <template #default="{ row }">
            <div v-if="row.clusterNodeList.length > 0" >
              <el-tag type="info" style="margin-right: 10px;">{{ row.clusterNodeList[0].nodeName }}</el-tag>
              <el-popover trigger="hover" :width="280" >
                <template #reference>
                  <el-tag type="info" v-if="row.clusterNodeList.length > 1">+{{ row.clusterNodeList.length-1 }}</el-tag>
                </template>
                <template #default>
                  <span v-for="(node, index) in row.clusterNodeList">
                    <el-tag type="info" v-if="index > 0" style="white-space: normal; display: inline-block; word-break: break-word; height: auto;">{{ node.nodeName }}</el-tag>
                  </span>
                </template>
              </el-popover>
            </div>
          </template>
        </el-table-column>
        <el-table-column width="280" prop="type" :label="$t(`alertShielding.effectTimeRange`)" >
          <template #default="{ row }">
            <template v-if="row.type === 'c'">
              {{$t(`alertShielding.schedule`)}}{{ row.startDate }} ~  {{ row.endDate }} <br> {{$t(`alertShielding.everyday`)}} {{ row.startTime }} ~  {{ row.endTime }}
            </template>
            <template v-else-if="row.type === 'b'">
              {{$t(`alertShielding.timeRangeSchedule`)}}{{ row.startDate }} {{ row.startTime }} ~ {{ row.endDate }} {{ row.endTime }}
            </template>
            <template v-else>
              {{$t(`alertShielding.permanent`)}}
            </template>
          </template>
        </el-table-column>
        <el-table-column align="center" width="80" prop="isEnable" :label="$t(`alertShielding.isEnable`)">
          <template #default="{ row  }">
            <el-switch v-model="row.isEnable" :active-value="1" :inactive-value="0" @change="handleSwitchChange(row)"/>
          </template>
        </el-table-column>
        <el-table-column align="center" width="100" :label="t('AlertClusterNodeConf.table[4]')">
          <template #default="scope">
            <el-link :underline="false" style="margin-right: 10px" type="primary" @click="edit(scope.row)">{{$t(`app.edit`)}} </el-link>
            <el-link :underline="false" type="primary" @click="deleteData(scope.row)">{{$t(`app.delete`)}} </el-link>
          </template>
        </el-table-column>
      </el-table>
        <el-pagination v-model:currentPage="page.currentPage" v-model:pageSize="page.pageSize" :total="page.total"
          :page-sizes="[10, 20, 30, 40]" class="pagination" layout="total,sizes,prev,pager,next" background small
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>
  </div>
  <div v-else>
    <Detail :id="shieldingId" :state="state" @updateShielding="updateShielding" @cancelShielding="cancelShielding" />
  </div>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'
import 'element-plus/es/components/message-box/style/index'
import { useRequest } from 'vue-request'
import request from '@/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { ref, onMounted } from 'vue'
import Detail from '@/views/alert/AlertShielding/Detail.vue'
import { cloneDeep } from 'lodash'
const { t } = useI18n()

const props = { multiple: true }
const showMain = ref<boolean>(true)
const formData = ref<any>({
  nodeId: '',
})
const page = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});
const value = ref()
const table = ref()
const srcTableDatas = ref<any[]>([])
const tableDatas = ref<any[]>([])
const shieldingId = ref<string>()
const state = ref<string>()
const clusterList = ref<any[]>([])

const { data: opsClusterData } = useRequest(() => request.get("/api/v1/environment/cluster"), { manual: false });
const treeTransform = (arr: any) => {
  let obj: any = [];
  if (arr instanceof Array) {
    arr.forEach((item) => {
      obj.push({
        label: item.clusterId ? item.clusterId : (item.azName ? item.azName + "_" : "") + item.publicIp + ":" + item.dbPort + (item.clusterRole ? "(" + item.clusterRole + ")" : ""),
        value: item.clusterId ? item.clusterId : item.nodeId,
        children: treeTransform(item.clusterNodes),
      });
    });
  }
  return obj;
};
watch(opsClusterData, (res: any) => {
  if (res && res.code === 200) {
    clusterList.value = treeTransform(res.data);
  }
});

const add = () => {
  state.value = 'add'
  showMain.value = false

}
const edit = (row: any) => {
  state.value = 'edit'
  shieldingId.value = row.id
  showMain.value = false
}
const deleteData = (row: any) => {
  if (!row.id) {
    ElMessage({
      message: t('app.selectDatasTip'),
      type: 'warning',
    })
    return
  }
  ElMessageBox.confirm(t('app.deleteTip'), t('app.tip'), {
    confirmButtonText: t('app.confirm'),
    cancelButtonText: t('app.cancel'),
    type: 'warning',
  })
    .then(() => {
      let id = row.id
      request.delete(`/api/v1/alertShielding/${id}`).then((res: any) => {
        if (res && res.code === 200) {
          ElMessage({
            type: 'success',
            message: t('app.delSuccess'),
          })
          page.currentPage = 1
          requestData()
        } else {
          ElMessage({
            type: 'error',
            message: t('app.delFail') + (res && res.msg ? ',' + res.msg : ''),
          })
        }
      })
    })
    .catch(() => {})
}

const batchDelete = () => {
  let rows = table.value.getSelectionRows() || []
  rows = rows.filter((item) => item.id)
  if (rows.length === 0) {
    ElMessage({
      message: t('app.selectDatasTip'),
      type: 'warning',
    })
    return
  }
  ElMessageBox.confirm(t('app.deleteTip'), t('app.tip'), {
    confirmButtonText: t('app.confirm'),
    cancelButtonText: t('app.cancel'),
    type: 'warning',
  })
    .then(() => {
      let ids = rows.map((item) => item.id).join(',')
      request.delete(`/api/v1/alertShielding?ids=${ids}`).then((res: any) => {
        if (res && res.code === 200) {
          ElMessage({
            type: 'success',
            message: t('app.delSuccess'),
          })
          page.currentPage = 1
          requestData()
        } else {
          ElMessage({
            type: 'error',
            message: t('app.delFail') + (res && res.msg ? ',' + res.msg : ''),
          })
        }
      })
    })
    .catch(() => {})
}

const enable = () => {
  let rows = table.value.getSelectionRows() || []
  rows = rows.filter((item) => item.id)
  if (rows.length === 0) {
    ElMessage({
      message: t('app.selectDatasTip'),
      type: 'warning',
    })
    return
  }
  let ids = rows.map((item) => item.id).join(',')
  let isEnable = true
  request.post(`/api/v1/alertShielding/enableBatch?ids=${ids}`).then((res: any) => {
    if (!res || !res.code === 200) {
      ElMessage({
        type: 'error',
        message: t('app.enableFail') + (res && res.msg ? ',' + res.msg : ''),
      })
    } 
    requestData();
  })
}

const handleSwitchChange = (row: any) => {
  if (!row.id) {
    ElMessage({
      message: t('app.selectDatasTip'),
      type: 'warning',
    })
    return
  }
  let id = row.id
  let enable = row.isEnable ? 'enable' : 'disable'
  request.post(`/api/v1/alertShielding/${id}/${enable}`).then((res: any) => {
    if (!res || !res.code === 200) {
      ElMessage({
        type: 'error',
        message: enable === 'enable' ? t('app.enableFail') : t('app.disableFail'),
      })
    } 
    requestData()
  })
}

const stop = () => {
  let rows = table.value.getSelectionRows() || []
  rows = rows.filter((item) => item.id)
  if (rows.length === 0) {
    ElMessage({
      message: t('app.selectDatasTip'),
      type: 'warning',
    })
    return
  }
  let ids = rows.map((item) => item.id).join(',')
  request.post(`/api/v1/alertShielding/disableBatch?ids=${ids}`).then((res: any) => {
    if (!res || !res.code === 200) {
      ElMessage({
        type: 'error',
        message: t('app.disableFail'),
      })
    } 
    requestData()
  })
}

const updateShielding = () => {
  shieldingId.value = undefined
  showMain.value = true
  requestData()
}
const cancelShielding = () => {
  shieldingId.value = undefined
  showMain.value = true
}

const { data: shieldingResult, run: requestData } = useRequest(
  () => {
    let params = {}
    let searchData = { nodeId: formData.value.nodeId?formData.value.nodeId[1]:'', ruleName: formData.value.ruleName}
    params = Object.assign(params, searchData, { pageNum: page.currentPage, pageSize: page.pageSize })
    return request.get("/api/v1/alertShielding", params)
  },
  { manual: true }
);
watch(shieldingResult, (shieldingResult: any) => {
  if (shieldingResult && shieldingResult.code === 200) {
    tableDatas.value = shieldingResult.data.records || []
    page.total = shieldingResult.data.total
  } else {
    tableDatas.value = []
    page.total = 0
    const msg = t("app.queryFail");
    ElMessage({
      showClose: true,
      message: msg,
      type: "error",
    });
  }
});

const search = () => {
  page.currentPage = 1
  page.pageSize = 10
  requestData()
}
const handleSizeChange = (val: any) => {
  page.pageSize = val
  page.currentPage = 1
  requestData()
}
const handleCurrentChange = (val: any) => {
  page.currentPage = val
  requestData()
}

onMounted(() => {
  requestData()
})
</script>
<style scoped lang="scss">
.el-table .cell {
  white-space: pre-line !important;
}
.el-select__suffix{
  display: none !important;
}
</style>
