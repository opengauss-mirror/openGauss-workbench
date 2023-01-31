<template>
  <el-table :data="props.data" v-loading="props.loading" stripe style="width: 100%">
    <el-table-column prop="attname" label="attname" min-width="180" />
    <el-table-column prop="typname" label="typname" min-width="180" />
    <el-table-column prop="attnotnull" label="attnotnull" min-width="180" />
    <el-table-column prop="description" label="description" min-width="180" />
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
