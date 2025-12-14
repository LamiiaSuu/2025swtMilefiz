<script setup lang="ts">
import { computed, onBeforeMount, onBeforeUnmount, onMounted, onServerPrefetch, ref, watch } from "vue";
import { useMilefizStore } from "@/stores/milefizstore";

// Zugriff auf globalen PiniaStore
const milefizStore = useMilefizStore()

/** Zugriff auf Cooldown-State
 * remainingSceonds: Wert vom Server
 * isCooldownActive: gibt an, ob Cooldwon serverseitig aktiv ist 
 */
const remainingSeconds = computed(() => milefizStore.cooldown.remainingSeconds)
const isCooldownActive = computed(() => milefizStore.cooldown.active)
/**
 * steuert, ob der Würfelbutton deaktiviert wird/bleibt
 * → true, solange Cooldown aktiv ist
 */
const disabled = computed(() =>
    isCooldownActive.value
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


/* Würfeln Hotkey Mapping auf Key "R"*/
const onKeypress = (e: KeyboardEvent) => {
    if (e.key.toLocaleLowerCase() === "r") {
        console.log("Würfeln angestoßen")
        rollDice()
    }
}


/**
 * Versucht einen Würfelwurf im Backend auszulösen.
 * - wenn disabled: Abbruch
 * - sonst Anfrage ans Backend senden
 * - und visuelles Feedback für das Aktivieren des Buttons 
 */
function rollDice() {
    if (disabled.value) {
        return
    }
    milefizStore.sendRollDice();
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

</script>
<template>
    <div class="action-button" :class="{ pressed: isPressed, disabled: disabled }">
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

.hotkey {
    position: absolute;
    bottom: -3px;
    left: 3px;
    font-size: 18px;
    font-weight: bold;
    color: #ffffff;
    border-radius: 3px;
    font-family: "AcmeFont", sans-serif;
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