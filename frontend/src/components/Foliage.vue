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
  [Sizes.Large]: { load: (useGLTF('/environment/trees/pine_high.glb', { draco: true })), scale: 1.8, collisionRadius: 2.5 },
  [Sizes.Medium]: { load: (useGLTF('/environment/trees/pine_high.glb', { draco: true })), scale: 1.2, collisionRadius: 2 },
  [Sizes.Small]: { load: (useGLTF('/environment/trees/pine_low.glb', { draco: true })), scale: 1.2, collisionRadius: 1 },
  [Sizes.Bush]: { load: (useGLTF('/environment/plants/bush_flowers.glb', { draco: true })), scale: 100, collisionRadius: 0.5 },
  [Sizes.Mushroom]: { load: (useGLTF('/environment/mushrooms/mushroom_group.glb', { draco: true })), scale: 1, collisionRadius: 0.3 },
  [Sizes.Grass_Smol]: { load: (useGLTF('/environment/plants/grass_smol.glb', { draco: true })), scale: 2, collisionRadius: 0.2 }
}

const imRefs = ref<InstancedMesh[]>([])


// Dummy-Objekt für PLatzierung der einzelnen Elemente im InstancedMesh
const dummy = new Object3D()

/**
 * Prüft ob ein Element mit einem Board-Asset kollidiert
 */
const BLOCK_RADIUS = 2
const BLOCK_RADIUS_SQ = BLOCK_RADIUS * BLOCK_RADIUS

const isCollidingWithAsset = (element: Element): boolean => {
  if (boardId !== STANDARD_BOARD_ID || !standardBoardAssets) return false

  const [tx, ty, tz] = element.position

  // FIX: Verwende direkt standardBoardAssets statt BoardAsset.standardBoardAssets
  for (let i = 0; i < standardBoardAssets.length; i++) {
    const asset = standardBoardAssets[i]
    if (!asset) continue
    const [ax, ay, az] = asset.position

    const dx = tx - ax
    const dy = ty - ay
    const dz = tz - az

    if (dx * dx + dy * dy + dz * dz < BLOCK_RADIUS_SQ) {
      return true
    }
  }

  return false
}


/**
 * Gefilterte Elemente ohne Kollisionen
 */
// Group elements by `type`, excluding colliding assets.
// This avoids repeated per-part filtering work and allocations.
const groupedElements = computed(() => {
  const map = new Map<any, ElementWithScale[] | Element[]>()
  if (!elements) return map
  for (let i = 0; i < elements.length; i++) {
    const e = elements[i]
    if (!e) continue
    if (isCollidingWithAsset(e)) continue
    const arr = map.get(e.type) || []
    arr.push(e)
    map.set(e.type, arr)
  }
  return map
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

const parts = computed<Part[]>(() => {
  const result: Part[] = []
  
  for (const key of Object.values(Sizes)) {
    const type = key as Sizes
    const partsT = getPartsForType(type)
    if (partsT.length > 0) {
      result.push(...partsT)
    }
  }
  
  return result
})

const getRef = (el: any, index: number) => {
  if (!el) return
  imRefs.value[index] = el
  // optimal usage for frequent instance updates
  el.instanceMatrix.setUsage(DynamicDrawUsage)
  // we control instance matrices manually
  el.matrixAutoUpdate = false
  // allow three.js frustum culling on the instanced mesh (default true)
  el.frustumCulled = true
}


const Y_AXIS = new Vector3(0, 1, 0)

function hash01(x: number, z: number) {
  const s = Math.sin(x * 12.9898 + z * 78.233) * 43758.5453
  return s - Math.floor(s) // Bereich 0..1
}

function rotationFromPosition(pos: [number, number, number]) {
  return hash01(pos[0], pos[2]) * Math.PI * 2
}

watch(
  [parts, groupedElements],
  () => {
    parts.value.forEach((part, partIndex) => {
      const ref = imRefs.value[partIndex]
      if (!ref) return

      const list = (groupedElements.value.get(part.type) as Element[] | undefined) || []

      ref.count = list.length

      for (let i = 0; i < list.length; i++) {
        const e = list[i]
        if (!e) continue
        dummy.position.set(...e.position)
        dummy.quaternion.copy(part.quaternion)

        const scale = models[part.type].scale
        dummy.scale.set(scale, scale, scale)

        const angle = rotationFromPosition(e.position)
        dummy.rotateOnWorldAxis(Y_AXIS, angle)

        dummy.updateMatrix()
        ref.setMatrixAt(i, dummy.matrix)
      }

      ref.instanceMatrix.needsUpdate = true
    })
  },
  { flush: 'post' }
)


</script>
<template>
  <TresInstancedMesh v-for="(part, index) in parts" :key="index" :ref="(el: any) => getRef(el, index)"
    :args="[part.geometry, part.material, (groupedElements.get(part.type)?.length ?? 0)]" />
</template>