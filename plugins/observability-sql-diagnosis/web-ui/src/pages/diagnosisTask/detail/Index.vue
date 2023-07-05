<template>
    <div class="page-container">
        <div class="detail-left" v-show="!showLarge">
            <my-card :title="$t('datasource.analysisReport')" :bodyPadding="false">
                <div class="filter">
                    <el-select v-model="pologyType" @change="getChangeSelect">
                        <el-option
                            v-for="item in pologyList"
                            :key="item.value"
                            :value="item.value"
                            :label="item.label"
                        />
                    </el-select>
                </div>
                <keep-alive>
                    <VisTopology :id="'TaskTopology'" :nodes="nodes" :edges="edges" @getNodesType="getNodeInfo" />
                </keep-alive>
            </my-card>
        </div>
        <div
            class="detail-right"
            :style="{
                width: showLarge ? '100%' : '450px',
                height: '100%',
                'margin-left': showLarge ? '0px' : '10px',
            }"
        >
            <div
                class="detail-info"
                :style="{
                    width: showLarge ? '100%' : 'auto',
                }"
            >
                <my-card
                    :title="$t('datasource.detailTitle')"
                    :bodyPadding="false"
                    :style="{
                        position: showLarge ? 'relative' : 'relative',
                        width: showLarge ? '100%' : 'auto',
                    }"
                >
                    <template #headerExtend>
                        <svg-icon v-if="!showLarge" name="expand" class="shrink-img" @click="showLargeWindow" />
                        <svg-icon v-if="showLarge" name="expand" class="shrink-img" @click="hideLargeWindow" />
                    </template>
                    <PointInfo @goto-large="showLarge = true" :nodesType="nodesType" :taskId="urlParam.dbId" />
                </my-card>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { getTask } from '@/api/historyDiagnosis'
import VisTopology from '@/components/TaskTopology.vue'
import PointInfo from '@/pages/diagnosisTask/detail/PointInfo.vue'
import { useRequest } from 'vue-request'
import iconA from '@/assets/svg/bulb.svg'
import iconB from '@/assets/svg/bulb-on.svg'
import { optionType } from '@/pages/datasource/common'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const showLarge = ref(false)
type Res =
    | {
          pointName: any
          title: string
          nodeName: string
          isHidden: boolean
          child: Res[]
      }
    | undefined
type NodesTypes = {
    id: string | number
    pid: string | number
    label: string
    type: string
    originalHidden: boolean
    isHidden: boolean
    none?: boolean
    image: {
        unselected: string
        selected: string
    }
}
interface edgesType {
    from: string | number
    to: string | number
}
const urlParam = reactive<{
    dbId: string
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
const pologyType = ref('false')
onMounted(() => {
    let paramsId = router.currentRoute.value.params.id
    let queryId = window.$wujie?.props.data.id as string
    if (queryId) urlParam.dbId = queryId
    else urlParam.dbId = Array.isArray(paramsId) ? paramsId[0] : paramsId
    requestData()
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
const getNodeInfo = (obj: any) => {
    nodesType.value = obj.pointName
    console.log('DEBUG: nodesType.value', nodesType.value)
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
const { data: res, run: requestData } = useRequest(
    () => {
        return getTask(urlParam.dbId)
    },
    { manual: true }
)
watch(res, (res: any) => {
    const isAll = queryData.value.all === 'true'
    if (res && Object.keys(res).length) {
        let node = {
            id: '1',
            pid: '0',
            label: res.title,
            pointName: res.pointName,
            type: res.nodeName,
            originalHidden: res.isHidden,
            isHidden: isAll ? false : res.isHidden,
            none: res.isHidden,
            image: { unselected: iconA, selected: iconB },
        }
        nodes.value.push(node)
        let curNodes = [
            {
                id: '1',
                pid: '0',
                label: res.title,
                pointName: res.pointName,
                type: res.nodeName,
                originalHidden: res.isHidden,
                isHidden: isAll ? false : res.isHidden,
                none: res.isHidden,
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
                    if (r && (r.isHidden === false || isAll)) {
                        node = {
                            id: node0.id + '-' + i,
                            pid: node0.id,
                            label: r.title,
                            pointName: r.pointName,
                            type: r.nodeName,
                            originalHidden: r.isHidden,
                            isHidden: isAll ? false : r.isHidden,
                            none: r.isHidden,
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
        overflow: isHidden;
        height: 100%;

        .filter {
            width: 120px;
            float: right;
            margin: 40px 30px 0;
        }
    }

    .detail-right {
        height: 100%;
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
