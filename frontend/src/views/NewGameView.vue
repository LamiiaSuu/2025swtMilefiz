<script setup lang="ts">

import { computed, onMounted, ref, watch } from 'vue'
import BackButton from '@/components/ui/pages/BackButton.vue'
import LobbyIDField from '@/components/ui/pages/LobbyIDField.vue'
import UsernameField from '@/components/ui/pages/UsernameField.vue'
import Header from '@/components/ui/pages/Header.vue'
import { useMilefizStore } from '@/stores/milefizstore'
import { storeToRefs } from 'pinia'
import { useAudioStore } from '@/stores/audioStore'

const { startGameCommand } = useMilefizStore()
const milefizStore = useMilefizStore()
const { isJoined, joinLobby, createJoinLobby, gamedata, sendLobbyMessage, isOwnLeader: storeIsOwnLeader, disconnectAndReset } = milefizStore
const audio = useAudioStore()

onMounted(() => {
    if (!isJoined) {
        // wenn keiner Lobby bereits gejoint -> erstelle neue
        milefizStore.createJoinLobby();
    }

})

function onHover() {
  audio.playSfx('hover')
}

// Reaktive Leader-Prüfung
const isOwnLeader = computed(() => storeIsOwnLeader())

// Lobby Wrapper mit allen Lobby-Daten
const lobby = computed({
    get: () => gamedata?.lobby,
    set: (value) => {
        // Lobby wird üblicherweise nicht direkt gesetzt, aber für Vollständigkeit
        if (gamedata && value) {
            gamedata.lobby = value
        }
    }
})

// Lobby-Name mit speziellem Setter
const lobbyName = computed({
    get: () => lobby.value?.lobbyName ?? '',
    set: (value: string) => {
        if (lobby.value) {
            lobby.value.lobbyName = value
        }
        if (!isOwnLeader.value) return // keine Änderung für non-Leader
        // Änderung an Backend senden
        const lobbyId = lobby.value?.id
        if (lobbyId) {
            const destination = `/app/milefiz/lobby/${lobbyId}/updateSettings`
            const payload = {
                newLobbyName: value,
                maxPlayers: lobby.value?.maxPlayers ?? 4,
            }
            sendLobbyMessage(destination, payload)
        }
    }
})

const username = ref('')
const mapMode = ref<'standard' | 'import'>('standard')

// Importierte Map Datei
const selectedFile = ref<File | null>(null)

/**
 * handleFileChange (event: Event)
 * @param event - Event vom Form
 * Diese Funktion wechselt die vom User hochgeladene/importierte Datei.
 */
const handleFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement
    if (target.files && target.files[0]) {
        selectedFile.value = target.files[0]
    }
}

</script>

<template>

    <div class="content">
        <!-- MI'lefiz Header -->
        <Header>Neues Spiel</Header>

        <div class="new-game-form">
            <form>
                <!-- Linke Spalte -->
                <div class="form-column">

                    <!-- Lobby-ID -->
                    <LobbyIDField />

                    <!-- Lobby-Name -->
                    <div class="form-row">
                        <label>Lobby-Name</label>
                        <input type="text" v-model="lobbyName" class="form-input" placeholder="Lobby-Name"
                            :disabled="!isOwnLeader">
                    </div>

                    <!-- Map Buttons (Nur bei Lobby-Ersteller)-->
                    <template v-if="isOwnLeader">
                        <div class="form-row">
                            <label>Map</label>
                            <div class="map-buttons">
                                <button type="button" @mouseenter="onHover" class="map-button" :class="{ active: mapMode === 'standard' }"
                                    @click="mapMode = 'standard'">
                                    Standardmap
                                </button>
                                <button type="button" @mouseenter="onHover" class="map-button" :class="{ active: mapMode === 'import' }"
                                    @click="mapMode = 'import'">
                                    Importieren
                                </button>
                            </div>
                        </div>

                        <!-- Datei importieren -->
                        <div class="form-row">
                            <label>Datei</label>
                            <input type="file" @mouseenter="onHover" @change="handleFileChange" class="file-input"
                                :disabled="mapMode === 'standard'" accept=".json,.map">
                        </div>
                    </template>

                </div>

                <!-- Rechte Spalte -->
                <div class="form-column">

                    <!-- Username -->
                    <UsernameField />

                    <!-- Spieler-Liste -->
                    <div class="form-row">
                        <label>Spieler</label>
                        <div class="players-list">
                            <div v-for="(player, index) in lobby?.players" :key="index" class="player-item">
                                <span class="player-color-dot" :style="{ backgroundColor: player.color }"></span>
                                <span style="pointer-events: none">{{ player.playerName }}</span>
                                <span v-if="player.leader" class="tooltip-wrapper">
                                    <img src="@/assets/buttons/sword-icon.png" style="filter: brightness(0)"
                                        alt="Leader" width="20" height="20"></img>
                                    <span class="tooltip">Leader</span>
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- Buttons -->
                    <div class="form-row">
                        <div class="button-container">
                            <button type="button" class="start-game-button"
                                @mouseenter="onHover" @click="() => { startGameCommand(); if (isOwnLeader) $router.push({ name: 'game' }); audio.playSfx('joinGame') }"
                                :disabled="!isOwnLeader" :class="{ active: isOwnLeader }">
                                {{ isOwnLeader ? 'Spiel Starten' : 'Warten auf Spielersteller...' }}
                            </button>
                            <BackButton @mouseenter="onHover" :to="{ name: 'Homepage' }" />
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>

</template>

<style scoped>
.content {
    position: relative;
    height: 100vh;
    width: 100vw;
    display: flex;
    flex-direction: column;
    align-items: center;
    overflow: hidden;
    padding-bottom: 4rem;
}

.content::before {
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

.map-button,
.game-start-button,
.player-item,
.file-input,
input {
    font-family: "AcmeFont", sans-serif;
    outline: none;
}

form {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 40px;
    width: 100%;
    justify-content: center;
    margin-top: -5vh;
}


.form-column {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.form-row {
    display: grid;
    grid-template-columns: 150px 1fr;
    align-items: center;
    gap: 15px;
    z-index: 20;
}

.form-row label {
    text-align: right;
}

input,
textarea,
select {
    text-align: center;
}

.form-input,
.copy-button,
.file-input,
.players-list {
    background-color: var(--background-color-forms);
}

.form-input,
.copy-button,
.file-input,
.players-list,
.map-button,
.start-game-button {
    border: 3px solid black;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
}

.input-with-button {
    display: flex;
    gap: 10px;
    align-items: center;
}

.input-with-button .form-input {
    flex: 1;
}

.form-input {
    padding: 10px 15px;
    font-size: 1.2rem;
}

.form-input:focus {
    background-color: #8b6f47;
    border-color: #5d4a2f;
    color: white;
}

.map-buttons {
    display: flex;
    gap: 10px;
    grid-column: 2;
}

.map-button {
    flex: 1;
    padding: 10px 20px;
    font-size: 1.1rem;
    cursor: pointer;
    background-color: var(--button-color-inactive);
    color: white;
    transition: background-color 0.2s;
    min-width: 0;
}

.map-button.active {
    background-image: var(--button-gradient-green);
}

.file-input {
    padding: 8px;
    font-size: 1rem;
    color: white;
    background-image: var(--button-gradient-green);
}

.file-input:disabled {
    background-color: var(--button-color-inactive);
    background-image: none;
}

.players-list {
    padding: 15px;
    /** Listen Hoehe fuer 4 Spieler:display: 
    *   4 * (.player-item font-size + player-item padding + Puffer px) + ().players-list padding * 2)
     */
    height: calc(4 * (1.1rem + 16px + 5px) + 30px);
}

.player-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 0;
    font-size: 1.1rem;
}

.player-color-dot {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    border: 2px solid rgba(0, 0, 0, 0.3);
}

.button-container {
    display: flex;
    flex-direction: column;
    gap: 15px;
    grid-column: 2;
}

.start-game-button {
    padding: 15px 30px;
    background-image: var(--button-gradient-red);
    color: white;
    font-size: 1.3rem;
    transition: background-color 0.2s;
}

.start-game-button:disabled {
    background-image: unset;
    background-color: var(--button-color-inactive);
    cursor: default;
}

.start-game-button:hover:enabled {
    background-color: rgba(180, 40, 40, 0.95);
    cursor: pointer;
}

.tooltip-wrapper {
    position: relative;
    display: inline-block;
}

.tooltip {
    position: absolute;
    bottom: 125%;
    left: 50%;
    transform: translateX(-50%);
    background: rgba(0, 0, 0, 0.85);
    color: white;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    white-space: nowrap;

    opacity: 0;
    pointer-events: none;
    transition: opacity 0.15s ease;
}

.tooltip-wrapper:hover .tooltip {
    opacity: 1;
}
</style>