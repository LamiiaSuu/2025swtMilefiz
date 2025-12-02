<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import type { TresObject } from '@tresjs/core'
import type { PerspectiveCamera, Vector3 } from 'three'

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
  const charPos = char?.characterPosition?.position || [0, 0, 0]

  // Rückgabe von Kamera Position (Charakter Position + Offset)
  return [
    charPos.x + offset.x,
    charPos.y + offset.y,
    charPos.z + offset.z
  ]
})

// Berechnete Rotation der Kamera neu, wenn sie sich ändert
const cameraRotation = computed((): [number, number, number] => {
  return [verticalRotation.value, horizontalRotation.value + Math.PI, 0]
})

// Kamera Maussteuerung
const onMouseMove = (e: MouseEvent) => {
  if (!props.useFirstPerson) return // Keine Maussteurung
  
  // Pointer Lock versuchen
  if (props.useFirstPerson) {
    const requestLock = () => {
      if (!document.pointerLockElement && props.useFirstPerson) {
        document.body.requestPointerLock()
      }
    }

    // Fallback: auf ersten Klick warten
    document.addEventListener('click', requestLock, { once: true })
  }

  
  
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

  // Direkt Pointer Lock versuchen
  if (props.useFirstPerson) {
    const requestLock = () => {
      if (!document.pointerLockElement) {
        document.body.requestPointerLock()
      }
    }

    // Einige Browser erlauben PointerLock nur nach Benutzerinteraktion
    // Falls möglich, direkt versuchen:
    requestLock()

    // Fallback: auf ersten Klick warten
    document.addEventListener('click', requestLock, { once: true })
  }

  const updateCamera = () => {
    
    // Kamera nur updaten, wenn First Person an und cameraRef existiert
    if (props.useFirstPerson && cameraRef.value && props.gameCharRef?.characterPosition) {
      const charPos = props.gameCharRef.characterPosition.position
      const offset = props.offset || { x: 0, y: 1, z: 0 }

      // Kamera-Position setzen
      cameraRef.value.position.set(
        charPos.x + offset.x,
        charPos.y + offset.y,
        charPos.z + offset.z
      )

    }

    // Nächsten Frame planen
    requestAnimationFrame(updateCamera)
  }

  // Nur starten, wenn cameraRef existiert
  const stopLoop = () => {
    if (cameraRef.value) {
      updateCamera()
    } else {
      requestAnimationFrame(stopLoop)
    }
  }

  stopLoop()
})

/**
 * Beobachtet die Rotation des Spielcharakters und synchronisiert sie mit der Kamerarotation.
 * 
 * Wird die Charakterrotation (z. B. durch Bewegung im Spiel) geändert, 
 * übernimmt die Kamera diese horizontale Ausrichtung, 
 * damit die Blickrichtung im First-Person-Modus mit dem Charakter übereinstimmt.
 * 
 * @param newRotation - Neuer Rotationswert des Charakters
 */
watch(
  () => props.gameCharRef?.characterRotation?.value,
  (newRotation) => {
    if (!props.useFirstPerson || newRotation == null) return
    horizontalRotation.value = newRotation
  }
)

onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  if (document.pointerLockElement) {
    document.exitPointerLock()
  }
})

// Gibt Kamera frei
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