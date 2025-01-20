/**
 Copyright(c) vue-admin-perfect(zouzhibin).
*/
import { defineStore } from 'pinia';
import { storePersist } from '@/config';
import { manualStringify, loadingInstance } from '@/utils';
import { VisitedView } from '@/types/tagsView';
import { Router, RouteLocationNormalized } from 'vue-router';

let loading = null;
export const useTagsViewStore = defineStore({
  id: 'tagsViewState',
  state: () => ({
    visitedViews: <VisitedView[]>[],
    maxTagsId: 0,
  }),
  getters: {
    maxTerminalNum: (state) => {
      const terminalViews = state.visitedViews.filter((item) => {
        return ['createTerminal', 'terminal'].includes(item.name);
      });
      if (terminalViews.length == 0) return 0;
      const nums: number[] = terminalViews.map((item) => {
        return Number(item.terminalNum) || 0;
      });
      return Math.max(...nums);
    },
  },
  actions: {
    // read value from storage
    initVisitedViews() {
      const visitedViews = JSON.parse(
        storePersist.visitedViews.storage.getItem(storePersist.visitedViews.key) || '[]',
      );
      this.visitedViews = visitedViews.filter((item) => item.name != 'debugChild');
      const ids = this.visitedViews.map((item) => {
        return Number(item.id || 0);
      });
      this.maxTagsId = Math.max(...ids, 0);
    },
    getViewByRoute(route: RouteLocationNormalized) {
      return this.visitedViews.find((item) => item.path === route.path);
    },
    getViewById(id: number | string) {
      return this.visitedViews.find((item) => item.id == id);
    },
    getViewByOid(oid: string) {
      return this.visitedViews.find((item) => item.query?.oid == oid);
    },
    setVisitedViewsStorage() {
      const visitedViews = this.visitedViews.map((item) => {
        const { matched, redirectedFrom, ...others } = item;
        return others;
      });
      storePersist.visitedViews.storage.setItem(
        storePersist.visitedViews.key,
        manualStringify(visitedViews),
      );
    },
    removeView(routes: string[]) {
      return new Promise((resolve) => {
        this.visitedViews = this.visitedViews.filter((item) => !routes.includes(item.path));
        this.setVisitedViewsStorage();
        resolve(null);
      });
    },
    addVisitedView(route: RouteLocationNormalized) {
      if (this.visitedViews.some((v) => v.path === route.path)) return;
      this.visitedViews.push(
        Object.assign({}, route, {
          id: ++this.maxTagsId,
          title: route.meta?.title || route.query?.title || 'no-name',
          showName: route.meta?.fileName || route.query?.fileName || 'no-name',
          connectInfoName: route.query?.connectInfoName,
          dbname: route.query?.dbname,
          terminalNum: route.query?.terminalNum,
          isReload: true,
          loadTime: 1,
          isEdit: false,
        }),
      );
      this.setVisitedViewsStorage();
    },
    delView(view: VisitedView | RouteLocationNormalized) {
      this.delVisitedView(view);
      return { visitedViews: [...this.visitedViews] };
    },
    delCurrentView(route: Record<'path', string>) {
      const currentView = this.visitedViews.find((item) => item.path === route.path);
      if (!currentView || currentView.path == '/home') return;
      this.delVisitedView(currentView);
    },
    delVisitedView(view: VisitedView) {
      if (view.path) {
        this.visitedViews = this.visitedViews.filter((v) => {
          return v.path !== view.path || v.meta.affix;
        });
        this.setVisitedViewsStorage();
      }
    },
    delViewById(id: number | string) {
      this.visitedViews = this.visitedViews.filter((v) => {
        return v.id != id || v.meta.affix;
      });
      this.setVisitedViewsStorage();
      return this.visitedViews;
    },
    delViewByIds(ids: (string | number)[]) {
      if (Array.isArray(ids)) {
        this.visitedViews = this.visitedViews.filter((v) => {
          return !ids.includes(v.id) || v.meta.affix;
        });
        this.setVisitedViewsStorage();
      }
      return this.visitedViews;
    },
    delDbViews(dbname: string) {
      return new Promise((resolve) => {
        this.visitedViews = this.visitedViews.filter((v) => {
          return v.dbname !== dbname || v.meta.affix;
        });
        this.setVisitedViewsStorage();
        resolve([...this.visitedViews]);
      });
    },
    delAllViews() {
      return new Promise((resolve) => {
        this.visitedViews = this.visitedViews.filter((v) => v.meta.affix);
        this.setVisitedViewsStorage();
        resolve([...this.visitedViews]);
      });
    },
    updateVisitedView(view: VisitedView | RouteLocationNormalized) {
      for (const v of this.visitedViews) {
        if (v.path === view.path) {
          Object.assign(v, view);
          break;
        }
      }
    },
    updateEditStatusById(tagId: number | string, status: boolean) {
      this.getViewById(tagId)!.isEdit = status;
    },
    updateEditStatusByRoute(route: RouteLocationNormalized, status: boolean) {
      this.getViewByRoute(route)!.isEdit = status;
    },
    reloadView(fullPath: string) {
      const view = this.visitedViews.find((item) => item.fullPath == fullPath);
      if (view) {
        view.isReload = false;
        view.loadTime = view.loadTime + 1;
        this.setVisitedViewsStorage();
        setTimeout(() => {
          view.isReload = true;
        }, 300);
      }
    },
    renameTagById(tagId: number | string, newName: string) {
      const tagView = this.visitedViews.find((item) => item.id == tagId);
      tagView.showName = encodeURIComponent(newName);
      tagView.title = encodeURIComponent(`${newName}-${tagView.query.title}`);
      this.setVisitedViewsStorage();
    },
    getDebugChildViews(parentTagId: number | string) {
      const availableViews = this.visitedViews.filter((item) => item.name == 'debugChild');
      function findChildViews(availableViews, parentTagId) {
        const already = [];
        const surplus = [];
        availableViews.forEach((item) => {
          item.query.parentTagId == parentTagId ? already.push(item) : surplus.push(item);
        });
        let result = already;
        if (already.length > 0) {
          already.forEach((item) => {
            result = result.concat(findChildViews(surplus, item.id));
          });
        }
        return result;
      }
      return findChildViews(availableViews, parentTagId);
    },
    closeAllChildViews(rootTagId: number | string) {
      if (!rootTagId) return;
      loading = loadingInstance();
      const allChildDebugViewIds = this.visitedViews
        .filter((item) => item.query?.rootTagId == rootTagId)
        .map((item) => item.id);
      this.delViewByIds(allChildDebugViewIds);
      loading.close();
      loading = null;
    },
    closeCurrentTabToLatest(router: Router, route: RouteLocationNormalized) {
      this.delCurrentView(route);
      router.push(this.visitedViews.slice(-1)[0].fullPath);
    },
  },
});
