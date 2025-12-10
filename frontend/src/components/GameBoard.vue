<script setup lang="ts">
import { ref, shallowRef, onMounted, onUnmounted, computed, watchEffect, type ShallowRef, type ComputedRef, type ComponentPublicInstance } from 'vue'
import { TresCanvas, type TresObject } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import GameCharacter from './GameCharacter.vue'
import { useBoardStore } from '@/stores/boardStore'
import Tile from './Tile.vue'
import Camera from './Camera.vue'
import { useMilefizStore } from "@/stores/milefizstore"
import type { Direction } from "@/types/movement"
import type { Object3D } from 'three'
import { Raycaster, Vector3 } from 'three'
import { watch } from 'vue'

const milefizStore = useMilefizStore();
const fpsCamera = shallowRef<any | null>(null)
const boardStore = useBoardStore()

// record: meepleID -> gameCharRef
const gameCharRefs: Record<string, ShallowRef<TresObject | null, TresObject | null>> = {}

// computed list of meeples with resolved 3D positions (reactive)

/**
 * Berechnet die aktuelle 3D-Position des Spielcharakters auf dem Spielfeld.
 * 
 * Nutzt die gespeicherte Meeple-Position aus dem BoardStore und 
 * wandelt sie in Three.js-Koordinaten um. 
 * Wird automatisch neu berechnet, wenn sich das Board oder die Meeple-Position ändert.
 * 
 * @returns {id, [x, y, z]} - Key: Id des Meeple, Value: Weltkoordinaten des Spielcharakters
 */
const meepleEntries = computed(() => {
  const out: { id: string; position: [number, number, number] }[] = []
  const board = boardStore.board
  for (const [meepleID, posID] of Object.entries(boardStore.meeplePositions)) {
    if (!board || !posID) {
      out.push({ id: meepleID, position: [0, 0, 0] })
      continue
    }
    const field = board.fields.find((f) => f.id === posID)
    if (!field) {
      out.push({ id: meepleID, position: [0, 0, 0] })
    } else {
      out.push({ id: meepleID, position: [field.position.x, 0, field.position.y] })
    }
  }
  return out
})

function registerGameCharRef(id: string, el: TresObject | null) {
  if (!gameCharRefs[id]) {
    gameCharRefs[id] = shallowRef<TresObject | null>(null)
  }
  gameCharRefs[id].value = el
  if (el) {
    console.log('GameCharacter created:', id, el)
  }
}

// Debug: watch meepleEntries and meeplePositions to see updates
watch(meepleEntries, (val) => {
  console.log('meepleEntries changed:', val)
}, { deep: true })

watch(() => boardStore.meeplePositions, (val) => {
  console.log('boardStore.meeplePositions changed:', JSON.stringify(val))
}, { deep: true })

function registerGameCharRefFromTemplate(id: string, el: Element | ComponentPublicInstance | null) {
  // Cast the template ref value to TresObject | null in a type-safe place
  registerGameCharRef(id, el as unknown as TresObject | null)
}

// Board-Daten laden wenn die App startet
onMounted(async () => {
  console.log('App mounted - loading board data...')
  await boardStore.getBoard()
})

// kleiner Helper zum testen
const firstMeepleId = computed(() => meepleEntries.value[0]?.id ?? null)

const useFirstPerson = ref(true) // Kamera-Mode-Flag

//Methode um alle Keyboard Events zu verwalten
const handleKeydown = (e: KeyboardEvent) => {
  toggleCamera(e)
  handleJump(e)
  handleMoveKeys(e)
}

const handleJump = (e: KeyboardEvent) => {
  if (e.code === 'Space') {
    e.preventDefault()
    const id = firstMeepleId.value
    if (!id) return
    const ref = gameCharRefs[id]
    if (!ref || !ref.value) return
    if (ref.value && ref.value.jump) {
      ref.value.jump()
    }
  }
}

// Keyboard toggle listener
const toggleCamera = (e: KeyboardEvent) => {
  if (e.key.toLowerCase() === 'o') {
    useFirstPerson.value = !useFirstPerson.value
  }
}

/**
 * Die Richtung wird relativ zur aktuellen Kamerasicht berechnet.
 * 
 * Ablauf:
 * 1. Prüft, ob sich der Spieler im First-Person-Modus befindet.
 * 2. Ermittelt, welche Bewegungstaste gedrückt wurde (`W`, `A`, `S`, `D` oder Pfeiltasten).
 * 3. Wandelt diese lokale Richtung (z. B. „vorwärts“) über die Kamerarotation (`Quaternion`)
 *    in eine Richtung im Weltkoordinatensystem um.
 * 4. Analysiert, ob sich die resultierende Richtung überwiegend entlang der X- oder Z-Achse bewegt:
 *    - X-Achse → EAST oder WEST
 *    - Z-Achse → NORTH oder SOUTH
 * 5. Sendet die berechnete Himmelsrichtung als Spielzug an den Server (`milefizStore`).
 *
 * @param {KeyboardEvent} e - Das Tastatur-Event, das die Eingabe auslöst.
 */
const handleMoveKeys = (e: KeyboardEvent) => {
  if (!useFirstPerson.value) return

  const cam = fpsCamera.value?.camera
  // const meepleId = gameCharRef.value?.meepleId
  const meepleId = firstMeepleId.value
  if (!cam || !meepleId) return

  // Blickrichtung der Kamera holen
  const lookDir = new Vector3()
  cam.getWorldDirection(lookDir)
  lookDir.setY(0).normalize() // nur horizontale Richtung
  lookDir.multiplyScalar(-1)

  // Vektor für Bewegung
  const moveDir = new Vector3()

  switch (e.code) {
    case "ArrowUp":
    case "KeyW":
      moveDir.copy(lookDir)
      break
    case "ArrowDown":
    case "KeyS":
      moveDir.copy(lookDir).negate()
      break
    case "ArrowLeft":
    case "KeyA":
      // Links = Kreuzprodukt von Up-Vektor × Blickrichtung
      moveDir.crossVectors(new Vector3(0, 1, 0), lookDir).normalize()
      break
    case "ArrowRight":
    case "KeyD":
      // Rechts = Kreuzprodukt von Blickrichtung × Up-Vektor
      moveDir.crossVectors(lookDir, new Vector3(0, 1, 0)).normalize()
      break
    default:
      return
  }

  e.preventDefault()

  // Richtung auf Hauptachsen runterbrechen (X/Z)
  const absX = Math.abs(moveDir.x)
  const absZ = Math.abs(moveDir.z)

  let direction: Direction
  if (absX > absZ) {
    direction = moveDir.x > 0 ? "EAST" : "WEST"
  } else {
    direction = moveDir.z > 0 ? "SOUTH" : "NORTH"
  }

  milefizStore.sendMove(meepleId, direction)
}







// Updated Rotation vom Charakter für First Person Kamera
const onRotateCharacter = (yRotation: number) => {
  const id = firstMeepleId.value
  if (!id) return
  const ref = gameCharRefs[id]
  if (!ref || !ref.value) return
  ref.value.setRotation(yRotation)
}

onMounted(() => {

  /**
   * Wartet, bis die First-Person-Kamera vollständig initialisiert ist.
   * 
   * Sobald die Kamera verfügbar ist:
   * - werden globale Event-Listener für Tastatur und Mausklicks aktiviert
   * - startet die Hover-Erkennung (checkHoverTile)
   * 
   * Diese Schleife verhindert Fehler, falls die Kamera-Referenz
   * beim Mounten der Komponente noch nicht gesetzt wurde.
   */
  const waitForCamera = () => {
    const cam = fpsCamera.value?.camera
    if (!cam) {
      requestAnimationFrame(waitForCamera)
      return
    }
    window.addEventListener("keydown", handleKeydown)
  }

  waitForCamera()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

</script>

<template>
  <!-- 3D-Canvas Element das den ganzen Bildschirm ausfüllt-->
  <TresCanvas window-size style="width: 100vw; height: 100vh" clear-color="#87CEEB">
    <!-- Kameraposition und Kamerasteuerung via OrbitControls -->
    <TresPerspectiveCamera v-if="!useFirstPerson" ref="orbitCam" :position="[0, 8, 15]" :fov="60" />
    <OrbitControls v-if="!useFirstPerson" />

    <!-- First Person Kamera (Folgt dem Charakter) -->
    <Camera ref="fpsCamera" :gameCharRef="(gameCharRefs[firstMeepleId ?? '']?.value) ?? null" :use-first-person="useFirstPerson"
      @rotate-character="onRotateCharacter" />

    <!-- 3D-Objekt für den Spielfeld-Boden rotation dreht den boden, damit er horizontal und nicht
     vertikal ist -->
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]">
      <TresPlaneGeometry :args="[500, 500]" />
      <TresMeshStandardMaterial :color="0x4FA200" />

    </TresMesh>

    <!-- Grundbeleuchtung der Szene (75% Intensität) -->
    <TresAmbientLight :intensity=".75" />

    <!-- Directional Licht von "vorne rechts" 200%-->
    <TresHemisphereLight :intensity=".75" skyColor="#ffffff" groundColor="#888888" />
    
    <GameCharacter v-for="entry in meepleEntries" :key="entry.id"
      :ref="el => registerGameCharRefFromTemplate(entry.id, el)"
      :position="entry.position" :meepleId="entry.id" bodyColor="pink" eyeColor="white" />

    <GameCharacter v-for="barrier in boardStore.barriersWithPositions" :key="barrier.fieldId"
      :position="barrier.position" bodyColor="gray" eyeColor="red" :meepleId="barrier.fieldId" :barrier="true" />

    <!-- Spielfeldtiles rendern -->
    <Tile v-for="field in boardStore.board?.fields" :key="field.id" :id="field.id"
      :position="[field.position.x, 0, field.position.y]" :type="field.type" />
  </TresCanvas>

  <!-- Fadenkreuz -->
  <div v-if="useFirstPerson" class="crosshair">
    <div class="dot"></div>
  </div>
</template>

<style scoped>
.crosshair {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  z-index: 9999;
}

.crosshair .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: white;
  /* <-- immer weiß */
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.5);
  transition: background 0.1s ease, transform 0.1s ease;
}
</style>
