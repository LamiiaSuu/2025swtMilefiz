<template>
  <div v-if="show" class="loading">

    <!-- Header – exakt wie auf Home -->
    <Header overlay></Header>

    <!-- Inhalt in der Mitte -->
    <div class="content">
      <div class="spinner"></div>

      <p class="tip">
        {{ currentTip }}
      </p>
    </div>

  </div>
</template>

<script setup>
import { ref, watch, onUnmounted } from "vue"
import Header from "@/components/ui/pages/Header.vue"

const props = defineProps({
  show: Boolean,
  interval: {
    type: Number,
    default: 2000
  }
})

const currentTip = ref("Vergiss Tips.")
let timer = null

function chooseRandomTip() {

}

watch(() => props.show, val => {
  if (val) {
    chooseRandomTip()
    timer = setInterval(chooseRandomTip, props.interval)
  } else {
    clearInterval(timer)
  }
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
/* entspricht */
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
  z-index: 9999;
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

  filter: blur(4px);
  filter: brightness(0.6);
  border: 1px solid black;
  z-index: -1;
}

/* Loading-Inhalt */
.content {
  margin-top: 10vh;
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #999;
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

.tip {
  font-size: 1.2rem;
  text-shadow: 0 2px 6px rgba(0,0,0,.6);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
