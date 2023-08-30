
<template>
  <a-select v-bind="propsValue">
  </a-select>
</template>
<script setup lang="ts">
import { Select } from '@arco-design/web-vue'
import { computed } from 'vue'
const props = defineProps({ ...Select.props })
const propsValue = computed(() => props)
</script>
<style scoped></style>
