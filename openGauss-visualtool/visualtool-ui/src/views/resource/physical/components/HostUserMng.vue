<template>
    <a-modal :mask-closable="false" :esc-to-close="false" :visible="data.show" :title="data.title" @cancel="close"
        :modal-style="{ width: '450px' }" :footer="false">
        <div class="flex-col-start">
            <a-button class="mb" type="primary" @click="handleAddUser('create')">{{
                $t('components.HostUserMng.5mpi1bru0w00')
            }}</a-button>
            <a-table class="full-w" :data="list.data" :columns="columns" :loading="list.loading" size="mini">
                <template #operation="{ record }">
                    <div class="flex-row-start" v-if="record.username !== 'root'">
                        <a-link class="mr" @click="handleEditUser('update', record)">{{
                            $t('components.HostUserMng.5mpi1bru1n40')
                        }}</a-link>
                        <a-popconfirm :content="$t('components.HostUserMng.5mpi1bru2700')" type="warning"
                            :ok-text="$t('components.HostUserMng.5mpi1bru2lo0')"
                            :cancel-text="$t('components.HostUserMng.5mpi1bru2s00')"
                            @ok="handleDelUser(record.hostUserId)">
                            <a-link status="danger">{{ $t('components.HostUserMng.5mpi1bru2yo0') }}</a-link>
                        </a-popconfirm>
                    </div>
                </template>
            </a-table>
        </div>
    </a-modal>
    <add-host-user ref="addUserRef" @finish="getHostUserPage"></add-host-user>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { computed, reactive, ref } from 'vue'
import { hostUserPage, editHostUser, delHostUser } from '@/api/ops' // eslint-disable-line
import AddHostUser from './AddHostUser.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const hostId = ref('')

const data = reactive<KeyValue>({
    show: false,
    title: t('components.HostUserMng.5mpi1bru3dk0'),
    hostData: {}
})

const list: {
    data: Array<KeyValue>,
    loading: boolean
} = reactive({
    data: [],
    loading: false
})
const columns = computed(() => [
    { title: t('components.HostUserMng.5mpi1bru3vk0'), dataIndex: 'username' },
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
    console.log(userId)
    list.loading = true
    delHostUser(userId).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
            getHostUserPage()
        }
    }).finally(() => {
        list.loading = false
    })
}

const close = () => {
    data.show = false
}

const getHostUserPage = () => {
    list.loading = true
    hostUserPage(hostId.value).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
            list.data = res.rows
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

</style>
