<script setup lang="ts">
import { ref, shallowRef, onMounted, onUnmounted } from 'vue'
import { TresCanvas, type TresObject } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import GameCharacter from './GameCharacter.vue'

const gameCharRef = shallowRef<TresObject | null>(null)
const useFirstPerson = ref(false) // Kamera-Mode-Flag

// Referenz zu beiden Kameras (Orbit + First-Person)
const orbitCam = shallowRef<TresObject | null>(null)
const firstCam = shallowRef<TresObject | null>(null)


// Keyboard toggle listener
const toggleCamera = (e: KeyboardEvent) => {
  if (e.key.toLowerCase() === 'o') {
    useFirstPerson.value = !useFirstPerson.value
    
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
  <TresCanvas
    window-size
    style="width: 100vw; height: 100vh"
    clear-color="#87CEEB"
  >
    <!-- Orbit Kamera (Third Person) -->
    <TresPerspectiveCamera
      v-if="!useFirstPerson"
      ref="orbitCam"
      :position="[0, 8, 15]"
      :fov="60"
    />
    <OrbitControls v-if="!useFirstPerson" />

    <!-- First Person Kamera (Folgt dem Charakter) -->
    <TresPerspectiveCamera
      v-else
      ref="firstCam"
      :position="[0, 2, 0]"
      :rotation="[0, 0, 0]"
    />

    <!-- Boden -->
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]">
      <TresPlaneGeometry :args="[20, 20]" />
      <TresMeshBasicMaterial :color="0x7cfc00" />
    </TresMesh>

    <!-- Licht -->
    <TresAmbientLight :intensity="0.8" />

    <!-- Spielfigur -->
    <GameCharacter
      ref="gameCharRef"
      :position="[0, 0, 0]"
      bodyColor="pink"
      eyeColor="white"
      :camera="firstCam"
      :useFirstPerson="useFirstPerson"
    />
  </TresCanvas>
</template>
