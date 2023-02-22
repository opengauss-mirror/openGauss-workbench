import { getDarkColor, getLightColor } from '@/utils/index';

export const changePrimary = (isDark) => {
  // const primaryColor = isDark ? '#282b33' : '#e41d1d';
  const primaryColor = '#e41d1d';
  // deep color
  document.documentElement.style.setProperty(
    '--el-color-primary-dark-2',
    `${getDarkColor(primaryColor, 0.1)}`,
  );
  document.documentElement.style.setProperty('--el-color-primary', primaryColor);
  // light color
  for (let i = 1; i <= 9; i++) {
    document.documentElement.style.setProperty(
      `--el-color-primary-light-${i}`,
      `${getLightColor(primaryColor, i / 10)}`,
    );
  }
};
