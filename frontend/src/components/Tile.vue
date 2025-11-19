<script setup lang="ts">
// https://cientos.tresjs.org/guide/loaders/use-gltf
import { useGLTF } from '@tresjs/cientos'
import { watchEffect } from 'vue'

// Props definieren
const props = defineProps<{
    position: [number, number, number],
    type?: string
}>()

// Modell: Grass Platform by J-Toastie [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/7xmlX1JEkM)
const {state} = useGLTF('/Grass Platform.glb', {draco: true})

// Position und Skalierung aktualisieren, sobald Modell oder Props sich ändern
const scale = 2
watchEffect(() => {
    if (!state.value) return
    const obj = state.value.scene
    obj.position.set(props.position[0], -0.3, props.position[2])
    obj.scale.set(scale, scale, scale)
    })
</script>

<template>
    <primitive v-if="state" :object="state?.scene" />
</template>