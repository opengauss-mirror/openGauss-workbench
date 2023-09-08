import router from './router/index';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';
import { connectListPersist } from '@/config';
import { sidebarForage } from '@/utils/localforage';
import pinia from '@/store/index';
import { useAppStore } from '@/store/modules/app';

NProgress.configure({ showSpinner: false });

router.beforeEach(async (to, from, next) => {
  const AppStore = useAppStore(pinia);
  NProgress.start();
  if (typeof to.meta.title === 'string') {
    document.title = to.meta.title || 'Openguass-Datastudio';
  }
  if (!['debugChild'].includes(to.name as string)) {
    AppStore.updateAppMounted(true);
  }
  const isDSConnect: any[] = (await sidebarForage.getItem(connectListPersist.key)) || [];
  if (isDSConnect.length == 0 && to.name == 'home' && to.fullPath != '/home') {
    next({ path: '/home' });
  } else {
    next();
  }
});

router.afterEach(() => {
  NProgress.done();
});
