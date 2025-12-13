<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
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

const onKeypress = (e: KeyboardEvent) => {
    if (e.key.toLocaleLowerCase() === "e") {
        saveEnergy()
    }
}

/**
 * Versucht das Speichern der Energie im Backend auszulösen
 * - wenn disabled: Abbruch
 * - sonst Anfrage ans Backend senden
 */
function saveEnergy() {
    if (disabled.value){
        return
    }
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
    <div class="action-button" :class="{ pressed: isPressed, disabled: disabled  }">
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