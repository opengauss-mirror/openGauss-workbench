<template>
    <svg
        aria-hidden="true"
        style="vertical-align: middle; cursor: pointer;"
        :class="$style.icon"
        :fill="color"
    >
        <use :href="symbolId" />
    </svg>
</template>

<script setup lang="ts">
const props = withDefaults(
    defineProps<{
        prefix?: string;
        name: string;
        color?: string;
    }>(),
    {
        prefix: "icon",
    }
);
const symbolId = computed(() => `#${props.prefix}-${props.name}`);
</script>

<style scoped module>
.icon {
    width: 14px;
    height: 14px;
}
</style>
