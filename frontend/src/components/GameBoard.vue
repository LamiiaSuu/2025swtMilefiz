<script setup lang="ts">
import { ref, shallowRef, onMounted, onUnmounted } from 'vue'
import { TresCanvas, type TresObject } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import GameCharacter from './GameCharacter.vue'
import Tile from './Tile.vue'
import type { Field } from '../types/fields'
import Camera from './Camera.vue'

const gameCharRef = shallowRef<TresObject | null>(null)

// API Spielfeld laden
const gameBoardTiles = ref<Field[]>([])
onMounted(async () => {
  const res = await fetch('api/game/getBoard')
  const data = await res.json()
  gameBoardTiles.value = data.fields
})


const useFirstPerson = ref(true) // Kamera-Mode-Flag

// Keyboard toggle listener
const toggleCamera = (e: KeyboardEvent) => {
  if (e.key.toLowerCase() === 'o') {
    useFirstPerson.value = !useFirstPerson.value
  }
}

// Updated Rotation vom Charakter für First Person Kamera
const onRotateCharacter = (yRotation: number) => {
  if (gameCharRef.value) {
    gameCharRef.value.setRotation(yRotation)
  }
}

onMounted(() => {
  window.addEventListener('keydown', toggleCamera)
})

onUnmounted(() => {
  window.removeEventListener('keydown', toggleCamera)
})

</script>

<template>
  <!-- 3D-Canvas Element das den ganzen Bildschirm ausfüllt-->
  <TresCanvas
    window-size
    style="width: 100vw; height: 100vh"
    clear-color="#87CEEB"
  >
    <!-- Kameraposition und Kamerasteuerung via OrbitControls -->
    <TresPerspectiveCamera
      v-if="!useFirstPerson"
      ref="orbitCam"
      :position="[0, 8, 15]"
      :fov="60"
    />
    <OrbitControls v-if="!useFirstPerson" />

    <!-- First Person Kamera (Folgt dem Charakter) -->
    <Camera
      :gameCharRef="gameCharRef"
      :use-first-person="useFirstPerson"
      @rotate-character="onRotateCharacter"
    />

    <!-- 3D-Objekt für den Spielfeld-Boden rotation dreht den boden, damit er horizontal und nicht
     vertikal ist -->
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]">
      <TresPlaneGeometry :args="[20, 20]" />
      <TresMeshBasicMaterial :color="0x7cfc00" />
    </TresMesh>

    <!-- Grundbeleuchtung der Szene (80% Intensität) -->
    <TresAmbientLight :intensity="0.8" />

    <!-- Game Character includiert (position - Position auf Plane), (bodyColor - Farbe der Figur), (eyeColor - Farbe der Augen) -->
    <GameCharacter
      ref="gameCharRef"
      :position="[0, 0, 0]"
      bodyColor="pink"
      eyeColor="white"
    />

    <!-- Spielfeldtiles rendern -->
    <Tile
      v-for="field in gameBoardTiles"
      :key="field.id"
      :position="[field.position.x, 0, field.position.y]"
      :type="field.type"
    />
  </TresCanvas>
</template>
