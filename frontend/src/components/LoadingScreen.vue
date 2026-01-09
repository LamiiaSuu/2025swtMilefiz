<template>
  <div v-if="show" class="loading">

    <!-- Header -->
    <Header overlay></Header>

    <!-- Progress bar -->
    <div class="content">
      <div class="progress-container">
        <div
          class="progress-bar"
          :style="{ width: progress + '%' }"> 
        </div>
      </div>
      <!-- Tip -->
      <p class="tip">
        {{ currentTip }}
      </p>
    </div>

  </div>
</template>

<script setup>
import { ref, watch, onUnmounted } from "vue"
import Header from "@/components/ui/pages/Header.vue"
import { tRandomTip } from '@/i18n'

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


function chooseRandomTip() {
  currentTip.value = tRandomTip()
}

function startLoading() {
  progress.value = 0
  startTime = Date.now()

  clearInterval(progressTimer)

  progressTimer = setInterval(() => {
    const elapsed = Date.now() - startTime
    const effectiveDuration = Math.max(props.duration - FINISH_EARLY_MS, 1)
    const t = Math.min(elapsed / effectiveDuration, 1)


    const target = t * 100

    if (Math.random() < SKIP_CHANCE && t < 0.95) {
      return
    }

    const delta =
      (target - progress.value) *
      (0.15 + Math.random() * 0.35)

    progress.value = Math.min(
      100,
      progress.value + Math.max(delta, 0.15)
    )

    if (progress.value >= 100 || t >= 1) {
      progress.value = 100
      clearInterval(progressTimer)
    }
  }, PROGRESS_TICK)
}




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
  padding-bottom: 4rem;

  color: white;
  z-index: 999999;
}

/* Hintergrund */
.loading::before {
  content: "";
  position: absolute;
  inset: 0;

  background-image: url("/backgrounds/BackgroundTest.webp");
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;

  filter: blur(4px) brightness(0.6);
  z-index: -1;
}

/* Loading-Inhalt */
.content {
  margin-top: 10vh;
  text-align: center;
}

.progress-container {
  width: min(90vw, 720px);
  height: 40px;
  padding: 0px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 10px;
  overflow: hidden;

  margin: 0 auto 1.5rem;
  box-shadow: inset 0 2px 4px rgba(0,0,0,1);
}

.progress-bar {
  height: 100%;
  width: 0%;

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
  font-size: 3.6vh;
  font-family: 'Acme', sans-serif;
  text-shadow: 0 2px 6px rgba(0,0,0,.9);
}

</style>
