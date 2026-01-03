<script setup lang="ts">
import { useMilefizStore } from '@/stores/milefizstore'
import GameBoard from '@/components/GameBoard.vue'
import { onMounted, onBeforeUnmount, computed } from 'vue'
import GameHUD from '@/components/ui/GameHUD.vue'
import { audioEngine } from '@/composables/audioEngine'
import DuelOverlay from '@/components/ui/popups/DuelOverlay.vue'

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

/**
 * Startet Ambient- und Musik-Playlists,
 * sobald die Spielansicht geladen ist.
 * 
 * Die Playlists sind in einer Schleife. Sie können individuell angepasst werden mit den Keys aus audioStore.ts.
 */
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

/**
 * Stoppt alle Audio-Streams beim Verlassen der Ansicht,
 * um Memory-Leaks und doppelte Audio-Instanzen zu vermeiden.
 */
onBeforeUnmount(() => {
  audioEngine.stopAmbient()
  audioEngine.stopMusic()
})

</script>

<template>
  <main >
    <!-- Duelle -->
    <DuelOverlay
      v-if="Object.keys(activeDuels).length > 0"
      :duels="Object.values(activeDuels)"
      @close="id => delete activeDuels[id]"
    />

    <!-- HUD (Spielstatus, Buttons etc.) -->
    <GameHUD />

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
