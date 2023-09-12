<template>
  <el-switch @change="(val: boolean) => changeSwitch(val)" v-model="isDarkTheme" />
</template>

<script setup lang="ts" name="switchDark">
  import { isDark, toggleDark } from '@/hooks/dark';

  const isDarkTheme = isDark;

  const changeSwitch = (val: boolean) => {
    toggleDark(val);
  };
</script>
