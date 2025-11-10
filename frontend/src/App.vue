<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import HelloWorld from './components/HelloWorld.vue'
import GameBoard from './components/GameBoard.vue';
</script>

<template>
    <GameBoard class="game-board"/>
</template>

<style>

.game-board {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
}


</style>
