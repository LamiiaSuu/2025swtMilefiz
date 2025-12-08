<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = defineProps<{ to: string | { name: string } }>()
const router = useRouter()

const goBack = () => {
  if (props.to) {
    router.push(props.to)
  }
}
</script>


<template>
  <button class="back-button" @click="goBack">
    Zurück
  </button>
</template>

<style>
.back-button {
  background-color: #234420;
  border: 4px solid #c8a25d;
  border-radius: 8px;
  color: white;
  padding: 10px 20px;
  cursor: pointer;
  font-family: "AcmeFont", sans-serif;
  font-size: 2rem;
  -webkit-text-stroke: 0 !important;
  text-shadow: none !important;
}

.back-button:hover {
  opacity: 0.8;
}
</style>