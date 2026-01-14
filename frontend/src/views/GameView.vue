<script setup lang="ts">
import { useMilefizStore } from '@/stores/milefizstore'
import GameBoard from '@/components/GameBoard.vue'
import { onMounted, onBeforeUnmount, computed, ref } from 'vue'
import GameHUD from '@/components/ui/GameHUD.vue'
import { audioEngine } from '@/composables/audioEngine'
import DuelOverlay from '@/components/ui/popups/DuelOverlay.vue'
import LoadingScreen from '@/components/LoadingScreen.vue'

/**
 * Spielansicht (In-Game Screen).
 *
 * Verantwortlich für:
 * - Initialisieren und Stoppen der Audio-Umgebung
 * - Rendern des HUDs
 * - Rendern des Spielfelds
 *
 * Audio ist in zwei Kategorien aufgeteilt:
 * - Ambient (Hintergrundgeräusche)
 * - Musik (Playlists)
 *
 * Beim Betreten werden Playlists gestartet,
 * beim Verlassen sauber gestoppt.
 */

const store = useMilefizStore()

const activeDuels = store.activeDuels

const loading = ref(true)

/**
 * Startet den Loading Screen für 7 Sekunden
 * Startet Ambient- und Musik-Playlists,
 * sobald die Spielansicht geladen ist.
 * 
 * Die Playlists sind in einer Schleife. Sie können individuell angepasst werden mit den Keys aus audioStore.ts.
 */
onMounted(() => {
  setTimeout(() => {
    loading.value = false
  
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
  }, 7000)
})

/**
 * Stoppt alle Audio-Streams beim Verlassen der Ansicht,
 * um Memory-Leaks und doppelte Audio-Instanzen zu vermeiden.
 */
onBeforeUnmount(() => {
  audioEngine.stopAmbient()
  audioEngine.stopMusic()
})

            activeDuels[0] = {
              duelId: 0,

              firstMeeple: 0,
              secondMeeple: 1,
              targetField: 1,

              miniGameId: 0,
              miniGameName: 0,
              miniGameType: 0,

              timeOut: 10,

              state: {}
            }

</script>

<template>
  <main >
    <!-- LoadingScreen -->
    <LoadingScreen :show="false" />

    <!-- Duelle -->
    <DuelOverlay
      v-if="Object.keys(activeDuels).length > 0"
      :duels="Object.values(activeDuels)"
      @close="id => delete activeDuels[id]"
    />

    <!-- HUD (Spielstatus, Buttons etc.) -->
    <GameHUD v-if="!false"/>

    <!-- Spielfeld -->
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
