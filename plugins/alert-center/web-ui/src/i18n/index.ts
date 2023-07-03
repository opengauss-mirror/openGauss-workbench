import { createI18n } from 'vue-i18n';
import zhCn from '@/i18n/zhCn';
import en from '@/i18n/en';

export type LocaleType = "zhCn" | "en"

export const i18n = createI18n({
    legacy: false,
    locale: 'zhCn',
    messages: {
        zhCn,
        en,
    },
})
