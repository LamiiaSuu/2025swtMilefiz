<script setup lang="ts">
import { useRouter } from 'vue-router'
import BackButton from '../pages/BackButton.vue'
import { useMilefizStore } from '@/stores/milefizstore'
import Header from '../pages/Header.vue'

import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'

const milefizStore = useMilefizStore()
const router = useRouter()
const audio = useAudioStore()

function onHover() {
    audio.playSfx('hover')
}

const onContinueClick = () => {
    audio.playSfx('click')
    milefizStore.closePopUpMenu()
}

const onSettingsClick = () => {
    audio.playSfx('click')
    milefizStore.openPopUpSettings()
}

const onBackClick = () => {
    audio.playSfx('click')
}

</script>

<template>
    <div class="overlay">

        
        <!-- MI'lefiz Header -->
        <Header>{{ tUI('NEW_GAME') }}</Header>
        <div class="popup">

            <!-- MENU -->
            <div v-if="!milefizStore.popUpSettingsOpen">

                <div class="button-container">
                    <button class="menu-button" @mouseenter="onHover" @click="onContinueClick()">{{
                        tUI('CONTINUE') }}</button>

                    <button class="menu-button" @mouseenter="onHover" @click="onSettingsClick()">{{
                        tUI('SETTINGS') }}</button>

                    <BackButton class="back-button" @mouseenter="onHover" @click="onBackClick()"
                        :to="{ name: 'Homepage' }">{{
                            tUI('LEAVE_GAME') }}</BackButton>
                </div>
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
}

.popup {
    display: flex;
    flex-direction: column;

    border-radius: 15px;
    width: 20vw;
    min-height: 45vh;

    justify-content: center;
    text-align: center;
    animation: fadeIn 0.3s ease;
    
    z-index: -1;
}

.settings-content {
    display: flex;
    flex-direction: column;
    gap: 2vh;
}

.button-container {
    display: flex;
    flex-direction: column;
    gap: 3vh;
    align-items: center;
}

.button-container :deep(button) {
    padding: 2vh 0;
    width: 15vw;
    color: white;
    font-size: 3vh;
    cursor: pointer;

    font-family: "AcmeFont", sans-serif;

    -webkit-text-stroke: 0;
    paint-order: fill;
    text-shadow: none;
    font-weight: 400;
}

.button-container :deep(.back-button) {

    border: 3px solid black;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
    background-image: var(--button-gradient-red);
}

.popup-menu-button {
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