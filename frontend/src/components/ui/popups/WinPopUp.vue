<script setup lang="ts">
import { useRouter } from 'vue-router'
import BackButton from '../pages/BackButton.vue'
import { useMilefizStore } from '@/stores/milefizstore'

const store = useMilefizStore()
const router = useRouter()
</script>

<template>
  
  <img src="/winPopUpAssets/confetti_down.gif" alt="Confetti" class="confetti"/>
  <div class="overlay">
    <div class="popup">
      <div class="text">
        <h1 class="title">{{ store.winnerName || 'Unbekannter Spieler' }} hat gewonnen!</h1>
        <p class="info-text">Du kannst jetzt zurück ins Hauptmenü gehen.</p>
      </div>
      <div class="button-container">
        <BackButton :to="{ name: 'Homepage' }">&lt; Zurück zum Hauptmenü</BackButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000;
  pointer-events: auto;
}

.confetti {
  width: 100vw;
  height: auto;  
  
  background-color: rgba(0, 0, 0, 0.6);
}

.popup {
  display: flex;
  flex-direction: column;
  background-color: var(--background-color-forms);
  border-radius: 15px;
  width: 45vw;
  height: 40vh;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  border: 7px solid #4caf50;
  animation: fadeIn 0.3s ease;
}

.text {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin: auto 0;
  text-align: center;
  justify-content: center;
  align-items: center;
}

.title {
  font-size: 7vh;
  color: #000000;
  margin-bottom: 1vh;
}

.info-text {
  font-size: 4vh;
  color: var(--button-color-inactive);
  margin-bottom: 3vh;
}

.button-container {
  display: flex;
  justify-content: center;

  margin-top: auto;
  margin-bottom: 3vh;
}

.button-container :deep(button) {
  padding: 15px 30px;
  background-image: var(--button-gradient-green);
  color: white;
  font-size: 3vh;
  cursor: pointer;

  border: 3px solid black;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
  font-family: "AcmeFont", sans-serif;

  -webkit-text-stroke: 0;
  paint-order: fill;
  text-shadow: none;
  font-weight: 400;
}

.button-container :deep(button):hover {
  transform: scale(1.05);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
