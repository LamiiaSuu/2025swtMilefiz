<script setup lang="ts">
import { computed, onBeforeMount, onBeforeUnmount, onMounted, onServerPrefetch, onUnmounted, ref, watch } from "vue";
import { useMilefizStore } from "@/stores/milefizstore";
import { useAudioStore } from "@/stores/audioStore";

// Zugriff auf globalen PiniaStore
const milefizStore = useMilefizStore()
const audio = useAudioStore()

/** Zugriff auf Cooldown-State
 * remainingSceonds: Wert vom Server
 * isCooldownActive: gibt an, ob Cooldwon serverseitig aktiv ist 
 */
const remainingSeconds = computed(() => milefizStore.cooldown.remainingSeconds)
const isCooldownActive = computed(() => milefizStore.cooldown.active)
const isMovesLeft = computed(() => (milefizStore.gamedata?.currentDiceRoll ?? 0) > 0)
/**
 * steuert, ob der Würfelbutton deaktiviert wird/bleibt
 * → true, solange Cooldown aktiv ist
 */
const disabled = computed(() =>
    isCooldownActive.value || isMovesLeft.value
)

/**
 * Lokaler Frontend Countdown, der den Cooldown visuell anzeigt
 */
const localCountdown = ref(0)


/**
 * Endzeitpunkt, zu dem der Cooldown exakt endet 
 */
let timestampEnd: number | null = null;

/**
 * Referenz auf ein setInterval für den lokalen Countdown
 */
let interval: number | null = null


/**
 * Reagiert auf Änderungen der serverseitigen remainingSeconds.
 * Sobald der Cooldown auf ungleich 0 Sekunden geändert wird, wird der lokale Countdown gestartet 
 */
watch(remainingSeconds, (value) => {
    if (value > 0) {
        timestampEnd = Date.now() + value * 1000;
    } else {
        timestampEnd = null;
        localCountdown.value = 0;
    }
})

/**
 * - Startet exakten UI Countdown für den Cooldown → läuft merhmals pro Sekunde für eine höhere Genauigkeit
 * - Registriert EventListener für Keyboard Input 
 */

onMounted(() => {
    interval = window.setInterval(() => {
        if (timestampEnd === null) {
            localCountdown.value = 0;
            return;
        }
        const diff = timestampEnd - Date.now();
        localCountdown.value = Math.max(0, Math.ceil(diff / 1000));
    }, 150)

    window.addEventListener("keydown", onKeypress)
});

/**
 * Beim unmounten wird Listener removed
 */
onUnmounted(() => {
    window.removeEventListener("keydown", onKeypress)
})


/* Würfeln Hotkey Mapping auf Key "R"*/
const onKeypress = (e: KeyboardEvent) => {
    if (e.key.toLocaleLowerCase() === "r") {
        console.log("Würfeln angestoßen")
        audio.playSfx('gameHUD')
        rollDice()
    }

    // shift+1 - shift+6 für requested Dice rolls
    if (e.shiftKey) {
        const match = e.code.match(/^Digit([1-6])$/)
        if (match) {
            e.preventDefault()
            const requestedValue = parseInt(match[1]!)
            console.log("Würfeln mit gewünschten Wert:", requestedValue)
            audio.playSfx('gameHUD')
            rollDice(requestedValue)
        }
    }
}

/**
 * Versucht einen Würfelwurf im Backend auszulösen.
 * - wenn disabled: Abbruch
 * - sonst Anfrage ans Backend senden
 * - und visuelles Feedback für das Aktivieren des Buttons 
 * @param requestedValue optionaler spezifischer Würfelwert (1-6)
 */
function rollDice(requestedValue?: number) {
    if (disabled.value) {
        triggerErrorAnimation()
    }
    milefizStore.sendRollDice(requestedValue);
    triggerPressAnimation();
}


/**
 * Bereinigung: Falls Komponente zerstört wird: Timer stoppen und Leaks vermeiden
 */
onBeforeUnmount(() => {
    if (interval) clearInterval(interval)
})

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
    <div class="action-button" :class="{ pressed: isPressed, disabled: disabled, error: isError }">
        <img src="@/assets/hud/dice.png" class="action-icon" />
        <div v-if="localCountdown > 0" class="cooldown-overlay">
            {{ localCountdown }}
        </div>
        <span class="hotkey">R</span>
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


.cooldown-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;
    font-size: 32px;
    font-weight: bold;
    pointer-events: none;
    font-family: "AcmeFont", sans-serif;
}
</style>