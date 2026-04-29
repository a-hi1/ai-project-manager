<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';

onMounted(() => {
  // 捕获并忽略 play() 中断错误
  window.addEventListener('unhandledrejection', (event) => {
    if (
      event.reason &&
      event.reason.name === 'AbortError' &&
      event.reason.message &&
      event.reason.message.includes('The play() request was interrupted')
    ) {
      event.preventDefault();
    }
  });

  // 捕获并忽略特定的 DOM 异常
  const originalOnError = window.onerror;
  window.onerror = (message, source, lineno, colno, error) => {
    if (
      message &&
      typeof message === 'string' &&
      message.includes('The play() request was interrupted')
    ) {
      return true;
    }
    if (originalOnError) {
      return originalOnError(message, source, lineno, colno, error);
    }
    return false;
  };
});
</script>

<style>
/* 页面切换淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
