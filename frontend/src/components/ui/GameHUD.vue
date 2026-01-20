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

/**
 * Hilfsfunktion zum Öffnen des Menüs
 */
function openMenu() {
  audio.playSfx('click')
  milefizStore.openPopUpMenu()
}

/**
 * Fordert den Browser auf, den Pointer Lock erneut zu aktivieren.
 *
 * Der Pointer Lock wird **nur dann** angefordert, wenn:
 * - aktuell **kein Pointer Lock aktiv** ist (`document.pointerLockElement === null`)
 * - **kein Menü / Overlay geöffnet** ist (`isAnyMenuOpen === false`)
 *
 * Hintergrund:
 * - Browser erlauben `requestPointerLock()` nur im Kontext einer
 *   gültigen Benutzerinteraktion (z. B. Keydown, Click).
 * - Diese Funktion wird daher ausschließlich aus solchen Events heraus aufgerufen
 *   (z. B. beim Schließen des Ingame-Menüs per Tastatur).
 *
 * Fehlerbehandlung:
 * - Pointer-Lock-Anfragen können vom Browser abgelehnt werden
 *   (z. B. Race-Conditions nach Escape oder fehlende User-Activation).
 * - Diese Fehler werden bewusst abgefangen und ignoriert,
 *   da sie kein inkonsistentes Spielverhalten verursachen.
 *
 * Cleanup:
 * - Setzt das interne `relockInProgress`-Flag unabhängig vom Erfolg zurück,
 *   um Folgelogik (z. B. PointerLockChange-Handler) wieder zu erlauben.
 *
 * @returns Promise<void>
 */
async function requestPointerLock() {
  try {
    if (!document.pointerLockElement && !milefizStore.isAnyMenuOpen) {
      await document.body.requestPointerLock()
    }

  } catch {

  } finally {
    relockInProgress = false
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (!milefizStore.isAnyPopUpOpen) { // Kein PopUp offen
    if (e.key === 'm' || e.key === 'M') { // Öffne Menü mit M
      milefizStore.openPopUpMenu()
      return
    }
  } else { // ein beliebiges PopUp Offen
    if (e.key === 'm' || e.key === 'M') { // M betätigt
      e.preventDefault()
      if (milefizStore.isAnyMenuOpen) { // beliebiges Menü offen
        relockInProgress = true
        milefizStore.closePopUpMenu() // Menü(s) schließen

        requestPointerLock() // pointerLock requesten
        return
      }
      if (milefizStore.isAnyDuelActive) { // Minigame(s) aktiv
        e.stopPropagation()
        console.log("DUEL ACTIVE! CAN'T OPEN MENU")
        return
      }
      if (milefizStore.minimap.isMiniMapOpen) { // Minimap offen
        milefizStore.openPopUpMenu() // Menü öffnen
      }
    } else { // Alles andere als M betätigt & beliebiges Pop Up offen
      if (milefizStore.isAnyDuelActive) {
        e.stopPropagation()
        console.log("DUEL ACTIVE! CAN'T PERFORM PLAYER ACTIONS")
      } else if (milefizStore.minimap.isMiniMapOpen) {
        e.stopPropagation()
        console.log("MINIMAP OPEN! CAN'T PERFORM PLAYER ACTIONS")
      } else if (milefizStore.gameFinished) {
        e.stopPropagation()
        console.log("GAME FINISHED! CAN'T PERFORM PLAYER ACTIONS")
      } else if (milefizStore.isAnyMenuOpen) {
        e.stopPropagation()
        console.log("MENU OPEN! CAN'T PERFORM PLAYER ACTIONS")
      }
    }
  }
  return
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown, {
    capture: true, // "Capture-Phase": Exklusiver Fokus --> Minigame fängt Inputs als erstes ab
    passive: false // erlaubt explizit e.preventDefault
  })
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown, {
    capture: true, // "Capture-Phase": Exklusiver Fokus --> Minigame fängt Inputs als erstes ab
  })
})

</script>


<template>
  <div class="hud-container">
    <template v-if="!milefizStore.gameFinished">


      <!-- Minimap Popup-->
      <transition name="fade">
        <MiniMapPopUp :is-open="milefizStore.minimap.isMiniMapOpen"
          :selected-field-id="milefizStore.minimap.selectedFieldId"
          :occupancy-by-field-id="milefizStore.minimap.occupancyByFieldId"
          @confirm="milefizStore.confirmMinimapSelection"
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

    </template>


    <!-- Win Popup -->
    <transition name="fade">
      <WinPopUp v-if="milefizStore.gameFinished" />
    </transition>
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
  position: fixed;
  top: calc(2vh + 6vh + 4vh);
  left: 1vw;
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

  width: 50px;
  height: auto;
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
  width: 100%;
  height: auto;
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