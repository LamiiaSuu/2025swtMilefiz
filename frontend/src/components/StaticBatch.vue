<script setup lang="ts">
import { computed, watchEffect, ref, onBeforeUnmount } from 'vue'
import { useGLTF } from '@tresjs/cientos'
import { Mesh, BufferGeometry, Matrix4, Object3D } from 'three'

type Item = {
  variant: string
  position: [number, number, number]
  scale?: number
  rotation?: number // degrees around Y
}

const props = defineProps<{
  items: Item[]
  typeFolder: string
}>()

const mergedMeshes = ref<Mesh[]>([])

// small helper dummy for transforms
const tmp = new Object3D()
const mat4 = new Matrix4()

// Track disposables to clean up
const disposables: { geometry?: BufferGeometry; material?: any; mesh?: Mesh }[] = []

// Collect unique variants and preload their gltfs (managed reactively)
const variants = computed(() => Array.from(new Set((props.items || []).map((i) => i.variant))))
const gltfs = ref<Record<string, ReturnType<typeof useGLTF>>>({})

// ensure useGLTF is called for each variant (do not call repeatedly)
watchEffect(() => {
  for (const v of variants.value) {
    if (!gltfs.value[v]) {
      // call useGLTF and stash result
      try {
        gltfs.value[v] = useGLTF(`/environment/${props.typeFolder}/${v}.glb`, { draco: true })
      } catch (e) {
        // ignore loader errors here; we'll guard later
        // eslint-disable-next-line no-console
        console.warn('useGLTF warning for variant', v, e)
      }
    }
  }
})

watchEffect(() => {
  // clear previous meshes
  for (const m of mergedMeshes.value) {
    try { m.geometry?.dispose() } catch {}
    try { (m.material as any)?.dispose?.() } catch {}
  }
  mergedMeshes.value.length = 0

  // build merged meshes per variant
  const items = props.items || []
  if (!items.length) return

  const byVariant = new Map<string, Item[]>()
  for (const it of items) {
    const arr = byVariant.get(it.variant) || []
    arr.push(it)
    byVariant.set(it.variant, arr)
  }

  // perform merging asynchronously to allow dynamic import of utils
  ;(async () => {
    let mergeFn: typeof import('three/examples/jsm/utils/BufferGeometryUtils')['mergeBufferGeometries'] | null = null
    try {
      const mod = await import('three/examples/jsm/utils/BufferGeometryUtils')
      mergeFn = mod.mergeBufferGeometries
    } catch (e) {
      // If examples are not available, skip merging and bail out silently
      // eslint-disable-next-line no-console
      console.warn('BufferGeometryUtils not available, skipping static batching', e)
      return
    }

    for (const [variant, instances] of byVariant.entries()) {
      const gltf = gltfs.value[variant]
      const scene = gltf?.state?.value?.scene
      if (!scene) continue

      scene.traverse((node: any) => {
        if (!node?.isMesh || !node.geometry) return
        const baseGeom = node.geometry as BufferGeometry
        const baseMat = node.material
        const geomCopies: BufferGeometry[] = []

        for (const inst of instances) {
          tmp.position.set(inst.position[0], inst.position[1], inst.position[2])
          tmp.scale.set(inst.scale ?? 1, inst.scale ?? 1, inst.scale ?? 1)
          tmp.rotation.set(0, (inst.rotation ?? 0) * Math.PI / 180, 0)
          tmp.updateMatrix()

          try {
            const g = baseGeom.clone()
            g.applyMatrix4(tmp.matrix)
            geomCopies.push(g)
          } catch (err) {
            // skip problematic geometry
            // eslint-disable-next-line no-console
            console.warn('Failed to clone/applyMatrix on geometry', err)
          }
        }

        if (!geomCopies.length) return

        try {
          const merged = mergeFn!(geomCopies, false)
          if (!merged) return

          const mesh = new Mesh(merged, (baseMat && (baseMat as any).clone ? (baseMat as any).clone() : baseMat))
          mesh.frustumCulled = true

          mergedMeshes.value.push(mesh)
          disposables.push({ geometry: merged, material: mesh.material, mesh })
        } catch (err) {
          // merging failed — skip
          // eslint-disable-next-line no-console
          console.warn('mergeBufferGeometries failed', err)
        }
      })
    }
  })()
})

onBeforeUnmount(() => {
  for (const d of disposables) {
    try { d.geometry?.dispose() } catch {}
    try { d.material?.dispose?.() } catch {}
    try { d.mesh?.geometry?.dispose() } catch {}
  }
})
</script>

<template>
  <template v-for="(mesh, i) in mergedMeshes" :key="i">
    <primitive :object="mesh" />
  </template>
</template>
