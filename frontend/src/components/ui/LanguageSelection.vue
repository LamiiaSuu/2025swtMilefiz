<template>
    <div class="language-switch">
      <img src="/flags/Flag_of_Germany.svg" alt="Deutsch" @mouseenter="onHover" @click="setLocale('de'), audio.playSfx('click')" class="flag" />
      <img src="/flags/flagge-grossbritannien.jpg" alt="English" @mouseenter="onHover" @click="setLocale('en'), audio.playSfx('click')" class="flag" />
      <img src="/flags/Flag_of_the_Netherlands.svg.png" alt="Netherlands" @mouseenter="onHover" @click="setLocale('nl'), audio.playSfx('click')" class="flag" />
    </div>
</template>

<script setup>
import { useAudioStore } from '@/stores/audioStore';
import { setLocale } from '@/i18n'

const audio = useAudioStore()

function onHover() {
  audio.playSfx('hover')
}
</script>

<style scoped>
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