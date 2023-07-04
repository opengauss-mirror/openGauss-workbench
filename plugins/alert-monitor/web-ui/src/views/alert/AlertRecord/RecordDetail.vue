<template>
    <div>
        <div class="page-header">
            <div class="icon"></div>
            <div class="title">{{ t('alertRecord.detailTitle') }}</div>
            <div class="seperator"></div>
            <div class="alert-title">{{ t('alertRecord.title') }} </div>
            <div class="alert-seperator">&nbsp;/&nbsp;</div>
            <div class="alert-title">{{ t('alertRecord.detailTitle') }} </div>
        </div>

        <el-row style="margin-top: 8px;">
            <el-col :span="8">
                <div class="record-detail">
                    <el-space wrap>
                        <el-icon size="18" color="red">
                            <Bell />
                        </el-icon>
                        <h5 class="title">{{ t('alertRecord.alertInstance') }}</h5>
                    </el-space>
                    <el-form label-position="left" label-width="120">
                        <el-form-item :label="$t('alertRecord.clusterName') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ formData.clusterId }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('alertRecord.IPAndPort') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ formData.hostIpAndPort }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('alertRecord.nodeRole') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ formData.nodeRole }}</span>
                        </el-form-item>
                    </el-form>
                </div>
            </el-col>
            <el-col :span="8">
                <div class="record-detail">
                    <el-space wrap>
                        <el-icon size="18" color="red">
                            <Bell />
                        </el-icon>
                        <h5 class="title">{{ t('alertRecord.alertRule') }}</h5>
                    </el-space>
                    <el-form label-position="left" label-width="100">
                        <el-form-item :label="$t('alertRule.ruleName') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ formData.templateRuleName }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('alertRecord.table[6]') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ $t(`alertRule.${formData.level}`) }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('alertRecord.table[3]') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ formData.templateName }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('alertRecord.table[11]') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ formData.notifyWayNames }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('alertRecord.alertContent') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ formData.alertContent }}</span>
                        </el-form-item>
                    </el-form>
                </div>
            </el-col>
            <el-col :span="8">
                <div class="record-detail">
                    <el-space wrap>
                        <el-icon size="18" color="red">
                            <Bell />
                        </el-icon>
                        <h5 class="title">{{ t('alertRecord.alertStatus') }}</h5>
                    </el-space>
                    <el-form label-position="left" label-width="100">
                        <el-form-item :label="$t('alertRecord.table[7]') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ formData.startTime }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('alertRecord.table[8]') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ formData.endTime }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('alertRecord.table[9]') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <span>{{ durationFormat(formData.duration) }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('alertRecord.table[12]') + ':'" style="margin-bottom: 5px !important;margin-left: 5px;">
                            <el-switch v-model="formData.recordStatus" :active-value="1" :inactive-value="0"
                                @change="markAs" />
                        </el-form-item>
                    </el-form>
                </div>
            </el-col>
        </el-row>
        <div class="alert-table" style="margin-top: 8px;" v-if="formData.templateRuleType === 'index'">
            <div class="page-header" style="padding: 7px;">
                <div class="icon"></div>
                <div class="title" style="font-size: 14px;font-weight: 500;">{{ t('alertRecord.alertRelationView') }}</div>
            </div>
            <div v-for="(item, index) in relationDatas" style="width: 100%; height: 200px;margin-top: 20px;" :key="index">
                <RecordLine :title="item.name" :datas="item.datas" :unit="item.unit" />
            </div>
        </div>
    </div>
</template>

<script setup lang='ts'>
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { Bell } from "@element-plus/icons-vue";
import { useI18n } from "vue-i18n";
import RecordLine from "@/views/alert/AlertRecord/components/RecordLine.vue"
const { t } = useI18n();
const theme = localStorage.getItem('theme');
const color = ref<string>(theme === 'dark' ? '#d4d4d4' : '#1d212a')
const background = ref<string>(theme === 'dark' ? '#303030' : '#F5F7FB')

const offsetHeight = ref<number>()
const carHeight = ref<number>()
const formData = ref<any>({})
const relationDatas = ref<any[]>([])

const { data: res, run: requestData } = useRequest(
    (id: any) => {
        return request.get(`/api/v1/alertRecord/${id}`)
    },
    { manual: true }
);
watch(res, (res: any) => {
    if (res && res.code === 200) {
        formData.value = res.data
    }
});

const markAs = (val: any) => {
    if (formData.value.id) {
        if (val === 0) {
            request.post(`/api/v1/alertRecord/markAsUnread?ids=${formData.value.id}`).then((res: any) => {
                if (res && res.code === 200) {
                    requestData(formData.value.id)
                }
            })
        }
        if (val === 1) {
            request.post(`/api/v1/alertRecord/markAsRead?ids=${formData.value.id}`).then((res: any) => {
                if (res && res.code === 200) {
                    requestData(formData.value.id)
                }
            })
        }
    }
}

const { data: relationRes, run: requestRelationData } = useRequest(
    (id: any) => {
        return request.get(`/api/v1/alertRecord/${id}/relation`)
    },
    { manual: true }
);
watch(relationRes, (relationRes: any) => {
    if (relationRes && relationRes.code === 200) {
        relationDatas.value = relationRes.data
        console.log(relationDatas.value)
    }
});

const durationFormat = (val: any) => {
    if (typeof val === 'number') {
        if (val <= 0) {
            return '00:00:00';
        } else {
            let hh = Math.floor(val / 3600); 
            let shh = val - hh * 3600;
            let ii = Math.floor(shh / 60);
            let ss = shh - ii * 60;
            return (hh < 10 ? '0' + hh : hh) + ':' + (ii < 10 ? '0' + ii : ii) + ':' + (ss < 10 ? '0' + ss : ss);
        }
    } else {
        return '00:00:00';
    }
}

const router = useRouter();
onMounted(() => {
    offsetHeight.value = document.body.offsetHeight - 50
    carHeight.value = offsetHeight.value * 0.4
    let _id = null
    const wujie = window.$wujie;
    if (wujie) {
        _id = wujie?.props.data.id as number;
    } else {
        let param = router.currentRoute.value.query
        _id = param.id
    }
    if (_id) {
        requestData(_id)
        requestRelationData(_id)
    }
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
.alert-table {
    min-height: calc(100vh - 110px - 72px - 238px);
}
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.record-detail {
    background: v-bind(background);
    color: v-bind(color);
    border-radius: 2px;
    margin: 5px;
    height: 220px;
}

.title {
    font-style: 'normal';
    font-weight: 'bold';
    font-size: 14px;
    line-height: 22px;
    margin: 5px
}

.content {
    margin-top: 10px;
}
</style>