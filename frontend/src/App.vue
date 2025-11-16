<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import HelloWorld from './components/HelloWorld.vue'
import GameBoard from './components/GameBoard.vue';
import { useBoardStore } from './stores/boardStore';
import { onMounted } from 'vue'

const boardStore = useBoardStore();
// Board-Daten laden wenn die App startet
onMounted(async () => {
  console.log('App mounted - loading board data...')
  await boardStore.getBoard()
})
</script>

<template>
  <!-- Einbindung des Hauptspielfelds-->
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
