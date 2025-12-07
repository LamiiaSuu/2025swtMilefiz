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
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 7rem;
  align-items: center;
}

.header-with-plate {
  position: relative; 
  z-index: 10;        
}

.button-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
  margin-top: -6rem;  /* Verkleinert Abstand zum Header */
}
</style>
