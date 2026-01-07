<template>
  <div v-if="show" class="overlay">
    <div class="content">
      <div class="spinner"></div>

      <p class="tip">
        {{ currentTip }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  tips: {
    type: Array,
    default: () => [
      "Tipp: Speichere regelmäßig!",
      "Tipp: Nutze die DevTools für Debugging.",
      "Tipp: Komponenten klein und wiederverwendbar halten.",
      "Tipp: Computed statt überflüssiger Watcher."
    ]
  },
  interval: {
    type: Number,
    default: 2000
  }
})

const currentTip = ref("")
let timer = null

function chooseRandomTip() {
  const index = Math.floor(Math.random() * props.tips.length)
  currentTip.value = props.tips[index]
}

watch(
  () => props.show,
  (val) => {
    if (val) {
      chooseRandomTip()
      timer = setInterval(chooseRandomTip, props.interval)
    } else {
      clearInterval(timer)
    }
  }
)

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: #0d1117dd;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.content {
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
  font-size: 1.1rem;
  opacity: .9;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
