<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { computed, watchEffect } from 'vue'

const props = defineProps<{
  position: [number, number, number]
  rotationY: number
  length?: number
}>()

// GLB Modell laden
const { state } = useGLTF('/paths/Rock_Path_Round_Small.glb', { draco: true })

const BASE_LENGTH = 1
const lengthScale = computed(() => (props.length ? props.length / BASE_LENGTH : 1))

// Szene aus dem geladenen GLB holen
const scene = computed(() => state.value?.scene ?? null)

// Skalierung der Szene anpassen, wenn sie sich ändert
watchEffect(() => {
  const s = scene.value
  if (!s) return
  if (typeof s.scale?.set === 'function') s.scale.set(1, 1, 1)
})
</script>

<template>
  <TresGroup :position="props.position" :rotation="[0, props.rotationY, 0]" :scale="[lengthScale*0.5, 0.5, 0.5]">
    <primitive v-if="scene" :object="scene" />
  </TresGroup>
</template>
