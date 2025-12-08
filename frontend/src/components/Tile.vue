<script setup lang="ts">
// https://cientos.tresjs.org/guide/loaders/use-gltf
import { useGLTF } from '@tresjs/cientos'
import { computed, watchEffect } from 'vue'
import { Object3D } from 'three'

// Props definieren
const props = defineProps<{
  id: string,
  position: [number, number, number],
  type?: string
}>()

const emit = defineEmits<{
  (e: 'tile-click', fieldId: string): void
  (e: 'tile-ready', payload: { id: string; object: Object3D }): void
}>();

const startColorMap: Record<string, string> = {
  START_RED: '#e24b4b',
  START_GREEN: '#3fc37a',
  START_YELLOW: '#ffd24d',
  START_BLUE: '#4da6ff'
}

// Modell: Grass Platform by J-Toastie [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/7xmlX1JEkM)
const { state } = useGLTF('/Grass Platform.glb', { draco: true })

const { state: houseState } = useGLTF('/House.glb', { draco: true })
const { state: goalState } = useGLTF('/Goal_Flag.glb', { draco: true })

// tileObject und optionales Overlay (Flagge / Haus)
const tileObject = computed<Object3D | null>(() => state.value?.scene ?? null)
const overlayObject = computed<Object3D | null>(() => {
  if (props.type === 'END') return goalState.value?.scene ?? null
  if (props.type?.startsWith?.('START_')) return houseState.value?.scene ?? null
  return null
})

// Position und Skalierung aktualisieren, sobald Modell oder Props sich ändern
const tileScale = 2
const overlayScale = computed(() => {
  switch (props.type) {
    case 'END': return 0.7
    default: return 2.7
  }
})
const overlayXOffset = computed(() => {
  switch (props.type) {
    case 'END': return 0.05
    default: return 0.2
  }
})
const tileYOffset = -0.3
const overlayYOffset = computed(() => {
  // Y höher = oberhalb, niedriger = unterhalb
  if (props.type === 'END') return -0.1
  if (props.type?.startsWith?.('START_')) return 0
  return 0
})
const overlayZOffset = computed(() => {
  // Neg. Wert = näher zum Ursprung
  if (props.type === 'END') return 0.5
  if (props.type?.startsWith?.('START_')) return 0.5
  return 0
})

//Setzt für tile die Position und Skalierung im Raum
//Setzt für Overlay die Position basierend auf dem dazu gehörigen tile und Skalierung
watchEffect(() => {
  const tile = tileObject.value
  if (tile) {
    if (typeof tile.position?.set === 'function') {
      tile.position.set(props.position[0], tileYOffset, props.position[2])
    }
    if (typeof tile.scale?.set === 'function') {
      tile.scale.set(tileScale, tileScale, tileScale)
    }
    //    Sobald das Modell existiert und positioniert wurde,
    //    wird ein Custom-Event an den übergeordneten Parent (GameBoard.vue) gesendet.
    //    Tile teilt mit, dass es fertig geladen ist und angeclickt werden kann.
    //    -> Der Parent speichert dann das Objekt in `clickableTiles`
    emit('tile-ready', { id: props.id, object: tile })
  }

  const overlay = overlayObject.value
  if (overlay) {

    if (props.type?.startsWith?.('START_')) {
      const col = startColorMap[props.type] ?? '#ffffff'
      setOverlayMainColor(overlay, col)
    }
    // Position + Scale für Overlay (höher platzieren)
    if (typeof overlay.position?.set === 'function') {
      overlay.position.set(props.position[0] + overlayXOffset.value, overlayYOffset.value, props.position[2] + overlayZOffset.value)
    }
    if (typeof overlay.scale?.set === 'function') {
      overlay.scale.set(overlayScale.value, overlayScale.value, overlayScale.value)
    }
  }
})

function handleClick() {
  emit('tile-click', props.id)
}
/**
 * Färbt einmalig das Material 'Main' des Overlays (in diesem Fall Haus) in `colorHex`.
 * Traversiert alle Meshes/Materialien, setzt bei 'Main' die Farbe und `material.needsUpdate = true`.
 * Verhindert Mehrfachausführung via `obj.userData.mainColorApplied`.
 * @param obj Overlay-`Object3D` (Hausmodell).
 * @param colorHex Hex-Farbe
 */
function setOverlayMainColor(obj: Object3D, colorHex: string) {
  if ((obj as any).userData?.mainColorApplied) return
  obj.traverse((child: any) => {
    if (!child.isMesh || !child.material) return
    const mats = Array.isArray(child.material) ? child.material : [child.material]
    mats.forEach((m: any) => {
      if (!m) return

      if (m.name === 'Main' && m.color) {
        m.color.set(colorHex)
        m.needsUpdate = true
      }
    })
  }); (obj as any).userData = { ...(obj as any).userData, mainColorApplied: true, mainColor: colorHex }
}

</script>

<template>
  <!-- Rendert Basis-Tile (immer) -->
  <primitive v-if="tileObject" :object="tileObject" @pointerdown="handleClick" />
  <!-- Rendert Optionales Overlay (Flagge oder Haus) -->
  <primitive v-if="overlayObject" :object="overlayObject" @pointerdown="handleClick" />
</template>