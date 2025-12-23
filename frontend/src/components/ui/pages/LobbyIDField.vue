<script lang="ts" setup>
import { useAudioStore } from '@/stores/audioStore';
import { useMilefizStore } from '@/stores/milefizstore';
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const milefizStore = useMilefizStore()

const router = useRouter()
const base = globalThis.location.origin

const lobbyId = computed(() => milefizStore.gamedata.lobby?.id ?? '---')
const audio = useAudioStore()
/**
 * copyToClipboard()
 * Diese Funktion kopiert den Join-Link mit der Lobby-ID ins Clipboard.
 */
const copyToClipboard = async () => {
    try {
        await navigator.clipboard.writeText(`${base}/join/${lobbyId.value}`)
        audio.playSfx('copyLobby')
    } catch (err) {
        console.error('Fehler beim Kopieren der Lobby-ID:', err)
    }
}

function onHover() {
  audio.playSfx('hover')
}
</script>

<template>
    <!-- Lobby-ID -->
    <div class="form-row">
        <label>Lobby-ID</label>

        <div class="input-with-button">
            <input type="text" v-model="lobbyId" disabled class="form-input">
            <button type="button" @mouseenter="onHover" @click="copyToClipboard" class="copy-button" title="In Zwischenablage kopieren">
                <img src="@/assets/buttons/copy-clipboard-icon.png" alt="Copy" width="20" height="20">
            </button>
        </div>
    </div>

</template>

<style scoped>
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
.file-input {
    font-family: "AcmeFont", sans-serif;
    outline: none;
    text-align: center;
    background-color: var(--background-color-forms);
    flex: 1;
    padding: 10px 15px;
    font-size: 1.2rem;
}

.form-input,
.copy-button {
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

.copy-button {
    width: 40px;
    height: 40px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s;
    background-image: var(--button-gradient-green);
}

.copy-button img {
    filter: brightness(0) invert(1);
}

.copy-button:hover {
    background-color: rgba(200, 200, 200, 0.95);
}
</style>