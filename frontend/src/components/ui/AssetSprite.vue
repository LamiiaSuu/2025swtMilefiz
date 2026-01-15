<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { computed, watchEffect } from 'vue'

const props = defineProps<{
  type: string     //'Ordner'    -> Typ von Object
  variant: string  //'Dateiname' -> Variante von Object
  position: [number, number, number]
  scale: number
  rotation: number
}>()

// Pfad zum 3D-Model
const modelPath = computed(() => 
  `/environment/${props.type}/${props.variant}.glb`
)

// Lade 3D-Model mit useGLTF (wie GameCharacter)
const { state } = useGLTF(modelPath, { draco: true })

// Rotation in Radiant umrechnen
const rotationRad = computed<[number, number, number]>(() => [
  0,
  props.rotation * Math.PI / 180,
  0
])

// Scale anwenden (wie bei GameCharacter)
watchEffect(() => {
  if (!state.value?.scene) return
  
  state.value.scene.scale.set(props.scale, props.scale, props.scale)
})
</script>

<template>
  <TresGroup
    :position="position"
    :rotation="rotationRad"
  >
    <primitive v-if="state" :object="state?.scene" />
  </TresGroup>
</template>