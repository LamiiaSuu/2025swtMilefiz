<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { ref, computed, watchEffect, watch, type VNodeRef, type Ref } from 'vue'
import { BoxGeometry, BufferGeometry, DoubleSide, DynamicDrawUsage, InstancedMesh, Material, Mesh, MeshNormalMaterial, Object3D, Quaternion, Sphere, Vector3 } from 'three'
import { startingbaseColors } from '@/types/colorsAssets';

import { Sizes }from '@/stores/ITreeDTD';
import { useGraph } from '@tresjs/core';

export type Element = {
  position: [number, number, number]
  type: Sizes
}

type Part = {
  name: string,
  geometry: BufferGeometry,
  material: Material,
  quaternion: Quaternion,
  type: Sizes
}

const { elements } = defineProps<{ elements?: Element[] }>()


const models = {
  [Sizes.Large]: { load: (useGLTF('/environment/trees/pine_high.glb', { draco: true })), scale: 1 },
  [Sizes.Medium]: { load: (useGLTF('/environment/plants/bush_flowers.glb', { draco: true })), scale: 1 },
  [Sizes.Small]: { load: (useGLTF('/environment/rocks/rock_smol.glb', { draco: true })), scale: 1 }
}

//const model = computed(() => models[type])
//const scene = computed(() => model.value?.state?.value?.scene ?? null)

const parts =ref<Part[]>([])
const imRefs = ref<InstancedMesh[]>([])

const dummy = new Object3D()

const getPartsForType = (type: Sizes) => {
  const model = models[type].load
  const scene = model?.state?.value?.scene ?? null
  const partsT: Part[] = []
  if (!scene) return partsT
  scene.traverse((node: Object3D) => {
    if ((node as Mesh).isMesh) {
      console.log(node)
      const mesh = node as Mesh;
       const geom = mesh.geometry.clone() as BufferGeometry
      geom.computeVertexNormals()
      const mat = (mesh.material as Material).clone();
      const quat = mesh.quaternion.clone();

            mat.side = DoubleSide;
            mat.visible = true;


      if (mat.transparent) {
      mat.depthTest = true;
      mat.alphaTest = 0.01;
      mat.depthWrite = true;
      mat.polygonOffset = true;
      mat.polygonOffsetFactor = -1;
      mat.polygonOffsetUnits = 1;
      }
      partsT.push({ name: mesh.name, geometry: geom, material: mat, quaternion: quat, type: type})
    }
  })
  return partsT
}

//const count = computed(() => position?.length ?? 10)

//watchEffect(() => scene.value?.scale.set(scale, scale, scale))

watchEffect(() => {
  parts.value = []
for (const key of Object.values(Sizes)) {
    const type = key as Sizes
      const partsT = getPartsForType(type)
      if (partsT.length > 0) {
        parts.value.push(...partsT)
  }
}
})


watch(imRefs, (imRefs: any) => {
  //imRefs.value.forEach((imRef: any) =>   imRef.instanceMatrix.setUsage(DynamicDrawUsage))
});

watchEffect(() => {
  if (parts.value.length > 0) {
    parts.value.forEach((part, i) => {
      const ref = imRefs.value[i];
      if (ref) {
    elements?.filter((e) => e.type === part.type).forEach((e: Element, i : number) => {
      dummy.position.set(...e.position)
      dummy.quaternion.copy(part.quaternion)
      const scale = models[part.type].scale as number
      dummy.scale.set(scale, scale, scale)
      dummy.updateMatrix()
      ref.setMatrixAt(i, dummy.matrix)
    })
    ref.instanceMatrix.needsUpdate = true
    ref.frustumCulled = false
      }
    })
  }
})

const getRef = (el : any, index : number) => {
  imRefs.value[index] = el;
}

</script>
<template>
  <TresInstancedMesh v-for="(part, index) in parts" :ref="(el) => getRef(el, index)"
    :args="[part.geometry, part.material, (elements?.filter(e => e.type === part.type))?.length ?? 1]" 
  />
</template>
