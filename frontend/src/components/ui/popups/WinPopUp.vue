<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import BackButton from '../pages/BackButton.vue'
import { useMilefizStore } from '@/stores/milefizstore'

const store = useMilefizStore()
const router = useRouter()

const newGameStart = () => {
  router.push({ name: 'game-start' })
}
</script>

<template>
  <div class="page">
    <div class="popup">


      <div class="win-message">
        <h1>Spieler 1 hat gewonnen!</h1>
      </div>



      <div class="button-container">
        <BackButton :to="{ name: 'Homepage' }">&lt; Hauptmenü</BackButton>
        <button class="menu-button" @click="newGameStart">Neues Spiel</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
}

.popup {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;

  margin: auto;
  width: 75vw;
  height: 75vh;

  padding: 1vh;

  overflow: hidden;

  background-color: var(--button-color);
  z-index: 100;
  border: 5px solid red;
}

.button-container {
  display: flex;
  flex-direction: row;
  gap: 4vh;
  align-items: center;
  width: 100%;

  border: 5px solid yellow;
}

.button-container :deep(button) {
  flex: 1;
}

.menu-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}

h1 {

  font-weight: bolder;
  color: #0A0A0A;
  paint-order: stroke fill;
}
</style>
