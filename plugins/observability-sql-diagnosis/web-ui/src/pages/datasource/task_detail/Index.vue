<template>
  <div class="page-container">
    <div class="detail-left" v-show="!showLarge">
      <my-card :title="$t('datasource.analysisReport')" :bodyPadding="false">
        <div class="filter">
          <el-select v-model="pologyType" @change="getChangeSelect">
            <el-option v-for="item in pologyList" :key="item.value" :value="item.value" :label="item.label" />
          </el-select>
        </div>
        <keep-alive>
          <VisTopology :id="'TaskTopology'" :nodes="nodes" :edges="edges" @getNodesType="getNodeInfo" />
        </keep-alive>
      </my-card>
    </div>
    <div class="detail-right" v-show="!showLarge">
      <div class="detail-info">
        <template v-if="requestType === 'NONE' || originalHiddenFlag">
          <my-card :title="$t('datasource.detailTitle')" :bodyPadding="false" style="position: relative">
            <template #headerExtend>
              <svg-icon name="expand" class="shrink-img" @click="goToTask" />
            </template>
            <div class="detail-wrap">
              <div class="main-suggestion">
                <el-icon color="#0093FF" size="18px">
                  <WarningFilled />
                </el-icon>
                <span class="main-suggestion-text">{{ $t('datasource.notMeetingConditions') }}</span>
              </div>
              <div class="noresult-wrap">
                <img src="@/assets/img/noresult.png" class="noresult-img" />
                <p class="noresult-text">{{ $t('datasource.noResult') }}</p>
              </div>
            </div>
          </my-card>
        </template>
        <template v-else>
          <my-card
            :title="$t('datasource.detailTitle')"
            :bodyPadding="false"
            style="position: relative"
            v-if="!reportNodeList.includes(nodesType)"
          >
            <template #headerExtend>
              <svg-icon name="expand" class="shrink-img" @click="goToTask" />
            </template>

            <el-tabs class="tast-detail-tabs">
              <el-tab-pane label="诊断结果">
                <TaskInfo @goto-large="showLarge = true" :nodesType="nodesType" />
              </el-tab-pane>
            </el-tabs>
          </my-card>
          <my-card
            :title="$t('datasource.reportDetail')"
            :bodyPadding="false"
            style="position: relative"
            v-if="reportNodeList.includes(nodesType)"
          >
            <template #headerExtend>
              <svg-icon name="expand" class="shrink-img" @click="goToTask" />
            </template>
            <ReportDetail :id="urlParam.dbId" :reportId="nodesType" :requestType="requestType" />
          </my-card>
        </template>
      </div>
    </div>
    <DetailLarge v-if="showLarge" :id="urlParam.dbId" :reportId="nodesType" @hideModel="hideLargeWindow" />
  </div>
</template>

<script lang="ts" setup>
import VisTopology from '@/components/TaskTopology.vue'
import ReportDetail from '@/components/reportDetail/Index.vue'
import DetailLarge from '@/pages/datasource/task_detail/detail_large.vue'
import TaskInfo from '@/pages/datasource/task_detail/task_info.vue'
import ogRequest from '@/request'
import { useRequest } from 'vue-request'
import iconA from '@/assets/svg/bulb.svg'
import iconB from '@/assets/svg/bulb-on.svg'
import { reportNodeList, optionType } from '@/pages/datasource/common'
import { WarningFilled } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const showLarge = ref(false)
type Res =
  | {
      title: string
      type: string
      hidden: boolean
      child: Res[]
    }
  | undefined
type NodesTypes = {
  id: string | number
  pid: string | number
  label: string
  type: string
  originalHidden: boolean
  hidden: boolean
  none?: boolean
  image: {
    unselected: string
    selected: string
  }
}
type Ret = {
  child: null
  data: string
  type: string
}
interface edgesType {
  from: string | number
  to: string | number
}
const urlParam = reactive<{
  dbId: string | string[]
}>({
  dbId: '',
})
const pologyList = ref<Array<optionType>>([
  { label: t('datasource.reportResultSelect[0]'), value: 'false' },
  { label: t('datasource.reportResultSelect[1]'), value: 'true' },
])
const nodes = ref<Array<NodesTypes>>([])
const edges = ref<Array<edgesType>>([])
const nodesType = ref('TaskInfo')
const originalHiddenFlag = ref(false)
const requestType = ref('')
const pologyType = ref('false')
onMounted(() => {
  let paramsId = router.currentRoute.value.params.id
  let queryId = window.$wujie?.props.data.id as string
  if (queryId) urlParam.dbId = queryId
  else urlParam.dbId = paramsId
  requestData()
  requestResult()
  const wujie = window.$wujie
  // Judge whether it is a plug-in environment or a local environment through wujie
  if (wujie) {
    // Monitoring platform language change
    wujie?.bus.$on('opengauss-locale-change', (val: string) => {
      console.log('task_detail opengauss-locale-change')
      nodes.value = []
      nextTick(() => {
        pologyList.value = [
          { label: t('datasource.reportResultSelect[0]'), value: 'false' },
          { label: t('datasource.reportResultSelect[1]'), value: 'true' },
        ]
        requestData()
        requestResult()
      })
    })
  }
})
const showLargeWindow = () => {
  showLarge.value = true
}
const hideLargeWindow = () => {
  showLarge.value = false
}
const router = useRouter()
const goToTask = () => {
  showLargeWindow()
}
const getNodeInfo = (obj: any) => {
  originalHiddenFlag.value = obj.originalHidden
  nodesType.value = obj.type
  requestResult()
}
const getChangeSelect = (val: string) => {
  nodes.value = []
  edges.value = []
  requestData()
}
const queryData = computed(() => {
  const queryObj = {
    all: pologyType.value,
  }
  return queryObj
})
const { data: ret, run: requestResult } = useRequest(
  () => {
    return ogRequest.get('/historyDiagnosis/api/tasks/' + urlParam.dbId + '/points/' + nodesType.value)
  },
  { manual: true }
)
const { data: res, run: requestData } = useRequest(
  () => {
    return ogRequest.get('/historyDiagnosis/api/tasks/' + urlParam.dbId + '/points', { all: false })
  },
  { manual: true }
)

watch(ret, (ret: Ret) => {
  if (ret && Object.keys(ret).length) {
    requestType.value = ret.type
    if (Array.isArray(ret.data)) {
      const key = t('spatial.nodeId')
      const nodeArr = ret.data.filter((item) => typeof item[key] === 'string' && item[key] !== '')
      if (Array.isArray(nodeArr) && nodeArr.length > 0) {
        localStorage.setItem('SQL_DIAGNOSIS_NODEID', nodeArr[0][key])
      }
    }
  }
})
watch(res, (res: Res) => {
  const isAll = queryData.value.all === 'true'
  if (res && Object.keys(res).length) {
    let node = {
      id: '1',
      pid: '0',
      label: res.title,
      type: res.type,
      originalHidden: res.hidden,
      hidden: isAll ? false : res.hidden,
      none: res.hidden,
      image: { unselected: iconA, selected: iconB },
    }
    nodes.value.push(node)
    let curNodes = [
      {
        id: '1',
        pid: '0',
        label: res.title,
        type: res.type,
        originalHidden: res.hidden,
        hidden: isAll ? false : res.hidden,
        none: res.hidden,
        image: { unselected: iconA, selected: iconB },
        child: res.child,
      },
    ]
    let nextNodes = []
    while (curNodes.length > 0) {
      for (let node0 of curNodes) {
        if (!node0.child || node0.child?.length <= 0) {
          continue
        }
        for (let [i, r] of node0.child.entries()) {
          if (r && (r.hidden === false || isAll)) {
            node = {
              id: node0.id + '-' + i,
              pid: node0.id,
              label: r.title,
              type: r.type,
              originalHidden: r.hidden,
              hidden: isAll ? false : r.hidden,
              none: r.hidden,
              image: { unselected: iconA, selected: iconB },
            }
            nodes.value.push(node)
            nextNodes.push(Object.assign(node, { child: r.child }))
          }
        }
      }
      curNodes = nextNodes
      nextNodes = []
    }
    nextTick(() => {
      for (let q of nodes.value) {
        edges.value.push({ from: q.pid, to: q.id })
      }
    })
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
.page-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-around;
  align-items: center;

  &:deep(.og-card) {
    height: 91vh;
  }

  &:deep(.og-card-header--extra) {
    display: none;
  }

  &:deep(.og-card-header) {
    justify-content: center;
  }

  .detail-left {
    flex: 1;
    overflow: hidden;
    height: 100%;

    .filter {
      width: 120px;
      float: right;
      margin: 40px 30px 0;
    }
  }

  .detail-right {
    height: 100%;
    width: 450px;
    margin-left: 10px;
    .detail-info {
      height: 100%;
    }
  }

  #TaskTopology {
    width: 100%;
    height: 90%;
  }

  .shrink-img {
    width: 20px;
    height: 20px;
  }

  .detail-wrap {
    width: 100%;
    box-sizing: border-box;
    padding: 30px 20px;

    .main-suggestion {
      display: flex;
      align-items: center;

      .main-suggestion-text {
        margin-left: 10px;
        font-size: 14px;
      }
    }

    .noresult-wrap {
      width: 100%;
      height: 500px;

      .noresult-img {
        width: 200px;
        display: block;
        margin: 100px auto 20px;
      }

      .noresult-text {
        text-align: center;
        color: #707070;
      }
    }
  }
}
</style>
