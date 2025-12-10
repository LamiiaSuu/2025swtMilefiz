<script setup lang="ts">

import { ref } from 'vue'
import { useRouter } from 'vue-router'
import BackButton from '@/components/ui/pages/BackButton.vue'
import LobbyIDField from '@/components/ui/pages/LobbyIDField.vue'
import UsernameField from '@/components/ui/pages/UsernameField.vue'
import Header from '@/components/ui/pages/Header.vue'

const router = useRouter()

// Daten
const lobbyId = ref('ABC-123-XYZ')
const lobbyName = ref('')
const username = ref('')
const mapMode = ref<'standard' | 'import'>('standard')

// Importierte Map Datei
const selectedFile = ref<File | null>(null)

// Spieler Farben: gruen, gelb, rot, blau
const playerColors = ['#44ff44', '#ffff44', '#ff4444', '#4444ff']

// Liste der Spieler
const players = ref([
    { name: 'Spieler 1', color: playerColors[0] },
    { name: 'Spieler 2', color: playerColors[1] },
    { name: 'Spieler 3', color: playerColors[2] },
    { name: 'Spieler 4', color: playerColors[3] },
])

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
                    <UsernameField />

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