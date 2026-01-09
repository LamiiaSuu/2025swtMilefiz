<template>
  <!-- Countdown-Container -->
  <div class="countdown-wrapper">
    <!-- Anzeige der verbleibenden Zeit (Sekunden) -->
    <div class="countdown-number">
      {{ timeLeft }}
    </div>

    <!-- Visueller Countdown-Balken -->
    <div class="countdown-bar">
        <div
            class="countdown-bar-fill"
            :style="{ width: progress + '%' }"
        ></div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted } from "vue"

/**
 * Übergabeparameter:
 * - seconds: Gesamtdauer des Countdowns in Sekunden
 */
const props = defineProps<{
  seconds: number
}>()

/**
 * Events:
 * - finished: Wird ausgelöst, sobald der Countdown abgelaufen ist
 */
const emit = defineEmits<{
  (e: "finished"): void
}>()

/**
 * Reaktive Anzeige der verbleibenden Sekunden.
 * Die Anzeige ist bewusst um 1 reduziert,
 * um ein spieltypisches Countdown-Gefühl zu erzeugen.
 */
const timeLeft = ref(0)

/**
 * Fortschritt des Countdown-Balkens in Prozent (100 -> 0).
 */
const progress = ref(100)

/**
 * Interne Timer-Referenz für das Update-Intervall.
 */
let interval: number | null = null

/**
 * Startzeitpunkt des Countdowns (in ms seit Epoch).
 */
let startTime = 0

/**
 * Startet den Countdown basierend auf der übergebenen Sekundenanzahl.
 *
 * Eigenschaften:
 * - Zeitbasierte Berechnung
 * - Lineares Abnehmen des Balkens
 * - Anzeige springt bewusst 1s früher. Damit hat man im Frontend noch eine Sekunde "Schonzeit"
 * - Unabhängig von Re-Renders oder Frame-Drops
 */
function startCountdown() {
  clearInterval(interval!)

  timeLeft.value = props.seconds
  progress.value = 100
  startTime = Date.now()

  interval = window.setInterval(() => {
    const elapsed = (Date.now() - startTime) / 1000
    const remaining = Math.max(props.seconds - elapsed, 0)

    timeLeft.value = Math.max(Math.ceil(remaining) - 1, 0)
    progress.value = Math.max((remaining / props.seconds) * 100, 0)

    if (remaining <= 0) {
      clearInterval(interval!)
      emit("finished")
    }
  }, 50)
}

/**
 * Beobachtet Änderungen der übergebenen Sekunden.
 *
 * Startet den Countdown automatisch:
 * - beim ersten Render
 * - bei jeder neuen Countdown-Dauer
 */
watch(
  () => props.seconds,
  value => {
    if (value > 0) startCountdown()
  },
  { immediate: true }
)

/**
 * Cleanup beim Entfernen der Komponente,
 * um Speicherlecks zu vermeiden.
 */
onUnmounted(() => {
  if (interval) clearInterval(interval)
})
</script>

<style scoped>
.countdown-wrapper {
  margin: 6px 0 10px;
  text-align: center;
}

/* Große Zahl */
.countdown-number {
  font-family: "Acme", sans-serif;
  font-size: 3.4rem;
  font-weight: 900;
  opacity: 0.95;

  text-shadow:
    0 0 2px rgba(0,0,0,.95),
    1px 1px 4px rgba(0,0,0,.95),
    -1px -1px 4px rgba(0,0,0,.95);
}

/* Balken-Hintergrund */
.countdown-bar {
  width: 100%;
  height: 6px;
  margin-top: 4px;
    background: #111;
  background: rgba(0,0,0,.4);
  border-radius: 6px;
  overflow: hidden;
}

/* Ablaufender Balken */
.countdown-bar-fill {
  height: 100%;
  width: 100%;

  background: linear-gradient(
    90deg,
    #27ae60,
    #f39c12,
    #c0392b
  );

  transition: width 0.05s linear;
}

</style>
