<script setup lang="ts">
import { computed, onBeforeMount, onBeforeUnmount, onMounted, onServerPrefetch, ref, watch } from "vue";
import { useMilefizStore } from "@/stores/milefizstore";

// Zugriff auf globalen PiniaStore
const milefizStore = useMilefizStore()

const isEnergyEnough = true;

/**
 * - Registriert EventListener für Keyboard Input 
 */
onMounted(() => {
    window.addEventListener("keydown", onKeypress)
});

/**
 * steuert, ob der Jump Button deaktiviert wird/bleibt
 * → true, solange nicht genügend Energie gesammelt wurde
 */
const disabled = computed(() =>
    !isEnergyEnough
)


/* Hüpfen Hotkey Mapping auf Key " " (Spacebar)*/
const onKeypress = (e: KeyboardEvent) => {
    if (e.key === " ") {
        console.log("Hüpfen angestoßen")
        jump()
    }
}


/**
 * -
 * - visuelles Feedback für Aktivierung des Buttons
 */
function jump() {
    /* if (disabled.value) {
        return
    } */

    /**
     * Jump Button deaktivieren, solange Meeple noch hüpft
     */
    if (milefizStore.isJumping) {
        console.log("Hüpfen nicht erlaubt!")
        return
    }
    
    milefizStore.requestJump()
    
    /* Press Animation für den Button*/
    triggerPressAnimation();
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
    <div class="action-button" :class="{ pressed: isPressed, disabled: milefizStore.isJumping}">
        <!-- Jumping Meeple Icon -->
        <img src="@/assets/hud/JumpingMeeple.png" class="action-icon" />
        <!-- Spacebar Icon -->
        <img src="@/assets/hud/spacebar_icon_light.png" class="hotkey-space" />
    </div>
</template>

<style>
.hotkey-space {
    position: absolute;
    bottom: -6px;
    left: 2px;
    width: 24px;
    height: auto;
    pointer-events: none;
}

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

.cooldown-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;
    font-size: 26px;
    font-weight: bold;
    pointer-events: none;
}
</style>