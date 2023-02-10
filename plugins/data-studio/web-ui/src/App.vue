<template>
  <el-config-provider size="small" :locale="locale">
    <router-view></router-view>
  </el-config-provider>
  <ConnectDialog
    v-if="AppStore.isReloadRouter"
    v-model="isConnectVisible"
    :type="connectType"
    :connectInfo="connectInfo"
  />
  <ConnectInfoDialog
    v-if="AppStore.isReloadRouter"
    v-model="isConnectInfoVisible"
    :connectInfo="connectInfo"
  />
</template>

<script lang="ts" setup>
  import zhCn from 'element-plus/dist/locale/zh-cn.mjs';
  import en from 'element-plus/dist/locale/en.mjs';
  import ConnectDialog from '@/views/connect/index.vue';
  import ConnectInfoDialog from '@/views/connect/Info.vue';
  import EventBus, { EventTypeName } from '@/utils/event-bus';
  import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
  import { useAppStore } from '@/store/modules/app';
  import { useUserStore } from '@/store/modules/user';
  import { useTagsViewStore } from '@/store/modules/tagsView';
  import { isDark, toggleDark } from '@/hooks/dark';
  import { useI18n } from 'vue-i18n';
  import { heartbeat } from '@/api/connect';

  const AppStore = useAppStore();
  const UserStore = useUserStore();
  const TagsViewStore = useTagsViewStore();
  TagsViewStore.initVisitedViews();

  const { locale: i18nLocale } = useI18n();
  const locale = computed(() => (AppStore.language === 'zh-CN' ? zhCn : en));

  const isConnectVisible = ref(false);
  const isConnectInfoVisible = ref(false);
  const connectType = ref<'create' | 'edit'>('create');
  const connectInfo = reactive<any>({});
  const heartbeatTimer = ref(null);

  onMounted(() => {
    UserStore.userId = 'A';
    // set init language(from platform)
    const lang = localStorage.getItem('locale') == 'en-US' ? 'en-US' : 'zh-CN';
    i18nLocale.value = lang;
    AppStore.setLanguage(lang);
    // set init theme(from platform)
    const isPlatformDarkTheme = localStorage.getItem('opengauss-theme') == 'dark';
    isDark.value = isPlatformDarkTheme;
    toggleDark(isPlatformDarkTheme);
    EventBus.listen(EventTypeName.OPEN_CONNECT_DIALOG, ({ dialogType, connectInfo: data }) => {
      Object.assign(connectInfo, data);
      connectType.value = dialogType;
      setTimeout(() => {
        isConnectVisible.value = true;
      }, 400);
    });
    EventBus.listen(EventTypeName.OPEN_CONNECT_INFO_DIALOG, (data) => {
      Object.assign(connectInfo, data);
      isConnectInfoVisible.value = true;
    });
    document.getElementById('app').style.height =
      import.meta.env.MODE === 'production' ? 'calc(100vh - 190px)' : '100%';

    window.$wujie?.bus.$on('opengauss-theme-change', (val) => {
      // 'light' | 'dark'
      isDark.value = val == 'dark';
      toggleDark(val == 'dark');
    });
    window.$wujie?.bus.$on('opengauss-locale-change', (val) => {
      // 'zh-CN' | 'en-US'
      i18nLocale.value = val;
      AppStore.setLanguage(val);
    });

    heartbeatTimer.value = setInterval(heartbeat, 20 * 1000);
  });
  onUnmounted(() => {
    EventBus.unListen(EventTypeName.OPEN_CONNECT_DIALOG);
    EventBus.unListen(EventTypeName.OPEN_CONNECT_INFO_DIALOG);
    clearInterval(heartbeatTimer.value);
    heartbeatTimer.value = null;
  });
</script>

<style lang="scss">
  #app {
    position: relative;
    width: 100%;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: #2c3e50;
  }
  .el-pager li:focus {
    border: none;
  }
  .el-dropdown:focus {
    border: none;
  }
  .svg-icon:focus {
    border: none;
  }
</style>
