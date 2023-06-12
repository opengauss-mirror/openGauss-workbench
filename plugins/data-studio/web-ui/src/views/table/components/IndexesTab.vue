<template>
  <el-table :data="props.data" v-loading="props.loading" stripe>
    <el-table-column prop="relname" label="relname" min-width="100" />
    <el-table-column prop="string_agg" label="string_agg" min-width="100" />
    <el-table-column prop="indisunique" label="indisunique" min-width="100" />
    <el-table-column prop="expression" label="expression" min-width="100" />
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
