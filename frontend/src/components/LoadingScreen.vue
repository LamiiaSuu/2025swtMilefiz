<template>
  <div v-if="show" class="loading">

    <!-- Header -->
    <Header overlay></Header>

    <!-- Tip -->
    <p class="tip">
      {{ currentTip }}
    </p>

    <!-- Progress bar -->
    <div class="content">
      <div class="progress-container">
        <div
          class="progress-bar"
          :style="{ width: progress + '%' }"> 
        </div>
      </div>

    </div>

  </div>
</template>

<script setup>
import { ref, watch, onUnmounted } from "vue"
import Header from "@/components/ui/pages/Header.vue"
import { tRandomTip } from '@/i18n'
/**
 * tRandomTip
 * Wählt einen zufälligen Lade-Tipp in der aktuell aktiven Sprache
 * und setzt ihn als angezeigten Text.
 *
 * Die eigentliche Lokalisierung und Zufallsauswahl
 * erfolgt zentral über das i18n-System (`tRandomTip`).
 */


const props = defineProps({
  show: Boolean,
  interval: {
    type: Number,
    default: 3250
  },
  duration: {
    type: Number,
    default: 7000
  }
})

const PROGRESS_TICK = 320 // ms für Loading-Balken Tick
const SKIP_CHANCE = 0.3 // 30% skip chance beim Progress-Tick (Ladebalken)
const FINISH_EARLY_MS = 750 // 0.75s früher fertig als der Loading Screen weg geht (Ladebalken)

const progress = ref(0)
const currentTip = ref("Vergiss Tips.")

let startTime = null
let progressTimer = null
let tipTimer = null


/**
 * Startet die Ladebalken-Animation.
 *
 * Der Fortschritt basiert auf einer Zeitkurve (`duration`),
 * wird jedoch absichtlich nicht linear dargestellt:
 *
 * - Fortschritt nähert sich einem zeitabhängigen Zielwert
 * - einzelne Ticks können zufällig übersprungen werden
 * - der Balken erreicht 100 % bewusst etwas früher
 *
 * Dadurch wirkt der Ladevorgang natürlicher
 * und vermeidet starre, mechanische Bewegungen.
 */
function startLoading() {
  progress.value = 0
  startTime = Date.now()

  // Sicherheitsreset, falls der Loader erneut gestartet wird
  clearInterval(progressTimer)

  progressTimer = setInterval(() => {

    // Vergangene Zeit seit Start des Loadings
    const elapsed = Date.now() - startTime

    // Effektive Dauer, damit der Balken vor dem Screen-Ende voll ist
    const effectiveDuration = Math.max(props.duration - FINISH_EARLY_MS, 1)

    // Normalisierte Zeit (0.0 – 1.0)
    const t = Math.min(elapsed / effectiveDuration, 1)

    // Zielwert, dem sich der Fortschritt annähert
    const target = t * 100

    // Zufälliges Überspringen einzelner Ticks,
    // außer kurz vor dem Abschluss
    if (Math.random() < SKIP_CHANCE && t < 0.95) {
      return
    }

    // Sanfte Annäherung an den Zielwert mit Zufallsfaktor
    const delta =
      (target - progress.value) *
      (0.15 + Math.random() * 0.35)

    // Fortschritt erhöhen, mit minimalem Schritt
    progress.value = Math.min(
      100,
      progress.value + Math.max(delta, 0.15)
    )

    // Abschlussbedingung
    if (progress.value >= 100 || t >= 1) {
      progress.value = 100
      clearInterval(progressTimer)
    }
  }, PROGRESS_TICK)
}

/**
 * Beobachtet die Sichtbarkeit des Loading Screens.
 *
 * - Startet Ladebalken und Tip-Rotation beim Einblenden
 * - Stoppt alle Timer beim Ausblenden
 * - `immediate: true`, damit Initialzustände korrekt behandelt werden
 */
watch(
  () => props.show,
  (val) => {
    if (val) {

      currentTip.value = tRandomTip()
      startLoading()

      clearInterval(tipTimer)

      tipTimer = setTimeout(() => {
        currentTip.value = tRandomTip()

        tipTimer = setInterval(() => {
          currentTip.value = tRandomTip()
        }, props.interval)
      }, props.interval)

    } else {
      clearInterval(progressTimer)
      clearInterval(tipTimer)
    }
  },
  { immediate: true }
)




onUnmounted(() => {
  clearInterval(progressTimer)
  clearInterval(tipTimer)
})

</script>

<style scoped>
.loading {
  position: fixed;
  inset: 0;

  display: flex;
  flex-direction: column;
  align-items: center;

  width: 100vw;
  height: 100vh;

  overflow: hidden;
  padding-bottom: 4vh;

  color: white;
  z-index: 999999;
}

/* Hintergrund */
.loading::before {
  content: "";
  position: absolute;
  inset: -12px;

  background-image: url("/backgrounds/BackgroundTest.webp");
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;

  filter: blur(6px) brightness(0.6);
  z-index: -1;
}

/* Loading-Inhalt */
.content {
  margin-top: 5vh;
  text-align: center;
}

.progress-container {
  width: min(90vw, 720px);
  height: 55px;
  padding: 10px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 10px;
  overflow: hidden;

  margin: 0 auto 1.5rem;
  box-shadow: inset 0 2px 4px rgba(0,0,0,1);
}

.progress-bar {
  height: 100%;
  width: 0%;
  border-radius: 5px;

  background: linear-gradient(
    90deg,
    #0f3d1e,
    #1f6b3a,
    #2e8b57
  );

  transition: width 0.6s cubic-bezier(.4,0,.2,1);
  box-shadow:
    0 0 14px rgba(46, 139, 87, 0.6),
    inset 0 1px 2px rgba(255,255,255,0.15);
}


.tip {
  margin-top: 20vh;
  font-size: 3.6vh;
  font-family: 'Acme', sans-serif;
  text-shadow: 0 2px 6px rgba(0,0,0,.9);
  z-index: 849028314;
  paint-order: stroke fill;
  -webkit-text-stroke: 7px black;
}

</style>
