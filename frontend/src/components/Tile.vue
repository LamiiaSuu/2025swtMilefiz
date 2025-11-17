<script setup lang="ts">
// https://cientos.tresjs.org/guide/loaders/use-gltf
import { useGLTF } from '@tresjs/cientos'
import { watchEffect } from 'vue'

// Grass Platform by J-Toastie [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/7xmlX1JEkM)
const {state} = useGLTF('/Grass Platform.glb', {draco: true})

// Modellgröße per Scaling-Factor, sobald geladen
const scale = 1
watchEffect(() => state.value?.scene.scale.set(scale, scale, scale))
</script>

<template>
    <primitive v-if="state" :object="state?.scene" />
</template>