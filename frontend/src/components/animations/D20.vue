<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useGLTF } from '@tresjs/cientos'

// Props
const props = defineProps<{
  number: number        // backend-provided number (1-20)
  scale?: number
  position?: [number, number, number]
}>()

// Load dice GLB
const modelPath = '/dice/d20.glb'
const { state } = useGLTF(modelPath)

// Computed scene
const gltfScene = computed(() => state.value?.scene ?? null)

// Rotation state
const rotation = ref<[number, number, number]>([0, 0, 0])
const targetRotation = ref<[number, number, number]>([0, 0, 0])
const startRotation = ref<[number, number, number]>([0, 0, 0])
const rolling = ref(false)
const rollProgress = ref(0)
const ROLL_DURATION = 1.5 // seconds
const previousNumber = ref<number>(1)

// D20 face rotations – tweak these to match your dice
const D20_FACE_ROTATIONS: Record<number, [number, number, number]> = {
  1: [0, 0, 0],
  2: [0, Math.PI / 3, 0],
  3: [Math.PI / 3, 0, 0],
  4: [0, -Math.PI / 3, 0],
  5: [-Math.PI / 3, 0, 0],
  6: [0, 0, Math.PI / 3],
  7: [0, 0, -Math.PI / 3],
  8: [Math.PI / 4, Math.PI / 4, 0],
  9: [-Math.PI / 4, Math.PI / 4, 0],
  10: [Math.PI / 4, -Math.PI / 4, 0],
  11: [-Math.PI / 4, -Math.PI / 4, 0],
  12: [0, Math.PI / 6, Math.PI / 6],
  13: [0, -Math.PI / 6, Math.PI / 6],
  14: [0, Math.PI / 6, -Math.PI / 6],
  15: [0, -Math.PI / 6, -Math.PI / 6],
  16: [Math.PI / 6, 0, Math.PI / 6],
  17: [-Math.PI / 6, 0, Math.PI / 6],
  18: [Math.PI / 6, 0, -Math.PI / 6],
  19: [-Math.PI / 6, 0, -Math.PI / 6],
  20: [Math.PI / 8, Math.PI / 8, Math.PI / 8],
}

// Easing function for smooth animation
function easeOutCubic(t: number): number {
  return 1 - Math.pow(1 - t, 3)
}

// Function to trigger a roll
function rollDice(number: number) {
  const finalRotation = D20_FACE_ROTATIONS[number] || [0, 0, 0]
  
  // Save current rotation as start
  startRotation.value = [...rotation.value] as [number, number, number]
  
  // Add multiple spins for rolling effect
  const spins = 3 + Math.random() * 2 // 3-5 full rotations
  targetRotation.value = [
    finalRotation[0] + Math.PI * 2 * spins,
    finalRotation[1] + Math.PI * 2 * spins * 0.8,
    finalRotation[2] + Math.PI * 2 * spins * 0.6
  ]
  
  rollProgress.value = 0
  rolling.value = true
}

// Initialize to face 1
onMounted(() => {
  const initialRotation = D20_FACE_ROTATIONS[1] || [0, 0, 0]
  rotation.value = initialRotation
  targetRotation.value = initialRotation
  startRotation.value = initialRotation
})

// Watch for backend number changes
watch(() => props.number, (newVal, oldVal) => {
  // Only roll if the number actually changed and it's not the initial value
  if (newVal && newVal !== previousNumber.value) {
    previousNumber.value = newVal
    rollDice(newVal)
  }
}, { immediate: true })

// Animation frame handler
function onFrame(state: any, delta: number) {
  if (!rolling.value) return
  
  rollProgress.value += delta / ROLL_DURATION
  
  if (rollProgress.value >= 1) {
    // Animation complete - snap to final rotation
    rotation.value = D20_FACE_ROTATIONS[props.number] || [0, 0, 0]
    rolling.value = false
    rollProgress.value = 1
  } else {
    // Interpolate with easing
    const t = easeOutCubic(rollProgress.value)
    rotation.value = startRotation.value.map((start, i) => {
      const target = targetRotation.value[i] ?? 0
      return start + (target - start) * t
    }) as [number, number, number]
  }
}

</script>

<template>
  <group
    :scale="props.scale ?? 1"
    :position="props.position ?? [0, 0, 0]"
    :rotation="rotation"
    @frame="onFrame"
  >
    <primitive v-if="gltfScene" :object="gltfScene" />
  </group>
</template>