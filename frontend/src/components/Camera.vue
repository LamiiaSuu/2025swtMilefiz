<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import type { TresObject } from '@tresjs/core'
import type { PerspectiveCamera } from 'three'
import { useMilefizStore } from '@/stores/milefizstore'

const milefizStore = useMilefizStore()


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

const cameraRef = ref<PerspectiveCamera>()

const verticalRotation = ref(0)
const horizontalRotation = ref(0)

const mouseSensitivity = 0.002
const maxVerticalAngle = Math.PI / 3


/* =========================
   Computed
========================= */


// Berechnete Kamera Position neu, wenn sie sich ändert
// Kamera Position = Charakter Position + Offset

const cameraPosition = computed<[number, number, number]>(() => {
  if (!props.gameCharRef || !props.useFirstPerson) return [0, 1, 0]

  const char = props.gameCharRef as any
  const offset = props.offset || { x: 0, y: 1, z: 0 }

  // Position wird vom Charakter abgefragt
  const charPos = char?.characterPosition?.position || { x: 0, y: 0, z: 0 }

  // Rückgabe von Kamera Position (Charakter Position + Offset)
  return [
    charPos.x + offset.x,
    charPos.y + offset.y,
    charPos.z + offset.z
  ]
})

const cameraRotation = computed<[number, number, number]>(() => [
  verticalRotation.value,
  horizontalRotation.value + Math.PI,
  0
])

/* =========================
   Mouse & Pointer Lock
========================= */

const onMouseMove = (e: MouseEvent) => {
  if (!props.useFirstPerson || milefizStore.gameFinished) return

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

const requestPointerLock = () => {
  if (!document.pointerLockElement && props.useFirstPerson) {
    document.body.requestPointerLock()
  }
}

/* =========================
   requestAnimationFrame Loop
========================= */

let rafId: number | null = null

const updateCamera = () => {
    
  // Kamera nur updaten, wenn First Person an und cameraRef existiert
  if (
    props.useFirstPerson &&
    cameraRef.value &&
    props.gameCharRef?.characterPosition
  ) {
    const charPos = props.gameCharRef.characterPosition.position
    const offset = props.offset || { x: 0, y: 0.55, z: 0 }

    // Kamera-Position setzen
    cameraRef.value.position.set(
      charPos.x + offset.x,
      charPos.y + offset.y,
      charPos.z + offset.z
    )
  }
  
  // Nächsten Frame planen
  rafId = requestAnimationFrame(updateCamera)
}

/* =========================
   Watcher
========================= */

watch(() => props.useFirstPerson, (isFP) => {
  if (isFP) {
    requestPointerLock()
  } else if (document.pointerLockElement) {
    document.exitPointerLock()
  }
})

watch(
  () => props.gameCharRef?.characterRotation?.value,
  (newRotation) => {
    if (!props.useFirstPerson || newRotation == null) return
    horizontalRotation.value = newRotation
  }
)

/* =========================
   Lifecycle
========================= */

onMounted(() => {
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('click', requestPointerLock)

  rafId = requestAnimationFrame(updateCamera)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('click', requestPointerLock)

  if (rafId !== null) {
    cancelAnimationFrame(rafId)
    rafId = null
  }

  if (document.pointerLockElement) {
    document.exitPointerLock()
  }
})

/* =========================
   Expose
========================= */

defineExpose({
  get camera() {
    return cameraRef.value
  }
})
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
