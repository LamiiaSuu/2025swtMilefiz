<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import HelloWorld from './components/HelloWorld.vue'
</script>

<template>
  <!-- Einbindung des Hauptspielfelds-->
    <RouterView></RouterView>
</template>

<style>

.game-board {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
}


</style>
