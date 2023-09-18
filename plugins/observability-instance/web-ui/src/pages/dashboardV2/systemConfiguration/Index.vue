<template>
  <div style="margin-top: 6px">
    <el-tabs v-model="resourceMonitorTabIndex" type="card" class="card-tabs">
      <el-tab-pane :label="$t('configParam.databaseConfig')" :name="'db'">
        <div class="filter-bar">
          <div class="query filter" style="flex-grow: 1">
            <el-button @click="handleQuery">{{ $t('app.refresh') }}</el-button>
            <el-button type="primary" @click="refreshData('', '0')">{{ $t('app.query') }}</el-button>
          </div>
          <div class="item">
            <el-select v-model="selectedSuggestDB" style="width: 130px; margin: 0 4px">
              <el-option :value="'1'" :label="$t('configParam.list.displayRecommendedValue')" />
              <el-option :value="'0'" :label="$t('app.all')" />
            </el-select>
            <el-select v-model="selectedTypeDB" style="width: 130px; margin: 0 4px">
              <el-option :value="''" :label="$t('app.all')" />
              <el-option v-for="item in typeSelectionsDB" :key="item" :value="item" :label="item" />
            </el-select>
          </div>
          <div class="item">
            <el-input
              v-model="searchTextDB"
              :placeholder="$t('configParam.searchParamName')"
              :suffix-icon="Search"
              style="width: 268px"
            />
          </div>
        </div>
        <el-table
          :table-layout="'auto'"
          :data="couputedDBParamData"
          style="width: 100%"
          border
          :header-cell-class-name="
            () => {
              return 'grid-header'
            }
          "
          v-loading="loadingDBData"
        >
          <el-table-column
            prop="paramName"
            :label="$t('configParam.list.parameterName')"
            width="200"
            show-overflow-tooltip
          />
          <el-table-column prop="parameterCategory" :label="$t('configParam.list.parameterCategory')" width="70" />
          <el-table-column prop="actualValue" :label="$t('configParam.list.currentValue')" width="80" />
          <el-table-column prop="defaultValue" :label="$t('configParam.list.defaultValue')" width="80" />
          <el-table-column prop="unit" :label="$t('configParam.list.unit')" width="70" />
          <el-table-column
            prop="valueRange"
            :label="$t('configParam.list.valueRange')"
            width="100"
            show-overflow-tooltip
          />
          <el-table-column
            prop="paramDetail"
            :label="$t('configParam.list.parameterDescription')"
            show-overflow-tooltip
          />
          <el-table-column prop="syncStatesuggestValue" :label="$t('configParam.list.recommendedValue')" width="100">
            <template #default="scope">
              <div>
                <svg-icon
                  v-if="scope.row.suggest"
                  class="suggest"
                  name="suggest"
                  style="vertical-align: middle; margin-right: 2px"
                />
                <span style="vertical-align: middle">{{ scope.row.suggestValue }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="suggestExplain" :label="$t('configParam.list.recommendation')" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>

      <el-tab-pane :label="$t('configParam.systemConfig')" :name="'system'">
        <div class="filter-bar">
          <div class="query filter" style="flex-grow: 1">
            <el-button @click="handleQuery">{{ $t('app.refresh') }}</el-button>
            <el-button type="primary" @click="refreshData('', '0')">{{ $t('app.query') }}</el-button>
          </div>
          <div class="item">
            <el-select v-model="selectedSuggestOS" style="width: 130px; margin: 0 4px">
              <el-option :value="'1'" :label="$t('configParam.list.displayRecommendedValue')" />
              <el-option :value="'0'" :label="$t('app.all')" />
            </el-select>
            <el-select v-model="selectedTypeOS" style="width: 130px; margin: 0 4px">
              <el-option :value="''" :label="$t('app.all')" />
              <el-option v-for="item in typeSelectionsOS" :key="item" :value="item" :label="item" />
            </el-select>
          </div>
          <div class="item">
            <el-input
              v-model="searchTextOS"
              :placeholder="$t('configParam.searchParamName')"
              :suffix-icon="Search"
              style="width: 268px"
            />
          </div>
        </div>
        <el-table
          :table-layout="'auto'"
          :data="couputedOSParamData"
          style="width: 100%"
          border
          :header-cell-class-name="
            () => {
              return 'grid-header'
            }
          "
          v-loading="loadingDBData"
        >
          <el-table-column
            prop="paramName"
            :label="$t('configParam.list.parameterName')"
            width="200"
            show-overflow-tooltip
          />
          <el-table-column prop="parameterCategory" :label="$t('configParam.list.parameterCategory')" width="70" />
          <el-table-column prop="actualValue" :label="$t('configParam.list.currentValue')" width="80" />
          <el-table-column prop="defaultValue" :label="$t('configParam.list.defaultValue')" width="80" />
          <el-table-column prop="unit" :label="$t('configParam.list.unit')" width="70" />
          <el-table-column
            prop="valueRange"
            :label="$t('configParam.list.valueRange')"
            width="100"
            show-overflow-tooltip
          />
          <el-table-column
            prop="paramDetail"
            :label="$t('configParam.list.parameterDescription')"
            show-overflow-tooltip
          />
          <el-table-column prop="syncStatesuggestValue" :label="$t('configParam.list.recommendedValue')" width="100">
            <template #default="scope">
              <div>
                <svg-icon
                  v-if="scope.row.suggest"
                  class="suggest"
                  name="suggest"
                  style="vertical-align: middle; margin-right: 2px"
                />
                <span style="vertical-align: middle">{{ scope.row.suggestValue }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="suggestExplain" :label="$t('configParam.list.recommendation')" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
  <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />
  <Password :show="snapshotManageShown" @changeModal="changeModalSnapshotManage" @confirm="refreshData" />
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request'
import restRequest from '@/request/restful'
import { useI18n } from 'vue-i18n'
import { Search } from '@element-plus/icons-vue'
import Password from '@/pages/dashboardV2/systemConfiguration/password.vue'
import { ElMessage } from 'element-plus'
import { useMonitorStore } from '@/store/monitor'
import { tabKeys } from '@/pages/dashboardV2/common' 
import { storeToRefs } from 'pinia'

const resourceMonitorTabIndex = ref<string>('db')

const { t } = useI18n()

const searchTextDB = ref<string>('')
const selectedSuggestDB = ref<string>('1')
const selectedTypeDB = ref<string>('')

const searchTextOS = ref<string>('')
const selectedSuggestOS = ref<string>('1')
const selectedTypeOS = ref<string>('')

const errorInfo = ref<string | Error>()

const props = withDefaults(defineProps<{ tabId: string }>(), {})
const { updateCounter, sourceType, tabNow } = storeToRefs(useMonitorStore(props.tabId))

const couputedDBParamData = computed(() => {
  return data.dbParamData.filter((obj: any) => {
    let result = true
    if (selectedSuggestDB.value === '1' && obj.suggest !== true) result = false
    if (selectedTypeDB.value !== '' && obj.parameterCategory !== selectedTypeDB.value) result = false
    if (searchTextDB.value !== '' && obj.paramName.indexOf(searchTextDB.value) < 0) result = false
    return result
  })
})
const couputedOSParamData = computed(() => {
  return data.osParamData.filter((obj: any) => {
    let result = true
    if (selectedSuggestOS.value === '1' && obj.suggest !== true) result = false
    if (selectedTypeOS.value !== '' && obj.parameterCategory !== selectedTypeOS.value) result = false
    if (searchTextOS.value !== '' && obj.paramName.indexOf(searchTextOS.value) < 0) result = false
    return result
  })
})

const data = reactive<{
  dbParamData: Array<Record<string, string>>
  osParamData: Array<Record<string, string>>
}>({
  dbParamData: [],
  osParamData: [],
})

// same for every page in index
onMounted(() => {
  if (useMonitorStore(props.tabId).instanceId) {
    refreshData('', '0')
  }
  // @ts-ignore
  const wujie = window.$wujie
  // Judge whether it is a plug-in environment or a local environment through wujie
  if (wujie) {
    // Monitoring platform language change
    wujie?.bus.$on('opengauss-locale-change', (val: string) => {
      console.log('log-search catch locale change')
      nextTick(() => {
        if (useMonitorStore(props.tabId).instanceId) {
          refreshData('', '0')
        }
      })
    })
  }
})
watch(
  updateCounter,
  () => {
    if (tabNow.value === tabKeys.SystemConfig) {
      if (updateCounter.value.source === sourceType.value.INSTANCE) refreshData('', '0')
      if (updateCounter.value.source === sourceType.value.TABCHANGE) refreshData('', '0')
    }
  },
  { immediate: false }
)

const typeSelectionsDB = computed(() => {
  const result: string[] = Array.from(new Set(data.dbParamData?.map((obj: any) => obj.parameterCategory)))
  return result
})
const typeSelectionsOS = computed(() => {
  const result: string[] = Array.from(new Set(data.osParamData?.map((obj: any) => obj.parameterCategory)))
  return result
})

// password dialog
const handleQuery = () => {
  if (!useMonitorStore(props.tabId).instanceId) {
    ElMessage({
      showClose: true,
      message: t('configParam.queryValidInfo'),
      type: 'warning',
    })
    return
  }
  showSnapshotManage()
}
const snapshotManageShown = ref(false)
const showSnapshotManage = () => {
  snapshotManageShown.value = true
}
const changeModalSnapshotManage = (val: boolean) => {
  snapshotManageShown.value = val
}

const refreshData = (password: string, isRefresh: string) => {
  if (!useMonitorStore(props.tabId).instanceId) {
    ElMessage({
      showClose: true,
      message: t('configParam.queryValidInfo'),
      type: 'warning',
    })
    return
  }
  requestData(password, isRefresh)
}
const {
  data: res,
  run: requestData,
  loading: loadingDBData,
} = useRequest(
  (password, isRefresh = '0') => {
    return restRequest
      .get('/observability/v1/param/paramInfo', {
        paramName: '',
        nodeId: useMonitorStore(props.tabId).instanceId,
        dbName: null,
        password,
        paramType: '',
        isRefresh,
      })
      .then(function (res) {
        return res
      })
      .catch(function (res) {
        data.dbParamData = []
        data.osParamData = []
      })
  },
  { manual: true }
)
watch(res, (res) => {
  if (res && res.length > 0) {
    data.dbParamData = res.filter((item: any) => item.paramType === 'DB')
    data.osParamData = res.filter((item: any) => item.paramType === 'OS')
  }
})
</script>

<style scoped lang="scss">
.head {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  .title {
    font-size: 16px;
    font-weight: bold;
  }
}
.list {
  display: flex;
  flex-direction: row;
  .list-item {
    width: 50%;
    .item-title {
      font-size: 14px;
      font-weight: bold;
      margin-bottom: 5px;
    }
    .item-list {
      display: flex;
      margin: 5px 0px;
      .item-list-left {
        width: 55%;
        display: flex;
        align-items: center;
        flex-shrink: 0;
        .detail-btn {
          margin-left: 10px;
        }
      }
      .item-list-right {
        width: 40%;
        display: flex;
        align-items: center;
        overflow: hidden;
        padding-right: 20px;
        padding-left: 20px;
        vertical-align: middle;
        .item-value {
          display: inline-block;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          text-align: left;
        }
        .suggest-btn-container {
          width: 20px;
          display: flex;
        }
        .suggest-btn {
          margin-left: 5px;
        }
      }
    }
  }
}
</style>
