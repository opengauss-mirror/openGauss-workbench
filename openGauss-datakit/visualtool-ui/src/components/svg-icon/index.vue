<template>
  <svg class="arco-icon" aria-hidden="true">
    <use :xlink:href="iconName" />
  </svg>
</template>

<script setup lang="ts">
  import { computed } from 'vue'

  const props = withDefaults(defineProps<{
    iconClass: string
  }>(), {
    iconClass: ''
  })

  const iconName = computed(() => `#icon-${props.iconClass}`)
</script>
