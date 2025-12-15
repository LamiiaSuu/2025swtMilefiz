<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router'
import BackButton from '../pages/BackButton.vue';

const router = useRouter()

const newGameStart = () => {
  router.push({ name: 'game-start' })
}
</script>

<template>

  <div class="popup">
    <span>Spieler 1 hat gewonnen!</span>

    <div class="button-container">
      <BackButton :to="{ name: 'Homepage' }">&lt; Hauptmenü</BackButton>

      <button class="menu-button" @click="newGameStart">Neues Spiel</button>
    </div>
  </div>
</template>

<style scoped>
.popup {
  position: relative;
  height: 50vh;
  width: 50vw;
  display: flex;
  flex-direction: column;

  align-items: center;
  overflow: hidden;
  padding-bottom: 4rem;

  z-index: 100;
}

.button-container {
  display: flex;
  flex-direction: row;
  gap: 2vh;
  align-items: center;
}

.menu-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}
</style>