<script setup lang="ts">
const props = withDefaults(defineProps<{
    type?: 'default' | 'error',
}>(), {
    type: 'default',
})
const color = computed(() => props.type === 'default' ? "#177DDC" : "#FF4D4F")
</script>

<template>
<div class="dot-wrapper">
    <div class="dot"></div>
    <slot />
</div>
</template>

<style scoped>
.dot-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
}
.dot {
    width: 6px;
    height: 6px;
    background: v-bind(color);
    border-radius: 100px;
}
</style>
