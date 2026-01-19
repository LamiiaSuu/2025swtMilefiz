<!-- Globales Heads-up Display (HUD), das über dem Spiel als Overlay gerendert wird. Die Button-Bar befindet sich unten rechts
 und enthält momentan nur den "Würfeln" Button -->

<script setup lang="ts">
import { storeToRefs } from 'pinia';
import DiceButton from './DiceButton.vue'
import DiceCounter from './DiceCounter.vue';
import JumpButton from './JumpButton.vue';
import EnergyBar from './EnergyBar.vue';
import MeepleBar from './MeepleBar.vue';
import SpielerListe from './SpielerListe.vue'
import SaveEnergyButton from './SaveEnergyButton.vue';
import { useMilefizStore } from '@/stores/milefizstore'
import WinPopUp from './popups/WinPopUp.vue';
import MenuPopUp from './popups/MenuPopUp.vue';
import SettingsPopUp from './popups/SettingsPopUp.vue';
import MiniMapPopUp from './popups/MiniMapPopUp.vue';
import MiniMapGraph from './popups/MiniMapGraph.vue';
import ErrorMessage from './ErrorMessage.vue';
import { useBoardStore } from '@/stores/boardStore';
import TutorialPopUp from './popups/TutorialPopUp.vue';
import { onMounted, onUnmounted } from 'vue';
import { useAudioStore } from '@/stores/audioStore';

const milefizStore = useMilefizStore()
const boardStore = useBoardStore()
const audio = useAudioStore()
const { board, ok } = storeToRefs(boardStore)

let relockInProgress = false

function colorToCss(c: string) {
  return { RED: "#e11", GREEN: "#2a6", BLUE: "#16f", YELLOW: "#fc0" }[c] ?? "#e11"
}

function openMenu() {
  audio.playSfx('click')
  milefizStore.openPopUpMenu()
}

async function requestPointerLock() {
  try {
    if (!document.pointerLockElement) {
      if (!milefizStore.popUpMenuOpen || milefizStore.popUpTutorialOpen || milefizStore.popUpSettingsOpen) {
        await document.body.requestPointerLock()
      }
    }
  } catch {

  } finally {
    relockInProgress = false
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'm' || e.key === 'M') {
    e.preventDefault()
    if (milefizStore.isAnyMenuOpen) {
      relockInProgress = true
      milefizStore.closePopUpMenu()

      requestPointerLock()
      return
    } else if (!milefizStore.isAnyDuelActive) {
      milefizStore.openPopUpMenu()
      return
    }
    e.stopPropagation()
    return
  }
  if (milefizStore.isAnyNonMenuOpen) {
    e.preventDefault()
    if (milefizStore.isAnyDuelActive) return
    e.stopPropagation()
    console.log("STOPPING PROPAGATION")
  }
  return

}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

</script>


<template>
  <div class="hud-container">
    <!-- Minimap Popup-->
    <transition name="fade">
      <MiniMapPopUp :is-open="milefizStore.minimap.isMiniMapOpen"
        :selected-field-id="milefizStore.minimap.selectedFieldId"
        :occupancy-by-field-id="milefizStore.minimap.occupancyByFieldId" @confirm="milefizStore.confirmMinimapSelection"
        :style="{ '--own-color': colorToCss(milefizStore.minimap.ownColor) }">
        <template #map>
          <MiniMapGraph v-if="board" :board="board" :occupancy-by-field-id="milefizStore.minimap.occupancyByFieldId"
            :selected-field-id="milefizStore.minimap.selectedFieldId" @select="milefizStore.selectMinimapField" />
          <div v-else style="display:grid; place-items:center; width:100%; height:100%;">
            Board lädt…
          </div>
        </template>

      </MiniMapPopUp>
    </transition>
    <!-- Win Popup -->
    <transition name="fade">
      <WinPopUp v-if="milefizStore.gameFinished" />
    </transition>

    <!-- Menu Popup -->
    <transition name="fade">
      <MenuPopUp
        v-if="milefizStore.popUpMenuOpen && !milefizStore.popUpSettingsOpen && !milefizStore.popUpTutorialOpen" />
    </transition>

    <!-- Settings Popup -->
    <transition name="fade">
      <SettingsPopUp v-if="milefizStore.popUpSettingsOpen" />
    </transition>

    <!-- Tutorial Popup -->
    <transition name="fade">
      <TutorialPopUp v-if="milefizStore.popUpTutorialOpen" />
    </transition>

    <!-- Meeple Bar -->
    <div style="position: absolute; top: 2vw; right: 0px;" class="meeple-icon-bar">
      <MeepleBar />
    </div>

    <!-- Menu Button -->
    <div>
      <button class="ingame-menu-button" @click="openMenu()">
        <img src="@/assets/hud/menus_white.png" class="ingame-menu-icon" alt="menu" />
        <span class="ingame-hotkey">[ M ]</span>
      </button>
    </div>


    <!-- Spielerliste -->
    <div class="spielerliste-container">
      <SpielerListe />
    </div>

    <!-- Würfelergebnis -->
    <div class="dice-counter-container">
      <DiceCounter />
    </div>

    <div class="error-message-container">
      <ErrorMessage />
    </div>

    <!-- Button Bar -->
    <div style="position: absolute;bottom: 2vw; right: 0px;">
      <div class="button-bar">
        <SaveEnergyButton />
        <JumpButton />
        <DiceButton />
      </div>
    </div>

    <!-- Energy Bar -->
    <div style="position: absolute;bottom: 3vw; left: 3vw;">
      <div class="energy-bar-container">
        <EnergyBar />
      </div>
    </div>



  </div>
</template>

<style>
.hud-container {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 99999
}

.dice-counter-container {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
}

.spielerliste-container {
  position: absolute;
  top: 2 vw;
  left: 0;
}

.error-message-container {
  position: absolute;
  top: 2vh;
  /* Abstand von oben */
  left: 2vw;
  /* Abstand von rechts */
}

.ingame-menu-button {
  position: fixed;
  top: 2vh;
  left: 3vw;

  display: flex;
  flex-direction: column;
  align-items: center;

  justify-content: center;

  background: transparent;
  cursor: pointer;
  border: none;
}

.ingame-menu-icon {
  position: relative;
  width: 6vh;
  height: 6vh;
}

.ingame-hotkey {
  font-size: 2vh;
  font-weight: bold;
  color: #ffffff;
  font-family: "AcmeFont", sans-serif;
  line-height: 1;
}


.button-bar {
  display: flex;
  gap: 10px;
  padding-top: 10px;
  padding-bottom: 10px;
  padding-left: 10px;
  padding-right: 25px;
  background: #00552d;
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
  width: 100%;
  box-sizing: border-box;
  right: 0px;
}

.energy-bar-container {
  display: flex;
  gap: 10px;
  padding-top: 10px;
  padding-bottom: 10px;
  padding-left: 10px;
  padding-right: 25px;
  border-radius: 8px;
  width: 25vw;
  box-sizing: border-box;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>