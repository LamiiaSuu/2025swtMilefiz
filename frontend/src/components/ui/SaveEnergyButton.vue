<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useMilefizStore } from '@/stores/milefizstore';

const milefizStore = useMilefizStore()

/**
 * - Registriert EventListener für Keyboard Input 
 */
onMounted(() => {
    window.addEventListener("keydown", onKeypress)
});

const onKeypress = (e: KeyboardEvent) => {
    if (e.key.toLocaleLowerCase() === "e") {
        saveEnergy()
    }
}

function saveEnergy(){
    console.log("Würfelzahl als Energie speichern")
    milefizStore.sendEnergySave()
    triggerPressAnimation()
}

/**
 * kurze Animation für den Button
 */
const isPressed = ref(false)

function triggerPressAnimation() {
    isPressed.value = true
    setTimeout(() => (isPressed.value = false), 150)
}
</script>

<template>
    <div class="action-button" :class="{pressed: isPressed}">
        <img src="@/assets/hud/lightning.png" class="action-icon" />
        <span class="hotkey">E</span>
    </div>

</template>

<style>
.action-button {
    position: relative;
    padding: 5px;
    width: 70px;
    height: 70px;
    border: 2px solid #c8a25d;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    background: #234420;
    transition: filter 120ms ease-out, transform 120ms ease-out;
}

@keyframes pressFeedback {
    0% {
        transform: scale(1);
        filter: brightness(1);
    }

    45% {
        transform: scale(0.9);
        filter: brightness(0.75);
    }

    100% {
        transform: scale(1);
        filter: brightness(1);
    }
}

.action-button.pressed {
    animation: pressFeedback 250ms cubic-bezier(.3, 1.7, .6, 1)
}

.action-button.disabled {
    filter: grayscale(0.9) brightness(0.5);
    cursor: not-allowed;
    pointer-events: none;
}

.action-icon {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    image-rendering: crisp-edges;
}

.hotkey {
    position: absolute;
    bottom: -3px;
    left: 3px;
    font-size: 18px;
    font-weight: bold;
    color: #ffffff;
    border-radius: 3px;
}

</style>