<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useMilefizStore } from '@/stores/milefizstore'
import AudioSettings from '../AudioSettings.vue'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'
import LanguageSelection from '../LanguageSelection.vue'
import PopUpCloseButton from './PopUpCloseButton.vue'
import Header from '../pages/Header.vue'

const milefizStore = useMilefizStore()
const router = useRouter()
const audio = useAudioStore()

function onHover() {
    audio.playSfx('hover')
}

const onBackClick = () => {
    audio.playSfx('click')
    milefizStore.closePopUpSettings()
}

</script>

<template>
    <div class="overlay">

        <!-- Close Button -->
        <PopUpCloseButton></PopUpCloseButton>

        <div class="header">
            <button class="menu-button" disabled>{{ tUI('SETTINGS') }}</button>
        </div>

        <div class="popup">

            <div class="language">

                <h1>{{ tUI('LANGUAGE') }}</h1>

                <div class="language-wrapper">
                    <!-- LANGUAGE SETTINGS -->
                    <LanguageSelection></LanguageSelection>
                </div>
            </div>

            <div class="audio">
                <h1 class="audio-title">Audio</h1>

                <div class="audio-wrapper">
                    <!-- AUDIO SETTINGS -->
                    <AudioSettings></AudioSettings>
                </div>
            </div>

            <div class="button-container">
                <button class="back-button" @mouseenter="onHover" @click="onBackClick()">{{ tUI('BACK') }}</button>
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
    z-index: 99991;
    pointer-events: auto;
    flex-direction: column;

    background-color: rgba(0, 0, 0, 0.6);

    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
}

.header {
    margin-bottom: -4vh;
}

.popup {
    display: flex;
    flex-direction: column;
    height: 68vh;
    width: 35vw;
    text-align: center;
    position: relative;
    padding-top: 1vh;

    justify-content: center;

    align-items: center;
    animation: fadeIn 0.3s ease;

    margin-top: 5vh;
    border-radius: 18px;

    background:
        linear-gradient(rgba(40, 60, 35, 0.92),
            rgba(25, 40, 25, 0.92));

    box-shadow:
        inset 0 0 0 2px rgba(255, 255, 255, 0.06),
        0 12px 30px rgba(0, 0, 0, 0.45);

    border: 3px solid rgba(120, 160, 110, 0.25);

    z-index: -1;
}


.audio {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    z-index: 10;

    margin-top: -1vh;
}

.audio h1 {
    align-self: flex-start;
    padding-left: 1.5vw;
    padding-bottom: -3vh;
    padding-top: -3vh;
}

h1 {
    color: white;
    font-size: 4vh;
    -webkit-text-stroke: 6px black;
    text-shadow:
        3px 3px 6px rgba(0, 0, 0, 0.8),
        0 0 10px rgba(0, 0, 0, 0.5);
    paint-order: stroke fill;
}

.audio-wrapper {
    display: inline-block;
}

.audio :deep(.settings-panel) {
    margin: 0vh 0 2vh 0;
    background: none;
    box-shadow: none;
    border: none;
}

.language :deep(.language-switch) {
    top: 50%;
    transform: translateY(-50%);
    margin-right: 4vh;
    justify-content: right;
}

.language {
    position: relative;
    display: flex;
    width: 100%;
    box-sizing: border-box;
    padding-left: 1.5vw;
    padding-right: 5vw;
    margin-bottom: 5vh;
    margin-top: 5vh;
}

.button-container {
    display: flex;
    flex-direction: column;

    padding: 0vh 0 6vh 0;
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
