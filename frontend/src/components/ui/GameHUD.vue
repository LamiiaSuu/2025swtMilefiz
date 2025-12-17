<!-- Globales Heads-up Display (HUD), das über dem Spiel als Overlay gerendert wird. Die Button-Bar befindet sich unten rechts
 und enthält momentan nur den "Würfeln" Button -->

<script setup lang="ts">
import DiceButton from './DiceButton.vue'
import DiceCounter from './DiceCounter.vue';
import JumpButton from './JumpButton.vue';
import EnergyBar from './EnergyBar.vue';
import SaveEnergyButton from './SaveEnergyButton.vue';
import { useMilefizStore } from '@/stores/milefizstore'
import WinPopUp from './popups/WinPopUp.vue';

const milefizStore = useMilefizStore()

import ErrorMessage from './ErrorMessage.vue';
</script>


<template>
  <div class="hud-container">
    <!-- Win Popup -->
    <transition name="fade">
      <WinPopUp v-if="milefizStore.gameFinished"/>
    </transition>

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

.error-message-container {
  position: absolute;
  top: 2vh;            /* Abstand von oben */
  left: 2vw;          /* Abstand von rechts */
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