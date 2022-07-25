/**
 Copyright(c) vue-admin-perfect(zouzhibin).
*/
import { defineStore } from 'pinia';
import { storePersist } from '@/config';
import { manualStringify } from '@/utils';

export const useTagsViewStore = defineStore({
  id: 'tagsViewState',
  state: () => ({
    visitedViews: [],
    cachedViews: [],
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
      this.visitedViews = JSON.parse(
        storePersist.visitedViews.storage.getItem(storePersist.visitedViews.key) || '[]',
      );
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
    addView(view) {
      this.addVisitedView(view);
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
          title: view.meta?.title || view.query?.title || 'no-name',
          terminalNum: view.query?.terminalNum,
          isReload: true,
          loadTime: 1,
        }),
      );
      this.setVisitedViewsStorage();
      if (view.meta.keepAlive) {
        this.cachedViews.push(view.name);
      }
    },
    delView(view) {
      return new Promise((resolve) => {
        this.delVisitedView(view);
        this.delCachedView(view);
        resolve({
          visitedViews: [...this.visitedViews],
          cachedViews: [...this.cachedViews],
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
        this.cachedViews = this.cachedViews.filter((v) => {
          return v.path !== view.path || v.meta.affix;
        });
        this.setVisitedViewsStorage();
        resolve([...this.visitedViews]);
      });
    },
    delCachedView(view) {
      return new Promise((resolve) => {
        const index = this.cachedViews.indexOf(view.name);
        index > -1 && this.cachedViews.splice(index, 1);
        resolve([...this.cachedViews]);
      });
    },
    clearVisitedView() {
      this.delAllViews();
    },
    delAllViews() {
      return new Promise((resolve) => {
        this.visitedViews = this.visitedViews.filter((v) => v.meta.affix);
        this.cachedViews = this.visitedViews.filter((v) => v.meta.affix);
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
        storePersist.visitedViews.storage.setItem(
          storePersist.visitedViews.key,
          manualStringify(this.visitedViews),
        );
        setTimeout(() => {
          view.isReload = true;
        }, 300);
      }
    },
  },
});
