<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { ref, computed, watch, watchEffect,  } from 'vue'

const props = defineProps<{
  bodyColor?: string | number
  numbersColor?: string | number
  scale?: number
  yPosition?: number
  yRotation?: number
}>()

const modelPath = '/dice/d20.glb'
const { state } = useGLTF(modelPath, { draco: true })

watchEffect(async () => {
  if (state.value?.scene) {
    state.value.scene.traverse((child: any) => {
      if (child.material) {

        // Dice body
        if (child.material.name === 'Main.002' || child.name?.includes('dice_body')) {
          if (props.bodyColor) {
            child.material.color.set(props.bodyColor);
          }
        }

        // Dice numbers
        else if (child.material.name === 'Letters.002' || child.name?.includes('dice_numbers')) {
          if (props.numbersColor) {
            child.material.color.set(props.numbersColor);
          }
        }
      }
    });
  }
});


const gltfScene = computed(() => state.value?.scene ?? null)


</script>

<template>
  <TresGroup
    :scale="props.scale ?? 1"
    :position="[0, props.yPosition ?? 0, 0]"
    bodyColor="white" numbersColor="red" 
  >
    <primitive v-if="gltfScene" :object="gltfScene" />
  </TresGroup>
</template>