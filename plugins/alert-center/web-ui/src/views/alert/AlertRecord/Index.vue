<template>
    <div>
        <div class="page-header">
            <div class="icon"></div>
            <div class="title">{{ t('alertRecord.title') }}</div>
            <div class="seperator"></div>
            <div class="alert-title">{{ t('alertRecord.title') }} </div>
            <div class="alert-seperator">&nbsp;/</div>
        </div>
        <div class="search-form">
            <div class="filter">
                <el-icon color="#F56C6C">
                    <Bell />
                </el-icon>
                <span>{{ $t(`alertRecord.alertTotal`) }}：</span><span class="statistics">{{ statisticsData.totalNum }}</span>
                <span style="margin-left: 20px;">{{ $t(`alertRecord.unread`) }}：</span><span class="statistics">{{ statisticsData.unReadNum
                }}</span>
                <span style="margin-left: 20px;">{{ $t(`alertRecord.read`) }}：</span><span class="statistics">{{ statisticsData.readNum
                }}</span>
            </div>
            <div class="seperator"></div>
            <div class="filter">
                <span>{{ $t(`alertRecord.cluster`) }}:&nbsp;</span>
                <el-cascader v-model="formData.clusterNodeId" :options="clusterList" @change="changeClusterNode" clearable/>
            </div>
            <div class="filter">
                <span class="demonstration">{{ $t(`alertRecord.alertTimeRange`) }}:&nbsp;</span>
                <el-date-picker v-model="alertTimeRange" type="datetimerange" range-separator="~"
                    format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"
                    :start-placeholder="t(`alertRecord.startTimePlaceholder`)"
                    :end-placeholder="t(`alertRecord.endTimePlaceholder`)" @change="changeAlertTimeRange" />
            </div>
        </div>
        <el-row style="width: 100%;">
            <el-col :span="8">
                <RecordPid :datas="recordStatusData" :title="$t('alertRecord.recordStatusTip')"></RecordPid>
            </el-col>
            <el-col :span="8">
                <RecordPid :datas="alertStatusData" :title="$t('alertRecord.alertStatusTip')"></RecordPid>
            </el-col>
            <el-col :span="8">
                <RecordPid :datas="levelData" :title="$t('alertRecord.levelTip')"></RecordPid>
            </el-col>
        </el-row>
        <div class="alert-table">
            <div class="page-header" style="padding: 7px;">
                <div class="icon"></div>
                <div class="title" style="font-size: 14px;font-weight: 500;">{{ t('alertRecord.detailTitle') }}</div>
            </div>
            <div class="search-form" style="margin: 0 8px !important;">
                <div class="filter">
                    <el-button  @click="markAsUnread">{{ t('alertRecord.markAsUnread') }}</el-button>
                    <el-button  @click="markAsRead">{{ t('alertRecord.markAsRead') }}</el-button>
                </div>
                <div class="seperator"></div>
                <div class="filter">
                    <span>{{ $t('alertRecord.recordStatus') }}:&nbsp;</span>
                    <el-checkbox-group v-model="formData.recordStatus" @change="changeRecordStatus" size="large">
                        <el-checkbox label="0">{{ $t(`alertRecord.unread`) }}</el-checkbox>
                        <el-checkbox label="1">{{ $t(`alertRecord.read`) }}</el-checkbox>
                    </el-checkbox-group>
                    <el-divider direction="vertical" />
                </div>
                <div class="filter">
                    <span>{{ $t('alertRecord.alertStatus') }}:&nbsp;</span>
                    <el-checkbox-group v-model="formData.alertStatus" @change="changeAlertStatus" size="large">
                        <el-checkbox label="0">{{ $t(`alertRecord.alerting`) }}</el-checkbox>
                        <el-checkbox label="1">{{ $t(`alertRecord.alerted`) }}</el-checkbox>
                    </el-checkbox-group>
                    <el-divider direction="vertical" />
                </div>
                <div class="filter">
                    <span>{{ $t('alertRecord.level') }}:&nbsp;</span>
                    <el-checkbox-group v-model="formData.alertLevel" @change="changeLevel" size="large">
                        <el-checkbox label="serious">{{ $t(`alertRule.serious`) }}</el-checkbox>
                        <el-checkbox label="warn">{{ $t(`alertRule.warn`) }}</el-checkbox>
                        <el-checkbox label="info">{{ $t(`alertRule.info`) }}</el-checkbox>
                    </el-checkbox-group>
                </div>
            </div>
            <el-table size="small" :data="tableDatas" style="width: 100%;" ref="recordTable"
                header-cell-class-name="grid-header" border>
                <el-table-column type="selection" width="40" align="center" header-align="center"/>
                <el-table-column prop="clusterNodeName" min-width="150" :label="$t('alertRecord.table[0]')" />
                <!-- <el-table-column prop="hostIp" :label="$t('alertRecord.table[1]')" />
                <el-table-column prop="dbType" :label="$t('alertRecord.table[2]')" /> -->
                <el-table-column prop="templateName" :label="$t('alertRecord.table[3]')" />
                <el-table-column prop="templateRuleName" :label="$t('alertRecord.table[4]')" />
                <el-table-column prop="templateRuleType" :label="$t('alertRecord.table[5]')">
                    <template #default="scope">
                        <span>{{ $t(`alertRule.${scope.row.templateRuleType}`) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="level" :label="$t('alertRecord.table[6]')">
                    <template #default="scope">
                        <svg-icon name="circle1" style="margin-right: 2px;" v-if="scope.row.level === 'info'" />
                        <svg-icon name="circle2" style="margin-right: 2px;" v-if="scope.row.level === 'warn'" />
                        <svg-icon name="circle3" style="margin-right: 2px;" v-if="scope.row.level === 'serious'" />
                        <span>{{ $t(`alertRule.${scope.row.level}`) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="startTime" :label="$t('alertRecord.table[7]')" />
                <el-table-column prop="endTime" :label="$t('alertRecord.table[8]')" />
                <el-table-column prop="duration" :label="$t('alertRecord.table[9]')">
                    <template #default="scope">
                        <span>{{ durationFormat(scope.row.duration) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="alertStatus" :label="$t('alertRecord.table[10]')">
                    <template #default="scope">
                        <span v-if="scope.row.alertStatus === 0"><svg-icon name="circle3" style="margin-right: 2px;" />{{
                            $t(`alertRecord.alerting`) }}</span>
                        <span v-if="scope.row.alertStatus === 1"><svg-icon name="circle1" style="margin-right: 2px;" />{{
                            $t(`alertRecord.alerted`) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="notifyWayName" :label="$t('alertRecord.table[11]')" />
                <el-table-column prop="recordStatus" :label="$t('alertRecord.table[12]')">
                    <template #default="scope">
                        <span v-if="scope.row.recordStatus === 0">{{ $t(`alertRecord.unread`) }}</span>
                        <span v-if="scope.row.recordStatus === 1">{{ $t(`alertRecord.read`) }}</span>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('alertRecord.table[13]')" fixed="right">
                    <template #default="scope">
                        <el-button link type="primary" size="small" @click="showDetail(scope.row.id)">{{ $t('app.view')
                        }}</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination">
                <el-pagination v-model:currentPage="page.currentPage" v-model:pageSize="page.pageSize" :total="page.total"
                    :page-sizes="[10, 20, 30, 40]" layout="total,sizes,prev,pager,next" background small
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div>
        </div>

    </div>
</template>

<script setup lang='ts'>
import { Bell } from "@element-plus/icons-vue";
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { i18n } from "@/i18n";
import { ElMessageBox, ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import RecordPid from "@/views/alert/AlertRecord/components/RecordPie.vue"
const { t } = useI18n();

const recordTable = ref()
const formData = ref<any>({
    clusterNodeId: [],
    startTime: '',
    endTime: '',
    recordStatus: [],
    alertStatus: [],
    alertLevel: []
})
const clusterList = ref<Array<any>[]>([])
const alertTimeRange = ref<Array<any>[]>([])
const statisticsData = ref<any>({})
const recordStatusData = ref<any[]>([])
const alertStatusData = ref<any[]>([])
const levelData = ref<any[]>([])
const tableDatas = ref<any[]>([])
const page = reactive({
    currentPage: 1,
    pageSize: 10,
    total: 0,
})
const { data: statisticsRes, run: requestStatisticsData } = useRequest(
    () => {
        return request.get("/alertCenter/api/v1/alertRecord/statistics")
    },
    { manual: true }
);
watch(statisticsRes, (statisticsRes: any) => {
    if (statisticsRes && statisticsRes.code === 200) {
        statisticsData.value = statisticsRes.data
    }
});
const { data: pieRes, run: requestPisData } = useRequest(
    () => {
        let param = {
            clusterNodeId: formData.value.clusterNodeId && formData.value.clusterNodeId.length > 1 ? formData.value.clusterNodeId[1] : '',
            startTime: formData.value.startTime,
            endTime: formData.value.endTime
        }
        return request.get("/alertCenter/api/v1/alertRecord/statistics", param)
    },
    { manual: true }
);
watch(pieRes, (pieRes: any) => {
    if (pieRes && pieRes.code === 200) {
        let data = pieRes.data
        recordStatusData.value = [{
            name: t(`alertRecord.unread`),
            value: data.unReadNum,
            itemStyle: {
                color: '#3DC7FF'
            }
        }, {
            name: t(`alertRecord.read`),
            value: data.readNum,
            itemStyle: {
                color: '#246CFF'
            }
        }]
        alertStatusData.value = [{
            name: t(`alertRecord.alerting`),
            value: data.firingNum,
            itemStyle: {
                color: '#246CFF'
            }
        }, {
            name: t(`alertRecord.alerted`),
            value: data.recoverNum,
            itemStyle: {
                color: '#3DC7FF'
            }
        }]
        levelData.value = [{
            name: t(`alertRule.serious`),
            value: data.seriousNum,
            itemStyle: {
                color: '#F53F3F'
            }
        }, {
            name: t(`alertRule.warn`),
            value: data.warnNum,
            itemStyle: {
                color: '#FF7D00'
            }
        }, {
            name: t(`alertRule.info`),
            value: data.infoNum,
            itemStyle: {
                color: '#246CFF'
            }
        }]
    }
});
const { data: res, run: requestData } = useRequest(
    () => {
        let params = {
            clusterNodeId: formData.value.clusterNodeId && formData.value.clusterNodeId.length > 1 ? formData.value.clusterNodeId[1] : '',
            startTime: formData.value.startTime,
            endTime: formData.value.endTime,
            recordStatus: formData.value.recordStatus && formData.value.recordStatus.length > 0 ? formData.value.recordStatus.join(',') : '',
            alertStatus: formData.value.alertStatus && formData.value.alertStatus.length > 0 ? formData.value.alertStatus.join(',') : '',
            alertLevel: formData.value.alertLevel && formData.value.alertLevel.length > 0 ? formData.value.alertLevel.join(',') : ''
        }
        params = Object.assign(params, { pageNum: page.currentPage, pageSize: page.pageSize })
        return request.get("/alertCenter/api/v1/alertRecord", params)
    },
    { manual: true }
);
watch(res, (res: any) => {
    if (res && res.code === 200) {
        tableDatas.value = res.rows
        page.total = res.total
    }
});
const durationFormat = (val: any) => {
    if (typeof val === 'number') {
        if (val <= 0) {
            return '00:00:00';
        } else {
            let hh = parseInt(val / 3600);
            let shh = val - hh * 3600;
            let ii = parseInt(shh / 60);
            let ss = shh - ii * 60;
            return (hh < 10 ? '0' + hh : hh) + ':' + (ii < 10 ? '0' + ii : ii) + ':' + (ss < 10 ? '0' + ss : ss);
        }
    } else {
        return '00:00:00';
    }
}
const { data: opsClusterData } = useRequest(() => request.get("/alertCenter/api/v1/environment/cluster"), { manual: false });
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

const changeAlertTimeRange = () => {
    if (alertTimeRange.value && alertTimeRange.value.length === 2) {
        formData.value.startTime = alertTimeRange.value[0]
        formData.value.endTime = alertTimeRange.value[1]
    } else {
        formData.value.startTime = ''
        formData.value.endTime = ''
    }
    requestPisData()
    requestData()
}

const changeClusterNode = () => {
    requestPisData()
    requestData()
}

const changeRecordStatus = () => {
    requestData()
}
const changeAlertStatus = () => {
    requestData()
}
const changeLevel = () => {
    requestData()
}
const markAsUnread = () => {
    const rows = recordTable.value.getSelectionRows();
    if (rows.length === 0) {
        ElMessage({
            message: t('alertRecord.tableDataSelectTip'),
            type: 'warning',
        })
    } else {
        const ids = rows.map((item: any) => item.id).join(',')
        request.post(`/alertCenter/api/v1/alertRecord/markAsUnread?ids=${ids}`).then((res: any) => {
            if (res && res.code === 200) {
                requestStatisticsData()
                requestPisData()
                requestData()
            }
        })
    }
}
const markAsRead = () => {
    const rows = recordTable.value.getSelectionRows();
    if (rows.length === 0) {
        ElMessage({
            message: t('alertRecord.tableDataSelectTip'),
            type: 'warning',
        })
    } else {
        const ids = rows.map((item: any) => item.id).join(',')
        request.post(`/alertCenter/api/v1/alertRecord/markAsRead?ids=${ids}`).then((res: any) => {
            if (res && res.code === 200) {
                requestStatisticsData()
                requestPisData()
                requestData()
            }
        })
    }
}
const router = useRouter();

const handleSizeChange = (val: any) => {
    page.currentPage = val
    requestData()
}
const handleCurrentChange = (val: any) => {
    page.pageSize = val
    requestData()
}

const showDetail = (id: number) => {
    if (process.env.mode === 'production') {
        window.$wujie?.props.methods.jump({
            name: `Static-pluginAlert-centerVemAlertRecordDetail`,
            query: {
                id,
            },
        });
    } else {
        router.push({
            path: `/vem/alert/recordDetail`,
            query: {
                id,
            },
        });
    }
}

onMounted(() => {
    requestStatisticsData()
    requestPisData()
    requestData()

    const wujie = window.$wujie;
    // Judge whether it is a plug-in environment or a local environment through wujie
    if (wujie) {
        // Monitoring platform language change
        wujie?.bus.$on('opengauss-locale-change', (val: string) => {
            console.log('log-search catch locale change');
            nextTick(() => {
                requestPisData()
            });
        });
        // wujie?.bus.$on('opengauss-theme-change', (val: string) => {
        //     nextTick(() => {
        //         requestPisData()
        //     });
        // });
    }
})
</script>
<style scoped lang='scss'>
.el-table {
    height: calc(100vh - 170px - 62px - 250px - 34px);
}
</style>