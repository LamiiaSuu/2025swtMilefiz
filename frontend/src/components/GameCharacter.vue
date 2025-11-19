<script setup lang="ts">
// https://cientos.tresjs.org/guide/loaders/use-gltf
import { useGLTF } from '@tresjs/cientos'
import { watchEffect, ref, computed } from 'vue'

//Definierte Props für Augen, Körperfarbe und Position
const props = defineProps<{
  bodyColor?: string | number
  eyeColor?: string | number
  position?: [number, number, number]
}>()

const characterRotation = ref(0)
const characterPosition = ref(null)

//Variablen für Anpassung des Sprungs definiert
const jumpOffset = ref(0)
const isJumping = ref(false)
const jumpHeight = 4 //gibt Höhe an
const upDuration = 300 //gibt Zeit in ms an um hochzuspringen
const fallDuration = 2000 //gibt Zeit in ms an um runterzufallen

// Animation-Variablen
const mixer = ref<any>(null)
const jumpAction = ref<any>(null)

// Berechne aktuelle Position (inklusive jumpOffset)
const currentPosition = computed((): [number, number, number] => [
  props.position?.[0] || 0,
  (props.position?.[1] || 0) + jumpOffset.value,
  props.position?.[2] || 0
])

// Block Character by J-Toastie [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/ozSIyRIcIj)
const { state } = useGLTF('/Block Character.glb', { draco: true })

// Modellgröße per Scaling-Faktor, sobald geladen
const scale = 1
watchEffect(async () => {
  if (state.value?.scene) {
    state.value.scene.scale.set(scale, scale, scale)

    //Geht über CharacterMesh und unterscheidet nach Körper und Eyes
    state.value.scene.traverse((child: any) => {
      if (child.material) {
        // Unterscheidung zwischen body und eye_color Material
        if (child.material.name === 'body' || child.name?.includes('body')) {
          if (props.bodyColor) {
            child.material.color.set(props.bodyColor)
          }
        } else if (child.material.name === 'eye_color' || child.name?.includes('eye')) {
          if (props.eyeColor) {
            child.material.color.set(props.eyeColor)
          }
        }
      }
    })

    // Animation Mixer einrichten
    if (state.value.animations && state.value.animations.length > 0) {
      const THREE = await import('three')
      mixer.value = new THREE.AnimationMixer(state.value.scene)

      // Suche nach Jump-Animation
      const jumpAnimation = state.value.animations.find((anim: any) =>
        anim.name.toLowerCase().includes('jump'),
      )

      if (jumpAnimation) {
        jumpAction.value = mixer.value.clipAction(jumpAnimation)
        jumpAction.value.setLoop(THREE.LoopOnce, 1) // Nur einmal abspielen
        jumpAction.value.clampWhenFinished = true

        console.log('Jump animation found:', jumpAnimation.name)
      } else {
        console.log(
          'Available animations:',
          state.value.animations.map((a) => a.name),
        )
      }
    }
  }
})

// Animation updaten
const clock = ref<any>(null)
watchEffect(async () => {
  if (mixer.value && !clock.value) {
    const THREE = await import('three')
    clock.value = new THREE.Clock()

    const animate = () => {
      if (mixer.value) {
        const delta = clock.value.getDelta()
        mixer.value.update(delta)
        requestAnimationFrame(animate)
      }
    }
    animate()
  }
})

// Updated die Rotation des Charakters
const setRotation = (yRotation: number) => {
  characterRotation.value = yRotation
}

// Sprung-Animation 
const animateCustomJump = () => {
  const startTime = Date.now()
  
  const animate = () => {
    const elapsed = Date.now() - startTime

    if (elapsed < upDuration) {
      // Hoch-Phase
      const progress = elapsed / upDuration
      jumpOffset.value = jumpHeight * Math.sin(progress * Math.PI * 0.5)
    } else if (elapsed < upDuration + fallDuration) {
      // Fall-Phase (langsamer)
      const progress = (elapsed - upDuration) / fallDuration
      jumpOffset.value = jumpHeight * Math.cos(progress * Math.PI * 0.5)
    } else {
      // Landung
      jumpOffset.value = 0
      isJumping.value = false
      return
    }

    requestAnimationFrame(animate)
  }
  animate()
}

const jump = () => {
  if (isJumping.value) return

  isJumping.value = true

  // Spiele GLB-Animation ab (falls verfügbar)
  if (jumpAction.value) {
    jumpAction.value.reset()
    jumpAction.value.play()
  }

  // Führe immer Custom-Animation für Höhe aus
  animateCustomJump()
}

// Gibt Rotation und Position frei
defineExpose({ setRotation, jump, characterPosition })
</script>

<template>
  <TresGroup
    ref="characterPosition"
    :position="currentPosition"
    :rotation="[0, characterRotation, 0]"
  >
    <primitive v-if="state" :object="state?.scene" />
  </TresGroup>
</template>
