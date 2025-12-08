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
    color: white;
    border: none;
    padding: 25px 45px;
    border-radius: 4px;
    cursor: pointer;
    
}

.back-button:hover {
    opacity: 0.8;
}
</style>