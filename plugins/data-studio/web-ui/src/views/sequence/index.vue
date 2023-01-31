<template>
  <div class="table-page">
    <div class="ace-wrapper" v-loading="loading">
      <AceEditor ref="editorRef" style="margin: 4px 0; border: 1px solid #ddd" />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, reactive, ref } from 'vue';
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
    const { connectInfoName, sequenceName, schema } = route.query;
    Object.assign(commonParams, {
      connectionName: connectInfoName,
      sequenceName,
      schema,
    });
    getData();
  });
</script>
<style lang="scss" scoped>
  .table-page {
    height: 100%;
    padding: 10px 20px;
    position: relative;
  }
  .ace-wrapper {
    flex: 1;
    padding: 10px;
    height: 100%;
  }
</style>
