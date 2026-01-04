<script setup lang="ts">
import { computed, onBeforeMount, onBeforeUnmount, onMounted, onServerPrefetch, onUnmounted, ref, watch } from "vue";
import { useMilefizStore } from "@/stores/milefizstore";
import { useAudioStore } from "@/stores/audioStore";

// Zugriff auf globalen PiniaStore
const milefizStore = useMilefizStore()
const audio = useAudioStore()

const isEnergyFull = computed(()=> milefizStore.energy.isEnergyFull);

/**
 * - Registriert EventListener für Keyboard Input 
 */
onMounted(() => {
    window.addEventListener("keydown", onKeypress)
});

/**
 * Beim unmounten wird Listener removed
 */
onUnmounted(() => {
  window.removeEventListener("keydown", onKeypress)
})

/**
 * steuert, ob der Jump Button deaktiviert wird/bleibt
 * → true, solange nicht genügend Energie gesammelt wurde
 */
const disabled = computed(() =>
    !isEnergyFull.value
)


/* Hüpfen Hotkey Mapping auf Key " " (Spacebar)*/
const onKeypress = (e: KeyboardEvent) => {
    if (e.key === " ") {
        audio.playSfx('gameHUD')
        jump()
    }
}


/**
 * - Überprüft zunächst frontendseitig, ob Spieler genug Energie zum Hüpfen hat
 * - visuelles Feedback für Aktivierung des Buttons
 */
function jump() {
    if (disabled.value) {
        console.log("Hüpfen nicht erlaubt!")
        triggerErrorAnimation()
    }
    
    console.log("Hüpfen Request gesendet.")
    milefizStore.sendEnergyConsume()
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

/**
 * Error Animation für ungültige Aktionen
 */
const isError = ref(false)

function triggerErrorAnimation() {
    isError.value = true
    setTimeout(() => (isError.value = false), 600)
}

</script>
<template>
    <div class="action-button" :class="{ pressed: isPressed, disabled: disabled, error: isError}">
        <img src="@/assets/hud/JumpingMeeple.png" class="action-icon" />
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
</style>