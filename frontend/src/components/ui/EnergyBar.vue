<script setup lang="ts">
import { ref, computed } from 'vue'

/* erstmal hardcoded energy */
const maxEnergy = 6
const currentEnergy = ref(2)

const barWidth = computed(() => (currentEnergy.value / maxEnergy) * 100 + "%")
</script>

<template>
    <div class="energy-bar">
        <div class="saved-energy" :style="{ width: barWidth }"></div>
    </div>
</template>

<style>
.energy-bar {
    position: absolute;
    width: 25vw;
    height: 30px;
    
    background-color: #666;
    border: 4px solid #004b2e;
    border-radius: 10px;
    box-sizing: border-box;
}

.saved-energy {
    height: 100%;
    background-color: rgb(1, 208, 1);
    border-top-left-radius: 6px;
    border-bottom-left-radius: 6px;;
    transition: width 0.3s ease;
}
</style>