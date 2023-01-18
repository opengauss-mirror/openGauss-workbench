<script setup lang="ts">
import { i18n, LocaleType } from '.';

const changeLang = (locale: LocaleType) => {
    i18n.global.locale.value = locale
}
</script>

<template>
    <el-dropdown>
        <svg-icon name="lang" style="width: 20px;height: 20px;" />
        <template #dropdown>
            <el-dropdown-menu>
                <el-dropdown-item
                    v-for="locale in $i18n.availableLocales"
                    :key="`locale-${locale}`"
                    @click="changeLang(locale as LocaleType)"
                >
                    {{ locale === 'en' ? '英文' : '中文' }}
                </el-dropdown-item>
            </el-dropdown-menu>
        </template>
    </el-dropdown>
</template>
