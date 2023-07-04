<template>
    <div v-if="showMain">
        <div class="page-header">
            <div class="icon"></div>
            <div class="title">{{ t('alertRule.title') }}</div>
            <div class="seperator"></div>
            <div class="alert-title">{{ t('alertRule.title') }} </div>
            <div class="alert-seperator">&nbsp;/</div>
        </div>
        <div class="search-form">
            <div class="filter">
                <span>{{ $t('alertRule.ruleType') }}&nbsp;</span>
                <el-select v-model="formData.ruleType" :placeholder="$t('alertRule.selectRuleType')" clearable
                    @change="search">
                    <el-option v-for="item in ruleTypeList" :key="item" :value="item" :label="$t(`alertRule.${item}`)" />
                </el-select>
            </div>
            <div class="filter">
                <span>{{ $t('alertRule.level') }}&nbsp;</span>
                <el-select v-model="formData.level" :placeholder="$t('alertRule.selectLevelType')" style="max-width: 100px"
                    clearable @change="search">
                    <el-option v-for="item in levelList" :key="item" :value="item" :label="$t(`alertRule.${item}`)" />
                </el-select>
            </div>
            <div class="filter">
                <el-input v-model="formData.ruleName" style="width: 150px"
                    :placeholder="$t('alertRule.ruleNamePlaceholder')">
                    <template #suffix>
                        <el-button :icon="Search" @click="search" />
                    </template>
                </el-input>
            </div>
        </div>
        <div class="alert-rule">
            <div class="alert-record-table">
                <el-table size="small" :data="tableDatas" style="width: 100%;" header-cell-class-name="grid-header" border>
                    <el-table-column :label="$t('alertRule.table[0]')" prop="ruleName" width="140">
                        <!-- <template #default="scope">
                            <el-link :underline="false" type="primary" @click="showDetail(scope.row.ruleId)">{{
                                scope.row.ruleName }}</el-link>
                        </template> -->
                    </el-table-column>
                    <el-table-column :label="$t('alertRule.table[1]')" prop="ruleType" width="100">
                        <template #default="scope">
                            <span>{{ $t(`alertRule.${scope.row.ruleType}`) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="level" :label="$t('alertRule.table[2]')" width="100">
                        <template #default="scope">
                            <span>{{ $t(`alertRule.${scope.row.level}`) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="ruleExpDesc" :label="$t('alertRule.table[8]')" min-width="200">
                        <template #default="scope">
                            <span v-html="scope.row.ruleExpDesc"></span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="ruleExpComb" :label="$t('alertRule.table[9]')">
                    </el-table-column>
                    <el-table-column prop="isRepeat" :label="$t('alertRule.table[3]')" width="100">
                        <template #default="scope">
                            <span>{{ scope.row.isRepeat === 1 ? $t('alertRule.isRepeat') :
                                $t('alertRule.isNotRepeat') }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="isSilence" :label="$t('alertRule.table[4]')" width="200">
                        <template #default="scope">
                            <span>{{ scope.row.isSilence === 1 ? scope.row.silenceStartTime + $t('alertRule.to') +
                                scope.row.silenceEndTime : $t('alertRule.isNotSilence') }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="alertNotify" :label="$t('alertRule.table[5]')" width="120">
                        <template #default="scope">
                            <span v-text="showAlertNotify(scope.row.alertNotify)"></span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="ruleContent" :label="$t('alertRule.table[6]')" min-width="200" />
                    <el-table-column :label="$t('alertRule.table[7]')" width="120" fixed="right">
                        <template #default="scope">
                            <el-button link type="primary" size="small" @click="showDetail(scope.row.ruleId)">{{
                                $t('app.view')
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
        <RuleDetail :ruleId="curId" @updateRule="updateRule" @cancelRule="cancelRule" :state="state" />
    </div>
</template>

<script setup lang="ts">
import { Search } from "@element-plus/icons-vue";
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import RuleDetail from "@/views/alert/AlertRule/RuleDetail.vue";
const { t } = useI18n();

const showMain = ref<boolean>(true)
const curId = ref<number>()
const state = ref<string>()
const tableDatas = ref<any[]>([]);
const page = reactive({
    currentPage: 1,
    pageSize: 10,
    total: 0,
});
const ruleTypeList = reactive(['index', 'log'])
const levelList = reactive(['serious', 'warn', 'info'])
const formData = ref<any>({
    ruleName: '',
    ruleType: '',
    level: ''
})

const { data: ruleResult, run: requestData } = useRequest(
    () => {
        let params = {}
        params = Object.assign(params, formData.value, { pageNum: page.currentPage, pageSize: page.pageSize })
        return request.get("/api/v1/alertRule", params)
    },
    { manual: true }
);
watch(ruleResult, (ruleResult: any) => {
    if (ruleResult && ruleResult.code === 200) {
        tableDatas.value = ruleResult.rows || []
        page.total = ruleResult.total
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
const handleSizeChange = (val: any) => {
    page.pageSize = val
    requestData()
}
const handleCurrentChange = (val: any) => {
    page.currentPage = val
    requestData()
}
const showAlertNotify = (val: string) => {
    if (!val) {
        return ''
    }
    let arr = val.split(',');
    let result = arr.map(item => t(`alertRule.${item}`)).join(',')
    return result
}

const showDetail = (id: number) => {
    curId.value = id
    state.value = "detail"
    showMain.value = false
}
const updateRule = () => {
    formData.value = {
        ruleName: '',
        ruleType: '',
        level: ''
    }
    search()
    curId.value = undefined
    showMain.value = true
}
const cancelRule = () => {
    curId.value = undefined
    showMain.value = true
}
onMounted(() => {
    requestData()
})

</script>
<style lang='scss' scoped>
.el-table {
    height: calc(100vh - 110px - 62px - 42px - 34px);
}
</style>