<!-- Globales Heads-up Display (HUD), das über dem Map-Editor als Overlay gerendert wird. Die Button-Bar befindet sich unten rechts. -->

<script setup lang="ts">
import { tUI } from '@/i18n';
import ImportButton from './buttons/ImportButton.vue';
import SaveButton from './buttons/SaveButton.vue';
</script>


<template>
  <div class="editor-hud-container">

    <!-- Button Bar -->
    <div style="position: absolute;bottom: 2vw; left: 0px;">
      <div class="editor-button-bar-left">
        <div
        class="editor-icon-with-text"
        >
          <div
            class="editor-icon-wrapper editor-save-button"
          >
            <SaveButton />
          </div>
          <p>{{ tUI('EDITOR_SAVE') }} (S)</p>
        </div>
        <div
        class="editor-icon-with-text"
        >
          <div
            class="editor-icon-wrapper editor-save-button"
          >
            <ImportButton />
          </div>
          <p>{{ tUI('EDITOR_IMPORT') }} (O)</p>
        </div>
      </div>
    </div>



  </div>
</template>

<style scoped>
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

.editor-button-bar-left {
  display: flex;
  gap: 30px;
  padding-top: 20px;
  padding-bottom: 15px;
  padding-left: 25px;
  padding-right: 25px;
  background: #00552d;
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
  width: 100%;
  box-sizing: border-box;
  pointer-events: auto;
  left: 0px;
  box-shadow: 7.5px 7.5px 15px rgba(0,0,0,0.5);
}

.editor-save-button:hover {
  outline: 10px solid #ffd36a;
  outline-offset: -5px; 
  box-shadow: 0 0 25px rgba(255, 211, 106, 0.6);
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
  margin-top: 8px;
  color: #FFFFFF;
  font-family: 'Acme', sans-serif;
  text-align: center;
}


</style>