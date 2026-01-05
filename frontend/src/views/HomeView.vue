<template>
  <div class="home">
    <!--MI'lefiz Header -->
    <Header overlay></Header>

    <!-- Language Selection-->
    <LanguageSelection></LanguageSelection>

    <!-- Settings Button-->
    <button class="settings-button" @mouseenter="onHover" @click="goToSettings">
      <img src="@/assets/buttons/settings_icon.png" alt="Settings"/>
    </button>

    <!-- Menu Buttons -->
    <div class="button-container">
      <button class="menu-button" @mouseenter="onHover" @click="newGameStart">{{ tUI('NEW_GAME') }}</button>
      <button class="menu-button" @mouseenter="onHover" @click="goToJoinGame">{{ tUI('JOIN_GAME') }}</button>
      <button class="menu-button" @mouseenter="onHover" @click="goToMapEditor">{{ tUI('MAP_EDITOR') }}</button>
      <button class="menu-button" @mouseenter="onHover" @click="goToTutorial">{{ tUI('TUTORIAL') }}</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import Header from '@/components/ui/pages/Header.vue'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'
import LanguageSelection from '@/components/ui/LanguageSelection.vue'

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

const goToTutorial = () => {
  audio.playSfx('click')
  router.push('/tutorial')
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
  margin-top: -5vh;

  z-index: 2;
}

.menu-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}

.settings-button {
  position: absolute;
  top: 4.5vh;
  right: -22.5vh;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.settings-button img {
  width: 15%;
  height: 15%;
  filter: brightness(0) invert(1);
}

.settings-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}

@keyframes wiggle {
  0% { transform: rotate(0deg); }
  25% { transform: rotate(15deg); }
  50% { transform: rotate(-10deg); }
  75% { transform: rotate(15deg); }
  100% { transform: rotate(0deg); }
}

.settings-button:hover img {
  animation: wiggle 1.2s ease-in-out infinite;
}
</style>
