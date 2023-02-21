import router from './router/index';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';
import { connectListPersist } from '@/config';
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
  const isDSConnect = connectListPersist.storage.getItem(connectListPersist.key);
  if (isDSConnect) {
    next();
  } else {
    if (to.fullPath == '/home') {
      next();
    } else {
      next({ path: '/home' });
    }
  }
});

router.afterEach(() => {
  NProgress.done();
});
