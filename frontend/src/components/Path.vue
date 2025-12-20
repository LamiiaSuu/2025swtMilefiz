<script setup lang="ts">
import { TextureLoader, RepeatWrapping } from 'three'
import { computed } from 'vue'

const props = defineProps<{
  position: [number, number, number]
  rotationY: number
}>()

// Textur laden
const texture = new TextureLoader().load(
  new URL('/path.png', import.meta.url).href
)

// Textur konfigurieren
texture.wrapS = RepeatWrapping
texture.wrapT = RepeatWrapping
texture.repeat.set(1, 1)

const WIDTH = 0.6
const LENGTH = 1
</script>

<template>
  <TresMesh
    :position="[props.position[0], 0.01, props.position[2]]"
    :rotation="[-Math.PI / 2, props.rotationY, 0]"
  >
    <TresPlaneGeometry :args="[WIDTH, LENGTH]" />
    <TresMeshStandardMaterial
      :map="texture"
      :transparent="true"
      :roughness="1"
      :metalness="0"
    />
  </TresMesh>
</template>
