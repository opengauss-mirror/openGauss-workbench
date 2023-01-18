import { createI18n } from 'vue-i18n';
import zhCn from './zhCn';
import en from './en';

export type LocaleType = "zhCn" | "en"

export const i18n = createI18n({
    legacy: false,
    locale: 'zhCn',
    messages: {
        zhCn,
        en,
    },
})
