import { createI18n } from 'vue-i18n';
import zhCn from './lang/zh-Cn';
import en from './lang/en';
import { storePersist } from '@/config';

export type LocaleType = 'zh-CN' | 'en-US';

const appState = JSON.parse(
  storePersist.appState.storage.getItem(storePersist.appState.key) || '{}',
);

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: appState.language || 'zh-CN',
  fallbackLocale: 'zh-CN',
  silentFallbackWarn: true,
  messages: {
    'zh-CN': {
      ...zhCn,
    },
    'en-US': {
      ...en,
    },
  },
});
