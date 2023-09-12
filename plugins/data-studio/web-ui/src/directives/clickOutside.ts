import { DirectiveBinding } from 'vue';
function isBody(node) {
  return node && node.nodeType == 1 && node.tagName.toLowerCase() == 'body';
}

function canFindParent(el, selectorClassName) {
  if (!el) return false;
  if (isBody(el)) return false;
  if (el.classList.contains(selectorClassName)) return true;
  return canFindParent(el.parentNode, selectorClassName);
}

export default {
  mounted(el, binding: DirectiveBinding) {
    function documentHandler(e: Event) {
      if (el.contains(e.target)) {
        return false;
      }
      const extraClass = Object.keys(binding.modifiers);
      for (const item of extraClass) {
        if (canFindParent(e.target, item)) return false;
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
