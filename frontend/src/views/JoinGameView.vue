<script lang="ts" setup>
import Header from '@/components/ui/pages/Header.vue'
import ComponentList from '@/components/ui/pages/ComponentList.vue'
import LobbyList, { type Lobby } from '@/components/ui/pages/LobbyList.vue'
import BackButton from '@/components/ui/pages/BackButton.vue'
import { ref } from 'vue'

const lobbies = ref<Lobby[]>([])
const lobbyid = ref<string>('')

const username = ref<string>('')
const selectedLobby = ref<string>('')


/* ## Testcode ## */
const addtest = () => {
  if (lobbies.value)
    lobbies.value.push({ id: Math.random().toString(36).slice(2), name: Math.random().toString(36).slice(2) })
}
addtest()
addtest()
addtest()
/* #### */
</script>

<template>
  <div class="lobbylist">
    <Header>Spiel Beitreten</Header>
      <ComponentList>
        <div class="game-container">
          <div class="game-label">Username</div>
          <div class="username-input game-content">
            <input type="text" v-model="username" placeholder="Username"></div>
        </div>
        <div class="game-container">
          <div class="game-label">Lobby-ID</div>
          <div class="lobbyid-input game-content">
            <input type="text" v-model="lobbyid" placeholder="Lobby-ID">
            <svg viewBox="0 -960 960 960"><path d="M784-120 532-372q-30 24-69 38t-83 14q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l252 252-56 56ZM380-400q75 0 127.5-52.5T560-580q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z"/></svg>
          </div>
        </div>
        <LobbyList v-model:lobbyid="selectedLobby" :lobbies="lobbies" label="Lobbys" />
        <div class="game-container button-container">
          <button class="start-game-button game-content" :disabled="!selectedLobby" @click="$router.push({ name: 'game' })">
            Beitreten
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
  margin-top: 2vh;
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

.start-game-button:disabled {
  background: unset;
  cursor: default;
  background-color: var(--button-color-inactive);
}

.username-input, .lobbyid-input {
  position: relative;
  padding: 10px 15px;
  font-size: 1.2rem;
}

.lobbyid-input svg {
  position: absolute;
  height: 24px;
  width: 24px;
  fill: black;
  right: 10px;
  pointer-events: none;
}

.lobbyid-input input:focus~svg {
fill: var(--text-color-input-focus);
}
</style>
<style>
.game-container {
  display: flex;
  flex-direction: row;
  max-height: 100%;
  gap: 15px;
}

.game-container .game-label {
  position: absolute;
  transform: translateX(-100%);
  padding-right: 20px;
  font-family: "MainFont", sans-serif;
  font-size: 2rem;
  font-weight: 400;
  color: white;
  -webkit-text-stroke: 6px black;
  text-shadow:
    1px 1px 2px rgba(0, 0, 0, 0.8),
    0 0 5px rgba(0, 0, 0, 0.5);

  paint-order: stroke fill;
}


.game-content {
  font-family: "AcmeFont", sans-serif;
  background-color: var(--background-color-forms);
  border: 3px solid black;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
  width: 100%;
}

.game-content input {
  all: unset;
  width: 100%;
  height: 100%;
  text-align: center;
}

.game-content:has(input:focus) {
  background-color: var(--background-color-input-focus);
  border-color: var(--border-color-input-focus);
  color: var(--text-color-input-focus);
}
</style>