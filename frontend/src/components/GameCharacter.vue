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

// Block Character by J-Toastie [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/ozSIyRIcIj)
const { state } = useGLTF('/Block Character.glb', { draco: true })

// Modellgröße per Scaling-Faktor, sobald geladen
const scale = 1
watchEffect(() => {
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
  }
})

// Updated die Rotation des Charakters
const setRotation = (yRotation: number) => {
  characterRotation.value = yRotation
}

const jump = () => {
  if (isJumping.value) return

  isJumping.value = true
  const jumpHeight = 2
  const jumpDuration = 600

  const startTime = Date.now()
  const animateJump = () => {
    const elapsed = Date.now() - startTime
    const progress = elapsed/jumpDuration

    if (progress < 1) {
      jumpOffset.value = jumpHeight * Math.sin(progress * Math.PI)
      requestAnimationFrame(animateJump)
    }else{
      jumpOffset.value = 0
      isJumping.value = false
    }
  }
  animateJump()
}

// Gibt Rotation frei
defineExpose({ setRotation, jump})
</script>

<template>
    <TresGroup 
      :position="[
        (props.position?.[0] || 0), 
        (props.position?.[1] || 0) + jumpOffset, 
        (props.position?.[2] || 0)
      ]"
      :rotation="[0, characterRotation, 0]"
    >
      <primitive v-if="state" :object="state?.scene" />
  </TresGroup>
</template>
