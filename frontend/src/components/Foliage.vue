<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { ref, watchEffect, watch } from 'vue'
import { BufferGeometry, DynamicDrawUsage, InstancedMesh, Material, Mesh, Object3D, Quaternion } from 'three'

import { Sizes } from '@/stores/ITreeDTD';

// Typ für Prop
export type Element = {
  position: [number, number, number]
  type: Sizes
}

// Typ für einzelne Meshes
type Part = {
  name: string,
  geometry: BufferGeometry,
  material: Material,
  quaternion: Quaternion,
  type: Sizes
}

// Props
const { elements } = defineProps<{ elements?: Element[] }>()

/* Modelle */
const models = {
  [Sizes.Large]: { load: (useGLTF('/environment/trees/pine_high.glb', { draco: true })), scale: 1 },
  [Sizes.Medium]: { load: (useGLTF('/environment/mushrooms/mushroom_group.glb', { draco: true })), scale: 1 },
  [Sizes.Small]: { load: (useGLTF('/environment/plants/bush_flowers.glb', { draco: true })), scale: 100 },

}

const parts = ref<Part[]>([])
const imRefs = ref<InstancedMesh[]>([])

// Dummy-Objekt für PLatzierung der einzelnen Elemente im InstancedMesh
const dummy = new Object3D()

/**
 * Sucht für den Angegebenen Type alle im zugehörigen Modell vorhandenen Meshes und gibt ein Array an
 * {@link Part} zurück
 * @param type 
 */
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
      const mat = (mesh.material as Material).clone();
      geom.computeVertexNormals()

      const meshWorldQuat = new Quaternion()
      mesh.getWorldQuaternion(meshWorldQuat)
      const quat = meshWorldQuat.clone();

      if (mat.transparent) {
        mat.depthTest = true;
        mat.alphaTest = 0.01;
        mat.depthWrite = true;
      }
      partsT.push({ name: mesh.name, geometry: geom, material: mat, quaternion: quat, type: type })
    }
  })
  return partsT
}

watchEffect(() => {
  // Über alle Werte im Sizes-Enum iterieren und Parts hinzufügen */
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
  imRefs.value.forEach((imRef: any) => imRef.instanceMatrix.setUsage(DynamicDrawUsage))
});

watchEffect(() => {
  if (parts.value.length > 0) {
    parts.value.forEach((part, i) => {
      const ref = imRefs.value[i]; //zugehörige Referenz des InstancedMesh
      if (ref) {
        elements?.filter((e) => e.type === part.type).forEach((e: Element, i: number) => {
          // setze für alle gefundenen Elemente position, Sklalierung, Quaternion in der Matrix des Mesh
          dummy.position.set(...e.position)
          dummy.quaternion.copy(part.quaternion)
          const scale = models[part.type].scale as number
          dummy.scale.set(scale, scale, scale)
          dummy.updateMatrix()
          ref.setMatrixAt(i, dummy.matrix)
        })
        ref.instanceMatrix.needsUpdate = true
      }
    })
  }
})

const getRef = (el: any, index: number) => {
  imRefs.value[index] = el;
}

</script>
<template>
  <TresInstancedMesh v-for="(part, index) in parts" :ref="(el) => getRef(el, index)"
    :args="[part.geometry, part.material, (elements?.filter(e => e.type === part.type))?.length ?? 1]" />
</template>
