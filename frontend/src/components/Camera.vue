<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import type { TresObject } from '@tresjs/core'

// Definiert Props für den zugehörigen Character, 
// ob First Person an ist und das Offset der Kamera
const props = defineProps<{
  gameCharRef: TresObject | null
  useFirstPerson: boolean
  offset?: { x: number; y: number; z: number }
}>()

const emit = defineEmits<{
  rotateCharacter: [yRotation: number]
}>()

const cameraRef = ref<TresObject | null>(null)
const verticalRotation = ref(0) // Hoch + Runter Rotation
const horizontalRotation = ref(0) // Links + Rechts Rotation

const mouseSensitivity = 0.002
const maxVerticalAngle = Math.PI / 3 // Limitiert Hoch/Runter

// Berechnete Kamera Position neu, wenn sie sich ändert
// Kamera Position = Charakter Position + Offset
const cameraPosition = computed((): [number, number, number] => {
  if (!props.gameCharRef || !props.useFirstPerson) {
    return [0, 1, 0]
  }

  const char = props.gameCharRef as any
  const offset = props.offset || { x: 0, y: 1, z: 0 }
  
  // Position wird vom Charakter abgefragt
  const charPos = char.position || [0, 0, 0]
  
  // Rückgabe von Kamera Position (Charakter Position + Offset)
  return [
    charPos[0] + offset.x,
    charPos[1] + offset.y,
    charPos[2] + offset.z
  ]
})

// Berechnete Rotation der Kamera neu, wenn sie sich ändert
const cameraRotation = computed((): [number, number, number] => {
  return [verticalRotation.value, horizontalRotation.value, 0]
})

// Kamera Maussteuerung
const onMouseMove = (e: MouseEvent) => {
  if (!props.useFirstPerson) return // Keine Maussteurung

  // Horizontale Rotation - Dreht Charakter!
  horizontalRotation.value -= e.movementX * mouseSensitivity
  emit('rotateCharacter', horizontalRotation.value)

  // Vertikale Rotation - Beweget NUR die Kamera, nicht den Charakter
  verticalRotation.value -= e.movementY * mouseSensitivity
  verticalRotation.value = Math.max(
    -maxVerticalAngle,
    Math.min(maxVerticalAngle, verticalRotation.value)
  )
}

// Lässt Mauszeiger in der First Person Kamera verschwinden 
// Wenn man im First Person Mode esc drückt, 
// taucht der Zeiger wieder auf und man kann sich noch umschauen
watch(() => props.useFirstPerson, (isFirstPerson) => {
  if (isFirstPerson) {
    document.body.requestPointerLock()
  } else {
    document.exitPointerLock() // Mauszeiger bei OrbitControl wieder an
  }
})

onMounted(() => {
  document.addEventListener('mousemove', onMouseMove)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  if (document.pointerLockElement) {
    document.exitPointerLock()
  }
})

// Gibt Kamera frei
defineExpose({ cameraRef })

</script>

<template>
    <TresPerspectiveCamera
        v-if="useFirstPerson"
        ref="cameraRef"
        :position="cameraPosition"
        :rotation="cameraRotation"
        :fov="90"
        rotation-order="YXZ"
    />
</template>