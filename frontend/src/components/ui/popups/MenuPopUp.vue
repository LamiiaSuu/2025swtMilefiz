<script setup lang="ts">
import { useRouter } from 'vue-router'
import BackButton from '../pages/BackButton.vue'
import { useMilefizStore } from '@/stores/milefizstore'

const store = useMilefizStore()
const router = useRouter()

</script>

<template>
    <div class="overlay">
        <div class="popup">

            <div class="text">
                <h1 class="title">Menu</h1>
            </div>

            <div class="button-container">
                <button class="popup-menu-button" @click="store.togglePopUpMenu()">Weiter</button>
                <button class="popup-menu-button">Einstellungen</button>
                <BackButton class="back-button" :to="{ name: 'Homepage' }">&lt; Spiel verlassen</BackButton>
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
    height: 50vh;
    text-align: center;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
    border: 7px solid #b52326;
    animation: fadeIn 0.3s ease;
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

.back-button {
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
