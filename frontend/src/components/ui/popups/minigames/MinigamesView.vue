<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useMilefizStore } from '@/stores/milefizstore'

import DiceMinigame from './DiceMinigame.vue';

const store = useMilefizStore()
const router = useRouter()

</script>

<template>
    <div class="overlay">
        <!-- {{ store.minigame|| 'Unbekannte Minispiel' }} -->
        <div class="popup">

            <div class="text">
                <h1 class="title">Unbekannte Minispiel</h1>
            </div>

            <DiceMinigame />
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
    width: 75vw;
    height: 85vh;
    text-align: center;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
    border: 7px solid var(--button-light-green);
    animation: fadeIn 0.3s ease;
}

.text {
    flex: 1;
    display: flex;
    flex-direction: column;
    margin: 3vh 0;
    text-align: center;
    align-items: center;
}

.title {
    font-size: 7vh;
    color: #000000;
    margin-bottom: 1vh;
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
