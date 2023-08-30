<template>
    <div class="install-prompt-c">
        <div class="flex-col">
            <div
                class="mb"
                style="width: 50%;"
                v-for="(itemNode, index) in data.nodeData"
                :key="index"
            >
                <div class="full-w">
                    <div class="item-node-top flex-between full-w">
                        <div class="flex-row">
                            <a-tag
                                class="mr"
                                color="#86909C"
                            >{{ getRoleName(itemNode.clusterRole) }}</a-tag>
                            <div class="label-color">{{ $t('lightweight.InstallPrompt.5mpmly4bw340') }}: {{
                                itemNode.privateIp }}({{
        itemNode.publicIp
    }})</div>
                        </div>
                        <div class="flex-row">
                            <icon-down
                                class="label-color"
                                style="cursor: pointer;"
                                v-if="!itemNode.isShow"
                                @click="itemNode.isShow = true"
                            />
                            <icon-up
                                class="label-color"
                                style="cursor: pointer;"
                                v-else
                                @click="itemNode.isShow = false"
                            />
                        </div>
                    </div>
                    <div
                        class="label-color item-node-center full-w flex-col-start"
                        v-show="itemNode.isShow"
                    >
                        <div
                            class="full-w"
                            v-if="index === 0"
                        >
                            <div class="flex-row">
                                <div class="lable-w">{{ $t('lightweight.InstallConfig.5mpmkfqy8nc0') }}</div>
                                <div>{{ itemNode.clusterId }}</div>
                            </div>
                            <a-divider></a-divider>
                        </div>
                        <div class="flex-row">
                            <div class="lable-w">{{ $t('lightweight.InstallPrompt.5mpmly4bwww0') }}</div>
                            <div>{{ itemNode.publicIp }}</div>
                        </div>
                        <a-divider></a-divider>
                        <div class="flex-row">
                            <div class="lable-w">{{ $t('lightweight.InstallConfig.5mpmkfqy9rw0') }}</div>
                            <div>{{ itemNode.installUserName }}</div>
                        </div>
                        <a-divider></a-divider>
                        <div class="flex-row">
                            <div class="lable-w">{{ $t('lightweight.InstallPrompt.5mpmly4bx7g0') }}</div>
                            <div>{{ itemNode.installPath }}</div>
                        </div>
                        <div
                            class="full-w"
                            v-if="index === 0"
                        >
                            <a-divider></a-divider>
                            <div class="flex-row">
                                <div class="lable-w">{{ $t('simple.InstallConfig.else6') }}</div>
                                <div>{{ itemNode.installPackagePath }}</div>
                            </div>
                        </div>
                        <a-divider></a-divider>
                        <div class="flex-row">
                            <div class="lable-w">{{ $t('lightweight.InstallPrompt.5mpmly4bxfg0') }}</div>
                            <div>{{ itemNode.dataPath }}</div>
                        </div>
                        <div
                            class="full-w"
                            v-if="index === 0"
                        >
                            <a-divider></a-divider>
                            <div class="flex-row">
                                <div class="lable-w">{{ $t('lightweight.InstallPrompt.5mpmly4bxlo0') }}</div>
                                <div>{{ itemNode.port }}</div>
                            </div>
                            <a-divider></a-divider>
                            <div class="flex-row">
                                <div class="lable-w">{{ $t('simple.InstallConfig.else11') }}</div>
                                <div>{{ itemNode.isEnvSeparate ? $t('enterprise.InstallPrompt.5mpmb9e6r4g0') :
                                    $t('enterprise.InstallPrompt.5mpmb9e6r800') }}</div>
                            </div>
                            <a-divider v-if="itemNode.isEnvSeparate"></a-divider>
                            <div
                                class="flex-row"
                                v-if="itemNode.isEnvSeparate"
                            >
                                <div class="lable-w">{{ $t('simple.InstallConfig.else9') }}</div>
                                <div>{{ itemNode.envPath }}</div>
                            </div>
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
import { inject, onMounted, reactive } from 'vue'
import { ClusterRoleEnum } from '@/types/ops/install'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const installStore = useOpsStore()

const data: {
    nodeData: KeyValue
} = reactive({
    nodeData: []
})

const loadingFunc = inject<any>('loading')

onMounted(() => {
    loadingFunc.setNextBtnShow(true)
    loadingFunc.setBackBtnShow(true)
    data.nodeData = []
    const storeInstallData = installStore.getInstallConfig
    const liteConfitData = installStore.getLiteConfig
    installStore.getLiteConfig.nodeConfigList.forEach(item => {
        const itemNode = getNodeData()
        Object.assign(itemNode, {
            clusterId: storeInstallData.clusterId,
            clusterRole: item.clusterRole,
            publicIp: item.publicIp,
            privateIp: item.privateIp,
            installUserName: item.installUserName,
            installPath: item.installPath,
            installPackagePath: liteConfitData.installPackagePath,
            dataPath: item.dataPath,
            port: item.port,
            isEnvSeparate: item.isEnvSeparate,
            envPath: item.envPath
        })
        data.nodeData.push(itemNode)
    })
})

const getNodeData = () => {
    return {
        isShow: true,
        clusterId: '',
        clusterRole: '',
        publicIp: '',
        privateIp: '',
        installUserName: '',
        installPath: '',
        installPackagePath: '',
        dataPath: '',
        port: '',
        isEnvSeparate: '',
        envPath: ''
    }
}

const getRoleName = (type: ClusterRoleEnum) => {
    switch (type) {
        case ClusterRoleEnum.MASTER:
            return t('lightweight.InstallPrompt.5mpmly4bxtg0')
        case ClusterRoleEnum.SLAVE:
            return t('lightweight.InstallPrompt.5mpmly4by1k0')
        default:
            return t('lightweight.InstallPrompt.5mpmly4by980')
    }
}

</script>
<style lang="less" scoped>
.install-prompt-c {
    height: 100%;
    overflow-y: auto;

    .item-node-top {
        background-color: var(--color-text-4);
        line-height: 40px;
        border-radius: 4px;
        padding: 0 16px;
    }

    .item-node-center {
        padding: 20px;

        .lable-w {
            width: 150px;
        }
    }
}
</style>
