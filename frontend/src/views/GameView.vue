<script setup lang="ts">
import { useMilefizStore } from '@/stores/milefizstore'
import GameBoard from '@/components/GameBoard.vue'
import { onMounted, onBeforeUnmount } from 'vue'
import GameHUD from '@/components/ui/GameHUD.vue'
import { audioEngine } from '@/composables/audioEngine'

onMounted(() => {
  audioEngine.playAmbient('ambientForest05')
  audioEngine.playMusic('zambolinoCuckoo')
})

onBeforeUnmount(() => {
  if (audioEngine.ambientSource) audioEngine.ambientSource.stop()
  if (audioEngine.musicSource) audioEngine.musicSource.stop()
})

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
