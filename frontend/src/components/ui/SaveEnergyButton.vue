<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { useMilefizStore } from '@/stores/milefizstore';

// Zugriff auf den globalen PiniaStore
const milefizStore = useMilefizStore()

/**
 * Zugriff auf Energy-State
 *  isEnergyFresh: gibt an, ob es sich um "frisch gewürfelte" Energie handelt --> true, wenn sich Spieler noch nicht bewegt hat
 *  isEnergyFull: true, wenn die gesammelte Energie gleich der maxEnergy ist
 */
const isEnergyFresh = computed(()=> milefizStore.energy.isEnergyFresh)
const isEnergyFull = computed(() => milefizStore.energy.isEnergyFull)


/**
 * Steuert, ob der Würfelbutton deaktiviert wird/bleibt
 * Wird deaktiviert,
 *  - sobald Energy nicht mehr fresh ist oder
 *  - solange maxEnergy erreicht ist
 */
const disabled = computed(()=> {
    return !isEnergyFresh.value || isEnergyFull.value
}
)

/**
 * Registriert EventListener für Keyboard Input
 * Hotkey "E"
 */
onMounted(() => {
    window.addEventListener("keydown", onKeypress)
});


/**
 * Beim unmounten wird Listener removed
 */
onUnmounted(() => {
    window.removeEventListener("keydown", onKeypress)
});

const onKeypress = (e: KeyboardEvent) => {
    if (e.key.toLocaleLowerCase() === "e") {
        saveEnergy()
    }
}

/**
 * Versucht das Speichern der Energie im Backend auszulösen
 * - wenn disabled: Triggert die ErrorAnimation
 * - Anfrage ans Backend senden
 */
function saveEnergy() {
    if (disabled.value) {
        triggerErrorAnimation()
    }
    milefizStore.sendEnergySave()
    triggerPressAnimation()
}

/**
 * kurze Animation für den Button Press
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
    <div class="action-button" :class="{ pressed: isPressed, disabled: disabled, error: isError  }">
        <img src="@/assets/hud/lightning.png" class="action-icon"/>
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
/*Standard Press Animation*/
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

/* Error Animation - Shake + Red Flash */
@keyframes errorFeedback {
    0% { 
        transform: translateX(0) scale(1);
        border-color: #c8a25d;
        filter: brightness(1);
        box-shadow: none;
    }
    10% { 
        transform: translateX(-4px) scale(1.05);
        border-color: #ff4444;
        filter: brightness(1.2);
        box-shadow: 
            0 0 25px rgba(255, 68, 68, 1),        /* Großer äußerer Glow */
            0 0 15px rgba(255, 0, 0, 0.8),        /* Mittlerer roter Glow */
            0 0 8px rgba(255, 255, 255, 0.3);     /* Weißer innerer Glow */
    }
    20% { 
        transform: translateX(4px) scale(1.05);
        border-color: #ff4444;
        box-shadow: 
            0 0 20px rgba(255, 68, 68, 0.9),
            0 0 12px rgba(255, 0, 0, 0.7),
            0 0 6px rgba(255, 255, 255, 0.25);
    }
    30% { 
        transform: translateX(-3px) scale(1.02);
        border-color: #ff4444;
        box-shadow: 
            0 0 18px rgba(255, 68, 68, 0.8),
            0 0 10px rgba(255, 0, 0, 0.6),
            0 0 5px rgba(255, 255, 255, 0.2);
    }
    40% { 
        transform: translateX(3px) scale(1.02);
        border-color: #ff4444;
        box-shadow: 
            0 0 15px rgba(255, 68, 68, 0.7),
            0 0 8px rgba(255, 0, 0, 0.5),
            0 0 4px rgba(255, 255, 255, 0.15);
    }
    50% { 
        transform: translateX(-2px) scale(1.01);
        border-color: #ff6666;
        box-shadow: 
            0 0 12px rgba(255, 102, 102, 0.6),
            0 0 6px rgba(255, 0, 0, 0.4);
    }
    60% { 
        transform: translateX(2px) scale(1.01);
        border-color: #ff6666;
        box-shadow: 
            0 0 10px rgba(255, 102, 102, 0.5),
            0 0 5px rgba(255, 0, 0, 0.3);
    }
    70% { 
        transform: translateX(-1px) scale(1);
        border-color: #ff8888;
        box-shadow: 
            0 0 8px rgba(255, 136, 136, 0.4),
            0 0 4px rgba(255, 0, 0, 0.2);
    }
    80% { 
        transform: translateX(1px) scale(1);
        border-color: #ffaaaa;
        box-shadow: 0 0 6px rgba(255, 170, 170, 0.3);
    }
    90% { 
        transform: translateX(0) scale(1);
        border-color: #ffcccc;
        filter: brightness(1.1);
        box-shadow: 0 0 4px rgba(255, 204, 204, 0.2);
    }
    100% { 
        transform: translateX(0) scale(1);
        border-color: #c8a25d;
        filter: brightness(1);
        box-shadow: none;
    }
}

.action-button.error {
    animation: errorFeedback 600ms ease-out;
}

.action-button.disabled {
    filter: grayscale(0.9) brightness(0.5);
    cursor: not-allowed;
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