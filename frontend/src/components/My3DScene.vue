<script setup>
import { TresCanvas } from '@tresjs/core'
import { OrbitControls, Box } from '@tresjs/cientos'
</script>

<template>
  <TresCanvas clear-color="#202020">
    <OrbitControls />
    <TresPerspectiveCamera :position="[3, 3, 3]" />
    <TresMesh>
      <Box />
      <TresMeshStandardMaterial color="#4FC08D" />
    </TresMesh>
    <TresAmbientLight :intensity="0.5" />
    <TresDirectionalLight :position="[5, 5, 5]" />
  </TresCanvas>
</template>

