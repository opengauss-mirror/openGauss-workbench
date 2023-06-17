import type { RouteLocationNormalized } from 'vue-router'
import { defineStore } from 'pinia'
import { DEFAULT_ROUTE, DEFAULT_ROUTE_NAME } from '@/router/routes/index'
import { TabBarState, TagProps } from './types'

const formatTag = (route: RouteLocationNormalized): TagProps => {
  const { name, meta, fullPath, query } = route
  return {
    title: meta.title || '',
    name: String(name),
    fullPath,
    query,
    ignoreCache: meta.ignoreCache
  }
}

const useAppStore = defineStore('tabBar', {
  state: (): TabBarState => ({
    cacheTabList: new Set([DEFAULT_ROUTE_NAME]),
    tagList: [DEFAULT_ROUTE]
  }),

  getters: {
    getTabList (): TagProps[] {
      return this.tagList
    },
    getCacheList (): string[] {
      return Array.from(this.cacheTabList)
    }
  },

  actions: {
    updateTabList (route: RouteLocationNormalized) {
      this.tagList.push(formatTag(route))
      if (!route.meta.ignoreCache) {
        this.cacheTabList.add(route.name as string)
      }
    },
    deleteTag (idx: number, tag: TagProps) {
      this.tagList.splice(idx, 1)
      this.cacheTabList.delete(tag.name)
    },
    deleteTags (tag: TagProps) {
      const idx = this.tagList.findIndex(item => item.name === tag.name)
      this.tagList.splice(idx, 1)
      this.cacheTabList.delete(tag.name)
    },
    deleteTagByFullPath (tag: TagProps) {
      const idx = this.tagList.findIndex(item => item.fullPath === tag.fullPath)
      if (idx !== -1) {
        this.tagList.splice(idx, 1)
        this.cacheTabList.delete(tag.name)
      }
    },
    freshTabList (tags: TagProps[]) {
      this.tagList = tags
      this.cacheTabList.clear()
      // To judge ignoreCache first
      this.tagList
        .filter((el) => !el.ignoreCache)
        .map((el) => el.name)
        .forEach((x) => this.cacheTabList.add(x))
    },
    resetTabList () {
      this.tagList = [DEFAULT_ROUTE]
      this.cacheTabList.clear()
      this.cacheTabList.add(DEFAULT_ROUTE_NAME)
    }
  }
})

export default useAppStore
