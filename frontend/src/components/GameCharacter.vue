<script setup lang="ts">
// https://cientos.tresjs.org/guide/loaders/use-gltf
import { useGLTF } from '@tresjs/cientos'
import { watchEffect, ref } from 'vue'

//Definierte Props für Augen, Körperfarbe und Position
const props = defineProps<{
  bodyColor?: string | number
  eyeColor?: string | number
  position?: [number, number, number]
}>()

const characterRotation = ref(0)

const jumpOffset = ref(0)
const isJumping = ref(false)
const jumpHeight = 4
const upDuration = 300
const fallDuration = 2000

// Animation-Variablen
const mixer = ref<any>(null)
const jumpAction = ref<any>(null)

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

      // Suche nach Jump-Animation (oft heißt sie "Jump", "jump" oder ähnlich)
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

const jump = () => {
  if (isJumping.value) return

  isJumping.value = true

  // Verwende eingebaute Animation wenn verfügbar
  if (jumpAction.value) {
    jumpAction.value.reset()
    jumpAction.value.play()

     // ZUSÄTZLICH: Eigene Höhen-Animation mit deinen Werten
    const startTime = Date.now()
    const animateCustomJump = () => {
      const elapsed = Date.now() - startTime

      if (elapsed < upDuration) {
        // Hoch-Phase
        const upProgress = elapsed / upDuration
        jumpOffset.value = jumpHeight * Math.sin(upProgress * Math.PI * 0.5)
      } else if (elapsed < upDuration + fallDuration) {
        // Fall-Phase (langsamer)
        const fallProgress = (elapsed - upDuration) / fallDuration
        jumpOffset.value = jumpHeight * Math.cos(fallProgress * Math.PI * 0.5)
      } else {
        // Landung
        jumpOffset.value = 0
        isJumping.value = false
        return
      }

      requestAnimationFrame(animateCustomJump)
    }
    animateCustomJump()

  } else {
    const startTime = Date.now()
    const animateJump = () => {
      const elapsed = Date.now() - startTime

      if (elapsed < upDuration) {
        // Hoch-Phase
        const upProgress = elapsed / upDuration
        jumpOffset.value = jumpHeight * Math.sin(upProgress * Math.PI * 0.5)
      } else if (elapsed < upDuration + fallDuration) {
        // Fall-Phase (langsamer)
        const fallProgress = (elapsed - upDuration) / fallDuration
        jumpOffset.value = jumpHeight * Math.cos(fallProgress * Math.PI * 0.5)
      } else {
        // Landung
        jumpOffset.value = 0
        isJumping.value = false
        return
      }

      requestAnimationFrame(animateJump)
    }
    animateJump()
  }
}

// Gibt Rotation frei
defineExpose({ setRotation, jump })
</script>

<template>
  <TresGroup
    :position="[
      props.position?.[0] || 0,
      (props.position?.[1] || 0) + jumpOffset,
      props.position?.[2] || 0,
    ]"
    :rotation="[0, characterRotation, 0]"
  >
    <primitive v-if="state" :object="state?.scene" />
  </TresGroup>
</template>
