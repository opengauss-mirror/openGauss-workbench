<template>
  <div class="home">
    <TerminalEditor editorType="sql" v-if="showHome"></TerminalEditor>
  </div>
</template>

<script lang="ts" setup>
  import { useRoute, useRouter } from 'vue-router';
  import TerminalEditor from '@/components/terminal-editor/index.vue';
  import { ref, watch } from 'vue';
  import { useAppStore } from '@/store/modules/app';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { connectListPersist } from '@/config';

  const route = useRoute();
  const router = useRouter();
  const AppStore = useAppStore();
  const showHome = ref<boolean>(false);
  watch(
    route,
    (route, oldRoute) => {
      setTimeout(() => {
        if (route.name == 'home' && route?.fullPath != oldRoute?.fullPath) {
          if (route.fullPath == '/home') {
            const isDSConnect = JSON.parse(
              connectListPersist.storage.getItem(connectListPersist.key) || '[]',
            );
            showHome.value = false;
            if (isDSConnect.length) {
              router.replace({
                path: '/home',
                query: {
                  rootId: AppStore.lastestConnectDatabase.rootId,
                  connectInfoName: AppStore.currentConnectInfo?.name,
                  uuid: AppStore.lastestConnectDatabase.uuid,
                  dbname: AppStore.lastestConnectDatabase.databaseName,
                  time: Date.now(),
                },
              });
            } else {
              EventBus.notify(EventTypeName.CLOSE_ALL_TAB);
              EventBus.notify(EventTypeName.OPEN_CONNECT_DIALOG, 'create');
            }
          } else {
            showHome.value = !!(
              route.query.connectInfoName &&
              route.query.time &&
              AppStore.currentConnectInfo?.name
            );
          }
        }
      }, 200);
    },
    {
      immediate: true,
    },
  );
</script>
<style lang="scss" scoped>
  .home {
    height: 100%;
  }
</style>
