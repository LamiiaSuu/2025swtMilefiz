<script setup lang="ts">
import { useRouter } from 'vue-router'
import BackButton from '../pages/BackButton.vue'
import { useMilefizStore } from '@/stores/milefizstore'

const store = useMilefizStore()
const router = useRouter()
</script>

<template>
  <div class="overlay">
    <div class="popup">
      <h1 class="title">{{ store.winnerName || 'Unbekannter Spieler' }} hat gewonnen!</h1>
      <p class="info-text">Du kannst jetzt zurück ins Hauptmenü gehen.</p>
      <div class="button-container">
        <BackButton :to="{ name: 'Homepage' }">Zurück zum Hauptmenü</BackButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.6); /* dunkler Hintergrund */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000;
  pointer-events: auto;
}

.popup {
  background-color: #ffffff;
  padding: 2rem 3rem;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0,0,0,0.3);
  border: 4px solid #4caf50;
  animation: fadeIn 0.3s ease;
}

.title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: #333333;
}

.info-text {
  font-size: 1.1rem;
  margin-bottom: 2rem;
  color: #555555;
}

.button-container {
  display: flex;
  justify-content: center;
}

.button-container :deep(button) {
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.button-container :deep(button):hover {
  transform: scale(1.05);
  background-color: #45a049;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}
</style>
