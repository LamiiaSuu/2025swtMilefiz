<!-- Globales Heads-up Display (HUD), das über dem Spiel als Overlay gerendert wird. Die Button-Bar befindet sich unten rechts
 und enthält momentan nur den "Würfeln" Button -->

<script setup lang="ts">
import DiceButton from './DiceButton.vue'
import DiceCounter from './DiceCounter.vue';
import JumpButton from './JumpButton.vue';
import SaveEnergyButton from './SaveEnergyButton.vue'
import ErrorMessage from './ErrorMessage.vue';
</script>


<template>
  <div class="hud-container">
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
  top: 20px;           /* Abstand von oben */
  left: 50%;           /* Mittig horizontal */
  transform: translateX(-50%);  /* Zentriert das Element */
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
</style>