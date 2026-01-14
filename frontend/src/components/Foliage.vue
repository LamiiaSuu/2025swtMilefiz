<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { ref, watchEffect, watch, computed } from 'vue'
import { BufferGeometry, DynamicDrawUsage, InstancedMesh, Material, Mesh, Object3D, Quaternion, Vector3 } from 'three'

import { Sizes } from '@/stores/ITreeDTD';
import { STANDARD_BOARD_ID, standardBoardAssets } from '@/types/BoardAsset';

// Typ für Prop
export type Element = {
  position: [number, number, number]
  type: Sizes
}

type ElementWithScale = Element & { scale: number }

// Typ für einzelne Meshes
type Part = {
  name: string,
  geometry: BufferGeometry,
  material: Material,
  quaternion: Quaternion,
  type: Sizes
}

// Props
const { elements, boardId } = defineProps<{ elements?: Element[], boardId?: string }>()

/**
 * Modelle
 * scale – Skalierung des Modells.
 * variance – Varianz des Scalings um den in scale angegebenen wert in prozent (0 = Scaling wird 1:1 übernommen)
 */
const models = {
  [Sizes.Large]: { load: (useGLTF('/environment/trees/pine_high.glb', { draco: true })), scale: 1.8, collisionRadius: 3.5 },
  [Sizes.Medium]: { load: (useGLTF('/environment/trees/pine_high.glb', { draco: true })), scale: 1.2, collisionRadius: 2.5 },
  [Sizes.Small]: { load: (useGLTF('/environment/trees/pine_low.glb', { draco: true })), scale: 1.2, collisionRadius: 2 },
  [Sizes.Bush]: { load: (useGLTF('/environment/plants/bush_flowers.glb', { draco: true })), scale: 100, collisionRadius: 1 },
  [Sizes.Mushroom]: { load: (useGLTF('/environment/mushrooms/mushroom_group.glb', { draco: true })), scale: 1, collisionRadius: 0.6 },
  [Sizes.Grass_Smol]: { load: (useGLTF('/environment/plants/grass_smol.glb', { draco: true })), scale: 2, collisionRadius: 0.4 }
}
const parts = ref<Part[]>([])
const imRefs = ref<InstancedMesh[]>([])


// Dummy-Objekt für PLatzierung der einzelnen Elemente im InstancedMesh
const dummy = new Object3D()

/**
 * Prüft ob ein Element mit einem Board-Asset kollidiert
 */
const isCollidingWithAsset = (element: Element): boolean => {
  if (boardId !== STANDARD_BOARD_ID || !standardBoardAssets) return false

  const treePos = new Vector3(...element.position)
  const blockRadius = 5 // Erhöhter Radius für bessere Sichtbarkeit

  return standardBoardAssets.some(asset => {
    const assetPos = new Vector3(...asset.position)
    const distance = treePos.distanceTo(assetPos)
    return distance < blockRadius
  })
}

/**
 * Gefilterte Elemente ohne Kollisionen
 */
const filteredElements = computed(() => {
  if (!elements) return []
  return elements.filter(element => !isCollidingWithAsset(element))
})

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
  scene.traverse((node) => {
    if ((node as Mesh).isMesh) {
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
        filteredElements.value?.filter((e) => e.type === part.type).forEach((e: Element, i: number) => {
          // setze für alle gefundenen Elemente position, Sklalierung, Quaternion in der Matrix des Mesh
          dummy.position.set(...e.position)
          dummy.quaternion.copy(part.quaternion)
          const scale = (models[part.type].scale as number); // scaling mit varianz
          dummy.scale.set(scale, scale, scale)
          dummy.rotateOnWorldAxis(new Vector3(0, 1, 0), Math.floor(Math.random() * 361))
          dummy.updateMatrix()
          ref.setMatrixAt(i, dummy.matrix)
        })
        ref.instanceMatrix.needsUpdate = true
      }
    })
  }
})

const getRef = (el: any, index: number) => {
  if (el) {
    imRefs.value[index] = el;
  }
}

</script>
<template>
  <TresInstancedMesh v-for="(part, index) in parts" :key="index" :ref="(el: any) => getRef(el, index)"
    :args="[part.geometry, part.material, (filteredElements?.filter(e => e.type === part.type))?.length ?? 0]" />
</template>