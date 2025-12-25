

<script setup>
import BackButton from '@/components/ui/pages/BackButton.vue'
import Header from '@/components/ui/pages/Header.vue'
import AudioSettings from '@/components/ui/AudioSettings.vue';
import { useAudioStore } from '@/stores/audioStore';
import { tUI, setLocale } from '@/i18n'

const audio = useAudioStore()

function onHover() {
  audio.playSfx('hover')
}

</script>

<template>
  <div class="settings">
    <Header overlay>{{ tUI('SETTINGS') }}</Header>
    <div class="language-switch">
      <img src="/flags/Flag_of_Germany.svg" alt="Deutsch" @mouseenter="onHover" @click="setLocale('de'), audio.playSfx('click')" class="flag" />
      <img src="/flags/flagge-grossbritannien.jpg" alt="English" @mouseenter="onHover" @click="setLocale('en'), audio.playSfx('click')" class="flag" />
      <img src="/flags/Flag_of_the_Netherlands.svg.png" alt="Netherlands" @mouseenter="onHover" @click="setLocale('nl'), audio.playSfx('click')" class="flag" />
    </div>
    <!-- AUDIO SETTINGS -->
    <AudioSettings></AudioSettings>

    <!-- BACK -->
    <div class="settings-button-container">
      <BackButton @mouseenter="onHover" :to="{ name: 'Homepage' }" />
    </div>
  </div>
</template>

<style scoped>
.settings {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;

  align-items: center;
  overflow: hidden;
  padding-bottom: 4rem;
}

.settings::before {
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

.settings-button-container {
  display: flex;
  flex-direction: column;
  gap: 2vh;
  align-items: center;
  margin-top: 1.2vh;
}

.settings-menu-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}

.language-switch {
  position: absolute;
  top: 1rem; 
  right: 1rem;
  display: flex;
  gap: 0.5rem; 
}

.language-switch .flag {
  width: 32px; 
  height: 20px; 
  cursor: pointer;
  border-radius: 3px;
  transition: transform 0.2s ease;
}

.language-switch .flag:hover {
  transform: scale(1.2);
}
</style>
