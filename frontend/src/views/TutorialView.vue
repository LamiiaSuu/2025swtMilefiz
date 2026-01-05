<script setup>
import { useRouter } from 'vue-router'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'
import LanguageSelection from '@/components/ui/LanguageSelection.vue'
import TutorialPopUp from '@/components/ui/popups/TutorialPopUp.vue'

const router = useRouter()
const audio = useAudioStore()

</script>

<template>
  <div class="home">

    <!-- Language Selection-->
    <LanguageSelection></LanguageSelection>

    
    <!-- Tutorial PopUp -->
    <TutorialPopUp></TutorialPopUp>


  </div>
</template>

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
</style>