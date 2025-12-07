<template>
  <div class="home">
    <div class="header-with-plate">
      <h1>MI'lefiz</h1>
    </div>

    <div class="button-container">
      <button class="menu-button">Neues Spiel</button>
      <button class="menu-button">Spiel beitreten</button>
      <button class="menu-button">Map erstellen</button>
      <button class="menu-button">Einstellungen</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.home {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;

  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding-bottom: 8rem;
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

.header-with-plate,
.button-container {
  position: relative;
  z-index: 1;
}

.header-with-plate {
  position: relative; 
  z-index: 10;        
}

.button-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
  margin-top: -3.5rem;  /* Verkleinert Abstand zum Header */
}
</style>
