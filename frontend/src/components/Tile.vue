<script setup lang="ts">
// https://cientos.tresjs.org/guide/loaders/use-gltf
import { useGLTF } from '@tresjs/cientos'
import { watchEffect } from 'vue'
import type { Object3D } from 'three'

// Props definieren
const props = defineProps<{
    id: string,
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
    //    Sobald das Modell existiert und positioniert wurde,
    //    wird ein Custom-Event an den übergeordneten Parent (GameBoard.vue) gesendet.
    //    Tile teilt mit, dass es fertig geladen ist und angeclickt werden kann.
    //    -> Der Parent speichert dann das Objekt in `clickableTiles`
    emit('tile-ready', { id: props.id, object: obj })
    })

const emit = defineEmits<{
  (e: 'tile-click', fieldId: string): void
  (e: 'tile-ready', payload: { id: string; object: Object3D }): void
}>();

function handleClick() {
  emit('tile-click', props.id)  
}

</script>

<template>
    <primitive v-if="state" :object="state?.scene" @pointerdown="handleClick"/>
</template>