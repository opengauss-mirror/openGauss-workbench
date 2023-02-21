<template>
  <TerminalEditor v-if="AppStore.isMainViewMounted" editorType="debugChild"></TerminalEditor>
</template>

<script lang="ts" setup>
  import TerminalEditor from '@/components/terminal-editor/index.vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useAppStore } from '@/store/modules/app';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import { loadingInstance } from '@/utils';
  import { onMounted, ref } from 'vue';

  const route = useRoute();
  const router = useRouter();
  const AppStore = useAppStore();
  const TagsViewStore = useTagsViewStore();

  const loading = ref(null);

  onMounted(() => {
    if (!AppStore.isMainViewMounted) {
      const rootTagId = route.query.rootTagId;
      const rootDebugView = TagsViewStore.getViewById(rootTagId);
      if (rootDebugView?.fullPath) {
        loading.value = loadingInstance();
        router.push(rootDebugView.fullPath);
        const allChildDebugViewIds = TagsViewStore.visitedViews
          .filter((item) => item.query?.rootTagId == rootTagId)
          .map((item) => item.id);
        TagsViewStore.delViewByIds(allChildDebugViewIds);
        loading.value.close();
      }
    }
  });
</script>
