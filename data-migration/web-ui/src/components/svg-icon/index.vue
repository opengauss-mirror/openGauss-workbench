<template>
  <svg class="arco-icon" aria-hidden="true">
    <use :xlink:href="iconName" />
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  iconClass: String
})

const iconName = computed(() => `#icon-${props.iconClass}`)
</script>
