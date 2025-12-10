<script lang="ts" setup>
import Header from '@/components/ui/pages/Header.vue'
import ComponentList from '@/components/ui/pages/ComponentList.vue'
import LobbyList, {type Lobby} from '@/components/ui/pages/LobbyList.vue'
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
      <LobbyList v-model:lobbyid="selectedLobby" :lobbies="lobbies" />
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
</style>
