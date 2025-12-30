<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useMilefizStore } from '@/stores/milefizstore'
import { useAudioStore } from '@/stores/audioStore';
import { tUI } from '@/i18n';

const milefizStore = useMilefizStore()
const { disconnectAndReset } = milefizStore

const props = defineProps<{ to: string | { name: string } }>()
const router = useRouter()
const audio = useAudioStore()

const goBack = () => {
  if (props.to) {
    audio.playSfx('click')
    router.push(props.to)
    disconnectAndReset() // disconnect current lobby
  }
}
</script>


<template>
  <button class="back-button" @click="goBack">
    <slot>&lt; {{ tUI('BACK') }}</slot>
  </button>
</template>

<style scoped>
.back-button {
  background: none;
  border: none;
  color: white;
  font-size: 4vh;
  font-weight: bold;
  cursor: pointer;
  font-family: "AcmeFont", sans-serif;
  transition: all 0.2s;
  -webkit-text-stroke: 6px black;
  text-shadow:
    2px 2px 4px rgba(0, 0, 0, 0.8),
    0 0 8px rgba(0, 0, 0, 0.5);
  paint-order: stroke fill;
}

.back-button:hover {
  transform: scale(1.05);
}
</style>