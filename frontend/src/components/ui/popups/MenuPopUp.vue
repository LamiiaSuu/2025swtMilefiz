<script setup lang="ts">
import { useRouter } from 'vue-router'
import BackButton from '../pages/BackButton.vue'
import { useMilefizStore } from '@/stores/milefizstore'
import { onBeforeUnmount } from 'vue'

import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'

const milefizStore = useMilefizStore()
const router = useRouter()
const audio = useAudioStore()

function onHover() {
    audio.playSfx('hover')
}

onBeforeUnmount(() => {
    milefizStore.closePopUpMenu()
    milefizStore.closePopUpSettings()
})

</script>

<template>
    <div class="overlay">
        <div class="popup">

            <!-- MENU -->
            <div v-if="!milefizStore.popUpSettingsOpen">
                <h1 class="title">Menu</h1>

                <div class="button-container">
                    <button class="popup-menu-button" @mouseenter="onHover" @click="milefizStore.closePopUpMenu()">{{
                        tUI('CONTINUE') }}</button>

                    <button class="popup-menu-button" @mouseenter="onHover" @click="milefizStore.openPopUpSettings()">{{
                        tUI('SETTINGS') }}</button>

                    <BackButton class="back-button" @mouseenter="onHover" :to="{ name: 'Homepage' }">{{
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
    z-index: 10000;
    pointer-events: auto;

    background-color: rgba(0, 0, 0, 0.6);
}

.popup {
    display: flex;
    flex-direction: column;
    background-color: var(--background-color-forms);
    border-radius: 15px;
    width: 25vw;
    min-height: 50vh;
    padding: 2vh;
    text-align: center;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
    border: 7px solid #57AA51;
    animation: fadeIn 0.3s ease;
}

.settings-content {
    display: flex;
    flex-direction: column;
    gap: 2vh;
}

.text {
    display: flex;
    flex-direction: column;
    margin: 3vh 0;
    text-align: center;
    align-items: center;
}

.title {
    font-size: 7vh;
    color: #000000;
    margin-bottom: 2vh;
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

    border: 3px solid black;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
    font-family: "AcmeFont", sans-serif;

    -webkit-text-stroke: 0;
    paint-order: fill;
    text-shadow: none;
    font-weight: 400;
}

.button-container :deep(.back-button) {
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