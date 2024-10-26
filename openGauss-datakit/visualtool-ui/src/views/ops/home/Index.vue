<template>
    <div class="home-c" id="home">
        <div class="left mr">
            <cluster-top></cluster-top>
            <cluster-list :has-plugin="data.isInstallPlugin"></cluster-list>
        </div>
        <div class="right">
            <common-oper></common-oper>
            <module-summary></module-summary>
            <busi-flow-summary @is-install-plugin="handleIsInstallPlugin"></busi-flow-summary>
        </div>
    </div>
</template>

<script setup lang="ts">
import ClusterList from '@/views/ops/home/components/ClusterList.vue'
import ClusterTop from './components/ClusterTop.vue'
import CommonOper from './components/CommonOper.vue'
import ModuleSummary from './components/ModuleSummary.vue'
import BusiFlowSummary from './components/BusiFlowSummary.vue'
import { reactive } from 'vue'
import { KeyValue } from '@/types/global'

const data = reactive<KeyValue>({
    isInstallPlugin: true
})

const handleIsInstallPlugin = (val: boolean) => {
    console.log(val)
    data.isInstallPlugin = false
}

</script>

<style lang="less" scoped>
.home-c {
    padding: 16px 20px;
    display: flex;

    .left {
        width: 70%;
    }

    .right {
        width: 30%;
    }
}
</style>
