<script setup lang="ts">
import { useMilefizStore } from '@/stores/milefizstore'
import GameBoard from '@/components/GameBoard.vue'
import { onMounted } from 'vue'
import GameHUD from '@/components/ui/GameHUD.vue'

const { gamedata, startMilefizLiveUpdate, sendSocketMessage, joinLobby } = useMilefizStore()

// startMilefizLiveUpdate()
joinLobby()

setTimeout(function () {
  sendSocketMessage({ msg: 'Hello World' })
  console.log('Home view!!')
}, 5000)
</script>

<template>
  <main >
    <!-- Einbindung des globalen HUDs -->
    <GameHUD />
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
