<script setup lang="ts">
import { TresCanvas } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
</script>

<template>
      <!-- 3D-Canvas Element das den ganzen Bildschirm ausfüllt -->
  <TresCanvas window-size style="width: 100vw; height: 100vh;" clear-color="#87CEEB">
    <!-- Kamerapoistion und Kamerasteuerung via OrbitControls-->
    <TresPerspectiveCamera :position="[0, 8, 15]" />
    <OrbitControls />
    
    <!-- 3D-Objekt für den Spielfeld-Boden rotation dreht den boden, damit er horizontal und nicht
     vertikal ist-->
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]">
      <TresPlaneGeometry :args="[20, 20]" />
      <TresMeshBasicMaterial :color="0x7CFC00" />
    </TresMesh>
    
    <!-- Grundbeleuchtung der Szene (80% Intensität) -->
    <TresAmbientLight :intensity="0.8" />
  </TresCanvas>
</template>