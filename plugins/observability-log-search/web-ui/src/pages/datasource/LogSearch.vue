<template>
    <el-container>
        <el-aside :width="isCollapse ? '0px' : '300px'">
            <div style="height: 13px"></div>
            <Install />
        </el-aside>
        <el-main style="position: relative; overflow: visible; padding-top: 0px">
            <div>
                <div style="position: absolute; left: 20px; top: 3px; z-index: 9999" @click="toggleCollapse">
                    <el-icon v-if="!isCollapse" size="20px"><Fold /></el-icon>
                    <el-icon v-if="isCollapse" size="20px"><Expand /></el-icon>
                </div>
            </div>
            <div class="search-form">
                <div class="filter" v-if="showContextCount">
                    <div class="log-context">
                        <span>{{ $t('datasource.logContext') }}&nbsp;&nbsp;</span>
                        <select v-model="formData.contextCount" style="width: 70px">
                            <option  :value="5">{{t('app.logContextCountLabelList[0]') }}</option> 
                            <option  :value="10">{{t('app.logContextCountLabelList[1]')}}</option> 
                            <option  :value="20">{{t('app.logContextCountLabelList[2]') }}</option> 
                            <option  :value="30">{{t('app.logContextCountLabelList[3]') }}</option> 
                            <option  :value="40">{{t('app.logContextCountLabelList[4]') }}</option> 
                            <option  :value="50">{{t('app.logContextCountLabelList[5]') }}</option> 
                        </select>
                        <!-- top: -6px; -->
                        <el-icon class="el-input__icon" @click="hideContextCount" style="position: relative; left: 0; top: 2px; cursor: pointer"><CloseBold /></el-icon>
                        <!-- <el-button text bg type="" :icon="CloseBold" size="small" style="position: relative;  left: 0" @click="hideContextCount" /> -->
                    </div>
                </div>
                <div class="filter">
                    <el-input v-model="formData.searchText" style="width: 265px" :prefix-icon="Search" :placeholder="$t('datasource.logSearchPlaceholder')" />
                    <show-info :title="$t('datasource.showInfoTitleTip')">
                      <template #context>
                        <div style="white-space: pre-line;" v-html="$t('datasource.showInfoContextTip')"></div>
                      </template>
                    </show-info>
                </div>
                <div class="filter">
                    <span>{{ $t('datasource.logSearchTime') }}&nbsp;</span>
                    <MyDatePicker v-model="formData.dateValue" class="search-time-range" type="datetimerange" :valueFormatToUTC="true" />
                </div>
                <el-button type="primary" class="search-button" @click="refreshLog" :title="$t('app.query')" :icon="Search">{{ $t('app.query') }}</el-button>
                <el-button @click="clean" :title="$t('app.reset')" :icon="Refresh">{{ $t('app.reset') }}</el-button>
            </div>

            <div>
                <my-card :title="$t('datasource.logDistributionMap')" :legend="waitLegends" :bodyPadding="false" ref="cardRef" collapse :overflowHidden="overflowHidden" @click="toggleCard">
                    <div class="loading-cover" v-if="refreshingBar" v-loading="refreshingBar"></div>
                    <div class="line-wrap-chart" style="height: 250px" ref="lineChartRef">
                        <my-bar v-if="showBar" :yname="$t('datasource.numberofLogs')" :xData="xData" :data="showData" :unit="$t('datasource.unit')" :barWidth="barWidth" :barHeight="barHeight"/>
                    </div>
                </my-card>
            </div>
            <div class="content-wrap" v-infinite-scroll="scrollToBottom" :infinite-scroll-disabled="!canScroll">
                <div class="content-wrap-left">
                    <div class="tree-wrap">
                        <div class="tree-item">
                            <my-card :title="$t('app.clusterAndInstance')" :bodyPadding="false" collapse>
                                <el-scrollbar height="150px" style="width: 200px">
                                    <div class="tree">
                                        <el-tree ref="clusterTree" :data="treeData1" :props="defaultProps" node-key="value" :default-expanded-keys="nodeIds" :default-checked-keys="nodeIds" show-checkbox @check="selectNodes">
                                            <template #default="{ node }">
                                                <el-popover placement="right" width="auto" trigger="hover" :content="node.label">
                                                    <template #reference>
                                                        <span class="cluster-item">{{ node.label }}</span>
                                                    </template>
                                                </el-popover>
                                            </template>
                                        </el-tree>
                                    </div>
                                </el-scrollbar>
                            </my-card>
                        </div>
                        <div class="tree-item">
                            <my-card :title="$t('datasource.type')" :bodyPadding="false" collapse collapsed>
                                <el-scrollbar height="150px">
                                    <el-tree ref="logTypeTree" :data="treeData2" :props="defaultProps" show-checkbox @check="selectTypes" />
                                </el-scrollbar>
                            </my-card>
                        </div>
                        <div class="tree-item">
                            <my-card :title="$t('datasource.level')" :bodyPadding="false" collapse collapsed>
                                <el-scrollbar height="150px">
                                    <div class="tree-item-option">
                                        <el-checkbox-group v-model="logLevelSelected" @change="selectLevels">
                                            <el-checkbox v-for="item in logLevelData" :label="item" :key="item" />
                                        </el-checkbox-group>
                                    </div>
                                </el-scrollbar>
                            </my-card>
                        </div>
                    </div>
                </div>
                <div class="content-wrap-right">
                    <el-table :data="tableData" size="small" :row-style="tableRowStyle" style="width: 100%" @cell-mouse-enter="cellMouseEnter" @mouseleave="mouseLeave" ref="logTableData">
                        <el-table-column :label="$t('datasource.logSearchTable[0]')" width="160" align="center">
                            <template #default="scope">
                                <span>{{ dayjs.utc(scope.row.logTime).local().format('YYYY-MM-DD HH:mm:ss') }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('datasource.logSearchTable[1]')" prop="logType" width="120" align="center" >
                            <template #default="scope">
                                <el-popover trigger="hover" :content="scope.row.logType" popper-class="sql-popover-tip">
                                            <template #reference>
                                                <div style="height: 23px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">{{ scope.row.logType }}</div>
                                            </template>
                                </el-popover>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('datasource.logSearchTable[2]')" prop="logLevel" width="100" align="center" >
                            <template #default="scope">
                                <el-popover trigger="hover" :content="scope.row.logLevel" popper-class="sql-popover-tip">
                                            <template #reference>
                                                <div style="height: 23px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">{{ scope.row.logLevel }}</div>
                                            </template>
                                </el-popover>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('datasource.logSearchTable[3]')" header-align="center">
                            <template #default="scope">
                                <span v-if="scope.row.logData && isOverLimit(scope.row.logData, 300)">
                                    <el-popover width="700px" trigger="hover" popper-class="sql-popover-tip">
                                        <template #default>
                                            <span v-html="scope.row.logData"></span>
                                        </template>
                                        <template #reference>
                                            <span v-html="showLimitWord(scope.row.logData, 300) + '...'"></span>
                                        </template>
                                    </el-popover>
                                </span>
                                <span v-else v-html="scope.row.logData"></span>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('datasource.logSearchTable[4]')" header-align="center" prop="logClusterId" width="100" >
                            <template #default="scope">
                                <el-popover trigger="hover" :content="scope.row.logClusterId" popper-class="sql-popover-tip">
                                        <template #reference>
                                            <div style="height: 23px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">{{ scope.row.logClusterId }}</div>
                                        </template>
                                </el-popover>
                            </template>
                        </el-table-column>
                        <!-- <el-table-column :label="$t('datasource.logSearchTable[5]')" header-align="center" show-overflow-tooltip prop="logNodeId" width="100" /> -->
                        <el-table-column :label="$t('datasource.logSearchTable[5]')" header-align="center" prop="logNodeId" width="100" >
                            <template #default="scope">
                                <el-popover trigger="hover" :content="scope.row.logNodeId" popper-class="sql-popover-tip">
                                        <template #reference>
                                            <div style="height: 23px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">{{ scope.row.logNodeId }}</div>
                                        </template>
                                </el-popover>
                            </template>
                        </el-table-column>
                    </el-table>
                    <p v-if="loading">Loading...</p>
                    <p v-if="noMore">No more</p>
                </div>
                <div v-if="showSearchBtn">
                    <el-button :icon="DCaret" circle size="large" :style="searchBthStyle" @click="gotoNewLogSearch" />
                </div>
            </div>
        </el-main>
    </el-container>
</template>

<script lang="ts" setup>
import { Fold, Expand } from '@element-plus/icons-vue';
import qs from 'qs';
import dayjs from 'dayjs';
import ogRequest from '../../request';
import { useRequest } from 'vue-request';
import { Search, Refresh, DCaret, CloseBold } from '@element-plus/icons-vue';
import { cloneDeep } from 'lodash-es';
import { LineData } from '../../components/MyBar.vue';
import { useWindowStore } from '../../store/window';
import { storeToRefs } from 'pinia';
import Install from './install/Index.vue';
import { useI18n } from "vue-i18n";
import showInfo from '@/components/ShowInfo.vue'
const { t } = useI18n();

const { theme } = storeToRefs(useWindowStore());

const pageSize = ref(30);
const loading = ref(false);
const refreshingBar = ref(false);
const showBar = ref(false);
const noMore = ref(false);
const scrollId = ref();
const clusterTree = ref<any>();
const logTypeTree = ref<any>();
const nodeIds = ref<string[]>([]);
const sorts = ref<string[]>([]);
const typeNames = ref<string[]>([]);
const logLevelSelected = ref<string[]>([]);

type LogsRes =
    | {
          scrollId: string;
          logs: string[];
          sorts: string[];
      }
    | undefined;
type MapResItem = {
    dateTime: string;
    logCounts: MapResItem[];
    children: MapResItem[];
};
type MapRes = MapResItem[];
interface Tree {
    value: '';
    label: string;
    children?: Tree[];
}
const initFormData = {
    contextCount: 20,
    searchText: '',
    dateValue: [],
};

const defaultProps = {
    children: 'children',
    label: 'label',
    value: 'value',
};
const showData = ref<LineData[]>([{}]);
const formData = reactive(cloneDeep(initFormData));
const waitLegends = ref<{ color: string; name: string }[]>([]);
const cardRef = ref();
const tableData = ref<Array<any>>([]);
const treeData1 = ref<Array<Tree>>([]);
const treeData2 = ref<Array<Tree>>([]);
const logLevelData = ref<Array<String>>([]);
const xData = ref<string[]>([]);
const curLogData = ref<any>({ logDate: '', logType: '', logData: '', logLevelSelected: '', typeNames: '' });
const barWidth = ref<string>('100%');
const barHeight = ref<string>('100%');
const lineChartRef = ref();

const isCollapse = ref(true);
const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value;
};

const overflowHidden = ref<boolean>(false)
const toggleCard = () => {
    overflowHidden.value = !overflowHidden.value
}

const selectNodes = (item: Tree) => {
    nodeIds.value = [];
    clusterTree.value!.getCheckedNodes(true).forEach((item: any) => {
        nodeIds.value.push(item.value);
    });
    refreshLog();
};
const selectTypes = (item: Tree) => {
    typeNames.value = [];
    logTypeTree.value!.getCheckedNodes(true).forEach((item: any) => {
        typeNames.value.push(item.value);
    });
    refreshLog();
};
const selectLevels = (item: Tree) => {
    typeNames.value = [];
    logTypeTree.value!.getCheckedNodes(true).forEach((item: any) => {
        typeNames.value.push(item.value);
    });
    refreshLog();
};
const canScroll = ref<boolean>(true);
// functions
const scrollToBottom = () => {
    if (!noMore.value) listLogScrollData();
};
const refreshLog = () => {
    if (formData.searchText || (formData.dateValue && formData.dateValue.length > 0)) {
        showContextCount.value = false;
    }
    noMore.value = false;
    loading.value = false;
    scrollId.value = null;
    sorts.value = [];
    tableData.value = [];
    listLogScrollData();
    refreshMap();
};
const clean = () => {
    formData.searchText = '';
    formData.dateValue = [];
    refreshLog();
};
const showHighLightWord = (val: string) => {
    const reg = new RegExp(formData.searchText.replace(/([()[{*+.$^\\|?])/g, '\\$1'), 'ig');
    return val.replace(reg, (substring) => {
        return `<font color="orange">${substring}</font>`;
    });
};

const isOverLimit = (words: string, maxLength: number) => {
    if (!words || words.length <= maxLength) {
        return false
    }
    const str = words.replace(/<span[^>]*>|<\/span>/g, '');
    if (!str || str.length <= maxLength) {
        return false
    }
    return true
}

const showLimitWord = (words: string, maxLength: number) => {
    let result = '';
    let count = 0;
    let isInTag = false;
    let isPre = false;
    let n = words.length
    for (let i = 0; i < n; i++) {
        if (words[i] === '<') {
            if (words[i+1] === 's' && words[i+2] === 'p' && words[i+3] === 'a' && words[i+4] === 'n') {
                isInTag = true;
                isPre = true;
            }
            if (words[i+1] === '/' && words[i+2] === 's' && words[i+3] === 'p' && words[i+4] === 'a' && words[i+5] === 'n' && words[i+6] === '>') {
                isInTag = true;
                isPre = false;
            }
        } else if (words[i] === '>') {
            isInTag = false;
        } else {
            if (!isInTag) {
                count++;
            }
        }
        result += words[i];
        if (count === maxLength) {
            break;
        }
    }
    if (isPre) {
        result += '</span>'
    }
    return result
}

// cluster data
type Rer =
    | [
          {
              [propName: string]: string | number;
          }
      ]
    | undefined;
const { data: clusterData, run: listClusterData } = useRequest(
    () => {
        return ogRequest.get('/logSearch/api/v1/clusters', '');
    },
    { manual: true }
);
watch(clusterData, (rer: Rer) => {
    if (rer && Object.keys(rer).length) {
        treeData1.value = treeTransform(rer);
    }
});
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

const searchBthStyle = ref();

const showSearchBtn = ref<boolean>(false);
const curRow = ref();

const cellMouseEnter = (row: any, column: any, cell: any, event: any) => {
    if (curRow.value == row) {
        return;
    }
    let cellHeight = 0;
    let cellHeightStr = window.getComputedStyle(cell).getPropertyValue('height');
    if (cellHeightStr && cellHeightStr.endsWith('px')) {
        cellHeight = parseInt(cellHeightStr.replace('px', ''));
    }
    searchBthStyle.value = {
        position: 'fixed',
        right: '30px',
        top: event.pageY - event.offsetY + cellHeight / 2 - 15 + 'px',
        'z-index': 1000,
    };
    setTimeout(function () {}, 200);
    showSearchBtn.value = true;
    curRow.value = row;
};
const logTableData = ref();
const mouseLeave = (event: any) => {
    let pageY = event.pageY;
    let pageX = event.pageX;
    let top = logTableData.value.$el.getBoundingClientRect().top;
    let left = logTableData.value.$el.getBoundingClientRect().left;
    if (pageY <= top) {
        showSearchBtn.value = false;
        curRow.value = {};
        return;
    }
    if (pageX <= left) {
        showSearchBtn.value = false;
        curRow.value = {};
    }
};
const showContextCount = ref<boolean>(false);
const router = useRouter();
const gotoNewLogSearch = () => {
    if (process.env.mode === 'production') {
        window.$wujie?.props.methods.jump({
            name: `Static-pluginObservability-log-searchVemLog`,
            query: {
                showContextCount: 'true',
                nodeIds: nodeIds.value && nodeIds.value.length > 0 ? nodeIds.value : [curRow.value.logNodeId],
                // typeNames: typeNames.value.length > 0 ? typeNames.value.join(',') : '',
                // logLevelSelected: logLevelSelected.value.length > 0 ? logLevelSelected.value.join(',') : '',
                // logTime: curRow.value.logTime,
                // logType: curRow.value.logType,
                id: curRow.value.id,
                date: new Date().getTime(),
            },
        });
    } else {
        formData.searchText = '';
        formData.dateValue = [];
        formData.contextCount = 20;
        router.push({
            path: `/vem/log`,
            query: {
                showContextCount: 'true',
                nodeIds: nodeIds.value && nodeIds.value.length > 0 ? nodeIds.value : [curRow.value.logNodeId],
                // typeNames: typeNames.value.length > 0 ? typeNames.value.join(',') : '',
                // logLevelSelected: logLevelSelected.value.length > 0 ? logLevelSelected.value.join(',') : '',
                // logTime: curRow.value.logTime,
                // logType: curRow.value.logType,
                id: curRow.value.id,
                date: new Date().getTime(),
            },
        });
    }
    // showContextCount.value = true
};

const tableRowStyle = ({ row, rowIndex }) => {
    if (showContextCount.value && curLogData.id && curLogData.id == row.id) {
        return theme.value === 'dark'
            ? {
                  'background-color': 'rgba(235,223,132,0.2)',
              }
            : {
                  'background-color': 'rgba(238,102,102,0.1)',
              };
    }
};

const route = useRoute();
watch(
    () => route.query,
    (res) => {
        if (res && res.showContextCount && res.showContextCount == 'true') {
            showContextCount.value = true;
            formData.contextCount = 20;
        } else {
            showContextCount.value = false;
        }
        if (res && res.nodeIds) {
            nodeIds.value = (res.nodeIds as string[]) || [];
        }
        if (res && res.id) {
            curLogData.id = res.id;
        }
        // if (res && res.logTime) {
        //     curLogData.logTime = res.logTime;
        // }
        // if (res && res.logType) {
        //     curLogData.logType = res.logType;
        // }
        listLogTypeData();
        listLogLevelData();
        refreshLog();
    }
);

const hideContextCount = () => {
    showContextCount.value = false;
    formData.contextCount = 20;
    refreshLog();
};

type LogType =
    | [
          {
              [propName: string]: string | number;
          }
      ]
    | undefined;
const { data: logTypeData, run: listLogTypeData } = useRequest(
    () => {
        return ogRequest.get('/logSearch/api/v1/logTypes', '');
    },
    { manual: true }
);
watch(logTypeData, (rer: LogType) => {
    if (rer && Object.keys(rer).length) {
        treeData2.value = logTypeTreeTransform(rer, null);
    }
});
const logTypeTreeTransform = (arr: any, parent: any) => {
    let obj: any = [];
    if (arr instanceof Array) {
        arr.forEach((item) => {
            obj.push({
                label: item.typeName,
                value: (parent == null ? '' : parent.typeName + '-') + item.typeName,
                children: logTypeTreeTransform(item.children, item),
            });
        });
    }
    return obj;
};

// level data
type LogLevel = String[];
const { data: logLevelResData, run: listLogLevelData } = useRequest(
    () => {
        return ogRequest.get('/logSearch/api/v1/logLevels', '');
    },
    { manual: true }
);
watch(logLevelResData, (rer: LogLevel) => {
    logLevelData.value = rer;
});

// logs data
const {
    data: logsData,
    run: listLogScrollData,
    error,
} = useRequest(
    () => {
        loading.value = true;
        if (showContextCount.value) {
            return ogRequest
                .get(
                    '/logSearch/api/v1/logContextSearch?' +
                        qs.stringify(
                            filterNonNull({
                                nodeId: nodeIds.value.length > 0 ? nodeIds.value.join(',') : null,
                                logType: typeNames.value.length > 0 ? typeNames.value.join(',') : curLogData.typeNames ? curLogData.typeNames : null,
                                logLevel: logLevelSelected.value.length > 0 ? logLevelSelected.value.join(',') : curLogData.value.logLevelSelected ? curLogData.value.logLevelSelected : null,
                                // logDate: dayjs.utc(curLogData.logTime).tz('Europe/London').format('YYYY-MM-DDTHH:mm:ss.SSS') + 'Z',
                                scrollId: scrollId.value,
                                aboveCount: formData.contextCount,
                                belowCount: formData.contextCount,
                                sorts: sorts.value,
                                id: curLogData.id,
                                startDate: formData.dateValue.length ? dayjs.utc(formData.dateValue[0]).format('YYYY-MM-DDTHH:mm:ss.SSS') + 'Z' : null,
                                endDate: formData.dateValue.length ? dayjs.utc(formData.dateValue[1]).format('YYYY-MM-DDTHH:mm:ss.SSS') + 'Z' : null,
                                searchPhrase: formData.searchText.length > 0 ? formData.searchText : null,
                            })
                        )
                )
                .finally(function () {
                    loading.value = false;
                });
        } else {
            canScroll.value = false;
            return ogRequest
                .get(
                    '/logSearch/api/v1/logs?' +
                        qs.stringify(
                            filterNonNull({
                                nodeId: nodeIds.value.length > 0 ? nodeIds.value.join(',') : null,
                                logType: typeNames.value.length > 0 ? typeNames.value.join(',') : null,
                                searchPhrase: formData.searchText.length > 0 ? formData.searchText : null,
                                logLevel: logLevelSelected.value.length > 0 ? logLevelSelected.value.join(',') : null,
                                startDate: formData.dateValue.length ? dayjs.utc(formData.dateValue[0]).format('YYYY-MM-DDTHH:mm:ss.SSS') + 'Z' : null,
                                endDate: formData.dateValue.length ? dayjs.utc(formData.dateValue[1]).format('YYYY-MM-DDTHH:mm:ss.SSS') + 'Z' : null,
                                scrollId: scrollId.value,
                                sorts: sorts.value,
                                rowCount: pageSize.value,
                            })
                        )
                )
                .finally(function () {
                    loading.value = false;
                });
        }
    },
    { manual: true }
);
function filterNonNull(obj) {
    return Object.fromEntries(Object.entries(obj).filter(([k, v]) => v));
}

watch(error, () => {
    loading.value = false;
});
watch(logsData, (res: LogsRes) => {
    if (res && Object.keys(res).length) {
        if (res.logs && (!showContextCount && res.logs.length < pageSize.value || showContextCount && res.logs.length < formData.contextCount)) noMore.value = true;
        tableData.value = tableData.value.concat(res.logs);
        scrollId.value = res.scrollId;
        sorts.value = res.sorts;
    } else {
        tableData.value = [];
    }
    canScroll.value = true;
});

// map data
const { data: mapData, run: refreshMap } = useRequest(
    () => {
        refreshingBar.value = true;
        return ogRequest
            .get(
                '/logSearch/api/v1/logDistributionMap?' +
                    qs.stringify(
                        filterNonNull({
                            nodeId: nodeIds.value.length > 0 ? (nodeIds.value.join(',') as string) : null,
                            logType: typeNames.value.length > 0 ? (typeNames.value.join(',') as string) : null,
                            logLevel: logLevelSelected.value.length > 0 ? (logLevelSelected.value.join(',') as string) : null,
                            searchPhrase: formData.searchText.length > 0 ? formData.searchText : null,
                            startDate: formData.dateValue.length ? dayjs.utc(formData.dateValue[0]).format('YYYY-MM-DDTHH:mm:ss.SSS') + 'Z' : null,
                            endDate: formData.dateValue.length ? dayjs.utc(formData.dateValue[1]).format('YYYY-MM-DDTHH:mm:ss.SSS') + 'Z' : null,
                        })
                    )
            )
            .finally(function () {
                refreshingBar.value = false;
            });
    },
    { manual: true, debounceInterval: 300 }
);
watch(mapData, (res: MapRes) => {
    showBar.value = false;
    xData.value = [];
    showData.value = [{}];
    if (res && Object.keys(res).length) {
        // first loop: base on time
        res.forEach((element: MapResItem) => {
            xData.value.push(dayjs.utc(element.dateTime).local().format('MM-DD HH:mm:ss'));
            // second loop: base on first type
            element.logCounts.forEach((logCountFirst: any) => {
                const firstLogType = logCountFirst.logType;

                // second level data
                logCountFirst.children.forEach((logCountSecond: any) => {
                    let exist2 = false;
                    let typeIndex2 = -1;
                    const secondLogType = logCountSecond.logType;
                    const secondLogTypeShowName = firstLogType + '-' + secondLogType;
                    for (let index = 0; index < showData.value.length; index++) {
                        const showDataItem = showData.value[index];
                        const showDataType = showDataItem.name;
                        // is first level existed type
                        if (secondLogTypeShowName === showDataType) {
                            typeIndex2 = index;
                            exist2 = true;
                            break;
                        }
                    }
                    // not exist add
                    if (!exist2) {
                        showData.value.push({
                            name: secondLogTypeShowName,
                            stack: firstLogType,
                            data: [],
                        });
                        typeIndex2 = showData.value.length - 1;
                    }
                    // add data
                    showData.value[typeIndex2].data[res.indexOf(element)] = logCountSecond.logCount;
                });
            });
        });
    } else {
        showData.value = [{}];
        // tableData.value = [];
    }

    nextTick(() => {
        showBar.value = true;
    });
});

onMounted(() => {
    let _showContextCount = window.$wujie?.props.data.showContextCount as string;
    let _nodeIds = window.$wujie?.props.data.nodeIds as string[];
    let _id = window.$wujie?.props.data.id as string;
    // let _logTime = window.$wujie?.props.data.logTime as string;
    // let _logType = window.$wujie?.props.data.logType as string;
    // let _typeNames = window.$wujie?.props.data.typeNames as string;
    // let _logLevelSelected = window.$wujie?.props.data.logLevelSelected as string;
    let param = router.currentRoute.value.query;
    if (_showContextCount && _showContextCount == 'true') {
        showContextCount.value = true;
    } else {
        showContextCount.value = param && param.showContextCount && param.showContextCount == 'true' ? true : false;
    }
    _nodeIds = _nodeIds && _nodeIds.length > 0 ? _nodeIds : param && param.nodeIds ? (param.nodeIds as string[]) : [];
    if (typeof _nodeIds == 'string') {
        nodeIds.value = [];
        nodeIds.value.push(_nodeIds);
    } else {
        nodeIds.value = _nodeIds;
    }
    curLogData.id = _id ? _id : param && param.id ? param.id : '';
    listClusterData();
    listLogTypeData();
    listLogLevelData();
    refreshLog();

    let _barWidth = window.getComputedStyle(lineChartRef.value).getPropertyValue('width')
    let _barHeight = window.getComputedStyle(lineChartRef.value).getPropertyValue('height')
    barWidth.value = _barWidth
    barHeight.value = _barHeight
    // @ts-ignore
    const wujie = window.$wujie;
    // Judge whether it is a plug-in environment or a local environment through wujie
    if (wujie) {
        // Monitoring platform language change
        wujie?.bus.$on('opengauss-locale-change', (val: string) => {
            console.log('log-search catch locale change');
            showBar.value = false;
            nextTick(() => {
                showBar.value = true;
            });
        });
        wujie?.bus.$on('opengauss-theme-change', (val: string) => {
            showBar.value = false;
            nextTick(() => {
                showBar.value = true;
            });
        });
    }
});
</script>

<style lang="scss" scoped>
.loading-cover {
    height: 250px;
}
.content-wrap {
    margin-top: 20px;
    display: flex;

    &-left {
        margin-right: 20px;

        .tree-item {
            margin-bottom: 20px;

            .cluster-item {
                width: 300px;
                overflow-x: hidden;
                height: 20px;
                vertical-align: middle;
            }
        }

        .tree-item-option {
            height: 160px;
            overflow-y: scroll;
            padding: 0 10px;
        }
    }

    &-right {
        flex: 1;
    }
}

:deep(.el-collapse) {
    background-color: #212121;
    --el-collapse-header-bg-color: #212121;
    --el-collapse-content-bg-color: #212121;
    --el-collapse-border-color: none;
    border: none;
}

:deep(.el-collapse-item__header) {
    color: #d4d4d4;
    padding: 0 20px;
}

:deep(.el-collapse-item__content) {
    padding-bottom: 0;
}

:deep(.tree-item-option .el-checkbox-group) {
    display: flex;
    flex-direction: column;
}

.infinite-list {
    height: 300px;
    padding: 0;
    margin: 0;
    list-style: none;
}

.infinite-list .infinite-list-item {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 50px;
    background: var(--el-color-primary-light-9);
    margin: 10px;
    color: var(--el-color-primary);
}

.infinite-list .infinite-list-item + .list-item {
    margin-top: 10px;
}
.log-context {
    background-color: #f2f3f5;
    border: solid #dcdfe6;
    font-size: 13px;
    border-radius: 5px;
    color: #babcc2;
    padding: 1px 7px;
}
.log-context select {
    border: none;
    background-color: #f2f3f5;
    width: 100px;
    font-size: 15px;
    outline: none;
    // appearance:none; //去掉倒三角
}

html.dark .log-context {
    background-color: #343435;border: solid #484849;font-size:13px;border-radius:5px;color: #8b8b8e;padding:1px 7px
}
html.dark .log-context select {
    border: none;background-color: #343435;width: 100px;font-size:15px;outline:none;
}
// .log-context select:focus-visible{
//     border: 0;
// }
</style>
