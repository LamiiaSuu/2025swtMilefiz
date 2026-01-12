<template>
    <div class="slot-container">
        <div class="slot-reel">
            <img v-for="(icon, index) in icons" :key="index" :class="['symbol', { active: currentIndex === index }]"
                :src="icon" />
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps<{
    speed?: number   // Geschwindigkeit des Wechsels in ms
    result?: string // Das finale Ergebnis (z.B. 'BLUE', 'GREEN', etc)
}>()

const icons = [
    '/meepleIcons/BLUE_default.png',
    '/meepleIcons/GREEN_default.png',
    '/meepleIcons/RED_default.png',
    '/meepleIcons/YELLOW_default.png'
]

const colorToIndex: Record<string, number> = {
    'BLUE': 0,
    'GREEN': 1,
    'RED': 2,
    'YELLOW': 3
}

const currentIndex = ref(0)
let interval: number | null = null

function startRotation() {

    interval = setInterval(() => {
        currentIndex.value = (currentIndex.value + 1) % icons.length //wechselt durch die einzelnen icons durch
    }, props.speed ?? 150)
}

function stopRotation(result: string) {
    if (interval) {
        clearInterval(interval)
        interval = null
    }

    //Index wird bassierend auf dem Ergebnis gesetzt
    const index = colorToIndex[result]
    if (index !== undefined) {
        currentIndex.value = index
    }
}

//result Prop überwachen
watch(() => props.result, (newResult) => {
    console.log('Slot result changed:', newResult)
    if (newResult) {
        console.log('Stopping rotation with result:', newResult)
        stopRotation(newResult)
    }
})

onMounted(() => {
    currentIndex.value = Math.floor(Math.random() * icons.length) //setzt initial einen zufälligen index Wert, damit nicht alle Slots an gleicher stelle starten

    console.log('Start rotaion')
    startRotation()
})

onUnmounted(() => {
    if (interval) clearInterval(interval) //ohne würde setInterval in startRotation, auch nach dem minigame weiter laufen
})
</script>

<style scoped>
.slot-container {
    position: relative;
    width: 80px;
    height: 80px;
}

.slot-reel {
    position: relative;
    width: 100%;
    height: 100%;
}

.symbol {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;
    opacity: 0;
    transition: opacity 0.1s;
    object-fit: contain;
}

.symbol.active {
    opacity: 1;
}
</style>