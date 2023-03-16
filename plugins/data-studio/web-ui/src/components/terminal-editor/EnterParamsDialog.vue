<template>
  <div class="EnterParamsDialog">
    <el-dialog
      v-model="visible"
      :title="$t('paramsDialog.title')"
      width="740px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @close="cancel"
    >
      <el-table :data="props.data" stripe max-height="300">
        <el-table-column
          type="index"
          width="60"
          align="center"
          :label="$t('paramsDialog.column.no')"
        />
        <el-table-column
          prop="name"
          width="200"
          align="center"
          :label="$t('paramsDialog.column.parameterName')"
        />
        <el-table-column
          prop="type"
          width="200"
          align="center"
          :label="$t('paramsDialog.column.dataType')"
        />
        <el-table-column
          prop="value"
          align="center"
          :label="$t('paramsDialog.column.value')"
        >
          <template #default="scope">
            <el-input v-model="scope.row.value" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="confirm">{{ $t('button.confirm') }}</el-button>
        <el-button @click="clear">{{ $t('button.clear') }}</el-button>
        <el-button @click="cancel">{{ $t('button.cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';

  interface Data {
    name: string;
    type: string;
    value: string;
  }
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      data: Array<Data>;
    }>(),
    {
      modelValue: false,
    },
  );
  const myEmit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void;
    (e: 'confirm'): void;
    (e: 'clear'): void;
    (e: 'cancel'): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });

  const isStop = ref(true);
  const timer = ref(null);
  watch(visible, (val) => {
    if (val) isStop.value = true;
  });

  const confirm = () => {
    visible.value = false;
    isStop.value = false;
    myEmit('confirm');
  };
  const clear = () => {
    props.data.forEach((item) => {
      item.value = '';
    });
  };
  const cancel = () => {
    if (timer.value) return;
    visible.value = false;
    if (isStop.value) {
      myEmit('cancel');
    }
    timer.value = setTimeout(() => {
      timer.value = null;
    }, 400);
  };
</script>

<style scoped lang="scss">
  :deep(.el-dialog__body) {
    padding-top: 0;
    padding-bottom: 10px;
  }
</style>
