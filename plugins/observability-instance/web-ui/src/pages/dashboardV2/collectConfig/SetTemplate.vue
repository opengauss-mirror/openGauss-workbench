<template>
  <div class="dialog">
    <el-dialog
      width="900px"
      :title="$t('collectConfig.chooseTemplate')"
      v-model="visible"
      :close-on-click-modal="false"
      draggable
      @close="closeDialog"
    >
      <div class="dialog-content">
        <el-form>
          <el-form-item :label="t('collectConfig.templateName')" prop="rootPassword">
            <el-select v-model="templateId" class="m-2" placeholder="Select" size="large">
              <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        :data="metricsData.details"
        style="width: 100%; height: 200px"
        border
        :header-cell-class-name="
          () => {
            return 'grid-header'
          }
        "
        v-loading="loading"
      >
        <el-table-column
          prop="metricGroupName"
          :label="$t('collectConfig.metricName')"
          width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="interval" :label="$t('collectConfig.scrapeInterval')" width="80" align="center" />
        <el-table-column prop="metricGroupDescription" :label="$t('collectConfig.metricDes')" />
      </el-table>

      <template #footer>
        <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t('app.cancel') }}</el-button>
        <el-button style="padding: 5px 20px" type="primary" @click="handleconfirmModel" :loading="setting">{{
          $t('app.confirm')
        }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request'
import { getTemplates, getTemplateDetail, TemplateDetail, setTemplateNodes } from '@/api/collectConfig'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const visible = ref(false)
const templateId = ref('')
const props = withDefaults(defineProps<{ show: boolean; nodeId: string }>(), {})
const options = ref<any[]>([])
const defaultDetail = {
  templateId: null,
  templateName: null,
  details: [],
}
const metricsData = ref<TemplateDetail>(defaultDetail)

onMounted(() => {
  load()
})

const { data: detailData, run: requestTemplateDetail, loading } = useRequest(getTemplateDetail, { manual: true })

watch(
  () => props.show,
  (newValue) => {
    visible.value = newValue
  },
  { immediate: true }
)
watch(
  () => templateId.value,
  (newValue) => {
    if (newValue != null) requestTemplateDetail(newValue)
  },
  { immediate: true }
)

// get templates data
const load = (checkTab?: boolean, checkRange?: boolean) => {
  requestData()
}
const { data: indexData, run: requestData } = useRequest(getTemplates, { manual: true })
watch(
  indexData,
  () => {
    if (!indexData.value) return
    options.value = indexData.value.map(function (item) {
      return {
        value: item.id,
        label: item.name,
      }
    })
    if (options.value.length > 0) templateId.value = options.value[0].value
  },
  { deep: true }
)

// get template detail data
watch(
  detailData,
  () => {
    // clear data
    metricsData.value = defaultDetail

    const baseData = detailData.value
    if (!baseData) return

    // info
    metricsData.value = baseData
  },
  { deep: true }
)

// set template
const emit = defineEmits(['changeModal', 'confirm'])
async function handleconfirmModel() {
  if (!templateId.value) return
  requestSetTemplateNodes({
    templateId: templateId.value,
    nodeId: props.nodeId,
  })
}
const {
  data: setResult,
  run: requestSetTemplateNodes,
  loading: setting,
} = useRequest(setTemplateNodes, { manual: true })
watch(
  setResult,
  () => {
    visible.value = false
    emit('confirm')
    emit('changeModal', visible.value)
  },
  { deep: true }
)

const handleCancelModel = () => {
  visible.value = false
  emit('changeModal', visible.value)
}
const closeDialog = () => {
  visible.value = false
  emit('changeModal', visible.value)
}
</script>
<style lang="scss" scoped></style>
