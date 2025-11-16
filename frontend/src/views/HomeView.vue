<script setup lang="ts">
import TheWelcome from '../components/TheWelcome.vue'
import { useMilefizStore } from '@/stores/milefizstore'
import GameBoard from '@/components/GameBoard.vue';

// const milefizStore = useMilefizStore();
const { gamedata, startMilefizLiveUpdate, sendSocketMessage } = useMilefizStore();
startMilefizLiveUpdate();

setTimeout(function () {
  sendSocketMessage({ msg: "Hello World" });
  console.log('Home view!!')
}, 5000);
</script>

<template>
  <main>
    <TheWelcome />
    <GameBoard class="game-board" />
  </main>
</template>
