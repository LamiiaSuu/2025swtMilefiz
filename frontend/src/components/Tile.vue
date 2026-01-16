<script setup lang="ts">
// https://cientos.tresjs.org/guide/loaders/use-gltf
import { useGLTF } from '@tresjs/cientos'
import { computed, watchEffect } from 'vue'
import { DoubleSide, Object3D } from 'three'
import { startingbaseColors } from '@/types/colorsAssets';
import { useBoardStore } from '@/stores/boardStore';

// Props definieren
const props = defineProps<{
  id: string,
  position: [number, number, number],
  type?: string
}>()

const emit = defineEmits<{
}>();

// Modell: Grass Platform by J-Toastie [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/7xmlX1JEkM)
const { state } = useGLTF('/Grass_Platform.glb', { draco: true })

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
    default: return 3.75
  }
})
const overlayXOffset = computed(() => {
  switch (props.type) {
    case 'END': return 0.05
    default: return 0.35
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

// Berechnet die Rotation basierend auf dem benachbartem Tile
const houseRotation = computed(() => {
  if (!props.type?.startsWith?.('START_')) return 0

  const boardStore = useBoardStore()
  const currentField = boardStore.board?.fields.find(f => f.id === props.id)

  if (!currentField) return 0

  // Check in welcher Richtung das Angrenzende Feld ist: Math.PI = 180 Grad
  if (currentField.north) {
    const northField = boardStore.board?.fields.find(f => f.id === currentField.north)
    if (northField && !northField.type.startsWith('START_')) return 0  // Nachbar im Norden
  }
  if (currentField.east) {
    const eastField = boardStore.board?.fields.find(f => f.id === currentField.east)
    if (eastField && !eastField.type.startsWith('START_')) return -Math.PI / 2  // Nachbar im Osten
  }
  if (currentField.south) {
    const southField = boardStore.board?.fields.find(f => f.id === currentField.south)
    if (southField && !southField.type.startsWith('START_')) return -Math.PI // Nachbar im Süden
  }
  if (currentField.west) {
    const westField = boardStore.board?.fields.find(f => f.id === currentField.west)
    if (westField && !westField.type.startsWith('START_')) return Math.PI / 2  // Nachbar im Westen
  }
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

  }

  const overlay = overlayObject.value
  if (overlay) {

    //Interior wird auf Double-Side gestellt, damit es von innen sichtbar ist.
    if (overlay && !(overlay as any).userData?.interiorFixed) {
      fixInteriorVisibility(overlay)
      ;(overlay as any).userData = {
        ...(overlay as any).userData,
        interiorFixed: true
      }
    }

    if (props.type?.startsWith?.('START_')) {
      const col = startingbaseColors[props.type as keyof typeof startingbaseColors] ?? '#ffffff'
      setOverlayMainColor(overlay, col)

      // Rotation und Position für die Häuser basierend auf anschließendem Tile
      const rotation = houseRotation.value

      // Rotation setzen
      if (typeof overlay.rotation?.set === 'function') {
        overlay.rotation.set(0, rotation, 0)
      }

      // Offset nach hinten und rechts relativ zur Rotation berechnen
      const backwardOffset = 0.3
      const rightOffset = -0.35

      // berechne Offset basierend auf Rotation
      const backwardX = Math.sin(rotation) * backwardOffset
      const backwardZ = Math.cos(rotation) * backwardOffset

      const rightX = Math.sin(rotation - Math.PI / 2) * rightOffset
      const rightZ = Math.cos(rotation - Math.PI / 2) * rightOffset

      const offsetX = backwardX + rightX
      const offsetZ = backwardZ + rightZ

      // Position mit rotierten Offsets setzen (mittig auf Tile + nach hinten + nach rechts verschoben)
      if (typeof overlay.position?.set === 'function') {
        overlay.position.set(props.position[0] + offsetX, overlayYOffset.value, props.position[2] + offsetZ)
      }
    } else {
      // Position + Scale für Overlay (höher platzieren): für nicht-START Felder
      if (typeof overlay.position?.set === 'function') {
        overlay.position.set(props.position[0] + overlayXOffset.value, overlayYOffset.value, props.position[2] + overlayZOffset.value)
      }
    }

    if (typeof overlay.scale?.set === 'function') {
      overlay.scale.set(overlayScale.value, overlayScale.value, overlayScale.value)
    }
  }
})

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

/**
 * Die Innenseite des Hausmodels wird als Double-Side markiert, damit man die Wände auch von innen sieht.
 * @param obj Overlay-`Object3D` (Hausmodell).
 * @author Robert Bothfeld
 */
function fixInteriorVisibility(obj: Object3D) {
  obj.traverse((child: any) => {
    if (!child.isMesh || !child.material) return

    const mats = Array.isArray(child.material) ? child.material : [child.material]

    mats.forEach((m: any) => {
      if (!m) return
      m.side = DoubleSide
      m.needsUpdate = true
    })
  })
}

</script>

<template>
  <!-- Rendert Basis-Tile (immer) -->
  <primitive v-if="tileObject" :object="tileObject" />
  <!-- Rendert Optionales Overlay (Flagge oder Haus) -->
  <primitive v-if="overlayObject" :object="overlayObject" />
</template>
