<template>
  <div class="filter-table-data-header-slot">
    <div v-if="isShowFilter" class="flex-header-start">
      <div style="word-break: keep-all; margin-right: 5px">
        <slot />
      </div>
      <div class="filter-wrapper">
        <el-icon @click="hideFilter" class="icon-pointer">
          <Search />
        </el-icon>
        <el-input class="border-bottom-input" v-model="filterInput" clearable />
      </div>
    </div>
    <div v-else class="flex-header-between">
      <div style="width: 12px"></div>
      <span><slot /></span>
      <el-icon @click="showFilter" class="icon-pointer">
        <Search />
      </el-icon>
    </div>
  </div>
</template>
<script lang="ts" setup>
  const props = withDefaults(
    defineProps<{
      modelValue: string;
      show?: boolean;
    }>(),
    {
      modelValue: '',
      show: false,
    },
  );
  const emit = defineEmits<{
    (event: 'update:modelValue', text: string): void;
    (event: 'update:show', value: boolean): void;
  }>();
  const filterInput = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val),
  });
  const isShowFilter = ref(false);
  watch(
    () => props.show,
    (value) => {
      isShowFilter.value = value;
      if (!value) filterInput.value = '';
    },
  );
  watch(isShowFilter, (value: boolean) => {
    emit('update:show', value);
  });
  const showFilter = () => {
    isShowFilter.value = true;
  };
  const hideFilter = () => {
    isShowFilter.value = false;
  };
  defineExpose({
    showFilter,
    hideFilter,
  });
</script>
<style lang="scss" scoped>
  .flex-header-start {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    .filter-wrapper {
      flex: 1;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  .flex-header-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .icon-pointer {
    cursor: pointer;
    :hover {
      color: var(--normal-color);
    }
  }
  .border-bottom-input {
    box-shadow: none;
    height: auto;
    :deep(.el-input__wrapper) {
      box-shadow: none;
      .el-input__inner {
        box-shadow: 0 1px 0 0 var(--el-input-border-color);
      }
    }
  }
</style>
