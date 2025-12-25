<script lang="ts" setup>
import Header from '@/components/ui/pages/Header.vue'
import ComponentList from '@/components/ui/pages/ComponentList.vue'
import LobbyList, { type Lobby } from '@/components/ui/pages/LobbyList.vue'
import BackButton from '@/components/ui/pages/BackButton.vue'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'

const lobbies = ref<Lobby[]>([])
const lobbyid = ref<string>('')

let interval: number

const username = ref<string>('')
const selectedLobby = ref<string>('')
const audio = useAudioStore()

const fetchLobbies = async () => {
  try {
    const response = await fetch('/api/lobby/list?filter=joinable') 
    if (!response.ok) throw new Error('Fehler beim Laden der Lobbies')
    const data = await response.json()
    lobbies.value = data
  } catch (err) {
    console.error('Lobby-Liste konnte nicht geladen werden:', err)
  }

}

const filteredLobbies = computed(() => {
  if (!lobbyid.value.trim()) {
    return lobbies.value
  }

  return lobbies.value.filter(lobby =>
    lobby.id.toLowerCase().includes(lobbyid.value.toLowerCase()) || lobby.lobbyName.toLowerCase().includes(lobbyid.value.toLowerCase())
  )
})

onMounted(() => {
  fetchLobbies()
  interval = globalThis.setInterval(fetchLobbies, 2000)
})

onUnmounted(() => {
  clearInterval(interval)
  selectedLobby.value = ''
})


function onHover() {
  audio.playSfx('hover')
}
</script>

<template>
  <div class="lobbylist">
    <Header>{{ tUI('JOIN_GAME') }}</Header>
      <ComponentList>
        <div class="game-container">
          <div class="game-label">{{ tUI('USERNAME') }}</div>
          <div class="username-input game-content">
            <input type="text" v-model="username" :placeholder="tUI('USERNAME') "></div>
        </div>
        <div class="game-container">
          <div class="game-label">{{ tUI('SEARCH') }}</div>
          <div class="lobbyid-input game-content">
            <input type="text" v-model="lobbyid" :placeholder="tUI('FOR_LOBBY_ID_OR_NAME')">
            <svg viewBox="0 -960 960 960"><path d="M784-120 532-372q-30 24-69 38t-83 14q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l252 252-56 56ZM380-400q75 0 127.5-52.5T560-580q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z"/></svg>
          </div>
        </div>
        <LobbyList v-model:lobbyid="selectedLobby" :lobbies="filteredLobbies" label="Lobbys" />
        <div class="game-container">
          <div class="button-container">
          <button class="start-game-button game-content" :disabled="!selectedLobby" @mouseenter="onHover" @click="() => {$router.push({ path: `/join/${selectedLobby}`, query: { username } }); audio.playSfx('joinGame')}">
            {{ tUI('JOIN_GAME') }}
          </button>
          <BackButton @mouseenter="onHover" :to="{ name: 'Homepage' }" />
        </div>
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
  width: 100%;
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

.game-container .game-label {
  position: absolute;
  transform: translateX(-100%);
  padding-right: 15px;
}

.game-content {
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
}

.game-content:has(input:focus) {
  background-color: var(--background-color-input-focus);
  border-color: var(--border-color-input-focus);
  color: var(--text-color-input-focus);
}
</style>