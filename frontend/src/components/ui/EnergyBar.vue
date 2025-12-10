<script setup lang="ts">
import { ref, computed } from 'vue'
import { useMilefizStore } from '@/stores/milefizstore'

// Zugriff auf PiniaStore
const milefizStore = useMilefizStore()
const currentEnergy = computed(() => milefizStore.gamedata.energy)

/* erstmal hardcoded energy */
const maxEnergy = 6
//const currentEnergy = ref(2)

/**
 * Energy
 */
const barWidth = computed(() => (currentEnergy.value / maxEnergy) * 100 + "%")
</script>

<template>
    <div>
        <hbox>

            <div class="energy-bar">
                <div class="saved-energy" :style="{ width: barWidth }"></div>
            </div>
        </hbox>
    </div>

</template>

<style>
.energy-bar {
    position: absolute;
    width: 25vw;
    height: 30px;
    fill-opacity: 80%;
    background-color: #b3dbd7; 
    /* border: 3px solid #c8a25d; */
    border-top-right-radius: 120px;
    border-bottom-right-radius: 20px;

    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    /* border-radius: 16px; */
    box-sizing: border-box;

}

.saved-energy {
    height: 100%;
    background-color: #5cfff1;
    /* border-radius: 14px; */
    /* border: 4px solid #c8a25d; */
    border-top-right-radius: 120px;
    border-bottom-right-radius: 20px;

    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    ;
    transition: width 0.8s ease;
}
</style>