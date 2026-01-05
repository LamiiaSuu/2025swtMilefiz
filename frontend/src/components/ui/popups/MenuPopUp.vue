<script setup lang="ts">
import { useRouter } from 'vue-router'
import BackButton from '../pages/BackButton.vue'
import { useMilefizStore } from '@/stores/milefizstore'
import Header from '../pages/Header.vue'
import PopUpCloseButton from './PopUpCloseButton.vue'
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

const onTutorialClick = () => {
    audio.playSfx('click')
}

const onControlsClick = () => {
    audio.playSfx('click')
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
        <Header>{{ tUI('POPUP_MENU') }}</Header>

        <!-- Close Button -->
        <PopUpCloseButton></PopUpCloseButton>

        <div class="popup">

            <!-- MENU -->
            <div v-if="!milefizStore.popUpSettingsOpen">

                <div class="button-container">
                    <button class="menu-button" @mouseenter="onHover" @click="onContinueClick()">{{
                        tUI('CONTINUE') }}</button>

                    <button class="menu-button" @mouseenter="onHover" @click="onTutorialClick()">{{ tUI('TUTORIAL')
                    }}</button>

                    <button class="menu-button" @mouseenter="onHover" @click="onControlsClick()">{{ tUI('CONTROLS')
                    }}</button>

                    <button class="menu-button" @mouseenter="onHover" @click="onSettingsClick()">{{ tUI('SETTINGS')
                    }}</button>

                    <BackButton class="back-button" @mouseenter="onHover" @click="onBackClick()"
                        :to="{ name: 'Homepage' }" :confirm="true"
                        :confirmText="tUI('BACK_TO_MAIN_MENU_CONFIRMATION_INGAME')">{{
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

    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);

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

    margin-top: 5vh;
    margin-bottom: 5vh;
}

.settings-content {
    display: flex;
    flex-direction: column;
    gap: 2vh;
}

.button-container {
    display: flex;
    flex-direction: column;
    gap: 2vh;
    align-items: center;
    margin-top: -15vh;
}


.button-container :deep(.back-button) {
    margin-top: 2vh;
    padding: 10px 45px;

    color: white;
    font-size: 3.5vh;

    border: 3px solid black;
    border-radius: 10px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
    background-image: var(--button-gradient-red);

    outline: none;

    padding-bottom: 1vh;

    -webkit-text-stroke: 6px black;
    text-shadow:
        3px 3px 6px rgba(0, 0, 0, 0.8),
        0 0 10px rgba(0, 0, 0, 0.5);
    paint-order: stroke fill;
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