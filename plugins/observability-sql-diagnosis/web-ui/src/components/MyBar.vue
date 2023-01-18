<script setup lang="ts">
import { useDebounceFn } from '@vueuse/core';
import { useWidthOverflow } from '../hooks/dom';

const props = withDefaults(defineProps<{
    title: string,
    legend?: { color: string, name: string }[],
    bodyPadding?: boolean,
    height?: string | number,
    collapse?: boolean,
    collapsed?: boolean,
    skipBodyHeight?: boolean, // do not set body height
    // enable resize bodyHeight
    resize?: boolean,
    maxBodyHeight?: string,
    overflowHidden?: boolean,
}>(), {
    bodyPadding: true,
    maxBodyHeight: 'unset',
    overflowHidden: true,
})
const hidden = ref(props.overflowHidden ? 'hidden' : 'unset')
const padding = ref(props.bodyPadding ? "24px 16px" : "0px");
const bodyRef = ref<HTMLDivElement>()
const bodyHeight = ref(`calc(100% - ${props.bodyPadding ? 90 : 42}px)`)
const minBodyHeight = ref(bodyHeight.value)
// unset transition animation when resize body height
onMounted(() => {
    nextTick(() => {
        if (!props.skipBodyHeight && bodyRef && bodyRef.value) {
            bodyHeight.value = `${bodyRef.value.offsetHeight + (props.bodyPadding && !props.collapse ? 48 : 0)}px`
        }
        if (props.collapsed) isCollapse.value = true
        if (bodyRef && bodyRef.value && props.resize && window && 'ResizeObserver' in window) {
            const resizeObserver = new ResizeObserver(entries => {
                for (const entry of entries) {
                    if (!collapsing.value) {
                        bodyHeight.value = `${entry.contentRect.height}px`
                        if (minBodyHeight.value.startsWith('calc')) {
                            minBodyHeight.value = bodyHeight.value
                        }
                        myEmit('resize-body-height', entry.contentRect.height)
                    }
                }
            })
            resizeObserver.observe(bodyRef.value)
        }
    })
})
const myEmit = defineEmits<{
    (event: 'resize-body-height', height: number): void
}>()
// ctl height
const height = computed(() => {
    if (!props.height) {
        return '100%'
    }
    return (["%", "px"].includes(`${props.height}`)) ? props.height : `${props.height}px`
});
// ctl collapse
const isCollapse = ref(false)
// skip ResizeObserver event when collapsing
const collapsing = ref(false)
const resetCollapsing = useDebounceFn(() => {
    collapsing.value = false
    if (bodyRef && bodyRef.value) {
        bodyRef.value.style.transition = 'unset'
        bodyRef.value.style.minHeight = isCollapse.value ? '0px' : minBodyHeight.value
    }
}, 500)
const toggleCollapse = () => {
    collapsing.value = true
    if (bodyRef && bodyRef.value) {
        bodyRef.value.style.transition = 'height 0.5s cubic-bezier(0.23, 1, 0.32, 1)'
        bodyRef.value.style.minHeight = !isCollapse.value ? '0px' : minBodyHeight.value
    }
    isCollapse.value = !isCollapse.value
    resetCollapsing()
}
const extraRef = ref()
const dropdownRef = ref()
const { overflow, isFinish, trigger } = useWidthOverflow(extraRef, dropdownRef)
defineExpose({
    trigger
})
watch(overflow, o => {
    console.log(props.title + ':', o)
})
</script>

<template>
<div class="og-card">
    <div class="og-card-header">
        <span class="og-card-header--title">
            <svg-icon
                v-if="props.collapse"
                name="arrow"
                :class="{'is-collapse': isCollapse}"
                @click="toggleCollapse"
            />
            {{ props.title }}
        </span>
        <slot name="extra">
            <div
                ref="extraRef"
                class="og-card-header--extra"
                :style="{justifyContent: overflow || !isFinish ? 'flex-start' : 'flex-end'}"
            >
                <el-dropdown ref="dropdownRef" :hide-on-click="false" :max-height="200" style="position: absolute;">
                    <div></div>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item v-for="l in props.legend" :key="l.name">
                                <div class="hover">
                                    <div :style="{ background: l.color }" class="rect"></div>
                                    <span :title="l.name">{{ l.name }}</span>
                                </div>
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
                <div v-for="l in props.legend" :key="l.name">
                    <div :style="{ background: l.color }" class="rect"></div>
                    <span :title="l.name">{{ l.name }}</span>
                </div>
            </div>
            <span v-show="overflow">...</span>
        </slot>
    </div>
    <div
        class="og-card-body"
        :class="{'is-collapse': isCollapse}"
        ref="bodyRef"
        :style="{
            resize: props.resize ? 'vertical' : 'none',
        }"
    >
        <div>
            <slot />
        </div>
    </div>
</div>
</template>   

<style scoped lang="scss">
.og-card {
    box-sizing: border-box;
    height: v-bind(height);
    border: 1px solid $og-border-color;
    border-radius: 8px;
    overflow: v-bind(hidden);
    &-header {
        display: flex;
        overflow: hidden;
        background-color: var(--el-bg-color-sub);
        justify-content: space-between;
        padding: 0 16px;
        align-items: center;
        min-width: 0;
        height: 39px;
        border-bottom: 1px solid $og-border-color;
        &--title {
            font-weight: 700;
            font-size: 16px;
            flex-shrink: 0;
            > svg {
                position: relative;
                top: -2px;
                margin-right: 12px;
                transition: transform var(--el-transition-duration);
                &.is-collapse {
                    transform: rotate(-90deg);
                }
            }
        }
        &--extra {
            position: relative;
            display: flex;
            margin-left: 16px;
            overflow: hidden;
            flex: 1;
            min-width: 0;
            align-items: center;
            font-size: 12px;
            height: 40px;
            > div {
                display: flex;
                flex-shrink: 0;
                margin-right: 12px;
                min-width: 0;
                > span {
                    margin-left: 4px;
                }
                &:last-of-type {
                    margin-right: 0;
                }
            }
        }
    }
    &-body {
        box-sizing: border-box;
        height: v-bind(bodyHeight);
        min-height: v-bind(minBodyHeight);
        max-height: v-bind(maxBodyHeight);
        overflow: v-bind(hidden);
        background-color: var(--el-bg-color-og);
        border-radius: 0 0 8px 8px;
        will-change: height;
        > div {
            box-sizing: border-box;
            padding: v-bind(padding);
            height: v-bind(bodyHeight);
        }
        &.is-collapse {
            height: 0!important;
        }
    }
}
.hover {
    display: flex;
    align-items: center;
    > span {
        margin-left: 4px;
    }
}
.rect {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
}
</style>
