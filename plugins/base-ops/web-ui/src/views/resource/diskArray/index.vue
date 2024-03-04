<template>
  <div id="disk-array">
    <a-row class="mb">
      <a-button type="primary" @click="create"
        >{{ $t("diskArray.create") }}
        <template #icon>
          <icon-plus />
        </template>
      </a-button>
    </a-row>
    <a-table
      style="height: 95%"
      row-key="name"
      :data="diskArrayList"
      :columns="columns"
      :pagination="true"
      :loading="isLoading"
      @page-change="currentPage"
      @page-size-change="pageSizeChange"
    >
      <template #operation="{ record }">
        <div class="flex-row">
          <a-link class="mr" @click="modify(record)">{{ $t("diskArray.modify") }}</a-link>
          <a-popconfirm
            :content="$t('diskArray.confirmDelete')"
            type="warning"
            :ok-text="$t('diskArray.ok')"
            :cancel-text="$t('diskArray.cancel')"
            @ok="remove(record)"
          >
            <a-link status="danger">{{ $t("diskArray.delete") }}</a-link>
          </a-popconfirm>
        </div>
      </template>
    </a-table>
    <Modal
      :isShow="isShowModal"
      :type="modalType"
      :data="selectItem"
      @updateList="getListData"
      @close="closeModal"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import Modal from "./Modal.vue";
import { MODAL_TYPE } from "./constant";
import { apiGetDiskArrayList, apiDeleteDiskArray } from "@/api/resource/diskArray";

const { t } = useI18n();

const isLoading = ref(false);
const isShowModal = ref(false);
const modalType = ref(MODAL_TYPE.CREATE);
const diskArrayList = ref<any>([]);
const selectItem = ref<any>({});

const columns = computed<any>(() => [
  {
    title: t("diskArray.diskArrayName"),
    dataIndex: "name",
    width: 300,
  },
  { title: t("diskArray.hostIp"), dataIndex: "hostIp", width: 220 },
  {
    title: t("diskArray.port"),
    dataIndex: "port",
    width: 200,
  },
  {
    title: t("diskArray.username"),
    dataIndex: "userName",
    width: 300,
  },
  {
    title: t("diskArray.pairId"),
    dataIndex: "pairId",
    width: 300,
  },
  {
    title: t("diskArray.operations"),
    slotName: "operation",
    width: 300,
    fixed: "right",
  },
]);

onMounted(() => {
  getListData();
});

const getListData = async () => {
  isLoading.value = true;
  const res: any = await apiGetDiskArrayList().catch(() => {});
  diskArrayList.value = res.data;
  isLoading.value = false;
};

const currentPage = (e: number) => {
  getListData();
};

const pageSizeChange = (e: number) => {
  getListData();
};

const create = () => {
  selectItem.value = {};
  modalType.value = MODAL_TYPE.CREATE;
  isShowModal.value = true;
};

const modify = (item: any) => {
  selectItem.value = Object.assign({}, item);
  modalType.value = MODAL_TYPE.UPDATE;
  isShowModal.value = true;
};

const remove = async (item: any) => {
  await apiDeleteDiskArray(item.name);
  getListData();
};

const closeModal = () => {
  isShowModal.value = false;
};
</script>

<style lang="less" scoped>
#disk-array {
  padding: 20px;
}
</style>
