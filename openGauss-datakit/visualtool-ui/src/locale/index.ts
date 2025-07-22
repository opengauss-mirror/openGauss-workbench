import { createI18n } from 'vue-i18n'
import en from './en-US'
import cn from './zh-CN'
import urlEn from './hrefLink/urlEn.json'
import urlZh from './hrefLink/urlZh.json'

export const LOCALE_OPTIONS = [
  { label: '简体中文', value: 'zh-CN' },
  { label: 'English', value: 'en-US' }
]
const defaultLocale = localStorage.getItem('locale') || 'zh-CN'

const i18n = createI18n({
  locale: defaultLocale,
  fallbackLocale: 'en-US',
  globalInjection: true,
  allowComposition: true,
  messages: {
    'en-US': { ...en, ...urlEn },
    'zh-CN': { ...cn, ...urlZh }
  }
})

export default i18n
