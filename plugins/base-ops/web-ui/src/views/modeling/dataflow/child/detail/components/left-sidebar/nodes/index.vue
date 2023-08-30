
<template>
  <component :is="componenta" :operate="operate" />
</template>
<script setup lang="ts">
import { defineAsyncComponent, computed, provide } from 'vue'
const coms: { [key: string]: any } = {
  TableNode: defineAsyncComponent(() => import(`./table/index.vue`)),
  BaseNode: defineAsyncComponent(() => import(`./BaseNode.vue`))
}
const props = defineProps<{ component: string, operate: any }>()
provide('node-event', props.operate)
const componenta = computed(() => {
  return coms[props.component]
})
</script>
<style scoped lang="less"></style>
