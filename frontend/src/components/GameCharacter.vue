<script setup lang="ts">
// https://cientos.tresjs.org/guide/loaders/use-gltf
import { useGLTF } from '@tresjs/cientos'
import { watchEffect } from 'vue'

//Definierte Props für Augen und Körperfarbe
const props = defineProps<{
  bodyColor?: string | number
  eyeColor?: string | number
}>()

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
</script>

<template>
  <primitive v-if="state" :object="state?.scene" />
</template>
