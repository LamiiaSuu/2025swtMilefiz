<script setup lang="ts">
import TheWelcome from '../components/TheWelcome.vue'
import { useMilefizStore } from '@/stores/milefizstore'
import GameBoard from '@/components/GameBoard.vue';

// const milefizStore = useMilefizStore();
const { startMilefizLiveUpdate } = useMilefizStore();
startMilefizLiveUpdate();

console.log('Home view!')
</script>

<template>
  <main>
    <TheWelcome />
    <GameBoard class="game-board"/>
  </main>
</template>
