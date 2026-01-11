<script setup>
import { useRouter } from 'vue-router'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'
import LanguageSelection from '@/components/ui/LanguageSelection.vue'
import Tutorial from '@/components/ui/Tutorial.vue'
import BackButton from '@/components/ui/pages/BackButton.vue'
import Header from '@/components/ui/pages/Header.vue'

const router = useRouter()
const audio = useAudioStore()

</script>

<template>
  <div class="home">

    <!--MI'lefiz Header -->
    <Header overlay>{{ tUI('TUTORIAL') }}</Header>

    <!-- Language Selection-->
    <LanguageSelection></LanguageSelection>

    <div class="content">
      <!-- Tutorial-->
      <Tutorial></Tutorial>
    </div>

    <div class="button-container">
      <BackButton @mouseenter="onHover" :to="{ name: 'Homepage' }" />
    </div>

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

.content {
  margin-top: -10vh;
}

.button-container {
  position: absolute;
  top: 38px;
  left: 43px;

  display: flex;
  flex-direction: column;
  gap: 2vh;
}
</style>