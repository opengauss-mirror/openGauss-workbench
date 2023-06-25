<script setup lang="ts">
import { routes } from '../router/index';

const route = useRoute()
const hasComponents: Record<string, string> = {}
for (const r of routes) {
    if (r.component) {
        hasComponents[r.meta?.title!] = r.path
    }
    if (r.children && r.children.length > 0) {
        for (const child of r.children) {
            if (child.component) {
                hasComponents[child.meta?.title!] = child.path
            }
        }
    }
}
const paths = computed(() => {
    const arr: { title?: string, path?: string }[] = []
    for (const r of routes) {
        if (r.path === route.path) {
            arr.push({ title: r.meta?.title })
            return arr
        }
        if (r.children && r.children.length > 0) {
            for (const child of r.children) {
                let isWithithParamPath = false;
                if (child.path.includes('/:')) {
                    isWithithParamPath = route.path.includes(child.path.slice(0, child.path.indexOf('/:')))
                }
                if ((child.path === route.path || isWithithParamPath) && child.meta?.breadcrumb) {
                    child.meta.breadcrumb.forEach(title => {
                        arr.push(hasComponents[title] ? { title, path: hasComponents[title] } : { title })
                    })
                    arr.push({ title: child.meta.title })
                    return arr
                }
            }
        }
    }
    return arr;
})
</script>

<template>
<el-breadcrumb>
    <el-breadcrumb-item v-for="p in paths" :key="p.title" :to="p.path">{{ $t(p.title!) }}</el-breadcrumb-item>
</el-breadcrumb>
</template>

<style scoped lang="scss">
    :deep(.el-breadcrumb__inner.is-link, .el-breadcrumb__inner a) {
        // color: $og-text-color;
        font-weight: 400;
    }
    :deep(.el-breadcrumb__inner.is-link:hover, .el-breadcrumb__inner a:hover) {
        text-decoration: underline;
    }
    :deep(.el-breadcrumb__item:last-of-type .el-breadcrumb__inner) {
        font-weight: 700;
    }
</style>
