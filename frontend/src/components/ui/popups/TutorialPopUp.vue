<script setup>
import { useRouter } from 'vue-router'
import { useMilefizStore } from '@/stores/milefizstore'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'
import Tutorial from '../Tutorial.vue'
import PopUpCloseButton from './PopUpCloseButton.vue'
import Header from '../pages/Header.vue'


const milefizStore = useMilefizStore()
const router = useRouter()
const audio = useAudioStore()


const onBackClick = () => {
    audio.playSfx('click')
    milefizStore.closePopUpTutorial()
}

</script>

<template>
    <div class="overlay">

        <!-- Close Button -->
        <PopUpCloseButton>(Esc)</PopUpCloseButton>

        <div class="header">
            <button class="menu-button" disabled>{{ tUI('TUTORIAL') }}</button>
        </div>

        <div class="popup">
            <!-- Tutorial-->
            <Tutorial></Tutorial>

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
    margin-top: 15vh;
    margin-bottom: -13vh;
}

.popup {
    
    margin-top: 7vh;
}

.button-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 2vh 0 6vh 0;
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
</style>