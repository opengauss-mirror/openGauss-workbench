<template>
    <div class="search-form">
        <div class="filter">
            <el-input v-model="formData.searchText" style="width: 200px" :prefix-icon="Search" :placeholder="$t('datasource.logSearchPlaceholder')" />
        </div>
        <div class="filter">
            <span>{{ $t('datasource.logSearchTime') }}&nbsp;</span>
            <MyDatePicker v-model="formData.dateValue" class="search-time-range" type="datetimerange" :valueFormatToUTC="true" />
        </div>
        <el-button type="primary" class="search-button" @click="refreshLog" :title="$t('app.query')" :icon="Search">{{ $t('app.query') }}</el-button>
        <el-button @click="clean" :title="$t('app.reset')" :icon="Refresh">{{ $t('app.reset') }}</el-button>
    </div>

    <div>
        <my-card :title="$t('datasource.logDistributionMap')" :legend="waitLegends" :bodyPadding="false" ref="cardRef" collapse>
            <div class="loading-cover" v-if="refreshingBar" v-loading="refreshingBar"></div>
            <div class="line-wrap-chart" style="height: 250px">
                <my-bar v-if="showBar" :yname="$t('datasource.numberofLogs')" :xData="xData" :data="showData" :unit="$t('datasource.unit')" />
            </div>
        </my-card>
    </div>
    <div class="content-wrap" v-infinite-scroll="scrollToBottom">
        <div class="content-wrap-left">
            <div class="tree-wrap">
                <div class="tree-item">
                    <my-card :title="$t('app.clusterAndInstance')" :bodyPadding="false" collapse>
                        <el-scrollbar height="150px" style="width: 200px">
                            <div class="tree">
                                <el-tree ref="clusterTree" :data="treeData1" :props="defaultProps" show-checkbox @check="selectNodes">
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
            <el-table :data="tableData" size="small" style="width: 100%">
                <el-table-column :label="$t('datasource.logSearchTable[0]')" width="160" align="center">
                    <template #default="scope">
                        <span>{{ dayjs.utc(scope.row.logTime).local().format('YYYY-MM-DD HH:mm:ss') }}</span>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('datasource.logSearchTable[1]')" prop="logType" show-overflow-tooltip width="120" align="center" />
                <el-table-column :label="$t('datasource.logSearchTable[2]')" prop="logLevel" show-overflow-tooltip width="100" align="center" />
                <el-table-column :label="$t('datasource.logSearchTable[3]')" header-align="center">
                    <template #default="scope">
                        <span v-if="scope.row.logData && scope.row.logData.length > 300">
                            <el-popover width="700px" trigger="hover" :content="scope.row.logData" popper-class="sql-popover-tip">
                                <template #reference>
                                    <span v-html="showHighLightWord(scope.row.logData.substr(0, 300)) + '...'"></span>
                                </template>
                            </el-popover>
                        </span>
                        <span v-else v-html="showHighLightWord(scope.row.logData)"></span>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('datasource.logSearchTable[4]')" header-align="center" show-overflow-tooltip prop="logClusterId" width="100" />
                <el-table-column :label="$t('datasource.logSearchTable[5]')" header-align="center" show-overflow-tooltip prop="logNodeId" width="100" />
            </el-table>
            <p v-if="loading">Loading...</p>
            <p v-if="noMore">No more</p>
        </div>
    </div>
</template>

<script lang="ts" setup>
import qs from 'qs';
import dayjs from 'dayjs';
import ogRequest from '../../request';
import { useRequest } from 'vue-request';
import { Search, Refresh } from '@element-plus/icons-vue';
import { cloneDeep } from 'lodash-es';
import { LineData } from '../../components/MyBar.vue';

const pageSize = ref(30);
const loading = ref(false);
const refreshingBar = ref(false);
const showBar = ref(false);
const noMore = ref(false);
const scrollId = ref();
const clusterTree = ref<any>();
const logTypeTree = ref<any>();
const nodeIds = ref<string[]>([]);
const typeNames = ref<string[]>([]);
const logLevelSelected = ref<string[]>([]);

type LogsRes =
    | {
          scrollId: string;
          logs: string[];
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

// functions
const scrollToBottom = () => {
    if (!noMore.value) listLogScrollData();
};
const refreshLog = () => {
    noMore.value = false;
    loading.value = false;
    scrollId.value = null;
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
                label: item.clusterId ? item.clusterId : item.azName + '_' + item.publicIp + '(' + item.nodeId + ')',
                value: item.clusterId ? item.clusterId : item.nodeId,
                children: treeTransform(item.clusterNodes),
            });
        });
    }
    return obj;
};

// type data
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
        return ogRequest
            .get(
                '/logSearch/api/v1/logs?' +
                    qs.stringify(
                        filterNonNull({
                            nodeId: nodeIds.value.length > 0 ? nodeIds.value.join(',') : null,
                            logType: typeNames.value.length > 0 ? typeNames.value.join(',') : null,
                            searchPhrase: formData.searchText.length > 0 ? formData.searchText : null,
                            logLevel: logLevelSelected.value.length > 0 ? logLevelSelected.value.join(',') : null,
                            startDate: formData.dateValue.length ? formData.dateValue[0] : null,
                            endDate: formData.dateValue.length ? formData.dateValue[1] : null,
                            scrollId: scrollId.value,
                            rowCount: pageSize.value,
                        })
                    )
            )
            .finally(function () {
                loading.value = false;
            });
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
        if (res.logs.length < pageSize.value) noMore.value = true;
        tableData.value = tableData.value.concat(res.logs);
        scrollId.value = res.scrollId;
    } else {
        tableData.value = [];
    }
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
                            nodeId: nodeIds.value.length > 0 ? nodeIds.value.join(',') : null,
                            logType: typeNames.value.length > 0 ? typeNames.value.join(',') : null,
                            logLevel: logLevelSelected.value.length > 0 ? logLevelSelected.value.join(',') : null,
                            searchPhrase: formData.searchText.length > 0 ? formData.searchText : null,
                            startDate: formData.dateValue.length ? formData.dateValue[0] : null,
                            endDate: formData.dateValue.length ? formData.dateValue[1] : null,
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
        tableData.value = [];
    }

    nextTick(() => {
        showBar.value = true;
    });
});

onMounted(() => {
    listClusterData();
    listLogTypeData();
    listLogLevelData();
    refreshLog();

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
</style>
