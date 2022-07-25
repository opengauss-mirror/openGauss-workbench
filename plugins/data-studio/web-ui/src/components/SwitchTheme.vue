<template>
  <el-switch @change="(val) => changeSwitch(val)" v-model="isDarkTheme" />
</template>

<script setup lang="ts" name="switchDark">
  import { isDark, toggleDark } from '@/hooks/dark';

  const isDarkTheme = isDark;

  const changeSwitch = (val) => {
    toggleDark(val);
  };
</script>
