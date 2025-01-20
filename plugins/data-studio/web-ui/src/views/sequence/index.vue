<template>
  <div class="table-page">
    <div class="wrapper" v-loading="loading">
      <AceEditor ref="editorRef" type="form" />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useRoute } from 'vue-router';
  import { useUserStore } from '@/store/modules/user';
  import { getSequenceDdls } from '@/api/sequence';
  import AceEditor from '@/components/AceEditor.vue';

  const route = useRoute();
  const UserStore = useUserStore();
  const editorRef = ref();
  const commonParams = reactive({
    connectionName: '',
    schema: '',
    sequenceName: '',
    webUser: UserStore.userId,
    uuid: '',
  });
  const loading = ref(false);

  const getData = async () => {
    loading.value = true;
    getSequenceDdls(commonParams)
      .then((res) => {
        editorRef.value.setValue(res);
      })
      .finally(() => {
        loading.value = false;
      });
  };

  onMounted(() => {
    const { connectInfoName, sequenceName, schema, uuid } = route.query;
    Object.assign(commonParams, {
      connectionName: connectInfoName,
      sequenceName,
      schema,
      uuid,
    });
    getData();
  });
</script>
<style lang="scss" scoped>
  .table-page {
    height: 100%;
    padding: 10px;
    padding-left: 0;
    position: relative;
  }
  .wrapper {
    flex: 1;
    height: 100%;
  }
</style>
