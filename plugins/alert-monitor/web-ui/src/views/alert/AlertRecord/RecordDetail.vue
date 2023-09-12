<template>
  <div>
    <div class="page-header">
      <div class="icon"></div>
      <div class="title">{{ t('alertRecord.detailTitle') }}</div>
      <div class="seperator"></div>
      <div class="alert-title">{{ t('alertRecord.title') }} </div>
      <div class="alert-seperator">&nbsp;/&nbsp;</div>
      <div class="alert-title">{{ t('alertRecord.detailTitle') }} </div>
    </div>

    <el-row style="margin-top: 8px;">
      <el-col :span="8">
        <div class="record-detail">
          <el-space wrap>
            <el-icon size="18" color="red">
              <Bell />
            </el-icon>
            <h5 class="title">{{ t('alertRecord.alertInstance') }}</h5>
          </el-space>
          <el-form label-position="left" label-width="120">
            <el-form-item :label="$t('alertRecord.clusterName') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ formData.clusterId }}</span>
            </el-form-item>
            <el-form-item :label="$t('alertRecord.IPAndPort') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ formData.hostIpAndPort }}</span>
            </el-form-item>
            <el-form-item :label="$t('alertRecord.nodeRole') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ formData.nodeRole }}</span>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="record-detail">
          <el-space wrap>
            <el-icon size="18" color="red">
              <Bell />
            </el-icon>
            <h5 class="title">{{ t('alertRecord.alertRule') }}</h5>
          </el-space>
          <el-form label-position="left" label-width="100">
            <el-form-item :label="$t('alertRule.ruleName') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ formData.templateRuleName }}</span>
            </el-form-item>
            <el-form-item :label="$t('alertRecord.table[6]') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ $t(`alertRule.${formData.level}`) }}</span>
            </el-form-item>
            <el-form-item :label="$t('alertRecord.table[3]') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ formData.templateName }}</span>
            </el-form-item>
            <el-form-item :label="$t('alertRecord.table[11]') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ formData.notifyWayNames }}</span>
            </el-form-item>
            <el-form-item :label="$t('alertRecord.alertContent') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ formData.alertContent }}</span>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="record-detail">
          <el-space wrap>
            <el-icon size="18" color="red">
              <Bell />
            </el-icon>
            <h5 class="title">{{ t('alertRecord.alertStatus') }}</h5>
          </el-space>
          <el-form label-position="left" label-width="100">
            <el-form-item :label="$t('alertRecord.table[7]') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ formData.startTime }}</span>
            </el-form-item>
            <el-form-item :label="$t('alertRecord.table[8]') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ formData.endTime }}</span>
            </el-form-item>
            <el-form-item :label="$t('alertRecord.table[9]') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <span>{{ durationFormat(formData.duration) }}</span>
            </el-form-item>
            <el-form-item :label="$t('alertRecord.table[12]') + ':'"
              style="margin-bottom: 5px !important;margin-left: 5px;">
              <el-switch v-model="formData.recordStatus" :active-value="1" :inactive-value="0" @change="markAs" />
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
    <div class="alert-table" style="margin-top: 8px;" v-if="formData.templateRuleType === 'index'">
      <div class="page-header" style="padding: 7px;">
        <div class="icon"></div>
        <div class="title" style="font-size: 14px;font-weight: 500;">{{ t('alertRecord.alertRelationView') }}</div>
      </div>
      <div v-for="(item, index) in relationDatas" style="width: 100%; height: 200px;margin-top: 20px;" :key="index">
        <RecordLine :title="item.name" :datas="item.datas" :unit="item.unit" />
      </div>
    </div>
    <!-- the overflow must be  visible, otherwise v-infinite-scroll will always scroll-->
    <div class="alert-table" style="margin-top: 8px; position: relative; overflow: visible;"
      v-if="formData.templateRuleType === 'log'">
      <div class="page-header" style="padding: 7px;">
        <div class="icon"></div>
        <div class="title" style="font-size: 14px;font-weight: 500;">{{ t('alertRecord.alertRelationLog') }}</div>
      </div>
      <div v-infinite-scroll="scrollToBottom" :infinite-scroll-disabled="!canScroll" style="margin: 5px;">
        <el-checkbox style="margin-bottom: 5px;" v-model="isAlertLog" :label="$t('alertRecord.justShowAlertLog')"
          @change="changeIsAlertLog" />
        <el-table size="small" :data="logData.logs" :row-style="tableRowStyle" style="width: 100%;"
          header-cell-class-name="grid-header" border>
          <el-table-column :label="$t('alertRecord.logSearchTable[0]')" width="160" align="center" prop="logTime">
          </el-table-column>
          <el-table-column :label="$t('alertRecord.logSearchTable[1]')" prop="logType" width="120" align="center">
            <template #default="scope">
              <el-popover trigger="hover" :content="scope.row.logType" popper-class="sql-popover-tip">
                <template #reference>
                  <div style="height: 23px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">{{
                    scope.row.logType }}</div>
                </template>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column :label="$t('alertRecord.logSearchTable[2]')" prop="logLevel" width="100" align="center">
            <template #default="scope">
              <el-popover trigger="hover" :content="scope.row.logLevel" popper-class="sql-popover-tip">
                <template #reference>
                  <div style="height: 23px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">{{
                    scope.row.logLevel }}</div>
                </template>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column :label="$t('alertRecord.logSearchTable[3]')" header-align="center">
            <template #default="scope">
              <span v-if="scope.row.logData && scope.row.logData.length > 300">
                <el-popover width="700px" trigger="hover" :content="scope.row.logData" popper-class="sql-popover-tip">
                  <template #reference>
                    <span v-html="scope.row.logData.substr(0, 300) + '...'"></span>
                  </template>
                </el-popover>
              </span>
              <span v-else v-html="scope.row.logData"></span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('alertRecord.logSearchTable[4]')" header-align="center" prop="logClusterId"
            width="100">
            <template #default="scope">
              <el-popover trigger="hover" :content="scope.row.logClusterId" popper-class="sql-popover-tip">
                <template #reference>
                  <div style="height: 23px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">{{
                    scope.row.logClusterId }}</div>
                </template>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column :label="$t('alertRecord.logSearchTable[5]')" header-align="center" prop="logNodeId"
            width="100">
            <template #default="scope">
              <el-popover trigger="hover" :content="scope.row.logNodeId" popper-class="sql-popover-tip">
                <template #reference>
                  <div style="height: 23px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">{{
                    scope.row.logNodeId }}</div>
                </template>
              </el-popover>
            </template>
          </el-table-column>
        </el-table>
        <p v-if="loading">Loading...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang='ts'>
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { Bell } from "@element-plus/icons-vue";
import { useI18n } from "vue-i18n";
import { storeToRefs } from 'pinia';
import { useWindowStore } from '@/store/window';
import RecordLine from "@/views/alert/AlertRecord/components/RecordLine.vue"
const { t } = useI18n();
const { theme } = storeToRefs(useWindowStore());

const offsetHeight = ref<number>()
const carHeight = ref<number>()
const formData = ref<any>({})
const relationDatas = ref<any[]>([])
const logData = ref<any>({
  logs: [],
  searchAfter: '',
  keyAndBlockWords: []
})
const isAlertLog = ref<boolean>(false)
const searchAfter = ref<string>('')
const canScroll = ref<boolean>(true);
const loading = ref(false);

const { data: res, run: requestData } = useRequest(
  (id: any) => {
    return request.get(`/api/v1/alertRecord/${id}`)
  },
  { manual: true }
);
watch(res, (res: any) => {
  if (res && res.code === 200) {
    formData.value = res.data
    if (formData.value.templateRuleType === 'log') {
      requestRelationLog(formData.value.id, isAlertLog.value, searchAfter.value)
    } else {
      requestRelationData(formData.value.id)
    }
  }
});

const markAs = (val: any) => {
  if (formData.value.id) {
    if (val === 0) {
      request.post(`/api/v1/alertRecord/markAsUnread?ids=${formData.value.id}`).then((res: any) => {
        if (res && res.code === 200) {
          requestData(formData.value.id)
        }
      })
    }
    if (val === 1) {
      request.post(`/api/v1/alertRecord/markAsRead?ids=${formData.value.id}`).then((res: any) => {
        if (res && res.code === 200) {
          requestData(formData.value.id)
        }
      })
    }
  }
}
const changeIsAlertLog = (val: boolean) => {
  logData.value = {
    logs: [],
    searchAfter: '',
    keyAndBlockWords: []
  }
  requestRelationLog(formData.value.id, isAlertLog.value, searchAfter.value)
}

const tableRowStyle = ({ row, rowIndex }) => {
  let val = row.logData
  let keyAndBlockWords = logData.value.keyAndBlockWords || [] as any[]
  if (keyAndBlockWords.length === 0 || !val) {
    return {}
  }
  lable: for (let word of keyAndBlockWords) {
    let blockWord = word.blockWord
    if (blockWord) {
      for (let block of blockWord.split(',')) {
        if (val.indexOf(block) > -1) {
          continue lable;
        }
      }
    }
    let keyword = word.keyword
    for (let key of keyword.split(',')) {
      if (val.indexOf(key) === -1) {
        continue lable;
      }
    }
    return theme.value === 'dark'
      ? {
        'background-color': 'rgba(235,223,132,0.2)',
      }
      : {
        'background-color': 'rgba(238,102,102,0.1)',
      };
  }
  return {}
};

const { data: relationRes, run: requestRelationData } = useRequest(
  (id: any) => {
    return request.get(`/api/v1/alertRecord/${id}/relation`)
  },
  { manual: true }
);
watch(relationRes, (relationRes: any) => {
  if (relationRes && relationRes.code === 200) {
    relationDatas.value = relationRes.data
  }
});

const { data: logRes, run: requestRelationLog, error } = useRequest(
  (id: number, isAlertLog: boolean, searchAfter: string) => {
    loading.value = true;
    return request.get(`/api/v1/alertRecord/${id}/relationLog`, { isAlertLog, searchAfter }).finally(() => {
      loading.value = false;
    })
  },
  { manual: true })
watch(logRes, (logRes: any) => {
  if (logRes && logRes.code === 200) {
    logData.value.keyAndBlockWords = logRes.data.keyAndBlockWords
    if (!logRes.data.logs || logRes.data.logs.length === 0) {
      canScroll.value = false
    } else {
      if (!searchAfter.value || searchAfter.value !== logRes.data.searchAfter) {
        searchAfter.value = logRes.data.searchAfter
        logData.value.logs.push(...logRes.data.logs)
      }
      canScroll.value = true
    }
  }
});
watch(error, () => {
  loading.value = false;
});
const scrollToBottom = () => {
  if (canScroll.value) {
    canScroll.value = false
    requestRelationLog(formData.value.id, isAlertLog.value, searchAfter.value);
  }
};

const durationFormat = (val: any) => {
  if (typeof val === 'number') {
    if (val <= 0) {
      return '00:00:00';
    } else {
      let hh = Math.floor(val / 3600);
      let shh = val - hh * 3600;
      let ii = Math.floor(shh / 60);
      let ss = shh - ii * 60;
      return (hh < 10 ? '0' + hh : hh) + ':' + (ii < 10 ? '0' + ii : ii) + ':' + (ss < 10 ? '0' + ss : ss);
    }
  } else {
    return '00:00:00';
  }
}

const router = useRouter();
onMounted(() => {
  offsetHeight.value = document.body.offsetHeight - 50
  carHeight.value = offsetHeight.value * 0.4
  let _id = null
  const wujie = window.$wujie;
  if (wujie) {
    _id = wujie?.props.data.id as number;
  } else {
    let param = router.currentRoute.value.query
    _id = param.id
  }
  if (_id) {
    requestData(_id)
  }
})
</script>
<style scoped lang='scss'>
.alert-table {
  min-height: calc(100vh - 110px - 72px - 238px);
}

.log-table {
  height: calc(100vh - 110px - 72px - 238px);
}

.infinite-list {
  height: 300px;
  padding: 0;
  margin: 0;
  list-style: none;
}

.infinite-list .infinite-list-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  background: var(--el-color-primary-light-9);
  margin: 10px;
  color: var(--el-color-primary);
}

.infinite-list .infinite-list-item+.list-item {
  margin-top: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-detail {
  background: var(--background-color-4);
  color: var(--color-hw-text-2);
  border-radius: 2px;
  margin: 5px;
  height: 220px;
}

.title {
  font-style: 'normal';
  font-weight: 'bold';
  font-size: 14px;
  line-height: 22px;
  margin: 5px
}

.content {
  margin-top: 10px;
}
</style>