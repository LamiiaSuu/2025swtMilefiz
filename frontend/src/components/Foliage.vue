<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { computed, watchEffect } from 'vue'
import { DoubleSide, Object3D } from 'three'
import { startingbaseColors } from '@/types/colorsAssets';

import { Sizes }from '@/stores/ITreeDTD';

// Props definieren
const { position, type } = defineProps<{
  position: [number, number, number],
  type: Sizes
}>()

const models = {
  [Sizes.Small]: (useGLTF('/environment/trees/pine_high.glb', { draco: true })).state,
  [Sizes.Medium]: (useGLTF('/environment/trees/pine_low.glb', { draco: true })).state,
  [Sizes.Large]: (useGLTF('/environment/plants/bush_flowers.glb', { draco: true })).state
}

const scale = 0.5

const scene = computed(() => models[type].value?.scene ?? null)
watchEffect(() => scene.value?.scale.set(scale, scale, scale))

</script>

<template>
  <TresGroup :position="position">
    <primitive v-if="scene" :object="scene" />
  </TresGroup>
</template>
