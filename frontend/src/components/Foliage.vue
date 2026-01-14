<script setup lang="ts">
import { useGLTF, Html } from '@tresjs/cientos'
import { ref, watchEffect, watch, computed } from 'vue'
import { BufferGeometry, DynamicDrawUsage, InstancedMesh, Material, Mesh, Object3D, Quaternion, Vector3 } from 'three'

import { Sizes } from '@/stores/ITreeDTD';
import { standardBoardAssets, STANDARD_BOARD_ID, type BoardAsset } from '@/types/BoardAsset'
import { useBoardStore } from '@/stores/boardStore'

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
const { elements } = defineProps<{ elements?: Element[] }>()

const boardStore = useBoardStore()

/**
 * Prüft ob ein Baum mit einem Asset kollidiert
 * @param treePos Position des Baums [x, y, z]
 * @param assetPos Position des Assets [x, y, z]
 * @param radius Kollisionsradius (Standard: 1.0)
 * @returns true wenn Kollision, false wenn frei
 */
const hasCollision = (treePos: [number, number, number], assetPos: [number, number, number], radius: number = 1.0): boolean => {
  const [treeX, , treeZ] = treePos
  const [assetX, , assetZ] = assetPos

  return Math.abs(treeX - assetX) <= radius && Math.abs(treeZ - assetZ) <= radius
}

/**
 * Filtert Bäume heraus, die mit Assets kollidieren würden
 * Wird nur für das Standard-Board angewendet
 */
const filteredElements = computed<Element[] | undefined>(() => {
  if (!elements) return undefined

  // Nur für Standard-Board filtern
  const currentBoardId = boardStore.board?.id
  if (currentBoardId !== STANDARD_BOARD_ID) {
    return elements
  }

  // Filtere Bäume, die mit Assets kollidieren
  return elements.filter(element => {
    return !standardBoardAssets.some(asset =>
      hasCollision(element.position, asset.position)
    )
  })
})

/**
 * Modelle
 * scale – Skalierung des Modells.
 * variance – Varianz des Scalings um den in scale angegebenen wert in prozent (0 = Scaling wird 1:1 übernommen)
 */
const models = {
  [Sizes.Large]: { load: (useGLTF('/environment/trees/pine_high.glb', { draco: true })), scale: 1.8 },
  [Sizes.Medium]: { load: (useGLTF('/environment/trees/pine_high.glb', { draco: true })), scale: 1.2 },
  [Sizes.Small]: { load: (useGLTF('/environment/trees/pine_low.glb', { draco: true })), scale: 1.2 },
  [Sizes.Bush]: { load: (useGLTF('/environment/plants/bush_flowers.glb', { draco: true })), scale: 100 },
  [Sizes.Mushroom]: { load: (useGLTF('/environment/mushrooms/mushroom_group.glb', { draco: true })), scale: 1 },
  [Sizes.Grass_Smol]: { load: (useGLTF('/environment/plants/grass_smol.glb', { draco: true })), scale: 2 }
}

const parts = ref<Part[]>([])
const imRefs = ref<InstancedMesh[]>([])

const elementsWithScale = computed<ElementWithScale[] | undefined>(() =>
  filteredElements.value?.map((element) => ({
    ...element,
    scale: (Math.random() * (models[element.type].variance * 2)) + (1 - models[element.type].variance)
  }))
)

// Dummy-Objekt für Platzierung der einzelnen Elemente im InstancedMesh
const dummy = new Object3D()

/**
 * Sucht für den angegebenen Type alle im zugehörigen Modell vorhandenen Meshes und gibt ein Array an
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
          // setze für alle gefundenen Elemente position, Skalierung, Quaternion in der Matrix des Mesh
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
  imRefs.value[index] = el;
}

</script>

<template>
  <TresInstancedMesh v-for="(part, index) in parts" :ref="(el) => getRef(el, index)"
    :args="[part.geometry, part.material, (elements?.filter(e => e.type === part.type))?.length ?? 0]" />

  <!-- Debugging der Positionen -->
  <Html v-if="isDebug" v-for="element in filteredElements" :position="element.position" center>
  <div class="label" :style="{ color: DebugColors[element.type] }">
    {{ element.position[0] }},{{ element.position[1] }},{{ element.position[2] }}
  </div>

  </Html>
</template>