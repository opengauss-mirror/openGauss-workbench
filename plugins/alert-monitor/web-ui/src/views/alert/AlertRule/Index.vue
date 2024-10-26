<template>
  <div v-if="showMain" id="alertRule">
    <div class="page-header">
      <div class="icon"></div>
      <div class="title">{{ t('alertRule.title') }}</div>
      <div class="seperator"></div>
      <el-breadcrumb separator="/" style="flex-grow: 1">
        <el-breadcrumb-item>
          {{ t('alertRule.title') }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="search-form">
      <div class="filter">
        <el-button type="primary" @click="addRule">{{ $t('app.add') }}</el-button>
      </div>
      <div class="seperator"></div>
      <div class="filter">
        <span>{{ $t('alertRule.ruleType') }}：</span>
        <el-select v-model="formData.ruleType" :placeholder="$t('alertRule.selectRuleType')" clearable @change="search" style="width: 150px">
          <el-option v-for="item in ruleTypeList" :key="item" :value="item" :label="$t(`alertRule.${item}`)" />
        </el-select>
      </div>
      <div class="filter">
        <span>{{ $t('alertRule.level') }}：</span>
        <el-select v-model="formData.level" :placeholder="$t('alertRule.selectLevelType')" style="width: 150px"
          clearable @change="search">
          <el-option v-for="item in levelList" :key="item" :value="item" :label="$t(`alertRule.${item}`)" />
        </el-select>
      </div>
      <div class="filter">
        <el-input v-model="formData.ruleName" style="width: 150px" :placeholder="$t('alertRule.ruleNamePlaceholder')" @keyup.enter="search">
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
          <el-table-column :label="$t('alertRule.table[8]')" min-width="200">
            <template #default="scope">
              <span v-html="showRuleExpDesc(scope.row)"></span>
            </template>
          </el-table-column>
          <el-table-column prop="ruleExpComb" :label="$t('alertRule.table[9]')">
            <template #default="scope">
              <span v-html="showRuleExpComb(scope.row.ruleExpComb)"></span>
            </template>
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
          <el-table-column :label="$t('alertRule.table[7]')" width="150" fixed="right">
            <template #default="scope">
              <el-link :underline="false" type="primary" size="small" style="margin-right: 10px" @click="showDetail(scope.row.ruleId)">{{
                $t('app.view')
              }}</el-link>
              <el-link :underline="false" type="primary" size="small" style="margin-right: 10px" @click="edit(scope.row.ruleId)">{{
                $t('app.edit')
              }}</el-link>
              <el-link :underline="false" type="primary" size="small" @click="del(scope.row.ruleId)">{{
                $t('app.delete')
              }}</el-link>
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
import { ElMessage, ElMessageBox } from "element-plus";
import { useI18n } from "vue-i18n";
import { i18n } from '@/i18n'
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
const ruleTypeList = reactive(['index', 'log', 'plugin'])
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

const showRuleExpDesc = (rule: any) => {
  const alertRuleItemList = rule.alertRuleItemList;
  if (!alertRuleItemList || alertRuleItemList.length === 0) {
    return '/';
  }

  if (rule.ruleType === 'index') {
    return alertRuleItemList.map((item: any) => {
      let ruleItemSrc = ruleItemSrcList.value.filter(item0 => item0.name === item.ruleExpName)[0]
      let paramStr = "";
      const ruleExpParam = item.ruleExpParam
      if (ruleExpParam) {
        let param = JSON.parse(ruleExpParam)
        paramStr = '(' + Object.keys(param).map((key: any) => param[key]).join(',') + ')'
      }
      let name = i18n.global.locale.value === 'zhCn' && ruleItemSrc && ruleItemSrc.nameZh ? ruleItemSrc.nameZh : (ruleItemSrc && ruleItemSrc.nameEn) ? ruleItemSrc.nameEn : ruleItemSrc ? t(`alertRule.${ruleItemSrc.name}`) : ''
      if (!item.operate) {
        return `[${item.ruleMark}]: ${name}`
      }
      return `[${item.ruleMark}]: ${name + paramStr + ' ' + (item.action === 'normal' ? (item.operate + item.limitValue + item.unit) : t(`alertRule.${item.action}Action`))}`
    }).join('<br />')
  } else {
    return alertRuleItemList.map((item: any) => {
      return `[${item.ruleMark}]:${t('alertRule.keyword')}(${item.keyword}${item.blockWord ? '，' + t('alertRule.blockWord') + ':' + item.blockWord : ''})${t('alertRule.happen')} ${item.operate}${item.limitValue}${t('alertRule.logUnit')}`
    }).join('<br />')
  }
}

const showRuleExpComb = (ruleExpComb: String) => {
  if (!ruleExpComb) {
    return '/'
  }
  return ruleExpComb.split(' ').map((item: String) => {
    if (item === 'and' || item === 'or') {
      return t(`alertRule.${item}`)
    }
    return item
  }).join(' ')
}

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
const addRule = () => {
  state.value = "add"
  showMain.value = false
}
const edit = (id: number) => {
  curId.value = id
  state.value = "edit"
  showMain.value = false
}
const showDetail = (id: number) => {
  curId.value = id
  state.value = "detail"
  showMain.value = false
}
const del = (id: number) => {
  ElMessageBox.confirm(
    t('app.deleteTip'),
    t('app.tip'),
    {
      confirmButtonText: t('app.confirm'),
      cancelButtonText: t('app.cancel'),
      type: 'warning',
    }
  ).then(() => {
    request.delete(`/api/v1/alertRule/${id}`).then((res: any) => {
      if (res && res.code === 200) {
        ElMessage({
          type: 'success',
          message: t('app.delSuccess'),
        })
        search()
      } else {
        ElMessage({
          type: 'error',
          message: t('app.delFail') + (res && res.msg ? "," + res.msg : ""),
        })
      }
    }).catch((res: any) => {
      ElMessage({
        type: 'error',
        message: t('app.delFail') + (res && res.msg ? "," + res.msg : ""),
      })
    })
  }).catch(() => {

  })
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
const ruleItemSrcList = ref<any[]>([])
const requestRuleItemSrcList = () => {
  request.get(`/api/v1/alertRule/ruleItemSrc/list`).then((res: any) => {
    if (res && res.code === 200) {
      ruleItemSrcList.value = res.data
    }
  })
}
onMounted(() => {
  requestData()
  requestRuleItemSrcList()
})

</script>
<style lang='scss' scoped>
.el-table {
  height: calc(100vh - 110px - 62px - 42px - 34px);
}
</style>