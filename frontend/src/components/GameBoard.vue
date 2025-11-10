<script setup lang="ts">
import { TresCanvas } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
</script>

<template>
  <TresCanvas window-size style="width: 100vw; height: 100vh;" clear-color="#87CEEB">
    <TresPerspectiveCamera :position="[0, 8, 15]" />
    <OrbitControls />
    
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]">
      <TresPlaneGeometry :args="[20, 20]" />
      <TresMeshBasicMaterial :color="0x7CFC00" />
    </TresMesh>
    
    <TresAmbientLight :intensity="0.8" />
  </TresCanvas>
</template>