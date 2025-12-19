<!-- Globales Heads-up Display (HUD), das über dem Map-Editor als Overlay gerendert wird. Die Button-Bar befindet sich unten rechts. -->

<script setup lang="ts">

import ErrorMessage from '../ErrorMessage.vue';
import BarrierButton from './buttons/BarrierButton.vue';
import DeleteButton from './buttons/DeleteButton.vue';
import TileButton from './buttons/TileButton.vue';
import GoalButton from './buttons/GoalButton.vue';
import HouseButton from './buttons/HouseButton.vue';
</script>


<template>
  <div class="hud-container">

    <div class="error-message-container">
        <ErrorMessage />
    </div>

    <!-- Button Bar -->
    <div style="position: absolute;bottom: 2vw; right: 0px;">
      <div class="button-bar">
        <HouseButton />
        <GoalButton />
        <TileButton />
        <BarrierButton />
        <div style="padding-left: 50px;">
            <DeleteButton />
        </div>
        
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

.error-message-container {
  position: absolute;
  top: 2vh;            /* Abstand von oben */
  left: 2vw;          /* Abstand von rechts */
}

.button-bar {
  display: flex;
  gap: 30px;
  padding-top: 20px;
  padding-bottom: 20px;
  padding-left: 25px;
  padding-right: 25px;
  background: #00552d;
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
  width: 100%;
  box-sizing: border-box;
  pointer-events: auto;
  right: 0px;

}

</style>