<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { ref, computed, watchEffect, watch } from 'vue'
import { BoxGeometry, BufferGeometry, DoubleSide, DynamicDrawUsage, Material, Mesh, MeshNormalMaterial, Object3D, Sphere, Vector3 } from 'three'
import { startingbaseColors } from '@/types/colorsAssets';

import { Sizes }from '@/stores/ITreeDTD';
import { useGraph } from '@tresjs/core';

// Props definieren
const { position, type } = defineProps<{
  position?: [number, number, number][],
  type: Sizes
}>()

const models = {
  [Sizes.Small]: (useGLTF('/environment/trees/pine_high.glb', { draco: true })),
  [Sizes.Medium]: (useGLTF('/environment/trees/pine_low.glb', { draco: true })),
  [Sizes.Large]: (useGLTF('/environment/plants/bush_flowers.glb', { draco: true }))
}

const scale = 0.5

const scene = computed(() => models[type].state.value?.scene)
const graph = useGraph(scene)
const meshes = computed(() => graph.value?.meshes ?? [])

watchEffect(() => scene.value?.scale.set(scale, scale, scale))

const imRef = ref()
const dummy = new Object3D()

const mesh = scene.value?.getObjectByName('tree_1_leaf_and_grass_0') as Mesh;

const count = computed(() => position?.length ?? 1)

const geometry = ref<BufferGeometry>()
const material = ref<Material>()

if (mesh) {
    geometry.value = mesh.geometry.clone() as BufferGeometry
    geometry.value.computeVertexNormals()
    material.value = mesh.material as Material
}

geometry.value = new BoxGeometry(0.5, 0.5, 0.5)
material.value = new MeshNormalMaterial();

watch(imRef, (mesh: { instanceMatrix: { setUsage: (arg0: number) => void; }; }) => {
  mesh.instanceMatrix.setUsage(DynamicDrawUsage);
});

watchEffect(() => {
  if (imRef.value && position) {

    position.forEach((pos : [number, number, number], i : number) => {
      dummy.position.set(...pos)
      dummy.updateMatrix()
      imRef.value.setMatrixAt(i, dummy.matrix)
    })
    imRef.value.instanceMatrix.needsUpdate = true
  }
})

</script>

<template>
  <TresInstancedMesh
    ref="imRef" 
    :args="[geometry, material, count]" 
  />
</template>

