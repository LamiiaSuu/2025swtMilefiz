<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { ref, computed, watch, watchEffect,  } from 'vue'

const props = defineProps<{
  bodyColor?: string | number
  eyeColor?: string | number
  scale?: number
  yPosition?: number
  yRotation?: number
}>()

const modelPath = '/Block Character.glb'
const { state } = useGLTF(modelPath, { draco: true })

// Three.js AnimationMixer
const mixer = ref<any>(null)
const jumpAction = ref<any>(null)
const clock = ref<any>(null)

const gltfScene = computed(() => state.value?.scene ?? null)


watchEffect(async () => {
  if (state.value?.scene) {
    //Geht über CharacterMesh und unterscheidet nach Körper und Eyes
    state.value.scene.traverse((child: any) => {
      if (child.material) {
        // Unterscheidung zwischen body und eye_color Material
        if (child.material.name === 'body' || child.name?.includes('body')) {
          if (props.bodyColor) {
            child.material.color.set(props.bodyColor)
          }
        } else if (child.material.name === 'eye_color' || child.name?.includes('eye')) {
          if (props.eyeColor) {
            child.material.color.set(props.eyeColor)
          }
        }
      }
    })
  
  if (state.value.animations && state.value.animations.length > 0) {
    const THREE = await import('three')
    mixer.value = new THREE.AnimationMixer(state.value.scene)

    const jumpAnim = state.value.animations.find((a: any) =>
      a.name.toLowerCase().includes('jump')
    )

    if (jumpAnim) {
      jumpAction.value = mixer.value.clipAction(jumpAnim)
      jumpAction.value.setLoop(THREE.LoopRepeat)
      jumpAction.value.play()
    }
  }
  }

  if (!clock.value) {
    const THREE = await import('three')
    clock.value = new THREE.Clock()

    const animate = () => {
      if (mixer.value) mixer.value.update(clock.value.getDelta())
      requestAnimationFrame(animate)
    }
    animate()
  }
})
</script>

<template>
  <TresGroup
    :scale="props.scale ?? 1"
    :position="[0, props.yPosition ?? 0, 0]"
    bodyColor="gray" eyeColor="red" 
  >
    <primitive v-if="gltfScene" :object="gltfScene" />
  </TresGroup>
</template>