<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { useRequest } from 'vue-request';
import { useIntervalTime } from '../../../hooks/time';
import ogRequest from '../../../request';
import { useMonitorStore } from '../../../store/monitor';

const { tab, refreshTime, instanceId } = storeToRefs(useMonitorStore())

const { data, run } = useRequest(() => {
    if (tab.value !== 0) {
        return new Promise(() => {})
    }
    return ogRequest.get("/observability/v1/monitoring/server-metric", {
        // id: 'ogbrench',
        id: instanceId.value || 'ogbrench',
    })
}, { manual: true })

const formatter = (v: string | null, unit: string = '') => {
    if (v == null) {
        return '--'
    }
    let n = 1
    if (unit && unit.startsWith('MB')) {
        n = 1048576
    } else if (unit && unit.startsWith('KB')) {
        n = 1024
    }
    return `${(Number.parseFloat(v) / n).toFixed(2)}${unit}`
}

useIntervalTime(() => {
    if (instanceId.value) {
        run();
    }
}, computed(() => refreshTime.value * 1000))
watch(instanceId, id => {
    if (id) {
        run()
    }
}, { immediate: true })
</script>

<template>
    <div class="og-list">
        <div class="og-list-item">
            <div>
                <div>{{ $t('dashboard.cpuUsage') }}</div>
                <div class="og-list-item--number">{{ (!data || data.cpu == undefined) ? '--' : formatter(data.cpu, '%') }}</div>
            </div>
            <svg-icon name="cpu" />
        </div>
        <div class="og-list-item">
            <div>
                <div>{{ $t('dashboard.load5m') }}</div>
                <div class="og-list-item--number">{{ (!data || !data.node_load5 || data.node_load5.value == undefined) ? '--' : formatter(data.node_load5.value) }}</div>
            </div>
            <svg-icon name="load" />
        </div>
        <div class="og-list-item">
            <div>
                <div>{{ $t('dashboard.memoryUsage') }}</div>
                <div class="og-list-item--number">{{ (!data || data.memory == undefined) ? '--' : formatter(data.memory, '%') }}</div>
            </div>
            <svg-icon name="memory" />
        </div>
        <div class="og-list-item">
            <div>
                <div>{{ $t('dashboard.diskReadRate') }}</div>
                <div class="og-list-item--number">{{ (!data || data.disk_read == undefined) ? '--' : formatter(data.disk_read, 'KB/s') }}</div>
            </div>
            <svg-icon name="disk" />
        </div>
        <div class="og-list-item">
            <div>
                <div>{{ $t('dashboard.diskWriteRate') }}</div>
                <div class="og-list-item--number">{{ (!data || data.disk_written == undefined) ? '--' : formatter(data.disk_written, 'KB/s') }}</div>
            </div>
            <svg-icon name="disk" />
        </div>
        <div class="og-list-item">
            <div>
                <div>{{ $t('dashboard.uploadRate') }} </div>
                <div class="og-list-item--number">{{ (!data || data.network_transmit == undefined) ? '--' : formatter(data.network_transmit, 'MB/s') }}</div>
            </div>
            <svg-icon name="speed_up" />
        </div>
        <div class="og-list-item">
            <div>
                <div>{{ $t('dashboard.downloadRate') }}</div>
                <div class="og-list-item--number">{{ (!data || data.network_receive == undefined) ? '--' : formatter(data.network_receive, 'MB/s') }}</div>
            </div>
            <svg-icon name="speed_down" />
        </div>
    </div>
</template>

<style scoped lang="scss">
    .og-list {
        margin-bottom: 16px;
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 10px;
        height: 82px;
        background: var(--el-bg-color-og);
        border: 1px solid var(--el-og-border-color);
        border-radius: 8px;
        align-self: stretch;
        &-item {
            height: 66px;
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 38px;
            flex: 1;
            min-width: 0;
            border-right: 1px solid var(--el-og-border-color);
            &:last-of-type {
                border-right: none;
            }
            svg {
                width: 28px;
                height: 28px;
                fill: $og-svg-fill-color;
            }
            &--number {
                color: $og-sub-text-color;
                font-size: 16px;
                line-height: 24px;
                font-weight: 700;
            }
        }
    }
</style>
