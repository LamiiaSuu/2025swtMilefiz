<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { ref, computed, watchEffect, watch, type VNodeRef, type Ref } from 'vue'
import { BoxGeometry, BufferGeometry, DoubleSide, DynamicDrawUsage, Material, Mesh, MeshNormalMaterial, Object3D, Quaternion, Sphere, Vector3 } from 'three'
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

const model = computed(() => models[type])
const scene = computed(() => model.value?.state?.value?.scene ?? null)


type Part = {
  name: string,
  geometry: BufferGeometry,
  material: Material,
  quaternion: Quaternion,
}
const parts = ref<Part[]>([])

const imRefs = ref<any[]>([])
const dummy = new Object3D()

const count = computed(() => position?.length ?? 10)

const geometry = ref<BufferGeometry>()
const material = ref<Material>()

watchEffect(() => scene.value?.scale.set(scale, scale, scale))

watchEffect(() => {
  parts.value = []
  if (!scene.value) return
  scene.value.traverse((node: Object3D) => {
    if (node.type == "Mesh") {
      const mesh = node as Mesh;
       const geom = mesh.geometry.clone() as BufferGeometry
      geom.computeVertexNormals()
      const mat = (mesh.material as Material).clone();
      const quat = mesh.quaternion.clone();

      if (mat.transparent) {
      mat.depthTest = true;
      math.alphaTest = true;
      mat.depthWrite = true;
      mat.polygonOffset = true;
      mat.polygonOffsetFactor = -1;
      mat.polygonOffsetUnits = 1;
      }
      parts.value.push({ name: mesh.name, geometry: geom, material: mat, quaternion: quat})
      console.log(material)

    }
  })
})


watch(imRefs, (imRefs: any) => {
  //imRefs.value.forEach((imRef: any) =>   imRef.instanceMatrix.setUsage(DynamicDrawUsage))
});

watchEffect(() => {
  if (parts.value.length > 0 && position) {
    parts.value.forEach((part, i) => {
      const ref = imRefs.value[i];
      console.log(ref)
      if (ref) {
    position.forEach((pos : [number, number, number], i : number) => {
      dummy.position.set(...pos)
      dummy.quaternion.copy(part.quaternion)
      dummy.updateMatrix()
      ref.setMatrixAt(i, dummy.matrix)
    })
    ref.instanceMatrix.needsUpdate = true
      }
    })
  }
})

const getRef = (el : any, index : number) => {
  imRefs.value[index] = el;
  console.log(imRefs.value) 
}

</script>
<template>
  <TresInstancedMesh v-for="(part, index) in parts" :ref="(el) => getRef(el, index)"
    :args="[part.geometry, part.material, count]" 
  />
</template>
