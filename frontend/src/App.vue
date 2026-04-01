<template>
  <div class="app-root">
    <router-view></router-view>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';

onMounted(() => {
  // Global splash or initialization if needed
});
</script>

<style>
.app-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>
