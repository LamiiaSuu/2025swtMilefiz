<script lang="ts" setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMilefizStore } from '@/stores/milefizstore'
import type { Player} from "@/types/lobbyupdate"
const router = useRouter()
const milefizStore = useMilefizStore;
const { gamedata, sendLobbyMessage} = milefizStore

const username = computed({
    get: () => {
        const me = getPlayerFromLobby(gamedata)
        if(!me) return ""
        return me.playerName
    },
    set: (strValue: string) => {
        if (gamedata.lobby.value) {
            const me = getPlayerFromLobby(gamedata)
            if (!me) return
            me.playerName = strValue
        }
        const lobbyId = gamedata.lobby.value?.id
        if (lobbyId) {
            const destination = `/app/milefiz/lobby/${lobbyId}/updatePlayerName`
            const payload = {
                newPlayerName: strValue
            }
            sendLobbyMessage(destination, payload)
  
        }
    }
})

function getPlayerFromLobby(gdata): Player | null {
    const lobby = gdata.lobby
    const myId = gdata.playerId
    if(!lobby || !myId) return null
    const me  = lobby.players.find((p) => p.id === myId)
    if (!me) return null
    return me

}
</script>

<template>
    <!-- Username -->
    <div class="form-row">
        <label>Username</label>
        <input type="text" v-model="username" class="form-input" placeholder="Username">
    </div>
</template>

<style scoped>
.file-input,
input {
    font-family: "AcmeFont", sans-serif;
    outline: none;
}

.form-row {
    display: grid;
    grid-template-columns: 150px 1fr;
    align-items: center;
    gap: 15px;
}

.form-row label {
    text-align: right;
}

input {
    text-align: center;
}

.form-input {
    background-color: var(--background-color-forms);
    border: 3px solid black;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
    padding: 10px 15px;
    font-size: 1.2rem;
}

.form-input:focus {
    background-color: #8b6f47;
    border-color: #5d4a2f;
    color: white;
}

.input-with-button {
    display: flex;
    gap: 10px;
    align-items: center;
}

.input-with-button .form-input {
    flex: 1;
}
</style>