<script setup lang="ts">

import { ref } from 'vue'
import BackButton from '@/components/ui/BackButton.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Daten
const lobbyId = ref('ABC-123-XYZ')
const lobbyName = ref('')
const username = ref('')
const mapMode = ref<'standard' | 'import'>('standard')
const selectedFile = ref<File | null>(null)

// Spieler Farben: rot, gruen, blau, gelb
const playerColors = ['#ff4444', '#44ff44', '#4444ff', '#ffff44']
const players = ref([
    { name: 'Spieler 1', color: playerColors[0] },
])

// Lobby-Id zu Clipboard kopieren
const copyToClipboard = async () => {
    try {
        await navigator.clipboard.writeText(lobbyId.value)
    } catch (err) {
        console.error('Fehler beim Kopieren der Lobby-ID:', err)
    }
}

// Datei importieren
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
        <div class="header-with-plate">
            <p>MI'lefiz</p>
        </div>

        <!-- Seiten Ueberschrift -->
        <div class="page-title">
            <h1>Neues Spiel</h1>
        </div>

        <div class="new-game-form">
            <form>
                <!-- Linke Spalte -->
                <div class="form-column">

                    <!-- Lobby-ID -->
                    <div class="form-row">
                        <label>Lobby-ID</label>

                        <div class="input-with-button">
                            <input type="text" v-model="lobbyId" disabled class="form-input">
                            <button type="button" @click="copyToClipboard" class="copy-button"
                                title="In Zwischenablage kopieren">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="20" height="20"
                                    fill="currentColor">
                                    <path
                                        d="M272 0H396.1c12.7 0 24.9 5.1 33.9 14.1l67.9 67.9c9 9 14.1 21.2 14.1 33.9V336c0 26.5-21.5 48-48 48H272c-26.5 0-48-21.5-48-48V48c0-26.5 21.5-48 48-48zM48 128H208v64H64V448H256V416h64v48c0 26.5-21.5 48-48 48H48c-26.5 0-48-21.5-48-48V176c0-26.5 21.5-48 48-48zm272 96h96v32H320V224zm0 64h96v32H320V288z" />
                                </svg>
                            </button>
                        </div>
                    </div>

                    <!-- Lobby-Name -->
                    <div class="form-row">
                        <label>Lobby-Name</label>
                        <input type="text" v-model="lobbyName" class="form-input" placeholder="Lobby-Name">
                    </div>

                    <!-- Map Buttons -->
                    <div class="form-row">
                        <!-- NOSONAR -->
                        <label>Map</label>
                        <div class="map-buttons">
                            <button type="button" class="map-button" :class="{ active: mapMode === 'standard' }"
                                @click="mapMode = 'standard'">
                                Standardmap
                            </button>
                            <button type="button" class="map-button" :class="{ active: mapMode === 'import' }"
                                @click="mapMode = 'import'">
                                Importieren
                            </button>
                        </div>
                    </div>

                    <!-- Datei importieren -->
                    <div class="form-row">
                        <label>Datei</label>
                        <input type="file" @change="handleFileChange" class="file-input"
                            :disabled="mapMode === 'standard'" accept=".json,.map">
                    </div>

                </div>

                <!-- Rechte Spalte -->
                <div class="form-column">

                    <!-- Username -->
                    <div class="form-row">
                        <label>Username</label>
                        <input type="text" v-model="username" class="form-input" placeholder="Username">
                    </div>

                    <!-- Spieler-Liste -->
                    <div class="form-row">
                        <label>Spieler</label>
                        <div class="players-list">
                            <div v-for="(player, index) in players" :key="index" class="player-item">
                                <span class="player-color-dot" :style="{ backgroundColor: player.color }"></span>
                                {{ player.name }}
                            </div>
                        </div>
                    </div>

                    <!-- Buttons -->
                    <div class="form-row">
                        <div class="button-container">
                            <button class="start-game-button" @click="$router.push({ name: 'game' })">
                                Spiel Starten
                            </button>
                            <BackButton :to="{ name: 'Homepage' }" />
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
    justify-content: center;
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
    background-color: rgba(230, 230, 230, 0.95);
}

.form-input,
.copy-button,
.file-input,
.players-list,
.map-button,
.start-game-button {
    border: 2px solid black;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
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

.copy-button {
    width: 40px;
    height: 40px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s;
}

.copy-button:hover {
    background-color: rgba(200, 200, 200, 0.95);
}

.map-buttons {
    display: flex;
    gap: 10px;
    grid-column: 2;
}

.map-button {
    flex: 1;
    padding: 10px 20px;
    border: 2px solid black;
    border-radius: 8px;
    font-size: 1.1rem;
    cursor: pointer;
    background-color: rgba(100, 100, 100, 0.8);
    color: white;
    transition: background-color 0.2s;
    min-width: 0;
}

.map-button.active {
    background-color: #234420;
}

.file-input {
    padding: 8px;
    font-size: 1rem;
    color: white;
    background-color: #234420;
}

.file-input:disabled {
    background-color: rgba(100, 100, 100, 0.8);
}

.players-list {
    padding: 15px;
    min-height: 100px;
}

.player-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 0;
    font-size: 1.1rem;
    pointer-events: none;
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
    background-color: rgba(200, 50, 50, 0.9);
    color: white;
    font-size: 1.3rem;
    cursor: pointer;
    transition: background-color 0.2s;
}

.start-game-button:hover {
    background-color: rgba(180, 40, 40, 0.95);
}
</style>