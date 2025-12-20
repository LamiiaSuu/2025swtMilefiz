<!-- Globales Heads-up Display (HUD), das über dem Map-Editor als Overlay gerendert wird. Die Button-Bar befindet sich unten rechts. -->

<script setup lang="ts">
import { ref } from 'vue'
import ErrorMessage from '../ErrorMessage.vue';
import BarrierButton from './buttons/BarrierButton.vue';
import DeleteButton from './buttons/DeleteButton.vue';
import TileButton from './buttons/TileButton.vue';
import GoalButton from './buttons/GoalButton.vue';
import HouseButton from './buttons/HouseButton.vue';

const selectedTool = ref<'start' | 'goal' | 'tile' | 'barrier' | 'delete'>('tile')
</script>


<template>
  <div class="editor-hud-container">

    <div class="editor-error-message-container">
        <ErrorMessage />
    </div>

    <!-- Button Bar -->
    <div style="position: absolute;bottom: 2vw; right: 0px;">
      <div class="editor-button-bar">
        <div
          class="editor-icon-with-text"
          @click="selectedTool = 'start'"
        >
          <div
            class="editor-icon-wrapper"
            :class="{ selected: selectedTool === 'start' }"
          >
            <HouseButton />
          </div>
          <p>Start (Q)</p>
        </div>
        <div
          class="editor-icon-with-text"
          @click="selectedTool = 'goal'"
        >
          <div
            class="editor-icon-wrapper"
            :class="{ selected: selectedTool === 'goal' }"
          >
            <GoalButton />
          </div>
          <p>Ziel (W)</p>
        </div>
        <div
          class="editor-icon-with-text"
          @click="selectedTool = 'tile'"
        >
          <div
            class="editor-icon-wrapper"
            :class="{ selected: selectedTool === 'tile' }"
          >
            <TileButton />
          </div>
          <p>Standard (E)</p>
        </div>
        <div
          class="editor-icon-with-text"
          @click="selectedTool = 'barrier'"
        >
          <div
            class="editor-icon-wrapper"
            :class="{ selected: selectedTool === 'barrier' }"
          >
            <BarrierButton />
          </div>
          <p>Sperre (R)</p>
        </div>
        <div style="padding-left: 50px;">
          <div
          class="editor-icon-with-text"
          >
            <div
              class="editor-icon-wrapper editor-delete-button"
            >
              <DeleteButton />
            </div>
            <p>Löschen (Z)</p>
          </div>
        </div>
        
      </div>
    </div>



  </div>
</template>

<style>
.editor-hud-container {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 99999
}

.editor-error-message-container {
  position: absolute;
  top: 2vh;            /* Abstand von oben */
  left: 2vw;          /* Abstand von rechts */
}

.editor-button-bar {
  display: flex;
  gap: 30px;
  padding-top: 20px;
  padding-bottom: 15px;
  padding-left: 25px;
  padding-right: 25px;
  background: #00552d;
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
  width: 100%;
  box-sizing: border-box;
  pointer-events: auto;
  right: 0px;
  box-shadow: -7.5px 7.5px 15px rgba(0,0,0,0.5);
}

.editor-icon-with-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.editor-icon-with-text.selected {
  border: 2px solid #ffd36a;
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(255, 211, 106, 0.6);
}

.editor-icon-with-text p {
  margin-top: 4px;
  color: #FFFFFF;
  font-family: 'Acme', sans-serif;
  text-align: center;
}

.editor-icon-wrapper {
  border-radius: 8px;
  transition: box-shadow 0.15s ease, border-color 0.15s ease, outline 0.15s ease;
}

.editor-icon-wrapper.selected {
  outline: 5px solid #ffd36a;
  outline-offset: -2px; 
  box-shadow: 0 0 25px rgba(255, 211, 106, 0.6);
}

.editor-delete-button:hover {
  outline: 10px solid #ffd36a;
  outline-offset: -5px; 
  box-shadow: 0 0 25px rgba(255, 211, 106, 0.6);
}

</style>