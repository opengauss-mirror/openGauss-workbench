<template>
    <el-dialog v-model="data.show" :title="$t('physical.index.selectUserTable')" draggable :before-close="close" class="openDesignDialog" :z-index="900">
        <div class="flex-col-start bodyHeight">
            <div class="topArea">
                <div class="btnArea">
                    <el-button class="mb" type="primary" @click="handleAddUser('create')">{{
                        $t('components.HostUserMng.5mpi1bru0w00')
                    }}</el-button>

                    <el-popconfirm :title="$t('components.HostUserMng.5mpi1bru2700')" :visible="showDelPopover"
                        @confirm="multiDelete">
                        <template #reference>
                            <el-button class="multiDel" @click="deleteSelectedUserId">{{ t('physical.index.5mphf11rr590') }}</el-button>
                        </template>
                    </el-popconfirm>
                </div>
                <div class="searchArea mb16">
                     <el-input ref="inputRef" v-model="filterParam.name" class="o-style-search" :maxlength="100"
                    :placeholder="t('label.LabelManageDlg.tagNamePlaceholder')" :prefix-icon="IconSearch"
                    clearable @keyup.enter.native="clickSearch" @clear="clearSearch"></el-input>
                </div>
            </div>
            <el-table class="full-w openDesignTable" :data="list.data" :columns="columns" :loading="list.loading"
                size="small" @selection-change="handleSelectionChange">
                 <template #empty>
                    <div style="text-align: center;">
                    <div class="o-table-empty">
                        <el-icon class="o-empty-icon">
                        <IconEmpty></IconEmpty>
                        </el-icon>
                    </div>
                    <p>{{ t('physical.index.noData') || '--' }}</p>
                    </div>
                </template>
                <el-table-column type="selection" width="36"></el-table-column>
                <el-table-column :label="$t('components.HostUserMng.5mpi1bru3vk0')" prop="username" ellipsis
                    tooltip></el-table-column>
                <el-table-column :label="$t('components.HostUserMng.isSudo')" prop="sudo" ellipsis tooltip>
                    <template #default="scope">
                        {{ scope.row.sudo ? $t('components.HostUserMng.yes') : $t('components.HostUserMng.no') }}
                    </template>
                </el-table-column>
                <el-table-column :label="$t('components.HostUserMng.5mpi1bru4c00')" prop="operation" ellipsis tooltip>
                    <template #default="{ row }">
                        <div class="flex-row-start">
                            <el-button text @click="handleEditUser('update', row)">
                                {{ $t('components.HostUserMng.5mpi1bru1n40') }}
                            </el-button>
                            <el-popconfirm :title="$t('components.HostUserMng.5mpi1bru2700')"
                                v-if="list.data?.length > 1" @confirm="handleDelUser(row.hostUserId)">
                                <template #reference>
                                    <el-button status="danger" text>{{ $t('components.HostUserMng.5mpi1bru2yo0')
                                        }}</el-button>
                                </template>
                            </el-popconfirm>
                            <el-tooltip :content="$t('components.HostUserMng.oneUserTip')" v-else>
                                <el-button text disabled>{{ $t('components.HostUserMng.5mpi1bru2yo0') }}</el-button>
                            </el-tooltip>
                        </div>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <template #footer>
            <div style="flex: auto" class="footerBtn">
                <el-button type="primary" @click="close">{{ $t('physical.index.close') }}</el-button>
            </div>
        </template>
    </el-dialog>
    <add-host-user ref="addUserRef" @finish="getHostUserPage"></add-host-user>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { computed, reactive, ref } from 'vue'
import { hostUserPage, editHostUser, delHostUser } from '@/api/ops' // eslint-disable-line
import AddHostUser from './AddHostUser.vue'
import { useI18n } from 'vue-i18n'
import showMessage from '@/hooks/showMessage'
import { IconEmpty, IconSearch } from '@computing/opendesign-icons'
const { t } = useI18n()
const hostId = ref('')
const selectedIdList = ref<KeyValue []>([]);
const data = reactive<KeyValue>({
    show: false,
    title: t('components.AddHostUser.5mphzt9pdn80'),
    hostData: {}
})
const filterParam = reactive({
    name: ''
})
const showDelPopover = ref(false)
const allListData: Array<KeyValue> = ref([]);

const clickSearch = () => {
    // Here, the front-end query - to determine the filtering situation
    if (filterParam.name) {
        list.data = allListData.value?.filter(item => item.username?.includes(filterParam.name))
    } else {
        list.data = allListData.value;
    }
}

const clearSearch = () => {
    filterParam.name = ''
    clickSearch()
}

const list: {
    data: Array<KeyValue>,
    loading: boolean
} = reactive({
    data: [],
    loading: false
})
const columns = computed(() => [
    { title: t('components.HostUserMng.5mpi1bru3vk0'), dataIndex: 'username' },
    { title: t('components.HostUserMng.isSudo'), slotName: 'sudo' },
    { title: t('components.HostUserMng.5mpi1bru4c00'), slotName: 'operation', width: 200 }
])
const addUserRef = ref<null | InstanceType<typeof AddHostUser>>(null)

const handleAddUser = (type: string) => {
    addUserRef.value?.open(type, data.hostData)
}

const handleEditUser = (type: string, userData: KeyValue) => {
    addUserRef.value?.open('update', data.hostData, userData)
}

const handleDelUser = (userId: string) => {
    list.loading = true
    delHostUser(userId).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
            // Here we need to limit the input - first regression
            getHostUserPage()
        }
    }).finally(() => {
        list.loading = false
    })
}

const deleteSelectedUserId = () => {
    if (selectedIdList.value.length === 0) {
        showMessage('warning', t('label.LabelManageDlg.delWarning'))
        showDelPopover.value = false
    } else if (selectedIdList.value.length === allListData.value.length) {
        showMessage('warning', t('components.HostUserMng.oneUserTip'))
        showDelPopover.value = false
    } else {
        showDelPopover.value = true
    }
}

const multiDelete = () => {
    for (const hostUserId of selectedIdList.value) {
        handleDelUser(hostUserId)
    }
}

const close = () => {
    clearSearch()
    data.show = false
}

const handleSelectionChange = async (rows: KeyValue []) => {
    // rows is the currently selected user, which can be deleted by traversing and deleting
    selectedIdList.value = rows.map(row => row.hostUserId)
}

const getHostUserPage = () => {
    list.loading = true
    hostUserPage(hostId.value).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
            // total Data
            allListData.value = res.rows || []
            if (filterParam.name) {
                list.data = allListData.value?.filter(item => item.username?.includes(filterParam.name))
            } else {
                list.data = res.rows
            }
        }
    }).finally(() => {
        list.loading = false
    })
}

const open = (hostData: KeyValue) => new Promise(resolve => { // eslint-disable-line
    data.show = true
    data.hostData = hostData
    hostId.value = hostData.hostId
    data.title = t('components.HostUserMng.5mpi1bru3dk0')
    getHostUserPage()
})
defineExpose({
    open
})
</script>
<style lang="less" scoped>
.full-w {
    margin-bottom: 24px;
}

.bodyHeight {
    max-height: 332px;
    ::v-deep(.o-style-search) {

        .el-input__suffix {
            &::after {
                margin-top: 6px;
            }
        }
    }
    .openDesignTable {
        flex: 1;
        margin-bottom: 0px;
        overflow-y: auto;
    }
}

.topArea {
    width: 100%;

    .el-button {
        margin-bottom: 16px;
    }

    .multiDel {
        margin-left: 16px;
    }
}
</style>
