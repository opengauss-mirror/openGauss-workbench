<template>
  <el-dialog
    class="common-dialog-wrapper"
    v-model="visible"
    :title="$t('table.export.dialogTitle')"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @closed="resetForm"
  >
    <div>
      {{ $t('table.export.chooseFormat') }}
      <el-radio-group v-model="fileType">
        <el-radio v-for="item in fileTypeList" :key="item" :label="item">{{ item }}</el-radio>
      </el-radio-group>
    </div>
    <template #footer>
      <el-button @click="cancel">{{ $t('button.cancel') }}</el-button>
      <el-button type="primary" @click="confirm">{{ $t('button.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
  import { exportTableFilterData } from '@/api/table';
  import { loadingInstance, downLoadMyBlobType } from '@/utils';
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      type: 'current' | 'all';
      winId: string;
      uuid: string;
      schema: string;
      tableName: string;
      filterExpress: { filtration: any[]; order: any[] };
      pageNum: number;
      pageSize: number;
    }>(),
    {
      modelValue: false,
      filterExpress: () => ({
        filtration: [],
        order: [],
      }),
    },
  );
  const myEmit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void;
    (e: 'confirm'): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });

  const loading = ref(null);
  const fileTypeList = ref(['Excel(xlsx)', 'Excel(xls)']);
  const fileType = ref(fileTypeList.value[0]);

  const resetForm = () => {
    fileType.value = fileTypeList.value[0];
  };
  const confirm = async () => {
    visible.value = false;
    const params = {
      winId: props.winId,
      uuid: props.uuid,
      schema: props.schema,
      tableName: props.tableName,
      pageNum: props.type == 'current' ? props.pageNum : undefined,
      pageSize: props.type == 'current' ? props.pageSize : undefined,
      expansion: {
        filtration: props.filterExpress.filtration,
        order: props.filterExpress.order
          .filter((item) => item.multipleOrder)
          .map((item) => `${item.name} ${item.multipleOrder}`),
      },
      fileType: fileType.value,
    };
    try {
      loading.value = loadingInstance();
      const res = await exportTableFilterData(params);
      downLoadMyBlobType(res.name, res.data);
    } finally {
      loading.value.close();
    }
  };
  const cancel = () => {
    visible.value = false;
    resetForm();
  };
</script>
