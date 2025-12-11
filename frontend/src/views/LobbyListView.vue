<script lang="ts" setup>
import Header from '@/components/ui/pages/Header.vue'
import ComponentList from '@/components/ui/pages/ComponentList.vue'
import LobbyList, {type Lobby} from '@/components/ui/pages/LobbyList.vue'
import BackButton from '@/components/ui/pages/BackButton.vue'
import UsernameField from '@/components/ui/pages/UsernameField.vue'
import { ref } from 'vue'

const lobbies = ref<Lobby[]>([])
const selectedLobby = ref<string>()

  /* ## Testcode ## */
const addtest = () => {
  if (lobbies.value)
    lobbies.value.push({id: Math.random().toString(36).slice(2), name: Math.random().toString(36).slice(2)})
}
  /* #### */
</script>

<template>
  <div class="lobbylist">
    <Header>Spiel Beitreten</Header>
    <ComponentList>
      {{ selectedLobby }}
      <button @click="addtest">click</button>
      <UsernameField></UsernameField>
      <LobbyList v-model:lobbyid="selectedLobby" :lobbies="lobbies" />
      <div class="button-container">
        <button class="start-game-button game-content" @click="$router.push({ name: 'game' })">
          Spiel Starten
        </button>
        <BackButton :to="{ name: 'Homepage' }" />
      </div>
    </ComponentList>
  </div>
</template>

<style scoped>
.lobbylist {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding-bottom: 4rem;
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

.button-container {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.start-game-button {
    padding: 15px 30px;
    background-image: var(--button-gradient-red);
    color: white;
    font-size: 1.3rem;
    cursor: pointer;
    transition: background-color 0.2s;
}

.start-game-button:hover {
    background-color: rgba(180, 40, 40, 0.95);
}

</style>
<style>
  .game-container {
      display: flex;
      flex-direction: row;
      max-height: 100%;
      min-height: 100px;
      gap: 15px;
      align-items: flex-start;
  }

  .game-label {
    position: relative;
    font-family: "MainFont", sans-serif;
    font-size: 1.5rem;
    font-weight: bolder;
    color: white;
    -webkit-text-stroke: 6px black;
    text-shadow:
      1px 1px 2px rgba(0, 0, 0, 0.8),
      0 0 5px rgba(0, 0, 0, 0.5);
      
    paint-order: stroke fill;
    }

  .game-content {
    background-color: var(--background-color-forms);
    border: 3px solid black;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
}
</style>