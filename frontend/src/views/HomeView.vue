<template>
  <div class="home">
    <Header overlay></Header>
    <div class="button-container">
      <button class="menu-button" @mouseenter="onHover" @click="newGameStart">Neues Spiel</button>
      <button class="menu-button" @mouseenter="onHover" @click="goToJoinGame">Spiel Beitreten</button>
      <button class="menu-button" @mouseenter="onHover" @click="goToMapEditor">Map Editor</button>
      <button class="menu-button" @mouseenter="onHover" @click="goToSettings">Einstellungen</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import Header from '@/components/ui/pages/Header.vue'
import { useAudioStore } from '@/stores/audioStore'

const router = useRouter()
const audio = useAudioStore()

const newGameStart = () => {
  audio.playSfx('click')
  router.push({ name: 'game-start' })
}

const goToJoinGame = () => {
  audio.playSfx('click')
  router.push('/join-game')
}

const goToMapEditor = () => {
  audio.playSfx('click')
  router.push('map-editor')
}

const goToSettings = () => {
  audio.playSfx('click')
  router.push('/settings')
}

function onHover() {
  audio.playSfx('hover')
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
