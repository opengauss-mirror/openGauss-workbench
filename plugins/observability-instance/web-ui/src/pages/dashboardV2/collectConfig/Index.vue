<template>
  <div style="margin-top: 6px">
    <div class="filter-bar">
      <div class="item" style="flex-grow: 1">
        <el-input
          v-model="searchTextDB"
          :placeholder="$t('configParam.searchParamName')"
          :suffix-icon="Search"
          style="width: 250px"
        />
      </div>
      <div class="query filter">
        <el-button v-bind:loading="setting" type="primary" @click="saveConfig(false)">{{ $t('app.save') }}</el-button>
        <el-button v-bind:loading="setting" @click="saveConfig(true)">{{ $t('app.reset') }}</el-button>
      </div>
    </div>
    <el-table
      :data="couputedTableData"
      style="width: 100%"
      border
      :header-cell-class-name="
        () => {
          return 'grid-header'
        }
      "
      v-loading="loading"
      @row-click="() => (editingIndex = -1)"
      @header-click="() => (editingIndex = -1)"
    >
      <el-table-column
        prop="metricGroupName"
        :label="$t('collectConfig.metricName')"
        width="260"
        show-overflow-tooltip
      />
      <el-table-column prop="metricGroupDescription" :label="$t('collectConfig.metricDes')" />
      <el-table-column width="260" prop="value" :label="$t('collectConfig.scrapeInterval')">
        <template #default="{ row }">
          <div style="display: flex; flex-direction: row" v-if="editingIndex === metricsData.details.indexOf(row)">
            <el-input-number
              @click.stop="() => {}"
              style="width: 100px"
              v-model="row.time"
              @input="allNumber(row)"
              controls-position="right"
              :min="5"
              :max="2592000"
            >
            </el-input-number>
          </div>
          <div style="display: flex; flex-direction: row" v-else>
            <span>{{ row.time }}</span>
            <el-link :underline="false" @click.stop="() => (editingIndex = metricsData.details.indexOf(row))">
              <svg-icon style="width: 12px; height: 12px; margin-left: 12px" name="edited-pen" v-if="row.isEdited" />
              <svg-icon style="width: 12px; height: 12px; margin-left: 12px" name="edit-pen" v-else />
            </el-link>
          </div>
        </template>
      </el-table-column>
      <el-table-column align="center" width="80" prop="value" :label="$t('collectConfig.metricEnable')">
        <template #default="{ row }">
          <el-switch v-model="row.isEnable" />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request'
import {
  getTemplateDetailByNodeId,
  TemplateDetail,
  setTemplateNodeDirect,
  TemplateNodeDirectDetail,
} from '@/api/collectConfig'
import { useMonitorStore } from '@/store/monitor'
import { tabKeys } from '@/pages/dashboardV2/common'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Search } from '@element-plus/icons-vue'

const { t } = useI18n()

const props = withDefaults(defineProps<{ tabId: string }>(), {})
const { updateCounter, sourceType, tabNow, instanceId } = storeToRefs(useMonitorStore(props.tabId))

const defaultDetail = {
  templateId: null,
  templateName: null,
  details: [],
}
const metricsData = ref<TemplateDetail>(defaultDetail)
const editingIndex = ref<number>(-1)
const searchTextDB = ref<string>('')

const couputedTableData = computed(() => {
  return metricsData.value.details.filter((obj: any) => {
    let result = true
    if (searchTextDB.value !== '' && obj.metricGroupName.indexOf(searchTextDB.value) < 0) result = false
    return result
  })
})
// get data
const load = (checkTab?: boolean, checkRange?: boolean) => {
  if (!instanceId.value) return
  requestData(instanceId.value)
}
const { data: indexData, run: requestData, loading } = useRequest(getTemplateDetailByNodeId, { manual: true })
watch(
  indexData,
  () => {
    // clear data
    metricsData.value = defaultDetail

    const baseData = indexData.value
    if (!baseData) return

    // info
    if (baseData.details && baseData.details.length > 0) {
      baseData.details.forEach((element) => {
        if (element.interval) {
          element.time = element.interval.substring(0, element.interval.length - 1)
          element.unit = element.interval.substring(element.interval.length - 1, element.interval.length)
        }
      })
    }
    metricsData.value = baseData
  },
  { deep: true }
)

const allNumber = (row: any) => {
  row.isEdited = true
}

// set template
async function saveConfig(isReset: boolean) {
  editingIndex.value = -1
  if (!instanceId.value) return
  let details: TemplateNodeDirectDetail[] = []
  metricsData.value.details.forEach((element) => {
    if (element.time) {
      details.push({
        metricKey: element.metricGroupKey,
        interval: isReset ? null : element.time.toString() + element.unit,
        isEnable: isReset ? true : element.isEnable,
      })
    }
  })
  requestSetTemplateNodeDirect({
    details,
    nodeId: instanceId.value,
  })
}
const { run: requestSetTemplateNodeDirect, loading: setting } = useRequest(setTemplateNodeDirect, {
  manual: true,
  onSuccess: (response) => {
    ElMessage({
      showClose: true,
      message: t('collectConfig.templateSetSuccess'),
      type: 'success',
    })
    load()
  },
})
// -------------------------- same for every page in index --------------------------
watch(
  updateCounter,
  () => {
    if (tabNow.value === tabKeys.CollectConfig) {
      if (updateCounter.value.source === sourceType.value.INSTANCE) load()
      if (updateCounter.value.source === sourceType.value.MANUALREFRESH) load()
      if (updateCounter.value.source === sourceType.value.TIMETYPE) load()
      if (updateCounter.value.source === sourceType.value.TIMERANGE) load()
      if (updateCounter.value.source === sourceType.value.TABCHANGE) load()
    }
  },
  { immediate: true }
)
// -------------------------- same for every page in index --------------------------
</script>
