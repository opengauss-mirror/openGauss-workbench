<template>
  <div>
    <div :class="bindClass" @click="update">
      <slot></slot>
    </div>
    <div v-if="$slots.bottom" style="text-align: center;">
      <slot name="bottom"></slot>
    </div>
  </div>
</template>
<script setup>
import { computed } from 'vue';
const props = defineProps(['value', 'modelValue'])
const emit = defineEmits(['update:modelValue'])
const bindClass = computed(() => ({ 'radio-item-content': true, 'radio-item-content-active': props.modelValue === props.value }))
function update() {
  emit('update:modelValue', props.value);
}
</script>
<style lang="less" scoped>
.radio-item-content {
  border-radius: 4px;
  border: 1px solid var(--color-neutral-3);
  padding: 8px 12px;
  font-size: 12px;
  color: var(--color-text-2);
  display: flex;
  text-align: center;
  flex-direction: column;
  cursor: pointer;
}

.radio-item-content:hover,
.radio-item-content-active {
  border-color: rgb(var(--primary-6));
}
</style>