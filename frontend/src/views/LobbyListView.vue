<script setup>
import Button from '@/components/pages/Button.vue'
import Header from '@/components/pages/Header.vue'
</script>

<template>
  <div class="lobbylist">
    <Header>Spiel Beitreten</Header>
    <div class="container">
      <Button>Neues Spiel</Button>
      <Button>Spiel beitreten</Button>
      <Button>Map erstellen</Button>
      <Button>Einstellungen</Button>
    </div>
  </div>
</template>

<style scoped>
.lobbylist {
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

.lobbylist::before {
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

.container {
  display: flex;
  flex-direction: column;
  gap: 2vh;
  align-items: center;
  margin-top: -6vh;  /* Verkleinert Abstand zum Header */
}
</style>
