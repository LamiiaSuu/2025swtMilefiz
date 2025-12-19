<!-- Globales Heads-up Display (HUD), das über dem Map-Editor als Overlay gerendert wird. Die Button-Bar befindet sich unten rechts. -->

<script setup lang="ts">
import { useMilefizStore } from '@/stores/milefizstore'

const milefizStore = useMilefizStore()

import ErrorMessage from './ErrorMessage.vue';
</script>


<template>
  <div class="hud-container">

    <div class="error-message-container">
        <ErrorMessage />
    </div>

    <!-- Button Bar -->
    <div style="position: absolute;bottom: 2vw; right: 0px;">
      <div class="button-bar">

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

</style>