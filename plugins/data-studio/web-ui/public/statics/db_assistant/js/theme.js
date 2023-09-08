const toggleTheme = (isDark) => {
  if (isDark) {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
};
const isDark =
  localStorage.getItem('opengauss-theme') === 'dark' || localStorage.getItem('isDark') === 'dark';

toggleTheme(isDark);

document.addEventListener('DOMContentLoaded', () => {
  window.addEventListener(
    'message',
    (e) => {
      toggleTheme(e.data?.type === 'isDark' && e.data?.value);
    },
    false,
  );
});
