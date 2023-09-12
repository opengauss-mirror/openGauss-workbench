<template>
  <el-table :data="props.data" v-loading="props.loading" border>
    <el-table-column
      property="partition_name"
      :label="$t('createTable.partition.partitionName')"
      align="center"
    />
    <el-table-column
      prop="boundaries"
      :label="$t('createTable.partition.partitionBoundaries')"
      align="center"
    />
  </el-table>
</template>

<script lang="ts" setup>
  const props = withDefaults(
    defineProps<{
      columns: string[];
      data: any[];
      loading?: boolean;
    }>(),
    {
      columns: () => [],
      data: () => [],
      loading: false,
    },
  );
</script>
