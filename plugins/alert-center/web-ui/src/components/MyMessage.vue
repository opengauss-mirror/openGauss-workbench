<script setup lang="ts">
import { WarningFilled, CircleCloseFilled } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
    type: string;
    tip?: Error | undefined | string;
    defaultTip?: string;
}>(), {
    type: 'info',
    tip: undefined,
    defaultTip: '',
})

const baseColor = ref('');

const finalTip = ref<Error | undefined | string >();

onMounted(() => {
    switch (props.type) {
    case 'info':
        baseColor.value = '#0093FF';
        break;
    case 'error':
        baseColor.value = '#ff4d4f99';
        break;
    default:
        baseColor.value = '#FFF'
    }

    finalTip.value = props.tip !== undefined ? props.tip : props.defaultTip
})

</script>

<template>
    <div class="message-error">
        <div class="message-error-info" :style="{ border: `1px solid ${baseColor}` }">
            <el-icon :color="baseColor" size="18px" v-if="props.type === 'info'">
                <WarningFilled />
            </el-icon>
            <el-icon :color="baseColor" size="18px" v-if="props.type === 'error'">
                <CircleCloseFilled />
            </el-icon>
            <p>
                {{ finalTip }}
            </p>
        </div>
    </div>
</template>

<style scoped lang="scss">
.message-error {
    display: flex;
    justify-content: center;

    &-info {
        display: flex;
        align-items: center;
        max-width: 60%;
        padding: 4px 20px;
        border: 1px solid #0093FF;

        &>p {
            margin: 0 0 0 10px;
            line-height: 24px;
            color: var(--el-text-color-og);
            font-size: 12px;
        }
    }
}
</style>
