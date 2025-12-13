<script setup lang="ts">
import { ref, computed } from 'vue'
import { useMilefizStore } from '@/stores/milefizstore'

// Zugriff auf PiniaStore
const milefizStore = useMilefizStore()

/**
 * 
 */
const currentEnergy = computed(() => milefizStore.gamedata?.energy ?? 0)

/**
 * Zugriff auf Energy-State
 *  maxEnergy: gibt die maxEnergy an, verwendet für die visuelle Darstellung X/6 
 * */ 
const maxEnergy = computed(()=>milefizStore.energy.maxEnergy || 1)


/**
 * Ermittelt den prozentualen Wert der currentEnergy in Bezug zur maxEnergy an
 */
const barWidth = computed(() => (currentEnergy.value / maxEnergy.value) * 100 + "%")
</script>

<template>
    <div class="energy-container">
        <div class="energy-info">
            <img src="@/assets/hud/lightning.png" class="energy-icon" />
            <span class="energy-text">{{ currentEnergy }}/{{ maxEnergy }}</span>
        </div>

        <div class="energy-bar">
            <div class="saved-energy" :style="{ width: barWidth }">
            </div>
        </div>
    </div>
</template>

<style>
.energy-container {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 40vw;
    max-width: 500px;
    min-width: 400px;
}

.energy-bar {
    flex: 1;
    height: 30px;
    background-color: rgba(179, 219, 215, 0.8);
    /* border: 3px solid #c8a25d; */
    border-top-right-radius: 120px;
    border-bottom-right-radius: 20px;

    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    /* border-radius: 16px; */
    box-sizing: border-box;

    overflow: hidden;

}

.saved-energy {
    height: 100%;
    background-color: #5cfff1;
    /* border-radius: 14px; */
    /* border: 4px solid #c8a25d; */
    border-top-right-radius: 120px;
    border-bottom-right-radius: 20px;
    transition: width 0.8s ease;
}


.energy-info {
    display: flex;
    align-items: center;
    gap: 6px;
    white-space: nowrap;
}

.energy-icon {
    width: 2vw;
    min-width: 20px;
}

.energy-text {
    font-weight: bold;
    color: #ffffff;
}
</style>