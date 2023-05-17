<template>
    <div class="install-prompt-c">
        <div class="flex-col">
            <div
                class="mb"
                style="width: 50%;"
            >
                <div class="full-w">
                    <div class="item-node-top flex-between full-w">
                        <div class="flex-row">
                            <a-tag
                                class="mr"
                                color="#86909C"
                            >{{ $t('simple.InstallPrompt.5mpmunt3eco0') }}</a-tag>
                            <div class="label-color">{{ $t('simple.InstallPrompt.5mpmunt3ezw0') }}: {{ nodeData.privateIp
                            }}({{
    nodeData.publicIp
}})</div>
                        </div>
                        <div class="flex-row">
                            <icon-down
                                class="label-color"
                                style="cursor: pointer;"
                                v-if="!nodeData.isShow"
                                @click="nodeData.isShow = true"
                            />
                            <icon-up
                                class="label-color"
                                style="cursor: pointer;"
                                v-else
                                @click="nodeData.isShow = false"
                            />
                        </div>
                    </div>
                    <div
                        class="item-node-center full-w flex-col-start"
                        v-show="nodeData.isShow"
                    >
                        <div class="flex-row">
                            <div class="label-color lable-w">{{ $t('simple.InstallConfig.5mpmu0laqc80') }}</div>
                            <div class="label-color">{{ nodeData.clusterId }}</div>
                        </div>
                        <a-divider></a-divider>
                        <div class="flex-row">
                            <div class="label-color lable-w">{{ $t('simple.InstallPrompt.5mpmunt3f5k0') }}</div>
                            <div class="label-color">{{ nodeData.publicIp }}</div>
                        </div>
                        <a-divider></a-divider>
                        <div class="flex-row">
                            <div class="label-color lable-w">{{ $t('simple.InstallConfig.5mpmu0lar0c0') }}</div>
                            <div class="label-color">{{ nodeData.installUserName }}</div>
                        </div>
                        <a-divider></a-divider>
                        <div class="flex-row">
                            <div class="label-color lable-w">{{ $t('simple.InstallPrompt.5mpmunt3f9s0') }}</div>
                            <div class="label-color">{{ nodeData.installPath }}</div>
                        </div>
                        <a-divider></a-divider>
                        <div class="flex-row">
                            <div class="label-color lable-w">{{ $t('simple.InstallPrompt.5mpmunt3fjc0') }}</div>
                            <div class="label-color">{{ nodeData.port }}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { useOpsStore } from '@/store'
import { KeyValue } from '@/types/global'
import { computed, inject, onMounted, reactive } from 'vue'
const installStore = useOpsStore()

const loadingFunc = inject<any>('loading')

const nodeData = reactive<KeyValue>({
    isShow: true,
    clusterRole: '',
    clusterId: '',
    publicIp: '',
    privateIp: '',
    installUserName: '',
    installPath: '',
    dataPath: '',
    port: ''
})

onMounted(() => {
    loadingFunc.setNextBtnShow(true)
    loadingFunc.setBackBtnShow(true)
    installStore.getMiniConfig.nodeConfigList.forEach((item: KeyValue) => {
        Object.assign(nodeData, {
            clusterRole: item.clusterRole,
            clusterId: item.clusterId,
            publicIp: item.publicIp,
            privateIp: item.privateIp,
            installUserName: item.installUserName,
            installPath: item.installPath,
            dataPath: item.dataPath,
            port: item.port
        })
    })
})

const installConfig = computed(() => installStore.getMiniConfig)
const installNodeConfig = computed(() => installStore.getMiniConfig.nodeConfigList[0])
</script>
<style lang="less" scoped>
.install-prompt-c {
    .item-node-top {
        background-color: var(--color-text-4);
        line-height: 40px;
        border-radius: 4px;
        padding: 0 16px;
    }

    .item-node-center {
        padding: 20px;

        .lable-w {
            width: 100px;
        }
    }
}
</style>
