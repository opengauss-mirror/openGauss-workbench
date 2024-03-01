<template>
  <el-switch
    @change="(val: boolean) => changeSwitch(val)"
    v-model="isDarkTheme"
    :active-action-icon="Moon"
    :inactive-action-icon="Sunny"
  />
</template>

<script setup lang="ts" name="switchDark">
  import { isDark, toggleDark } from '@/hooks/dark';
  import { Sunny, Moon } from '@element-plus/icons-vue';

  const isDarkTheme = isDark;

  const changeSwitch = (val: boolean) => {
    toggleDark(val);
  };
</script>
