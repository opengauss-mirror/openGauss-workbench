<template>
  <svg class="font-icon" aria-hidden="true">
    <use :xlink:href="`#icon-${props.icon}`"></use>
  </svg>
</template>

<script lang="ts" setup>
  const props = defineProps<{
    icon: string;
  }>();
</script>

<style>
  .font-icon {
    width: 1em;
    height: 1em;
    vertical-align: -0.15em;
    fill: currentColor;
    overflow: hidden;
  }
</style>
