import router from './router/index';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';
import { connectMenuPersist } from '@/config';

NProgress.configure({ showSpinner: false });

router.beforeEach(async (to, from, next) => {
  NProgress.start();
  if (typeof to.meta.title === 'string') {
    document.title = to.meta.title || 'Openguass-Datastudio';
  }

  const isDSConnect = connectMenuPersist.storage.getItem(connectMenuPersist.key);
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
