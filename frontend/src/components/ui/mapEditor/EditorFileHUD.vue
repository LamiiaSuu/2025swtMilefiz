<!-- Globales Heads-up Display (HUD), das über dem Map-Editor als Overlay gerendert wird. Die Button-Bar befindet sich unten rechts. -->

<script setup lang="ts">
import ImportButton from './buttons/ImportButton.vue';
import SaveButton from './buttons/SaveButton.vue';
</script>


<template>
  <div class="hud-container">

    <!-- Button Bar -->
    <div style="position: absolute;bottom: 2vw; left: 0px;">
      <div class="button-bar">
        <SaveButton/>
        <ImportButton/>

        
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
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
  width: 100%;
  box-sizing: border-box;
  pointer-events: auto;
  left: 0px;

}

</style>