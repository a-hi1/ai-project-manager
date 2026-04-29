import { ElLoading } from 'element-plus';

const loadingDirective = {
  mounted(el: HTMLElement, binding: any) {
    if (binding.value) {
      el._loadingInstance = ElLoading.service({
        target: el,
        text: binding.arg || '加载中...',
        background: 'rgba(0, 0, 0, 0.3)'
      });
    }
  },
  updated(el: HTMLElement, binding: any) {
    if (binding.value && !binding.oldValue) {
      el._loadingInstance = ElLoading.service({
        target: el,
        text: binding.arg || '加载中...',
        background: 'rgba(0, 0, 0, 0.3)'
      });
    } else if (!binding.value && binding.oldValue) {
      if (el._loadingInstance) {
        el._loadingInstance.close();
        el._loadingInstance = null;
      }
    }
  },
  unmounted(el: HTMLElement) {
    if (el._loadingInstance) {
      el._loadingInstance.close();
      el._loadingInstance = null;
    }
  }
};

declare global {
  interface HTMLElement {
    _loadingInstance?: any;
  }
}

export default loadingDirective;
