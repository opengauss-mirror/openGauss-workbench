<template>
  <el-table :data="props.data" v-loading="props.loading" border>
    <el-table-column type="index" width="100" :label="$t('common.serialNumber')" align="center" />
    <el-table-column prop="name" :label="$t('table.column.columnName')" align="center" />
    <el-table-column prop="type" :label="$t('table.column.dataType')" align="center" />
    <el-table-column prop="notnull" :label="$t('table.column.canBeNotNull')" align="center" />
  </el-table>
</template>

<script lang="ts" setup>
  const props = withDefaults(
    defineProps<{
      data: any[];
      loading?: boolean;
    }>(),
    {
      data: () => [],
      loading: false,
    },
  );
</script>
