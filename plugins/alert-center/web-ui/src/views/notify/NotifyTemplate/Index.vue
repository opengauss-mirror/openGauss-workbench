<template>
    <div v-if="showMain">
        <div class="page-header">
            <div class="icon"></div>
            <div class="title">{{ t('notifyTemplate.title') }}</div>
            <div class="seperator"></div>
            <div class="alert-title">{{ t('notifyTemplate.title') }} </div>
            <div class="alert-seperator">&nbsp;/</div>
        </div>
        <div class="search-form">
            <div class="filter">
                <el-button type="primary" @click="addTemplate">{{ $t('app.add') }}</el-button>
            </div>
            <div class="seperator"></div>
            <div class="filter">
                <span>{{ $t('notifyTemplate.templateType') }}</span>
                <el-select v-model="formData.notifyTemplateType" style="width: 100px;margin: 5px;"
                    @change="changeTemplateType" clearable>
                    <el-option v-for="item in templateTypeList" :key="item.value" :value="item.value" :label="item.name" />
                </el-select>
            </div>
            <div class="filter">
                <el-input v-model="formData.notifyTemplateName" style="width: 200px"
                    :placeholder="$t('notifyTemplate.templateNamePlaceholder')">
                    <template #suffix>
                        <el-button :icon="Search" @click="search" />
                    </template>
                </el-input>
            </div>
        </div>
        <div>
            <div>
                <el-table size="small" :data="tableDatas" style="width: 100%" header-cell-class-name="grid-header" border>
                    <el-table-column :label="$t('notifyTemplate.templateName')" prop="notifyTemplateName" width="100">
                    </el-table-column>
                    <el-table-column :label="$t('notifyTemplate.templateType')" prop="notifyTemplateType" width="100">
                        <template #default="scope">
                            <span>{{ $t(`notifyTemplate.${scope.row.notifyTemplateType}`) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('notifyTemplate.notifyTitle')" prop="notifyTitle" width="100">
                    </el-table-column>
                    <el-table-column :label="$t('notifyTemplate.notifyContent')" prop="notifyContent" min-width="200">
                        <template #default="scope">
                            <!-- <span style="white-space: pre-line;">{{ scope.row.notifyContent }}</span> -->
                            <el-popover trigger="hover" :content="scope.row.notifyContent"
                                popper-style="min-width: 200px;white-space: pre-line;" popper-class="sql-popover-tip">
                                <template #reference>
                                    <div style="overflow: hidden;text-overflow: ellipsis;white-space: pre-line;">
                                        {{ showSubWord(scope.row.notifyContent) }}</div>
                                </template>
                            </el-popover>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('notifyTemplate.templateDesc')" prop="notifyTemplateDesc" min-width="200">
                    </el-table-column>
                    <el-table-column :label="$t('app.operate')" width="120" fixed="right">
                        <template #default="scope">
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
        <NotifyTemplateDetail :id="curId" :state="state" @updateTemplate="updateTemplate"
            @cancelTemplate="cancelTemplate" />
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
import NotifyTemplateDetail from "@/views/notify/NotifyTemplate/NotifyTemplateDetail.vue";

const { t } = useI18n();
const initFormData = {
    notifyTemplateName: '',
    notifyTemplateType: ''
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
        return request.get("/alertCenter/api/v1/notifyTemplate", params)
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
const changeTemplateType = () => {
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
    state.value = 'add'
    showMain.value = false
}
const editTemplate = (id: number) => {
    state.value = 'edit'
    curId.value = id
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
        request.delete(`/alertCenter/api/v1/notifyTemplate/${id}`).then((res: any) => {
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
const updateTemplate = () => {
    formData.value = cloneDeep(initFormData)
    search()
    curId.value = undefined
    showMain.value = true
}
const cancelTemplate = () => {
    curId.value = undefined
    showMain.value = true
}

const showSubWord = (content: string) => {
    if (!content) {
        return ''
    }
    let contentArr = content.split('\n')
    if (contentArr.length > 1) {
        return contentArr[0] + '……'
    } else {
        return content;
    }
}

onMounted(() => {
    requestData()
})

</script>
<style scoped lang='scss'>
.el-table {
    height: calc(100vh - 110px - 62px - 42px - 34px);
}
</style>