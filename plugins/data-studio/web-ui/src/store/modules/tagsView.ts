/**
 Copyright(c) vue-admin-perfect(zouzhibin).
*/
import { defineStore } from 'pinia';
import { storePersist } from '@/config';
import { manualStringify, loadingInstance } from '@/utils';

let loading = null;
export const useTagsViewStore = defineStore({
  id: 'tagsViewState',
  state: () => ({
    visitedViews: [],
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
        return item.id || 0;
      });
      this.maxTagsId = Math.max(...ids) || 0;
    },
    getCurrentView(route) {
      return this.visitedViews.find((item) => item.path === route.path);
    },
    getViewById(id) {
      return this.visitedViews.find((item) => item.id == id);
    },
    setVisitedViewsStorage() {
      const visitedViews = this.visitedViews.map((item) => {
        const { matched, ...others } = item;
        return others;
      });
      storePersist.visitedViews.storage.setItem(
        storePersist.visitedViews.key,
        manualStringify(visitedViews),
      );
    },
    removeView(routes) {
      return new Promise((resolve) => {
        this.visitedViews = this.visitedViews.filter((item) => !routes.includes(item.path));
        this.setVisitedViewsStorage();
        resolve(null);
      });
    },
    addVisitedView(view) {
      if (this.visitedViews.some((v) => v.path === view.path)) return;

      this.visitedViews.push(
        Object.assign({}, view, {
          id: ++this.maxTagsId,
          title: view.meta?.title || view.query?.title || 'no-name',
          connectInfoName: view.query?.connectInfoName,
          dbname: view.query?.dbname,
          terminalNum: view.query?.terminalNum,
          isReload: true,
          loadTime: 1,
        }),
      );
      this.setVisitedViewsStorage();
    },
    delView(view) {
      return new Promise((resolve) => {
        this.delVisitedView(view);
        resolve({
          visitedViews: [...this.visitedViews],
        });
      });
    },
    delCurrentView(route) {
      const currentView = this.visitedViews.find((item) => item.path === route.path);
      if (!currentView || currentView.path == '/home') return;
      this.delVisitedView(currentView);
    },
    delVisitedView(view) {
      return new Promise((resolve) => {
        this.visitedViews = this.visitedViews.filter((v) => {
          return v.path !== view.path || v.meta.affix;
        });
        this.setVisitedViewsStorage();
        resolve([...this.visitedViews]);
      });
    },
    delViewById(id) {
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
    delDbViews(dbname) {
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
    updateVisitedView(view) {
      for (let v of this.visitedViews) {
        if (v.path === view.path) {
          v = Object.assign(v, view);
          break;
        }
      }
    },
    reloadView(fullPath) {
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
    closeAllChildViews(rootTagId, router) {
      const rootDebugView = this.getViewById(rootTagId);
      if (rootDebugView?.fullPath) {
        loading = loadingInstance();
        router.push(rootDebugView.fullPath);
        const allChildDebugViewIds = this.visitedViews
          .filter((item) => item.query?.rootTagId == rootTagId)
          .map((item) => item.id);
        this.delViewByIds(allChildDebugViewIds);
        loading.close();
        loading = null;
      }
    },
  },
});
