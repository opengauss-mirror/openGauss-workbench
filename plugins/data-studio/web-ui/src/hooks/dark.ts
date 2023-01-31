import { useDark, useToggle } from '@vueuse/core';
import { changePrimary } from '@/utils/theme';

export const isDark = useDark({
  storageKey: 'isDark',
});
export const toggleDark = (value?: boolean) => {
  useToggle(value || isDark);
  changePrimary(value || isDark.value);
};
