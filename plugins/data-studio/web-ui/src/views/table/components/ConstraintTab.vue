<template>
  <el-table :data="props.data" v-loading="props.loading" stripe>
    <el-table-column prop="conname" label="conname" min-width="100" />
    <el-table-column prop="attname" label="attname" min-width="100" />
    <el-table-column prop="contype" label="contype" min-width="100" />
    <el-table-column prop="pg_get_constraintdef" label="pg_get_constraintdef" min-width="100" />
    <el-table-column prop="condeferrable" label="condeferrable" min-width="80" />
    <el-table-column prop="nspname" label="nspname" min-width="100" />
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
