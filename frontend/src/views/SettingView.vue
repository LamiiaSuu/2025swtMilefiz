

<script setup>
import BackButton from '@/components/ui/pages/BackButton.vue'
import Header from '@/components/ui/pages/Header.vue'
import AudioSettings from '@/components/ui/AudioSettings.vue';

</script>

<template>
  <div class="settings">
    <Header overlay>Einstellungen</Header>

    <!-- AUDIO SETTINGS -->
    <AudioSettings></AudioSettings>

    <!-- BACK -->
    <div class="settings-button-container">
      <BackButton :to="{ name: 'Homepage' }" />
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

</style>
