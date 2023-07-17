<template>
  <el-table :data="props.data" v-loading="props.loading" stripe>
    <el-table-column prop="conName" label="conName" min-width="100" />
    <el-table-column prop="attName" label="attName" min-width="100" />
    <el-table-column prop="conType" label="conType" min-width="100" />
    <el-table-column prop="pg_get_constraintdef" label="pg_get_constraintdef" min-width="100" />
    <el-table-column prop="conDeferrable" label="conDeferrable" min-width="80" />
    <el-table-column prop="nspName" label="nspName" min-width="100" />
    <el-table-column prop="tablespace" label="tablespace" min-width="100" />
  </el-table>
</template>

<script lang="ts" setup>
  const props = withDefaults(
    defineProps<{
      data: any;
      loading?: boolean;
    }>(),
    {
      data: [],
      loading: false,
    },
  );
</script>
