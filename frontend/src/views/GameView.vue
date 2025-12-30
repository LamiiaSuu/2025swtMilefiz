<script setup lang="ts">
import { useMilefizStore } from '@/stores/milefizstore'
import GameBoard from '@/components/GameBoard.vue'
import { onMounted, onBeforeUnmount } from 'vue'
import GameHUD from '@/components/ui/GameHUD.vue'
import { audioEngine } from '@/composables/audioEngine'

onMounted(() => {
  audioEngine.playAmbientPlaylist([
    'ambientForest04',
    'ambientForest05',
  ], true)
  audioEngine.playMusicPlaylist([
    'cuddleClouds',
    'driftingMemories',
    'eveningHarmony',
    'floatingDream',
    'forgottenBiomes',
    'gentleBreeze',
    'goldenGleam',
    'polarLights',
    'strangeWorlds',
    'sunlightThroughLeaves',
    'wanderersTale',
    'whisperingWoods',
  ], true)
})

onBeforeUnmount(() => {
  audioEngine.stopAmbient()
  audioEngine.stopMusic()
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
