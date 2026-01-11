<template>
    <div class="slot-container">
        <div class="slot-reel" :class="{ spinning: isSpinning }">
            <img v-for="(icon, index) in icons" :key="index" :class="['symbol', { active: currentIndex === index }]"
                :src="icon" />
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps<{
    offset?: number  // Startverzögerung in ms, damit Slots versetzt sind
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
const isSpinning = ref(true)
let interval: number | null = null

function startRotation() {
    // Versetzter Start durch offset
    setTimeout(() => {
        interval = setInterval(() => {
            currentIndex.value = (currentIndex.value + 1) % icons.length
        }, props.speed ?? 250)
    }, props.offset ?? 0)
}

function stopRotation(result: string) {
    if (interval) {
        clearInterval(interval)
        interval = null
    }
    isSpinning.value = false

    //Index wird bassierend auf dem Ergebnis gesetzt
    const index = colorToIndex[result]
    if (index !== undefined) {
        currentIndex.value = index
    }
}

//result Prop überwachen
watch(() => props.result, (newResult) => {
    if (newResult && newResult !== '') {
        stopRotation(newResult)
    }
})

onMounted(() => {
    // Zufälliger Startindex, damit nicht alle bei 0 beginnen
    currentIndex.value = Math.floor(Math.random() * icons.length)
    startRotation()
})

onUnmounted(() => {
    if (interval) clearInterval(interval)
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

.slot-reel.spinning {
    animation: slotSpin 0.1s linear infinite;
    filter: blur(3px);
}

@keyframes slotSpin {
    0% {
        transform: translateY(20px);
    }

    100% {
        transform: translateY(-60px);
    }
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