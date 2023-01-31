<template>
  <el-dropdown>
    <svg-icon icon-class="lang" style="width: 20px; height: 20px" />
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="locale in $i18n.availableLocales"
          :key="`locale-${locale}`"
          @click="changeLang(locale as LocaleType)"
        >
          {{ locale === 'en-US' ? 'English' : '简体中文' }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
  import { LocaleType } from '@/i18n/index';
  import { useI18n } from 'vue-i18n';
  import { useAppStore } from '@/store/modules/app';
  const { locale } = useI18n();
  const AppStore = useAppStore();

  const changeLang = (lang: LocaleType) => {
    if (locale.value == lang) {
      return;
    } else {
      locale.value = lang;
      AppStore.setLanguage(lang);
    }
  };
</script>

<style scoped lang="scss">
  :deep(.svg-icon) {
    cursor: pointer;
    &:focus {
      outline: none;
    }
  }
</style>
