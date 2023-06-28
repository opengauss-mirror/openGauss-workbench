<template>
    <div v-if="showMain">
        <div class="search-form">
            <div class="filter">
                <el-button type="primary" @click="addTemplate">{{ $t('app.add') }}</el-button>
            </div>
            <div class="seperator"></div>
            <div class="filter">
                <span>{{ $t('notifyWay.notifyType') }}</span>
                <el-select v-model="formData.notifyType" style="width: 100px;margin: 5px;"
                    @change="changeNotifyType" clearable>
                    <el-option v-for="item in templateTypeList" :key="item.value" :value="item.value" :label="item.name" />
                </el-select>
            </div>
            <div class="filter search">
                <el-input v-model="formData.name" style="width: 200px"
                    :placeholder="$t('notifyWay.namePlaceholder')">
                    <template #suffix>
                        <el-button :icon="Search" @click="search" />
                    </template>
                </el-input>
            </div>
        </div>
        <div>
            <div>
                <el-table size="small" :data="tableDatas" style="width: 100%" header-cell-class-name="grid-header" border>
                    <el-table-column :label="$t('notifyWay.name')" prop="name">
                    </el-table-column>
                    <el-table-column :label="$t('notifyWay.notifyType')" prop="notifyType">
                        <template #default="scope">
                            <span>{{ $t(`notifyTemplate.${scope.row.notifyType}`) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('notifyWay.notifyTarget')" min-width="200">
                        <template #default="scope">
                            <div v-if="scope.row.notifyType === 'email'">{{ scope.row.email }}</div>
                            <div v-if="scope.row.notifyType === 'WeCom' || scope.row.notifyType === 'DingTalk'">
                                <div v-if="scope.row.personId">{{ $t('notifyWay.personId') + ': ' + scope.row.personId }}</div>
                                <div v-if="scope.row.deptId">{{ $t('notifyWay.deptId') + ': ' + scope.row.deptId }}</div>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('notifyTemplate.templateName')" prop="notifyTemplateName">
                    </el-table-column>
                    <el-table-column :label="$t('app.operate')" width="120" fixed="right">
                        <template #default="scope">
                            <!-- <el-button link type="primary" size="small" @click="showDetail(scope.row.id)">{{
                                $t('app.view')
                            }}</el-button> -->
                            <el-button link type="primary" size="small" @click="editTemplate(scope.row.id)">{{
                                $t('app.edit')
                            }}</el-button>
                            <el-button link type="primary" size="small" @click="delTemplate(scope.row.id)">{{
                                $t('app.delete')
                            }}</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination v-model:currentPage="page.currentPage" v-model:pageSize="page.pageSize" :total="page.total"
                    :page-sizes="[10, 20, 30, 40]" class="pagination" layout="total,sizes,prev,pager,next" background small
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div>
        </div>
    </div>
    <div v-else>
        <NotifyWayDetail :id="curId" :state="state" @updateNotifyWay="updateNotifyWay" style="margin-top: 8px;"
            @cancelNotifyWay="cancelNotifyWay" />
    </div>
</template>

<script setup lang='ts'>
import { Search } from "@element-plus/icons-vue";
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessageBox, ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { cloneDeep } from 'lodash-es';
import NotifyWayDetail from "@/views/notify/NotifyWay/NotifyWayDetail.vue";

const emit = defineEmits(["updateState"])

const theme = localStorage.getItem('theme');
const color = ref<string>(theme === 'dark' ? '#d4d4d4' : '#1d212a')
const background = ref<string>(theme === 'dark' ? '#303030' : '#F5F7FB')

const { t } = useI18n();
const initFormData = {
    name: '',
    notifyType: ''
}

const showMain = ref<boolean>(true)
const formData = ref<any>(cloneDeep(initFormData));
const tableDatas = ref<any[]>([]);
const page = reactive({
    currentPage: 1,
    pageSize: 10,
    total: 0,
});
const templateTypeList = ref<any[]>([{
    name: t('notifyTemplate.email'), value: 'email'
}, {
    name: t('notifyTemplate.WeCom'), value: 'WeCom'
}, {
    name: t('notifyTemplate.DingTalk'), value: 'DingTalk'
}])
const curId = ref<number>()
const state = ref<string>()

const { data: res, run: requestData } = useRequest(
    () => {
        let params = {}
        params = Object.assign(params, formData.value, { pageNum: page.currentPage, pageSize: page.pageSize })
        return request.get("/alertCenter/api/v1/notifyWay", params)
    },
    { manual: true }
);
watch(res, (res: any) => {
    if (res && res.code === 200) {
        tableDatas.value = res.rows || []
        page.total = res.total
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
const changeNotifyType = () => {
    search()
}
const handleSizeChange = (val: number) => {
    page.pageSize = val
    requestData()
}
const handleCurrentChange = (val: number) => {
    page.currentPage = val
    requestData()
}
const addTemplate = () => {
    emit('updateState', 'addNotifyWay')
    state.value = 'add'
    showMain.value = false
}
// const showDetail = (id: number) => {
//     state.value = 'detail'
//     curId.value = id
//     showMain.value = false
// }
const editTemplate = (id: number) => {
    emit('updateState', 'editNotifyWay')
    curId.value = id
    state.value = 'edit'
    showMain.value = false
}
const delTemplate = (id: number) => {
    ElMessageBox.confirm(
        t('app.deleteTip'),
        t('app.tip'),
        {
            confirmButtonText: t('app.confirm'),
            cancelButtonText: t('app.cancel'),
            type: 'warning',
        }
    ).then(() => {
        request.delete(`/alertCenter/api/v1/notifyWay/${id}`).then((res: any) => {
            if (res && res.code === 200) {
                ElMessage({
                    type: 'success',
                    message: t('app.delSuccess'),
                })
                formData.value = cloneDeep(initFormData)
                search()
            } else {
                ElMessage({
                    type: 'error',
                    message: t('app.delFail') + (res && res.msg ? ',' + res.msg : ''),
                })
            }
        }).catch((res: any) => {
            ElMessage({
                type: 'error',
                message: t('app.delFail') + (res && res.msg ? ',' + res.msg : ''),
            })
        })
    }).catch(() => {

    })
}
const updateNotifyWay = () => {
    formData.value = cloneDeep(initFormData)
    search()
    curId.value = undefined
    emit('updateState', '')
    showMain.value = true
}
const cancelNotifyWay = () => {
    curId.value = undefined
    emit('updateState', '')
    showMain.value = true
}

onMounted(() => {
    requestData()

    const wujie = window.$wujie;
    if (wujie) {
        wujie?.bus.$on('opengauss-theme-change', (val: string) => {
            nextTick(() => {
                color.value = theme === 'dark' ? '#d4d4d4' : '#1d212a'
                background.value = theme === 'dark' ? '#303030' : '#F5F7FB'
            });
        });
    }
})

</script>
<style scoped lang='scss'>
.el-table {
    height: calc(100vh - 170px - 62px - 82px - 34px);
}
</style>