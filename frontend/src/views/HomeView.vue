<template>
  <div class="home">
    <Header overlay></Header>
    <div class="button-container">
      <button class="menu-button" @click="newGameStart">Neues Spiel</button>
      <button class="menu-button" @click="goToJoinGame">Spiel beitreten</button>
      <button class="menu-button" @click="goToMapEditor">Map Editor</button>
      <button class="menu-button" @click="goToSettings">Einstellungen</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import Header from '@/components/ui/pages/Header.vue'

const router = useRouter()

const newGameStart = () => {
  router.push({ name: 'game-start' })
}

const goToJoinGame = () => {
  router.push('/join-game')
}

const goToMapEditor = () => {
  router.push('map-editor')
}

const goToSettings = () => {
  router.push('/settings')
}
</script>

<style scoped>
.home {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;

  align-items: center;
  overflow: hidden;
  padding-bottom: 4rem;
}

.home::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("/backgrounds/BackgroundTest.webp");
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  filter: blur(4px);
  z-index: -1;
}

.button-container {
  display: flex;
  flex-direction: column;
  gap: 2vh;
  align-items: center;
}

.menu-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}
</style>
