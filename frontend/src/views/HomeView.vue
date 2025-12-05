<script setup lang="ts">
import TheWelcome from '@/components/TheWelcome.vue'
import { useMilefizStore } from '@/stores/milefizstore'
import GameBoard from '@/components/GameBoard.vue'
import { onMounted } from 'vue'
import GameHUD from '@/components/ui/GameHUD.vue'

const { joinLobby } = useMilefizStore()

joinLobby()
</script>

<template>
  <main >
    <!-- Einbindung des globalen HUDs -->
    <GameHUD />
    <TheWelcome />
    <GameBoard />
  </main>
</template>
<style>
.game-board {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
}
</style>
