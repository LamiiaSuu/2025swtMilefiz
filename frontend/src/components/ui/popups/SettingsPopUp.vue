<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useMilefizStore } from '@/stores/milefizstore'
import AudioSettings from '../AudioSettings.vue'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'
import LanguageSelection from '../LanguageSelection.vue'

const milefizStore = useMilefizStore()
const router = useRouter()
const audio = useAudioStore()

function onHover() {
  audio.playSfx('hover')
}

</script>

<template>
    <div class="overlay">
        <div class="popup">

            <div class="text">
                <h1 class="title">Menu</h1>
            </div>
            
            <!-- LANGUAGE SETTINGS -->
            <LanguageSelection></LanguageSelection>

            <!-- AUDIO SETTINGS -->
            <AudioSettings></AudioSettings>

            <div class="button-container">
                <button class="back-button" @mouseenter="onHover" @click="milefizStore.closePopUpSettings()">{{ tUI('BACK') }}</button>
            </div>

        </div>
    </div>
</template>

<style scoped>
.overlay {
    position: fixed;
    inset: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 10000;
    pointer-events: auto;

    background-color: rgba(0, 0, 0, 0.6);
}

.popup {
    display: flex;
    flex-direction: column;
    background-color: var(--background-color-forms);
    border-radius: 15px;
    width: 40vw;
    height: 70vh;
    text-align: center;
    position: relative;
    
    align-items: center;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
    border: 7px solid #57AA51;
    animation: fadeIn 0.3s ease;
}

.text {
    display: flex;
    flex-direction: column;
    margin: 3vh 0 0 0;
    text-align: center;
    align-items: center;
}

.title {
    font-size: 7vh;
    color: #000000;
}

.button-container {
    display: flex;
    flex-direction: column;
    
    padding: 4vh 0;
    gap: 3vh;
}

.button-container :deep(button) {
    padding: 2vh 0;
    width: 15vw;
    color: white;
    font-size: 3vh;
    cursor: pointer;

    border: 3px solid black;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
    font-family: "AcmeFont", sans-serif;

    -webkit-text-stroke: 0;
    paint-order: fill;
    text-shadow: none;
    font-weight: 400;
}

.back-button {
    background-image: var(--button-gradient-green);
}

.button-container :deep(button):hover {
    transform: scale(1.05);
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: scale(0.9);
    }

    to {
        opacity: 1;
        transform: scale(1);
    }
}
</style>
