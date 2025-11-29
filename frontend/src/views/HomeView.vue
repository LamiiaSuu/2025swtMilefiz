<script setup lang="ts">
import TheWelcome from '@/components/TheWelcome.vue'
import { useMilefizStore } from '@/stores/milefizstore'
import { useUrlLobbyStore } from '@/stores/urlLobbyStore'
import GameBoard from '@/components/GameBoard.vue'
import { onMounted } from 'vue'

const { sendSocketMessage, joinLobby } = useMilefizStore()

onMounted(() => {

  // holt während des routings gespeicherte lobbyid aus dem urlLobbyStore
  const urlLobbyId = useUrlLobbyStore().getUrlLobbyId()

  // falls vorhanden, tritt mit lobbyid bei und lösche diese aus urlLobbyStore
  if (urlLobbyId) {
    joinLobby(urlLobbyId)
    useUrlLobbyStore().clear()
  } else {
    joinLobby()
  }
})

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
