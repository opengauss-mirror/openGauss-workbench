<template>
  <div class="cluster-install-item" id="dtClusterInstallItem">
    <div class="corner-mark">
      {{ role }}
    </div>
    <a-form auto-label-width :model="data" :rules="formRules" ref="formRef">
      <a-form-item field="cluster" :label="$t('dtCluster.cluster')" validate-trigger="blur">
        <a-select v-model="data.cluster" :placeholder="$t('dtCluster.selectCluster')">
          <a-option v-for="opt in clusterOptions" :label="opt" :value="opt" />
        </a-select>
      </a-form-item>
      <a-form-item field="diskArray" :label="$t('dtCluster.diskArray')" validate-trigger="blur">
        <a-select v-model="data.diskArray" :placeholder="$t('dtCluster.selectDiskArray')">
          <a-option v-for="opt in diskArrayOptions" :label="opt" :value="opt" />
        </a-select>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
const { t } = useI18n();

defineProps({
  role: {
    type: String,
    default: () => "",
  },
  data: {
    type: Object,
    default: () => ({}),
  },
  clusterOptions: {
    type: [String],
    default: () => [],
  },
  diskArrayOptions: {
    type: [String],
    default: () => [],
  },
});

const formRef = ref<any>();

const formRules = computed(() => {
  return {
    cluster: [{ required: true, message: t("dtCluster.selectCluster") }],
    diskArray: [{ required: true, message: t("dtCluster.selectDiskArray") }],
  };
});

defineExpose({
  validate: () => formRef.value?.validate(),
});
</script>

<style lang="less" scoped>
.cluster-install-item {
  border: 1px dashed var(--color-neutral-3);
  position: relative;
  padding: 20px;

  .corner-mark {
    background: var(--color-neutral-6);
    position: absolute;
    top: 0;
    left: 0;
    clip-path: polygon(0 0, 40px 0, 0 40px);
    width: 40px;
    height: 40px;
    color: #ffffff;
    padding: 5px 0 0 5px;
  }
}
</style>
