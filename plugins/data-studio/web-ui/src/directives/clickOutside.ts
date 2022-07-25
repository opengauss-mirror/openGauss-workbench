export default {
  mounted(el, binding) {
    function documentHandler(e: Event) {
      if (el.contains(e.target)) {
        return false;
      }
      if (binding.value && typeof binding.value == 'function') {
        binding.value(e);
      }
    }
    el.__vueClickOutside__ = documentHandler;
    document.addEventListener('click', documentHandler);
  },
  beforeUnmount(el) {
    document.removeEventListener('click', el.__vueClickOutside__);
    delete el.__vueClickOutside__;
  },
};
