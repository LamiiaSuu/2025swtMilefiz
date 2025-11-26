<script setup lang="ts">
import TheWelcome from '@/components/TheWelcome.vue'
import { useMilefizStore } from '@/stores/milefizstore'
import GameBoard from '@/components/GameBoard.vue'
import { useRoute } from 'vue-router'
import { onMounted, ref } from 'vue'

const { gamedata, startMilefizLiveUpdate, sendSocketMessage, joinLobby } = useMilefizStore()
const route = useRoute()

startMilefizLiveUpdate()
const lobbyid: string = route.redirectedFrom?.params.lobbyid as string
if (lobbyid) {
  joinLobby(lobbyid)
} else {
  joinLobby()
  
}

setTimeout(function () {
  sendSocketMessage({ msg: 'Hello World' })
  console.log('Home view!!')
}, 5000)
</script>

<template>
  <main>
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
