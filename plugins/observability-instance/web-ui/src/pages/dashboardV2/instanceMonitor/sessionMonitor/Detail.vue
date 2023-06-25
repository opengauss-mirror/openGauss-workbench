<template>
    <div class="tab-wrapper">
        <el-container>
            <el-main style="position: relative; padding-top: 0px">
                <div class="page-header">
                    <div class="icon"></div>
                    <div class="title">
                        {{ $t('session.detail.tabTitle') }}({{ $t('session.detail.sessionID') }}{{ sessionId }})
                    </div>
                </div>
                <div style="position: relative">
                    <div
                        style="
                            position: absolute;
                            right: 0px;
                            top: 7px;
                            z-index: 1;
                            display: flex;
                            flex-direction: row;
                            align-items: center;
                            font-size: 12px;
                        "
                    >
                        <div style="margin-right: 12px">{{ $t('app.refreshOn') }} {{ innerRefreshDoneTime }}</div>
                        <el-button
                            class="refresh-button"
                            type="primary"
                            :icon="Refresh"
                            style="margin-right: 8px"
                            @click="loadSessionData(instanceId, sessionId)"
                        />
                        <div>{{ $t('app.autoRefreshFor') }}</div>
                        <el-select
                            v-model="innerRefreshTime"
                            style="width: 60px; margin: 0 4px"
                            @change="updateTimerInner"
                        >
                            <el-option :value="1" label="1s" />
                            <el-option :value="15" label="15s" />
                            <el-option :value="30" label="30s" />
                            <el-option :value="60" label="60s" />
                        </el-select>
                    </div>
                    <el-tabs v-model="dashboardTabKey">
                        <el-tab-pane :label="$t('session.detail.info.tabTitle')" :name="tabKeys[0]">
                            <div class="line-tips center" v-if="tips">
                                <div class=""><svg-icon name="info" />{{ tips }}</div>
                            </div>
                            <el-row v-if="!tips" :gutter="12">
                                <el-col :span="12">
                                    <my-card
                                        :title="$t('session.detail.info.server')"
                                        height="210"
                                        :bodyPadding="false"
                                        class="card-margin-bottom"
                                    >
                                        <div class="text-in-card">
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.info.sessionStatus') }}</div>
                                                <div class="value">{{ sessionData.general.state }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.info.sessionID') }}</div>
                                                <div class="value">{{ sessionData.general.sessionid }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.info.osThreadID') }}</div>
                                                <div class="value">{{ sessionData.general.lwtid }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.info.dbUserName') }}</div>
                                                <div class="value">{{ sessionData.general.usename }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.info.loginTime') }}</div>
                                                <div class="value">{{ sessionData.general.backend_start }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.info.loginDuration') }}</div>
                                                <div class="value">{{ sessionData.general.backend_runtime }}</div>
                                            </div>

                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.info.resourcePool') }}</div>
                                                <div class="value">{{ sessionData.general.resource_pool }}</div>
                                            </div>
                                        </div>
                                    </my-card>
                                </el-col>
                                <el-col :span="12">
                                    <my-card
                                        :title="$t('session.detail.client.tabTitle')"
                                        height="210"
                                        :bodyPadding="false"
                                    >
                                        <div class="text-in-card">
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.client.clientIP') }}</div>
                                                <div class="value">{{ sessionData.general.client_addr }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">
                                                    {{ $t('session.detail.client.clientHostName') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.client_hostname }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.client.clientTCPPort') }}</div>
                                                <div class="value">{{ sessionData.general.client_port }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.client.appName') }}</div>
                                                <div class="value">{{ sessionData.general.application_name }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.client.connectDBName') }}</div>
                                                <div class="value">{{ sessionData.general.datname }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.client.txStartTime') }}</div>
                                                <div class="value">{{ sessionData.general.xact_start }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">
                                                    {{ $t('session.detail.client.queryStartTime') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.query_start }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label">{{ $t('session.detail.client.queryID') }}</div>
                                                <div class="value">{{ sessionData.general.query_id }}</div>
                                            </div>
                                        </div>
                                    </my-card>
                                </el-col>
                            </el-row>
                            <el-row v-if="!tips" :gutter="12">
                                <el-col :span="12">
                                    <my-card
                                        :title="$t('session.detail.block.tabTitle')"
                                        height="160"
                                        :bodyPadding="false"
                                        class="card-margin-bottom"
                                    >
                                        <div class="text-in-card">
                                            <div class="text-row">
                                                <div class="label" style="width: 90px">
                                                    {{ $t('session.detail.block.blockedSessionID') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.block_sessionid }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label" style="width: 90px">
                                                    {{ $t('session.detail.block.file') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.filepath }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label" style="width: 90px">
                                                    {{ $t('session.detail.block.pageNumber') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.page }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label" style="width: 90px">
                                                    {{ $t('session.detail.block.lineNumber') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.tuple }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label" style="width: 90px">
                                                    {{ $t('session.detail.block.bucketNumber') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.bucket }}</div>
                                            </div>
                                        </div>
                                    </my-card>
                                </el-col>
                                <el-col :span="12">
                                    <my-card
                                        :title="$t('session.detail.wait.tabTitle')"
                                        height="160"
                                        :bodyPadding="false"
                                    >
                                        <div class="text-in-card">
                                            <div class="text-row">
                                                <div class="label" style="width: 130px">
                                                    {{ $t('session.detail.wait.waitState') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.wait_status }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label" style="width: 130px">
                                                    {{ $t('session.detail.wait.waitEventType') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.wait_event }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label" style="width: 130px">
                                                    {{ $t('session.detail.wait.waitLockMode') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.lockmode }}</div>
                                            </div>
                                            <div class="text-row">
                                                <div class="label" style="width: 130px">
                                                    {{ $t('session.detail.wait.waitObject') }}
                                                </div>
                                                <div class="value">{{ sessionData.general.namespace_relation }}</div>
                                            </div>
                                        </div>
                                    </my-card>
                                </el-col>
                            </el-row>
                            <el-row v-if="!tips" :gutter="12">
                                <el-col :span="24">
                                    <my-card
                                        :title="$t('session.detail.currentQuerySQL')"
                                        height="200"
                                        :bodyPadding="false"
                                    >
                                        <div class="text-in-card">
                                            <div class="text-row">
                                                <div class="value">{{ sessionData.general.query }}</div>
                                            </div>
                                        </div>
                                    </my-card>
                                </el-col>
                            </el-row>
                        </el-tab-pane>
                        <el-tab-pane :label="$t('session.detail.statistic.tabTitle')" :name="tabKeys[1]">
                            <div class="line-tips center" v-if="tips">
                                <div class=""><svg-icon name="info" />{{ tips }}</div>
                            </div>
                            <el-row v-if="!tips" :gutter="12">
                                <el-col :span="12">
                                    <my-card
                                        :title="$t('session.detail.statistic.sessionStatusStatistics')"
                                        height="600"
                                        :bodyPadding="false"
                                        skipBodyHeight
                                    >
                                        <el-table
                                            :data="sessionData.statisticStatus"
                                            style="width: 100%; height: 560px"
                                            border
                                            :header-cell-class-name="
                                                () => {
                                                    return 'grid-header'
                                                }
                                            "
                                        >
                                            <el-table-column prop="name" :label="$t('session.detail.statistic.name')" />
                                            <el-table-column
                                                prop="value"
                                                :label="$t('session.detail.statistic.value')"
                                            />
                                        </el-table>
                                    </my-card>
                                </el-col>
                                <el-col :span="12">
                                    <my-card
                                        :title="$t('session.detail.statistic.sessionRuntimeInformation')"
                                        height="600"
                                        :bodyPadding="false"
                                        skipBodyHeight
                                    >
                                        <el-table
                                            :data="sessionData.statisticRuntime"
                                            style="width: 100%; height: 560px"
                                            border
                                            :header-cell-class-name="
                                                () => {
                                                    return 'grid-header'
                                                }
                                            "
                                        >
                                            <el-table-column prop="name" :label="$t('session.detail.statistic.name')" />
                                            <el-table-column
                                                prop="value"
                                                :label="$t('session.detail.statistic.value')"
                                            />
                                        </el-table>
                                    </my-card>
                                </el-col>
                            </el-row>
                        </el-tab-pane>
                        <el-tab-pane :label="$t('session.detail.blockTree.tabTitle')" :name="tabKeys[2]">
                            <div class="line-tips center" v-if="tips">
                                <div class=""><svg-icon name="info" />{{ tips }}</div>
                            </div>
                            <el-table
                                v-if="!tips"
                                :table-layout="'auto'"
                                :data="sessionData.blockTree"
                                style="width: 100%"
                                border
                                :header-cell-class-name="
                                    () => {
                                        return 'grid-header'
                                    }
                                "
                                row-key="id"
                                default-expand-all
                            >
                                <el-table-column :label="$t('session.detail.blockTree.sessionID')" width="120">
                                    <template #default="scope">
                                        <el-link type="primary" @click="gotoSessionDetail(scope.row.id)">
                                            {{ scope.row.id === '0' ? '' : scope.row.id }}
                                        </el-link>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('session.detail.blockTree.blockedSessionID')" width="100">
                                    <template #default="scope">
                                        <el-link type="primary" @click="gotoSessionDetail(scope.row.parentid)">
                                            {{ scope.row.parentid === '0' ? '' : scope.row.parentid }}
                                        </el-link>
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    prop="backend_start"
                                    :label="$t('session.detail.blockTree.sessionStartTime')"
                                    :formatter="(r) => dateFormat(r.backend_start, 'MM-DD HH:mm:ss')"
                                    width="110"
                                />
                                <el-table-column
                                    prop="wait_status"
                                    :label="$t('session.detail.blockTree.waitState')"
                                    width="100"
                                />
                                <el-table-column
                                    prop="wait_event"
                                    :label="$t('session.detail.blockTree.waitEvent')"
                                    width="100"
                                />
                                <el-table-column
                                    prop="lockmode"
                                    :label="$t('session.detail.blockTree.waitLockMode')"
                                    width="120"
                                />
                                <el-table-column
                                    prop="datname"
                                    :label="$t('session.detail.blockTree.dbName')"
                                    width="110"
                                />
                                <el-table-column
                                    prop="usename"
                                    :label="$t('session.detail.blockTree.userName')"
                                    width="90"
                                />
                                <el-table-column
                                    prop="client_addr"
                                    :label="$t('session.detail.blockTree.clientIP')"
                                    width="120"
                                />
                                <el-table-column
                                    prop="application_name"
                                    :label="$t('session.detail.blockTree.appName')"
                                />
                            </el-table>
                        </el-tab-pane>
                        <el-tab-pane :label="$t('session.detail.waitRecord.tabTitle')" :name="tabKeys[3]">
                            <div class="line-tips center" v-if="tips">
                                <div class=""><svg-icon name="info" />{{ tips }}</div>
                            </div>
                            <el-table
                                v-if="!tips"
                                :data="sessionData.waiting"
                                style="width: 100%; height: 560px"
                                border
                                :header-cell-class-name="
                                    () => {
                                        return 'grid-header'
                                    }
                                "
                            >
                                <el-table-column
                                    prop="sample_time"
                                    :label="$t('session.detail.waitRecord.sampleTime')"
                                    width="140"
                                />
                                <el-table-column
                                    prop="wait_status"
                                    :label="$t('session.detail.waitRecord.waitState')"
                                    width="120"
                                />
                                <el-table-column
                                    prop="event"
                                    :label="$t('session.detail.waitRecord.waitEvent')"
                                    width="120"
                                />
                                <el-table-column
                                    prop="lockmode"
                                    :label="$t('session.detail.waitRecord.waitLockMode')"
                                    width="160"
                                />
                                <el-table-column prop="locktag" :label="$t('session.detail.waitRecord.lockInfo')" />
                            </el-table>
                        </el-tab-pane>
                    </el-tabs>
                </div>
            </el-main>
        </el-container>
    </div>
</template>

<script setup lang="ts">
import { getSessionDetail, SessionGeneralDetail, BlockTable, Statistic, WaitingRecord } from '@/api/session'
import { useIntervalTime } from '@/hooks/time'
import { useRequest } from 'vue-request'
import { Refresh } from '@element-plus/icons-vue'
import moment from 'moment'
import { useRouter } from 'vue-router'
import { dateFormat, utcTimeFormat } from '@/utils/date'

const router = useRouter()
const instanceId = ref<string>('')
const sessionId = ref<string>('')

const dashboardTabKey = ref<string>('NormalInfo')
const tabKeys = ['NormalInfo', 'StatisticsInfo', 'BlockTree', 'WaitRecord']

const innerRefreshTime = ref<number>(30)
const innerRefreshDoneTime = ref<string>('')

interface SessionData {
    general: SessionGeneralDetail | any
    blockTree: BlockTable[]
    statisticStatus: Statistic[]
    statisticRuntime: Statistic[]
    waiting: WaitingRecord[]
}
const sessionDataDefault = {
    general: {},
    blockTree: [],
    statisticStatus: [],
    statisticRuntime: [],
    waiting: [],
}
const sessionData = ref<SessionData>(sessionDataDefault)

// same for every page in index
onMounted(() => {
    if (
        typeof router.currentRoute.value.params.dbid === 'string' &&
        typeof router.currentRoute.value.params.id === 'string'
    ) {
        instanceId.value = router.currentRoute.value.params.dbid
        sessionId.value = router.currentRoute.value.params.id
    } else {
        // @ts-ignore
        const wujie = window.$wujie
        instanceId.value = wujie?.props.data.dbid
        sessionId.value = wujie?.props.data.id
    }
    loadSessionData(instanceId.value, sessionId.value)
    updateTimerInner()
})

// load data
const { data: sessionResultWrapper, run: loadSessionData } = useRequest(getSessionDetail, { manual: true })
watch(
    sessionResultWrapper,
    () => {
        tips.value = ''
        if (sessionResultWrapper?.value?.code !== 200 && sessionResultWrapper?.value?.code !== '200') {
            tips.value = sessionResultWrapper?.value?.msg
        }

        console.log('DEBUG: sessionResultWrapper', sessionResultWrapper)

        let sessionResult = sessionResultWrapper?.value?.data
        console.log('DEBUG: sessionResult', sessionResult)

        innerRefreshDoneTime.value = moment(new Date()).format('HH:mm:ss')

        // clear data
        sessionData.value = sessionDataDefault

        if (sessionResult) {
            sessionData.value.general = sessionResult.general
            sessionData.value.statisticRuntime = sessionResult.statistic.filter((item) => {
                if (item.type === 'RUNTIME') {
                    return true
                }
                return false
            })
            sessionData.value.statisticStatus = sessionResult.statistic.filter((item) => {
                if (item.type === 'STATUS') {
                    return true
                }
                return false
            })

            // block sessions
            for (let index = 0; index < sessionResult.blockTree.length; index++) {
                const element = sessionResult.blockTree[index]
                if (element.children && element.children.length > 0) {
                    for (let index2 = 0; index2 < element.children.length; index2++) {
                        const element2 = element.children[index2]
                        element2.hasChildren = undefined
                        element2.children = undefined
                    }
                } else {
                    element.children = undefined
                }
            }
            sessionData.value.blockTree = sessionResult.blockTree

            // waiting
            for (let index = 0; index < sessionResult.waiting.length; index++) {
                const element = sessionResult.waiting[index]
                element.sample_time = utcTimeFormat(element.sample_time, 'YYYY-MM-DD HH:mm:ss')
            }
            sessionData.value.waiting = sessionResult.waiting
        }
    },
    { deep: true }
)
const tips = ref<string | undefined>()
const timerInner = ref<number>()
const updateTimerInner = () => {
    clearInterval(timerInner.value)
    const timeInner = innerRefreshTime.value
    timerInner.value = useIntervalTime(
        () => {
            loadSessionData(instanceId.value, sessionId.value)
        },
        computed(() => timeInner * 1000)
    )
}
const gotoSessionDetail = (id: string) => {
    const curMode = localStorage.getItem('INSTANCE_CURRENT_MODE')
    if (curMode === 'wujie') {
        // @ts-ignore plug-in components
        window.$wujie?.props.methods.jump({
            name: `Static-pluginObservability-instanceVemSessionDetail`,
            query: {
                dbid: instanceId.value,
                id,
            },
        })
    } else {
        // local
        window.sessionStorage.setItem('sqlId', id)
        router.push(`/vem/sessionDetail/${instanceId.value}/${id}`)
    }
}
</script>
