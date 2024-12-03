<template>
  <a-button @click="click" type="primary" :loading="loading">刷新</a-button>
</template>
<script setup>
import { ref } from 'vue';

const props = defineProps(['onClick']);
const loading = ref(false);
function click() {
  loading.value = true;
  props.onClick().finally(() => {
    loading.value = false;
  })
}
</script>